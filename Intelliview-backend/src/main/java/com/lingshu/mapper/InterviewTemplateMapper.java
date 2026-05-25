package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.InterviewTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InterviewTemplateMapper extends BaseMapper<InterviewTemplate> {

    @Select("SELECT * FROM interview_templates WHERE job_role_id = #{jobRoleId} ORDER BY id ASC LIMIT 1")
    InterviewTemplate findDefaultByJobRoleId(@Param("jobRoleId") Long jobRoleId);
}
