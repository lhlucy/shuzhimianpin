package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("job_role_skill_dimensions")
public class JobRoleSkillDimension {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("job_role_id")
    private Long jobRoleId;

    @TableField("dimension_code")
    private String dimensionCode;

    @TableField("dimension_name")
    private String dimensionName;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("description")
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
