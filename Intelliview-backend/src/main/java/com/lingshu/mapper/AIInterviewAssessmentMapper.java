package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.AIInterviewAssessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AIInterviewAssessmentMapper extends BaseMapper<AIInterviewAssessment> {

    @Select("SELECT * FROM ai_interview_assessments WHERE interview_id = #{interviewId} LIMIT 1")
    AIInterviewAssessment selectByInterviewId(@Param("interviewId") Long interviewId);
}
