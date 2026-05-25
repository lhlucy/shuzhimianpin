package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.JobRoleSkillDimension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobRoleSkillDimensionMapper extends BaseMapper<JobRoleSkillDimension> {

    @Select("SELECT * FROM job_role_skill_dimensions WHERE job_role_id = #{jobRoleId} ORDER BY weight DESC, id ASC")
    List<JobRoleSkillDimension> findByJobRoleId(@Param("jobRoleId") Long jobRoleId);
}
