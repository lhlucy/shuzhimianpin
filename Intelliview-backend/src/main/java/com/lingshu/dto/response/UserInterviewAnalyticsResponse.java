package com.lingshu.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserInterviewAnalyticsResponse {

    private OverallTrend overallTrend;
    private Map<String, SkillAnalysis> skillAnalysis;
    private List<String> weakestAreas;
    private List<String> recommendedPracticeAreas;
    private List<RoleTrend> roleTrends;
    private List<DimensionTrend> dimensionTrends;
    private List<RecentInterview> recentInterviews;
    private PracticeStats practiceStats;

    @Data
    public static class OverallTrend {
        private Double averageScore;
        private Double latestScore;
        private Double scoreChange;
        private List<Double> scoreTrend;
        private List<String> labels;
        private Integer interviewCount;
    }

    @Data
    public static class SkillAnalysis {
        private Double averageScore;
        private String trend; // improving, stable, declining
    }

    @Data
    public static class RoleTrend {
        private Long jobRoleId;
        private String roleName;
        private Integer interviewCount;
        private Double averageScore;
        private Double latestScore;
        private List<Double> scoreTrend;
        private List<String> labels;
        private List<String> weaknessTags;
    }

    @Data
    public static class DimensionTrend {
        private String name;
        private Double averageScore;
        private String trend;
        private List<Double> values;
        private List<String> labels;
    }

    @Data
    public static class RecentInterview {
        private Long interviewId;
        private String title;
        private String position;
        private String interviewDate;
        private Integer duration;
        private Double score;
        private Double expressionScore;
        private Double jobMatchScore;
        private List<String> strengths;
        private List<String> weaknesses;
    }

    @Data
    public static class PracticeStats {
        private Integer totalCount;
        private Integer completedCount;
        private Integer linkedInterviewCount;
        private Double completionRate;
    }
}
