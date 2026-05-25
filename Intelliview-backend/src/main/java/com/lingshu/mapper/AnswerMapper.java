package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingshu.entity.Answer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
    // 根据问题ID查询未删除的回答并按创建时间降序排序
    IPage<Answer> findByQuestionIdAndIsDeletedFalseOrderByCreatedAtDesc(IPage<Answer> page, @Param("questionId") Long questionId);
    
    // 根据用户ID查询未删除的回答并按创建时间降序排序
    IPage<Answer> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(IPage<Answer> page, @Param("userId") Long userId);
    
    // 根据ID查询未删除的回答
    Answer findByIdAndIsDeletedFalse(@Param("id") Long id);
    
    // 根据问题ID查询未删除的回答
    List<Answer> findByQuestionIdAndIsDeletedFalse(@Param("questionId") Long questionId);
    
    // 根据问题ID统计未删除的回答数量
    @Select("SELECT COUNT(*) FROM answers WHERE question_id = #{questionId} AND is_deleted = false")
    Long countByQuestionId(@Param("questionId") Long questionId);
    
    // 根据问题ID查询已审核未删除的回答并按点赞数降序排序
    @Select("SELECT * FROM answers WHERE question_id = #{questionId} AND is_approved = true AND is_deleted = false ORDER BY like_count DESC LIMIT #{limit}")
    List<Answer> findTopAnswersByQuestionId(@Param("questionId") Long questionId, @Param("limit") Integer limit);
}

