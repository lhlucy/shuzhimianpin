package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_calibration")
public class EvaluationCalibration {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("interview_id")
    private Long interviewId;

    @TableField("job_role_id")
    private Long jobRoleId;

    @TableField("source_type")
    private String sourceType;

    @TableField("evaluation_run_count")
    private Integer evaluationRunCount;

    @TableField("expected_score")
    private Double expectedScore;

    @TableField("ai_score")
    private Double aiScore;

    @TableField("score_variance")
    private Double scoreVariance;

    @TableField("bias_rate")
    private Double biasRate;

    @TableField("dimension_scores")
    private String dimensionScores;

    @TableField("calibration_status")
    private String calibrationStatus;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
