package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIInterviewResumeParseResponse {

    private String fileName;

    private String summary;

    private String content;

    private Integer contentLength;

    private String intentionJob;

    private String recruitmentType;

    private String intentionCity;

    private String expectedSalary;
}
