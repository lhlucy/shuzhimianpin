package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIInterviewResponse {

    private Long interviewId;

    private String title;

    private String status;

    private String targetPosition;

    private String interviewLanguage;

    private List<String> techStacks;

    private List<String> practicedQuestions;

    private String resumeFileName;

    private String openingMessage;

    private Boolean voiceEnabled;

    private Integer questionCount;

    private Integer answeredCount;

    private AIInterviewQuestionResponse currentQuestion;
}
