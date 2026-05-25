// dto/request/AnswerRequest.java
package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AnswerRequest {
    @NotBlank(message = "回答内容不能为空")
    @Size(min = 10, message = "回答内容至少10个字符")
    private String content;

    private Boolean isAnonymous = false;
}
