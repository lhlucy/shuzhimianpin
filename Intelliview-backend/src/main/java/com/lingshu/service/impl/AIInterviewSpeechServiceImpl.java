package com.lingshu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.config.AliyunSpeechProperties;
import com.lingshu.dto.response.AIInterviewSpeechResponse;
import com.lingshu.dto.response.AIInterviewTranscriptionResponse;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.service.AIInterviewSpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewSpeechServiceImpl implements AIInterviewSpeechService {

    private static final long MAX_AUDIO_SIZE = 10L * 1024L * 1024L;

    private final AliyunSpeechProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public AIInterviewTranscriptionResponse transcribe(MultipartFile file, String interviewLanguage) {
        ensureSpeechEnabled();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "请先上传语音文件");
        }
        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "语音文件不能超过10MB");
        }

        try {
            String mimeType = resolveMimeType(file);
            String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", properties.getAsrModel());
            requestBody.put("messages", List.of(Map.of(
                    "role", "user",
                    "content", List.of(Map.of(
                            "type", "input_audio",
                            "input_audio", Map.of("data", dataUri)
                    ))
            )));
            requestBody.put("stream", false);

            Map<String, Object> asrOptions = new LinkedHashMap<>();
            asrOptions.put("enable_itn", false);
            String language = resolveAsrLanguage(interviewLanguage);
            if (StringUtils.hasText(language)) {
                asrOptions.put("language", language);
            }
            requestBody.put("asr_options", asrOptions);

            JsonNode root = postJson(normalizeBaseUrl(properties.getAsrBaseUrl()) + "/chat/completions", requestBody);
            String transcript = extractTranscript(root);
            if (!StringUtils.hasText(transcript)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "语音识别结果为空，请重试");
            }
            return AIInterviewTranscriptionResponse.builder()
                    .transcript(transcript.trim())
                    .fileName(file.getOriginalFilename())
                    .mimeType(mimeType)
                    .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Aliyun speech transcription failed", ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "语音识别失败，请稍后重试");
        }
    }

    @Override
    public AIInterviewSpeechResponse synthesize(String text, String interviewLanguage) {
        ensureSpeechEnabled();
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "需要合成的文本不能为空");
        }

        String normalizedText = normalizeTtsText(text);
        try {
            Map<String, Object> input = new LinkedHashMap<>();
            input.put("text", normalizedText);
            input.put("voice", resolveVoice(interviewLanguage));
            input.put("format", properties.getAudioFormat());
            input.put("sample_rate", properties.getSampleRate());
            String languageHint = resolveLanguageHint(interviewLanguage);
            if (StringUtils.hasText(languageHint)) {
                input.put("language_hints", List.of(languageHint));
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", properties.getTtsModel());
            body.put("input", input);

            JsonNode root = postJson(properties.getTtsUrl(), body);
            JsonNode audio = root.path("output").path("audio");
            String audioUrl = audio.path("url").asText("");
            if (!StringUtils.hasText(audioUrl)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "语音合成结果为空，请稍后重试");
            }
            return AIInterviewSpeechResponse.builder()
                    .audioUrl(audioUrl)
                    .expiresAt(audio.path("expires_at").isMissingNode() ? null : audio.path("expires_at").asLong())
                    .voice(resolveVoice(interviewLanguage))
                    .format(properties.getAudioFormat())
                    .text(normalizedText)
                    .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Aliyun speech synthesis failed", ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "语音合成失败，请稍后重试");
        }
    }

    private JsonNode postJson(String url, Object body) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Aliyun speech request failed: HTTP " + response.statusCode() + ", " + response.body());
        }
        return objectMapper.readTree(response.body());
    }

    private void ensureSpeechEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "语音服务未启用");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "未配置 DASHSCOPE_API_KEY");
        }
    }

    private String resolveMimeType(MultipartFile file) {
        if (StringUtils.hasText(file.getContentType())) {
            return file.getContentType();
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".wav")) {
            return "audio/wav";
        }
        if (fileName.endsWith(".mp3")) {
            return "audio/mpeg";
        }
        if (fileName.endsWith(".ogg") || fileName.endsWith(".opus")) {
            return "audio/ogg";
        }
        return "audio/webm";
    }

    private String extractTranscript(JsonNode root) {
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText("");
        }
        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : contentNode) {
                if (item.isTextual()) {
                    builder.append(item.asText(""));
                } else if (item.has("text")) {
                    builder.append(item.path("text").asText(""));
                }
            }
            return builder.toString();
        }
        return "";
    }

    private String normalizeTtsText(String text) {
        String normalized = text.replace("\r", "\n")
                .replaceAll("\\n{2,}", "\n")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();
        if (normalized.length() <= 800) {
            return normalized;
        }
        return normalized.substring(0, 800);
    }

    private String resolveVoice(String interviewLanguage) {
        return isEnglish(interviewLanguage) ? properties.getEnglishVoice() : properties.getChineseVoice();
    }

    private String resolveLanguageHint(String interviewLanguage) {
        return isEnglish(interviewLanguage) ? "en" : "zh";
    }

    private String resolveAsrLanguage(String interviewLanguage) {
        if (!StringUtils.hasText(interviewLanguage)) {
            return "";
        }
        return isEnglish(interviewLanguage) ? "en" : "zh";
    }

    private boolean isEnglish(String interviewLanguage) {
        String normalized = interviewLanguage == null ? "" : interviewLanguage.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("english") || normalized.contains("英文");
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://dashscope.aliyuncs.com/compatible-mode/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
