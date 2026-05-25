package com.lingshu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lingshu.config.IflytekAvatarProperties;
import com.lingshu.dto.response.AIInterviewAvatarSessionResponse;
import com.lingshu.entity.AIInterview;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.AIInterviewMapper;
import com.lingshu.service.AIInterviewAvatarService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewAvatarServiceImpl implements AIInterviewAvatarService {

    private static final int PLAYER_TYPE_XRTC = 12;

    private final IflytekAvatarProperties properties;
    private final AIInterviewMapper aiInterviewMapper;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<String, AvatarConnection> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AIInterviewAvatarSessionResponse initSession(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        if (!isConfigured()) {
            return AIInterviewAvatarSessionResponse.builder()
                    .enabled(false)
                    .connected(false)
                    .width(properties.getWidth())
                    .height(properties.getHeight())
                    .message("讯飞数字人未完成配置，请补充 APPID、APIKey、APISecret、服务ID、形象ID 和声音ID")
                    .build();
        }

        String sessionKey = buildSessionKey(interview.getUserId(), interviewId);
        stopInternal(sessionKey);

        String requestUrl = buildAuthorizedUrl(properties.getWsUrl(), properties.getApiKey(), properties.getApiSecret());
        AvatarConnection connection = new AvatarConnection(sessionKey, interviewId, interview.getUserId(), buildFrontendUserId(interview));
        sessions.put(sessionKey, connection);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                    .build();
            AvatarWebSocketListener listener = new AvatarWebSocketListener(connection);
            WebSocket webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                    .buildAsync(URI.create(requestUrl), listener)
                    .join();
            connection.webSocket = webSocket;
            connection.open = true;
            sendJson(connection, buildStartRequest(connection.frontendUserId));

            AIInterviewAvatarSessionResponse response = connection.readyFuture.get(properties.getInitTimeoutMs(), TimeUnit.MILLISECONDS);
            scheduleHeartbeat(connection);
            return response;
        } catch (Exception ex) {
            stopInternal(sessionKey);
            log.error("Init iflytek avatar session failed. interviewId={}", interviewId, ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "数字人初始化失败，请检查讯飞资源配置");
        }
    }

    @Override
    public Boolean speak(Long interviewId, String text) {
        AIInterview interview = requireOwnedInterview(interviewId);
        String sessionKey = buildSessionKey(interview.getUserId(), interviewId);
        AvatarConnection connection = sessions.get(sessionKey);
        if (connection == null || !connection.open) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "数字人会话未建立，请先初始化");
        }
        try {
            connection.readyFuture.get(2, TimeUnit.SECONDS);
            sendJson(connection, buildResetRequest());
            sendJson(connection, buildTextRequest(text));
            return Boolean.TRUE;
        } catch (Exception ex) {
            log.warn("Avatar speak failed. interviewId={}", interviewId, ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "数字人播报失败，请稍后重试");
        }
    }

    @Override
    public void stopSession(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        stopInternal(buildSessionKey(interview.getUserId(), interviewId));
    }

    @PreDestroy
    public void shutdown() {
        sessions.keySet().forEach(this::stopInternal);
        scheduler.shutdownNow();
    }

    private void scheduleHeartbeat(AvatarConnection connection) {
        if (connection.heartbeatTask != null && !connection.heartbeatTask.isCancelled()) {
            connection.heartbeatTask.cancel(true);
        }
        connection.heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (connection.open) {
                    sendJson(connection, buildPingRequest());
                }
            } catch (Exception ex) {
                log.warn("Avatar heartbeat failed. interviewId={}", connection.interviewId, ex);
            }
        }, properties.getHeartbeatSeconds(), properties.getHeartbeatSeconds(), TimeUnit.SECONDS);
    }

    private void sendJson(AvatarConnection connection, ObjectNode payload) {
        if (connection.webSocket == null || !connection.open) {
            throw new IllegalStateException("avatar websocket is not connected");
        }
        String content = payload.toString();
        log.debug("avatar send: {}", content);
        connection.webSocket.sendText(content, true);
    }

    private ObjectNode buildStartRequest(String frontendUserId) {
        ObjectNode header = objectMapper.createObjectNode();
        header.put("app_id", properties.getAppId());
        header.put("ctrl", "start");
        header.put("request_id", UUID.randomUUID().toString());
        header.put("scene_id", properties.getSceneId());

        ObjectNode stream = objectMapper.createObjectNode();
        stream.put("protocol", "xrtc");
        stream.put("fps", properties.getFps());
        stream.put("bitrate", properties.getBitrate());
        stream.put("alpha", properties.getAlpha());

        ObjectNode avatar = objectMapper.createObjectNode();
        avatar.put("avatar_id", properties.getAvatarId());
        avatar.put("width", properties.getWidth());
        avatar.put("height", properties.getHeight());
        avatar.set("stream", stream);

        ObjectNode tts = objectMapper.createObjectNode();
        tts.put("speed", properties.getSpeed());
        tts.put("pitch", properties.getPitch());
        tts.put("volume", properties.getVolume());
        tts.put("vcn", properties.getVcn());

        ObjectNode subtitle = objectMapper.createObjectNode();
        subtitle.put("subtitle", 0);

        ObjectNode parameter = objectMapper.createObjectNode();
        parameter.set("avatar", avatar);
        parameter.set("tts", tts);
        parameter.set("subtitle", subtitle);

        ObjectNode request = objectMapper.createObjectNode();
        request.set("header", header);
        request.set("parameter", parameter);
        return request;
    }

    private ObjectNode buildTextRequest(String text) {
        ObjectNode header = objectMapper.createObjectNode();
        header.put("app_id", properties.getAppId());
        header.put("ctrl", "text_driver");
        header.put("request_id", UUID.randomUUID().toString());

        ObjectNode avatarDispatch = objectMapper.createObjectNode();
        avatarDispatch.put("interactive_mode", 0);

        ObjectNode tts = objectMapper.createObjectNode();
        tts.put("vcn", properties.getVcn());
        tts.put("speed", properties.getSpeed());
        tts.put("pitch", properties.getPitch());
        tts.put("volume", properties.getVolume());

        ObjectNode air = objectMapper.createObjectNode();
        air.put("air", 0);
        air.put("add_nonsemantic", 0);

        ObjectNode parameter = objectMapper.createObjectNode();
        parameter.set("avatar_dispatch", avatarDispatch);
        parameter.set("tts", tts);
        parameter.set("air", air);

        ObjectNode textNode = objectMapper.createObjectNode();
        textNode.put("content", normalizeSpeakText(text));
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("text", textNode);

        ObjectNode request = objectMapper.createObjectNode();
        request.set("header", header);
        request.set("parameter", parameter);
        request.set("payload", payload);
        return request;
    }

    private ObjectNode buildPingRequest() {
        ObjectNode header = objectMapper.createObjectNode();
        header.put("app_id", properties.getAppId());
        header.put("ctrl", "ping");
        header.put("request_id", UUID.randomUUID().toString());
        ObjectNode request = objectMapper.createObjectNode();
        request.set("header", header);
        return request;
    }

    private ObjectNode buildResetRequest() {
        ObjectNode header = objectMapper.createObjectNode();
        header.put("app_id", properties.getAppId());
        header.put("ctrl", "reset");
        header.put("request_id", UUID.randomUUID().toString());
        ObjectNode request = objectMapper.createObjectNode();
        request.set("header", header);
        return request;
    }

    private String normalizeSpeakText(String text) {
        String normalized = text == null ? "" : text.replace("\r", "\n").trim();
        if (normalized.length() <= 800) {
            return normalized;
        }
        return normalized.substring(0, 800);
    }

    private AIInterview requireOwnedInterview(Long interviewId) {
        AIInterview interview = aiInterviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND.getCode(), "面试不存在");
        }
        Long userId = securityUtil.getCurrentUserId();
        if (!Objects.equals(interview.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED.getCode(), "无权访问该面试");
        }
        return interview;
    }

    private String buildSessionKey(Long userId, Long interviewId) {
        return userId + ":" + interviewId;
    }

    private String buildFrontendUserId(AIInterview interview) {
        return "lingshu-" + interview.getUserId() + "-" + interview.getId() + "-" + secureRandom.nextInt(10000);
    }

    private void stopInternal(String sessionKey) {
        AvatarConnection connection = sessions.remove(sessionKey);
        if (connection == null) {
            return;
        }
        connection.open = false;
        if (connection.heartbeatTask != null) {
            connection.heartbeatTask.cancel(true);
        }
        if (connection.webSocket != null) {
            try {
                connection.webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "close");
            } catch (Exception ex) {
                log.debug("Close avatar websocket ignored: {}", ex.getMessage());
            }
        }
    }

    private boolean isConfigured() {
        return properties.isEnabled()
                && StringUtils.hasText(properties.getWsUrl())
                && StringUtils.hasText(properties.getAppId())
                && StringUtils.hasText(properties.getApiKey())
                && StringUtils.hasText(properties.getApiSecret())
                && StringUtils.hasText(properties.getSceneId())
                && StringUtils.hasText(properties.getAvatarId())
                && StringUtils.hasText(properties.getVcn());
    }

    private String buildAuthorizedUrl(String requestUrl, String apiKey, String apiSecret) {
        try {
            String httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://", "https://");
            URI uri = URI.create(httpRequestUrl);
            String host = uri.getHost();

            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = format.format(new Date());

            String requestLine = "GET " + uri.getPath() + " HTTP/1.1";
            String builder = "host: " + host + "\n" + "date: " + date + "\n" + requestLine;

            Mac mac = Mac.getInstance("hmacsha256");
            mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256"));
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(builder.getBytes(StandardCharsets.UTF_8)));
            String authorization = String.format(
                    "hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    apiKey,
                    "hmac-sha256",
                    "host date request-line",
                    signature
            );
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
            return String.format(
                    "%s?authorization=%s&host=%s&date=%s",
                    requestUrl,
                    URLEncoder.encode(authBase, StandardCharsets.UTF_8),
                    URLEncoder.encode(host, StandardCharsets.UTF_8),
                    URLEncoder.encode(date, StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            throw new IllegalStateException("assemble avatar request url failed", ex);
        }
    }

    private AIInterviewAvatarSessionResponse toSessionResponse(AvatarConnection connection, JsonNode root) {
        String streamUrl = findText(root, "stream_url");
        String sid = firstNonBlank(findText(root, "sid"), connection.sid);
        String userSign = findText(root, "user_sign");
        String playerAppId = firstNonBlank(findText(root, "appid"), properties.getPlayerAppId());
        if (!StringUtils.hasText(streamUrl) || !StringUtils.hasText(userSign)) {
            throw new IllegalStateException("avatar init response missing stream info");
        }

        URI uri = URI.create(streamUrl);
        String roomId = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
        String server = "https://" + uri.getHost();
        String timeStr = String.valueOf(System.currentTimeMillis());

        connection.sid = sid;
        connection.streamUrl = streamUrl;
        connection.userSign = userSign;
        connection.server = server;
        connection.roomId = roomId;
        connection.timeStr = timeStr;

        return AIInterviewAvatarSessionResponse.builder()
                .enabled(true)
                .connected(true)
                .playerType(PLAYER_TYPE_XRTC)
                .sid(sid)
                .server(server)
                .auth("Bearer " + userSign)
                .appid(playerAppId)
                .userId(connection.frontendUserId)
                .roomId(roomId)
                .timeStr(timeStr)
                .streamUrl(streamUrl)
                .width(properties.getWidth())
                .height(properties.getHeight())
                .message("数字人已连接")
                .build();
    }

    private String findText(JsonNode node, String key) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        JsonNode direct = node.get(key);
        if (direct != null && direct.isValueNode()) {
            return direct.asText("");
        }
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : iterable(node.fields())) {
                String value = findText(entry.getValue(), key);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                String value = findText(item, key);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    private Iterable<Map.Entry<String, JsonNode>> iterable(java.util.Iterator<Map.Entry<String, JsonNode>> iterator) {
        return () -> iterator;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private final class AvatarWebSocketListener implements WebSocket.Listener {

        private final AvatarConnection connection;
        private final StringBuilder textBuffer = new StringBuilder();

        private AvatarWebSocketListener(AvatarConnection connection) {
            this.connection = connection;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("Avatar websocket opened. interviewId={}", connection.interviewId);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String text = textBuffer.toString();
                textBuffer.setLength(0);
                handleMessage(text);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.info("Avatar websocket closed. interviewId={}, statusCode={}, reason={}", connection.interviewId, statusCode, reason);
            connection.open = false;
            if (!connection.readyFuture.isDone()) {
                connection.readyFuture.completeExceptionally(new IllegalStateException("avatar connection closed"));
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("Avatar websocket error. interviewId={}", connection.interviewId, error);
            connection.open = false;
            if (!connection.readyFuture.isDone()) {
                connection.readyFuture.completeExceptionally(error);
            }
        }

        private void handleMessage(String text) {
            try {
                JsonNode root = objectMapper.readTree(text);
                log.debug("avatar receive: {}", text);
                JsonNode header = root.path("header");
                int code = header.path("code").asInt(0);
                if (code != 0) {
                    String message = header.path("message").asText("数字人服务返回错误");
                    throw new IllegalStateException(message + "，code=" + code);
                }
                if (!connection.readyFuture.isDone()) {
                    String streamUrl = findText(root, "stream_url");
                    String userSign = findText(root, "user_sign");
                    if (StringUtils.hasText(streamUrl) && StringUtils.hasText(userSign)) {
                        connection.readyFuture.complete(toSessionResponse(connection, root));
                    }
                }
            } catch (Exception ex) {
                if (!connection.readyFuture.isDone()) {
                    connection.readyFuture.completeExceptionally(ex);
                } else {
                    log.warn("Handle avatar message failed. interviewId={}", connection.interviewId, ex);
                }
            }
        }
    }

    private static final class AvatarConnection {
        private final String sessionKey;
        private final Long interviewId;
        private final Long userId;
        private final String frontendUserId;
        private final CompletableFuture<AIInterviewAvatarSessionResponse> readyFuture = new CompletableFuture<>();

        private volatile WebSocket webSocket;
        private volatile boolean open;
        private volatile ScheduledFuture<?> heartbeatTask;
        private volatile String sid;
        private volatile String streamUrl;
        private volatile String userSign;
        private volatile String server;
        private volatile String roomId;
        private volatile String timeStr;

        private AvatarConnection(String sessionKey, Long interviewId, Long userId, String frontendUserId) {
            this.sessionKey = sessionKey;
            this.interviewId = interviewId;
            this.userId = userId;
            this.frontendUserId = frontendUserId;
        }
    }
}
