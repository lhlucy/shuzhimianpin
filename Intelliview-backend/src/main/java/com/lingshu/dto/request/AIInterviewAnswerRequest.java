package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AIInterviewAnswerRequest {

    @NotBlank(message = "回答内容不能为空")
    private String content;

    private String inputMode = "TEXT";

    private Integer duration = 0;
}
