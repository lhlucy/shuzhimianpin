package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIInterviewAnswerResponse {

    private Long answerId;

    private String nextAction;

    private String interviewerReply;

    private Double score;

    private boolean interviewCompleted;

    private boolean summaryReady;

    private AIInterviewQuestionResponse nextQuestion;
}
