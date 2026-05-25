package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIInterviewGrowthAnalysisResponse {

    private OverallTrend overallTrend;

    private List<DimensionTrend> dimensionTrends;

    private List<WeaknessTracking> weaknessTracking;

    private List<TrainingRecommendation> recommendations;

    @Data
    @Builder
    public static class OverallTrend {

        private Double averageScore;

        private Double latestScore;

        private Double scoreChange;

        private List<String> labels;

        private List<Double> scoreTrend;

        private Integer interviewCount;
    }

    @Data
    @Builder
    public static class DimensionTrend {

        private String key;

        private String name;

        private String trend;

        private List<Double> values;

        private List<String> labels;
    }

    @Data
    @Builder
    public static class WeaknessTracking {

        private String keyword;

        private String status;

        private Integer occurrences;

        private String suggestion;
    }

    @Data
    @Builder
    public static class TrainingRecommendation {

        private String type;

        private String title;

        private String reason;

        private List<Long> relatedQuestionIds;
    }
}
