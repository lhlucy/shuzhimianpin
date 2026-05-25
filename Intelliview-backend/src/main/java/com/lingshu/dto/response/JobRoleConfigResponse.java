package com.lingshu.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class JobRoleConfigResponse {
    private JobRoleOptionResponse role;
    private List<SkillDimension> skillDimensions;
    private InterviewTemplateConfig template;

    @Data
    public static class SkillDimension {
        private String code;
        private String name;
        private BigDecimal weight;
        private String description;
    }

    @Data
    public static class InterviewTemplateConfig {
        private String templateCode;
        private String name;
        private String interviewType;
        private String difficulty;
        private Integer durationMinutes;
        private Integer defaultQuestionCount;
        private String followUpIntensity;
        private Map<String, Integer> questionTypeDistribution;
        private Map<String, Integer> difficultyDistribution;
        private Map<String, Integer> scoringWeights;
    }
}
