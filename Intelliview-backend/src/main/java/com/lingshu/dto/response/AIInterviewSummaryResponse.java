package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AIInterviewSummaryResponse {

    private Long interviewId;

    private Double overallScore;

    private String summary;

    private Map<String, Double> dimensionScores;

    private List<CompetencyDimension> competencyModel;

    private List<String> strengths;

    private List<String> weaknesses;

    private List<String> suggestions;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer durationSeconds;

    private Double completionRate;

    private Double averageAnswerScore;

    private Double expressionScore;

    private Double keywordCoverageScore;

    private List<String> techStacks;

    private Map<String, Object> overview;

    private List<AIInterviewQuestionReview> questionReviews;

    private LocalDateTime completedAt;

    @Data
    @Builder
    public static class CompetencyDimension {

        private String code;

        private String name;

        private Double weight;

        private String description;

        private Double score;
    }

    @Data
    @Builder
    public static class AIInterviewQuestionReview {

        private Long questionId;

        private Integer questionOrder;

        private String questionType;

        private String questionContent;

        private String answerContent;

        private Double score;

        private Integer duration;

        private Integer confidenceLevel;

        private String feedbackSummary;

        private Map<String, Double> dimensionScores;

        private List<String> strengths;

        private List<String> weaknesses;

        private List<String> suggestions;

        private List<String> hitKeywords;

        private List<String> missingKeywords;
    }
}
