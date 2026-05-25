package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.AIInterviewQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AIInterviewQuestionMapper extends BaseMapper<AIInterviewQuestion> {

    @Select("SELECT * FROM ai_interview_questions WHERE interview_id = #{interviewId} ORDER BY question_order ASC, id ASC")
    List<AIInterviewQuestion> selectByInterviewId(@Param("interviewId") Long interviewId);

    @Update("UPDATE ai_interview_questions SET question_order = question_order + 1, sort_order = sort_order + 1 WHERE interview_id = #{interviewId} AND question_order > #{afterOrder}")
    int bumpQuestionOrderAfter(@Param("interviewId") Long interviewId, @Param("afterOrder") Integer afterOrder);

    @Select("SELECT COUNT(*) FROM ai_interview_questions WHERE parent_question_id = #{parentQuestionId}")
    int countByParentQuestionId(@Param("parentQuestionId") Long parentQuestionId);
}
