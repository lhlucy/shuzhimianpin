package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class AIInterviewAnswerRequest {

    @NotBlank(message = "回答内容不能为空")
    private String content;

    private String inputMode = "TEXT";

    private Integer duration = 0;

    private ExpressionMeta expressionMeta;

    @Data
    public static class ExpressionMeta {

        private String emotion;

        private String transcriptText;

        private Integer durationSeconds;

        private Map<String, Object> raw;
    }
}
