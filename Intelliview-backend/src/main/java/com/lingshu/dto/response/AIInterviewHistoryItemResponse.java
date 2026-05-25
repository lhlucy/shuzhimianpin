package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AIInterviewHistoryItemResponse {

    private Long interviewId;

    private String title;

    private String status;

    private String targetPosition;

    private String interviewLanguage;

    private List<String> techStacks;

    private Integer questionCount;

    private Integer answeredCount;

    private Double totalScore;

    private Integer duration;

    private LocalDateTime createdAt;

    private LocalDateTime endedAt;
}
