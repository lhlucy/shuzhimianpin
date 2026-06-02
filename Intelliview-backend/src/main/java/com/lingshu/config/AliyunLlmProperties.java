package com.lingshu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.llm.aliyun")
public class AliyunLlmProperties {

    /**
     * 是否启用阿里云大模型调用。
     */
    private boolean enabled = false;

    /**
     * OpenAI兼容模式基础地址，例如：
     * https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 模型名称，例如 qwen3.6-flash。
     */
    private String model = "qwen3.6-flash";

    /**
     * 百炼 API Key，建议通过环境变量 DASHSCOPE_API_KEY 注入。
     */
    private String apiKey = "";

    /**
     * 连接超时（毫秒）。
     */
    private int connectTimeoutMs = 10000;

    /**
     * 读取超时（毫秒）。
     */
    private int readTimeoutMs = 120000;
}
