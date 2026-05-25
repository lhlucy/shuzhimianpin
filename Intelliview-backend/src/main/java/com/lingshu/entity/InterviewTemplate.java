package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_templates")
public class InterviewTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("job_role_id")
    private Long jobRoleId;

    @TableField("template_code")
    private String templateCode;

    @TableField("name")
    private String name;

    @TableField("interview_type")
    private String interviewType;

    @TableField("difficulty")
    private String difficulty;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("default_question_count")
    private Integer defaultQuestionCount;

    @TableField("question_type_distribution")
    private String questionTypeDistribution;

    @TableField("difficulty_distribution")
    private String difficultyDistribution;

    @TableField("follow_up_intensity")
    private String followUpIntensity;

    @TableField("scoring_weights")
    private String scoringWeights;

    @TableField("config")
    private String config;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
