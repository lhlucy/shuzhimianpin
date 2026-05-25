package com.lingshu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.llm.aliyun.app")
public class AliyunBailianAppProperties {

    /**
     * 是否启用百炼智能体应用调用。
     */
    private boolean enabled = false;

    /**
     * 百炼应用调用基础地址。
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1/apps";

    /**
     * 百炼 API Key。
     */
    private String apiKey = "";

    /**
     * 百炼智能体应用 ID。
     */
    private String appId = "";

    /**
     * 连接超时（毫秒）。
     */
    private int connectTimeoutMs = 10000;

    /**
     * 读取超时（毫秒）。
     */
    private int readTimeoutMs = 120000;
}
