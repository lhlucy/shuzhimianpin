package com.lingshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.UserPracticeHistory;
import com.lingshu.entity.Question;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.UserPracticeHistoryMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.service.UserPracticeHistoryService;
import com.lingshu.dto.request.PracticeRecordRequest;
import com.lingshu.dto.response.PracticeHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 用户刷题记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserPracticeHistoryServiceImpl implements UserPracticeHistoryService {
    
    private final UserPracticeHistoryMapper userPracticeHistoryMapper;
    private final QuestionMapper questionMapper;
    private final JobRoleMapper jobRoleMapper;
    
    /**
     * 计算题目所需的最小停留时长（秒）
     */
    private int calculateRequiredDuration(Question question) {
        // 基础时长（秒）
        int baseDuration = 30; // 默认30秒
        
        // 根据难度调整基础时长
        switch (question.getDifficulty()) {
            case EASY:
                baseDuration = 30; // 简单题30秒
                break;
            case MEDIUM:
                baseDuration = 60; // 中等题60秒
                break;
            case HARD:
                baseDuration = 120; // 困难题120秒
                break;
        }
        
        // 计算内容长度（题目+答案）
        int contentLength = 0;
        if (question.getQuestionText() != null) {
            contentLength += question.getQuestionText().length();
        }
        if (question.getAnswerText() != null) {
            contentLength += question.getAnswerText().length();
        }
        
        // 每100个字符增加10秒
        int extraDuration = (contentLength / 100) * 10;
        
        // 最大调整不超过基础时长的2倍
        int maxExtraDuration = baseDuration * 2;
        int actualExtraDuration = Math.min(extraDuration, maxExtraDuration);
        
        return baseDuration + actualExtraDuration;
    }

    @Override
    @Transactional
    public UserPracticeHistory recordPractice(PracticeRecordRequest request, Long userId) {
        // 获取题目信息
        Question question = questionMapper.selectById(request.getQuestionId());
        
        // 计算所需的最小停留时长（秒）
        int requiredDuration = 0;
        if (question != null) {
            requiredDuration = calculateRequiredDuration(question);
        }
        
        // 计算实际停留时长（毫秒转换为秒）
        int actualDuration = request.getDuration() / 1000;
        
        // 判断是否完成：查看答案或停留时长达到要求
        boolean isCompleted = request.getViewAnswer() || actualDuration >= requiredDuration;
        
        // 检查是否已存在记录
        UserPracticeHistory existingRecord = userPracticeHistoryMapper.selectByUserIdAndQuestionId(userId, request.getQuestionId());
        
        if (existingRecord != null) {
            // 更新现有记录，累加学习时长
            existingRecord.setCompleted(isCompleted);
            existingRecord.setViewAnswer(request.getViewAnswer());
            // 累加学习时长
            existingRecord.setDuration(existingRecord.getDuration() + actualDuration);
            if (existingRecord.getJobRoleId() == null && question != null) {
                existingRecord.setJobRoleId(question.getPrimaryJobRoleId());
            }
            existingRecord.setUpdatedAt(LocalDateTime.now());
            userPracticeHistoryMapper.updateById(existingRecord);
            return existingRecord;
        } else {
            // 创建新记录
            UserPracticeHistory newRecord = new UserPracticeHistory();
            newRecord.setUserId(userId);
            newRecord.setQuestionId(request.getQuestionId());
            newRecord.setCompleted(isCompleted);
            newRecord.setViewAnswer(request.getViewAnswer());
            newRecord.setDuration(actualDuration);
            newRecord.setJobRoleId(question != null ? question.getPrimaryJobRoleId() : null);
            newRecord.setCreatedAt(LocalDateTime.now());
            newRecord.setUpdatedAt(LocalDateTime.now());
            userPracticeHistoryMapper.insert(newRecord);
            return newRecord;
        }
    }
    
    @Override
    public List<PracticeHistoryResponse> getUserPracticeHistory(Long userId, Integer page, Integer size) {
        Page<UserPracticeHistory> pageInfo = new Page<>(page + 1, size);
        
        QueryWrapper<UserPracticeHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("updated_at");
        
        Page<UserPracticeHistory> result = userPracticeHistoryMapper.selectPage(pageInfo, queryWrapper);
        
        // 转换为响应DTO
        return result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getUserPracticeStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总刷题数
        QueryWrapper<UserPracticeHistory> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("user_id", userId);
        Long totalCount = userPracticeHistoryMapper.selectCount(totalWrapper);
        
        // 已完成题目数
        QueryWrapper<UserPracticeHistory> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("user_id", userId)
                .eq("completed", true);
        Long completedCount = userPracticeHistoryMapper.selectCount(completedWrapper);
        
        // 查看答案的题目数
        QueryWrapper<UserPracticeHistory> viewAnswerWrapper = new QueryWrapper<>();
        viewAnswerWrapper.eq("user_id", userId)
                .eq("view_answer", true);
        Long viewAnswerCount = userPracticeHistoryMapper.selectCount(viewAnswerWrapper);
        
        // 总刷题时长（分钟）
        List<UserPracticeHistory> records = userPracticeHistoryMapper.selectByUserId(userId);
        int totalDuration = records.stream()
                .mapToInt(UserPracticeHistory::getDuration)
                .sum() / 60; // 转换为分钟
        
        stats.put("totalCount", totalCount);
        stats.put("completedCount", completedCount);
        stats.put("viewAnswerCount", viewAnswerCount);
        stats.put("totalDuration", totalDuration);
        stats.put("completionRate", totalCount > 0 ? (double) completedCount / totalCount * 100 : 0);
        
        return stats;
    }
    
    @Override
    public Boolean isQuestionCompleted(Long userId, Long questionId) {
        UserPracticeHistory record = userPracticeHistoryMapper.selectByUserIdAndQuestionId(userId, questionId);
        return record != null && record.getCompleted();
    }
    
    @Override
    public List<Long> getCompletedQuestionIds(Long userId) {
        QueryWrapper<UserPracticeHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("completed", true);
        
        List<UserPracticeHistory> records = userPracticeHistoryMapper.selectList(queryWrapper);
        return records.stream()
                .map(UserPracticeHistory::getQuestionId)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应DTO
     */
    private PracticeHistoryResponse convertToResponse(UserPracticeHistory record) {
        Question question = questionMapper.selectById(record.getQuestionId());
        Long jobRoleId = record.getJobRoleId() != null
                ? record.getJobRoleId()
                : (question != null ? question.getPrimaryJobRoleId() : null);
        JobRole jobRole = jobRoleId == null ? null : jobRoleMapper.selectById(jobRoleId);
        
        return PracticeHistoryResponse.builder()
                .id(record.getId())
                .questionId(record.getQuestionId())
                .questionTitle(question != null ? question.getTitle() : "")
                .questionSlug(question != null ? question.getSlug() : "")
                .jobRoleId(jobRoleId)
                .jobRoleName(jobRole != null ? jobRole.getName() : null)
                .difficulty(question != null ? question.getDifficulty().name() : "")
                .difficultyLabel(question != null ? question.getDifficulty().getLabel() : "")
                .completed(record.getCompleted())
                .viewAnswer(record.getViewAnswer())
                .duration(record.getDuration())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
