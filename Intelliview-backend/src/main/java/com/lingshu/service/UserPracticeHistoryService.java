package com.lingshu.service;

import com.lingshu.entity.UserPracticeHistory;
import com.lingshu.dto.request.PracticeRecordRequest;
import com.lingshu.dto.response.PracticeHistoryResponse;

import java.util.List;
import java.util.Map;

/**
 * 用户刷题记录服务接口
 */
public interface UserPracticeHistoryService {
    
    /**
     * 记录用户刷题行为
     */
    UserPracticeHistory recordPractice(PracticeRecordRequest request, Long userId);
    
    /**
     * 获取用户的刷题记录列表
     */
    List<PracticeHistoryResponse> getUserPracticeHistory(Long userId, Integer page, Integer size);
    
    /**
     * 获取用户的刷题统计信息
     */
    Map<String, Object> getUserPracticeStats(Long userId);
    
    /**
     * 检查题目是否已完成
     */
    Boolean isQuestionCompleted(Long userId, Long questionId);
    
    /**
     * 获取用户已完成的题目ID列表
     */
    List<Long> getCompletedQuestionIds(Long userId);
}
