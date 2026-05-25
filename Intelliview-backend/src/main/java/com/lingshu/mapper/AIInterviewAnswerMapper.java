package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.AIInterviewAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AIInterviewAnswerMapper extends BaseMapper<AIInterviewAnswer> {

    @Select("SELECT * FROM ai_interview_answers WHERE interview_id = #{interviewId} ORDER BY submitted_at ASC, id ASC")
    List<AIInterviewAnswer> selectByInterviewId(@Param("interviewId") Long interviewId);

    @Select("SELECT * FROM ai_interview_answers WHERE interview_id = #{interviewId} AND question_id = #{questionId} LIMIT 1")
    AIInterviewAnswer selectByInterviewIdAndQuestionId(@Param("interviewId") Long interviewId, @Param("questionId") Long questionId);
}
