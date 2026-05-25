package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.UserPracticeHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户刷题记录Mapper
 */
@Mapper
public interface UserPracticeHistoryMapper extends BaseMapper<UserPracticeHistory> {
    
    /**
     * 根据用户ID查询刷题记录
     */
    List<UserPracticeHistory> selectByUserId(Long userId);
    
    /**
     * 根据用户ID和题目ID查询记录
     */
    UserPracticeHistory selectByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 统计用户已完成的题目数量
     */
    Integer countCompletedByUserId(Long userId);
}
