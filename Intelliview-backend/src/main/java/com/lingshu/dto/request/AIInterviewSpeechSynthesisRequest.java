package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AIInterviewSpeechSynthesisRequest {

    @NotBlank(message = "需要合成的文本不能为空")
    @Size(max = 1200, message = "语音合成文本不能超过1200个字符")
    private String text;

    @Size(max = 30, message = "面试语言不能超过30个字符")
    private String interviewLanguage;
}
