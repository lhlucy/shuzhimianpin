package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeParseResult {

    private String content;
    private String summary;
    private String intentionJob;
    private String recruitmentType;
    private String intentionCity;
    private String expectedSalary;
    private Integer contentLength;
}
