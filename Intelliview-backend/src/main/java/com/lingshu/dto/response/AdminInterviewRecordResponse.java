package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminInterviewRecordResponse {

    private Long interviewId;

    private String title;

    private String status;

    private String targetPosition;

    private String interviewLanguage;

    private Long jobRoleId;

    private String jobRoleName;

    private String username;

    private String nickname;

    private List<String> techStacks;

    private Integer questionCount;

    private Integer answeredCount;

    private Double totalScore;

    private Integer duration;

    private LocalDateTime createdAt;

    private LocalDateTime endedAt;
}
