package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_interview_assessments")
public class AIInterviewAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("interview_id")
    private Long interviewId;

    @TableField("overall_score")
    private Double overallScore;

    @TableField("section_scores")
    private String sectionScores;

    @TableField("strengths")
    private String strengths;

    @TableField("weaknesses")
    private String weaknesses;

    @TableField("suggestions")
    private String suggestions;

    @TableField("recommended_resources")
    private String recommendedResources;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
