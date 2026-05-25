package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.JobRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobRoleMapper extends BaseMapper<JobRole> {

    @Select("SELECT * FROM job_roles WHERE is_active = true ORDER BY id ASC")
    List<JobRole> findActiveRoles();

    @Select("SELECT * FROM job_roles WHERE code = #{code} LIMIT 1")
    JobRole findByCode(@Param("code") String code);
}
