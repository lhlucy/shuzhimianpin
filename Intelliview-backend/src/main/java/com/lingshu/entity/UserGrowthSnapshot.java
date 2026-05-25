package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_growth_snapshots")
public class UserGrowthSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("job_role_id")
    private Long jobRoleId;

    @TableField("snapshot_date")
    private LocalDate snapshotDate;

    @TableField("overall_score")
    private Double overallScore;

    @TableField("job_match_score")
    private Double jobMatchScore;

    @TableField("dimension_scores")
    private String dimensionScores;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
