package com.lingshu.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.config.AliyunSpeechProperties;
import com.lingshu.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIInterviewRealtimeAsrWebSocketHandler extends BinaryWebSocketHandler {

    private static final String CLIENT_TYPE_FINISH = "finish";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(8);

    private final AliyunSpeechProperties properties;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final Map<String, RealtimeAsrConnection> connections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = resolveQuery(session, "token");
        if (!StringUtils.hasText(token) || !jwtService.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未授权的语音连接"));
            return;
        }
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getApiKey())) {
            session.close(CloseStatus.SERVER_ERROR.withReason("语音服务未配置"));
            return;
        }

        String language = resolveAsrLanguage(resolveQuery(session, "language"));
        RealtimeAsrConnection connection = new RealtimeAsrConnection(session, language);
        connections.put(session.getId(), connection);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        URI uri = UriComponentsBuilder.fromUriString(properties.getRealtimeAsrUrl())
                .queryParam("model", properties.getRealtimeAsrModel())
                .build(true)
                .toUri();

        RealtimeAsrListener listener = new RealtimeAsrListener(connection);
        WebSocket aliyunSocket = client.newWebSocketBuilder()
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("OpenAI-Beta", "realtime-v1")
                .connectTimeout(CONNECT_TIMEOUT)
                .buildAsync(uri, listener)
                .get(CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        connection.aliyunSocket = aliyunSocket;
        sendSessionUpdate(connection);
        sendClientJson(session, Map.of(
                "type", "ready",
                "sampleRate", properties.getRealtimeSampleRate()
        ));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        RealtimeAsrConnection connection = connections.get(session.getId());
        if (connection == null || connection.aliyunSocket == null) {
            return;
        }
        ByteBuffer payload = message.getPayload();
        byte[] bytes = new byte[payload.remaining()];
        payload.get(bytes);
        if (bytes.length == 0) {
            return;
        }
        String audio = Base64.getEncoder().encodeToString(bytes);
        sendAliyunJson(connection, Map.of(
                "type", "input_audio_buffer.append",
                "audio", audio
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        RealtimeAsrConnection connection = connections.get(session.getId());
        if (connection == null) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText("");
            if (CLIENT_TYPE_FINISH.equals(type)) {
                sendAliyunJson(connection, Map.of("type", "input_audio_buffer.commit"));
            }
        } catch (Exception ex) {
            log.debug("Ignore invalid realtime ASR client message: {}", message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        RealtimeAsrConnection connection = connections.remove(session.getId());
        if (connection != null && connection.aliyunSocket != null) {
            try {
                connection.aliyunSocket.sendClose(WebSocket.NORMAL_CLOSURE, "client closed");
            } catch (Exception ex) {
                log.debug("Close realtime asr websocket ignored: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Realtime ASR client websocket error. sessionId={}", session.getId(), exception);
    }

    private void sendSessionUpdate(RealtimeAsrConnection connection) {
        Map<String, Object> transcription = new LinkedHashMap<>();
        transcription.put("model", properties.getRealtimeAsrModel());
        transcription.put("language", connection.language);

        Map<String, Object> session = new LinkedHashMap<>();
        session.put("modalities", new String[]{"text"});
        session.put("input_audio_format", "pcm16");
        session.put("input_audio_transcription", transcription);

        sendAliyunJson(connection, Map.of(
                "type", "session.update",
                "session", session
        ));
    }

    private void sendAliyunJson(RealtimeAsrConnection connection, Object payload) {
        try {
            connection.aliyunSocket.sendText(objectMapper.writeValueAsString(payload), true);
        } catch (Exception ex) {
            sendError(connection.clientSession, "实时语音识别请求发送失败");
        }
    }

    private void handleAliyunEvent(RealtimeAsrConnection connection, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText("");
            if (type.endsWith(".error") || "error".equals(type)) {
                sendError(connection.clientSession, root.path("error").path("message").asText("实时语音识别失败"));
                return;
            }
            if ("conversation.item.input_audio_transcription.text".equals(type)) {
                String text = root.path("text").asText("");
                String stash = root.path("stash").asText("");
                String emotion = root.path("emotion").asText("");
                sendClientJson(connection.clientSession, Map.of(
                        "type", "partial",
                        "text", text,
                        "stash", stash,
                        "displayText", (text + stash).trim(),
                        "emotion", emotion
                ));
                return;
            }
            if ("conversation.item.input_audio_transcription.completed".equals(type)) {
                String transcript = root.path("transcript").asText("");
                String emotion = root.path("emotion").asText("");
                sendClientJson(connection.clientSession, Map.of(
                        "type", "final",
                        "transcript", transcript,
                        "emotion", emotion
                ));
            }
        } catch (Exception ex) {
            log.debug("Ignore unknown realtime asr event: {}", payload, ex);
        }
    }

    private void sendError(WebSocketSession session, String message) {
        sendClientJson(session, Map.of(
                "type", "error",
                "message", message
        ));
    }

    private void sendClientJson(WebSocketSession session, Object payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (IOException ex) {
            log.debug("Send realtime ASR client event failed: {}", ex.getMessage());
        }
    }

    private String resolveQuery(WebSocketSession session, String name) {
        URI uri = session.getUri();
        if (uri == null || !StringUtils.hasText(uri.getQuery())) {
            return "";
        }
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(name);
    }

    private String resolveAsrLanguage(String language) {
        String normalized = language == null ? "" : language.toLowerCase(Locale.ROOT);
        if (normalized.contains("en") || normalized.contains("english") || normalized.contains("英文")) {
            return "en";
        }
        return "zh";
    }

    private final class RealtimeAsrListener implements WebSocket.Listener {

        private final RealtimeAsrConnection connection;
        private final StringBuilder textBuffer = new StringBuilder();

        private RealtimeAsrListener(RealtimeAsrConnection connection) {
            this.connection = connection;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("Aliyun realtime ASR websocket opened. clientSessionId={}", connection.clientSession.getId());
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String payload = textBuffer.toString();
                textBuffer.setLength(0);
                handleAliyunEvent(connection, payload);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            sendClientJson(connection.clientSession, Map.of(
                    "type", "closed",
                    "statusCode", statusCode,
                    "reason", reason == null ? "" : reason
            ));
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("Aliyun realtime ASR websocket error. clientSessionId={}", connection.clientSession.getId(), error);
            sendError(connection.clientSession, "实时语音识别连接异常");
        }
    }

    private static final class RealtimeAsrConnection {
        private final WebSocketSession clientSession;
        private final String language;
        private volatile WebSocket aliyunSocket;

        private RealtimeAsrConnection(WebSocketSession clientSession, String language) {
            this.clientSession = clientSession;
            this.language = StringUtils.hasText(language) ? language : "zh";
        }
    }
}
