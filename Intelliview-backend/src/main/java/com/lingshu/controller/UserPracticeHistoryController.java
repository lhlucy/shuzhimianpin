package com.lingshu.controller;

import com.lingshu.service.UserPracticeHistoryService;
import com.lingshu.dto.request.PracticeRecordRequest;
import com.lingshu.dto.response.PracticeHistoryResponse;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

/**
 * 用户刷题记录Controller
 */
@RestController
@RequestMapping("/api/practice-history")
@RequiredArgsConstructor
public class UserPracticeHistoryController {
    
    private final UserPracticeHistoryService userPracticeHistoryService;
    private final SecurityUtil securityUtil;
    
    /**
     * 记录用户刷题行为
     */
    @PostMapping("/record")
    public ResponseEntity<ApiResponse<?>> recordPractice(
            @Valid @RequestBody PracticeRecordRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        var result = userPracticeHistoryService.recordPractice(request, userId);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 获取用户的刷题记录列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PracticeHistoryResponse>>> getUserPracticeHistory(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = securityUtil.getCurrentUserId();
        var result = userPracticeHistoryService.getUserPracticeHistory(userId, page, size);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 获取用户的刷题统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserPracticeStats() {
        
        Long userId = securityUtil.getCurrentUserId();
        var result = userPracticeHistoryService.getUserPracticeStats(userId);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 检查题目是否已完成
     */
    @GetMapping("/completed/{questionId}")
    public ResponseEntity<ApiResponse<Boolean>> isQuestionCompleted(
            @PathVariable Long questionId) {
        
        Long userId = securityUtil.getCurrentUserId();
        var result = userPracticeHistoryService.isQuestionCompleted(userId, questionId);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 获取用户已完成的题目ID列表
     */
    @GetMapping("/completed-questions")
    public ResponseEntity<ApiResponse<List<Long>>> getCompletedQuestionIds() {
        
        Long userId = securityUtil.getCurrentUserId();
        var result = userPracticeHistoryService.getCompletedQuestionIds(userId);
        return ok(ApiResponse.success(result));
    }
}
