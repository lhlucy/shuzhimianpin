package com.lingshu.controller;

import com.lingshu.dto.request.CloudKnowledgeChatRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.CloudKnowledgeChatResponse;
import com.lingshu.service.CloudKnowledgeAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/cloud-knowledge")
@RequiredArgsConstructor
public class CloudKnowledgeController {

    private final CloudKnowledgeAppService cloudKnowledgeAppService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<CloudKnowledgeChatResponse>> chat(@Valid @RequestBody CloudKnowledgeChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                cloudKnowledgeAppService.chat(request.getPrompt(), request.getSessionId())
        ));
    }
}
