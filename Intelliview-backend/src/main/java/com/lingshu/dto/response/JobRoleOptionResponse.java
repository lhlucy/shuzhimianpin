package com.lingshu.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class JobRoleOptionResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String applicableExperience;
    private List<String> typicalTechStack;
    private List<String> interviewFocus;
}
