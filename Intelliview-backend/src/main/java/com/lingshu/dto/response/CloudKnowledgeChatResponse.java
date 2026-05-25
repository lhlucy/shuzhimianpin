package com.lingshu.dto.response;

import lombok.Data;

@Data
public class CloudKnowledgeChatResponse {

    private String text;

    private String sessionId;

    private String requestId;

    private String appId;
}
