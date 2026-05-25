package com.lingshu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.interview.stability")
public class AIInterviewStabilityProperties {

    private int createUserLimitPerMinute = 3;

    private int createGlobalLimitPerMinute = 100;

    private int answerUserLimitPerMinute = 20;

    private int idempotencyTtlMinutes = 30;
}
