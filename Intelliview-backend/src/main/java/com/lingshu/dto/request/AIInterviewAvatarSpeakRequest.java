package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AIInterviewAvatarSpeakRequest {

    @NotBlank(message = "数字人播报文本不能为空")
    @Size(max = 800, message = "数字人单次播报文本不能超过800个字符")
    private String text;
}
