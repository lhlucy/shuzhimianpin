package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AIInterviewAnswerResponse {

    private Long answerId;

    private String nextAction;

    private String interviewerReply;

    private Double score;

    private Map<String, Double> dimensionScores;

    private boolean interviewCompleted;

    private boolean summaryReady;

    private AIInterviewQuestionResponse nextQuestion;
}
