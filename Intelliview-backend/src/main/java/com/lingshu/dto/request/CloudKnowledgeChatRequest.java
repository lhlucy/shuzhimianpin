package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CloudKnowledgeChatRequest {

    @NotBlank(message = "提问内容不能为空")
    private String prompt;

    /**
     * 可选：百炼应用会话 ID，用于延续上下文。
     */
    private String sessionId;
}
