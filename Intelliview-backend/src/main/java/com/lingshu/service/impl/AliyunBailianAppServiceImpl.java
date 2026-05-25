package com.lingshu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.config.AliyunBailianAppProperties;
import com.lingshu.dto.response.CloudKnowledgeChatResponse;
import com.lingshu.service.CloudKnowledgeAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AliyunBailianAppServiceImpl implements CloudKnowledgeAppService {

    private final AliyunBailianAppProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public CloudKnowledgeChatResponse chat(String prompt, String sessionId) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("百炼应用调用未启用");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("未配置 DASHSCOPE_API_KEY");
        }
        if (!StringUtils.hasText(properties.getAppId())) {
            throw new IllegalStateException("未配置 DASHSCOPE_APP_ID");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("提问内容不能为空");
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                    .build();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("input", Map.of("prompt", prompt));

            Map<String, Object> parameters = new LinkedHashMap<>();
            if (StringUtils.hasText(sessionId)) {
                parameters.put("session_id", sessionId);
            }
            body.put("parameters", parameters);
            body.put("debug", Map.of());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/" + properties.getAppId() + "/completion"))
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("百炼应用调用失败: HTTP " + response.statusCode() + ", " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode output = root.path("output");
            CloudKnowledgeChatResponse result = new CloudKnowledgeChatResponse();
            result.setText(output.path("text").asText(""));
            result.setSessionId(output.path("session_id").asText(""));
            result.setRequestId(root.path("request_id").asText(""));
            result.setAppId(properties.getAppId());
            return result;
        } catch (Exception ex) {
            throw new IllegalStateException("百炼应用调用异常: " + ex.getMessage(), ex);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://dashscope.aliyuncs.com/api/v1/apps";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
