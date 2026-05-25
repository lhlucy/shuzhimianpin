package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_interviews")
public class AIInterview {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("job_role_id")
    private Long jobRoleId;

    @TableField("target_position")
    private String targetPosition;

    @TableField("interview_language")
    private String interviewLanguage;

    @TableField("interview_type")
    private String interviewType;

    @TableField("status")
    private String status;

    @TableField("duration")
    private Integer duration;

    @TableField("actual_duration")
    private Integer actualDuration;

    @TableField("total_score")
    private Double totalScore;

    @TableField("difficulty")
    private String difficulty;

    @TableField("skill_tags")
    private String skillTags;

    @TableField("brushed_question_ids")
    private String brushedQuestionIds;

    @TableField("brushed_question_summary")
    private String brushedQuestionSummary;

    @TableField("voice_enabled")
    private Boolean voiceEnabled;

    @TableField("resume_file_name")
    private String resumeFileName;

    @TableField("resume_content")
    private String resumeContent;

    @TableField("interviewer_profile")
    private String interviewerProfile;

    @TableField("opening_message")
    private String openingMessage;

    @TableField("config_snapshot")
    private String configSnapshot;

    @TableField("question_count")
    private Integer questionCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("ended_at")
    private LocalDateTime endedAt;
}
