package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResumeResponse {

    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String content;
    private String summary;
    private String intentionJob;
    private String recruitmentType;
    private String intentionCity;
    private String expectedSalary;
    private LocalDateTime parsedAt;
    private LocalDateTime createdAt;
}
