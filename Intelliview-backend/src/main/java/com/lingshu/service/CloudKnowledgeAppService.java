package com.lingshu.service;

import com.lingshu.dto.response.CloudKnowledgeChatResponse;

public interface CloudKnowledgeAppService {

    CloudKnowledgeChatResponse chat(String prompt, String sessionId);

    default String ask(String prompt) {
        CloudKnowledgeChatResponse response = chat(prompt, null);
        return response != null ? response.getText() : "";
    }
}
