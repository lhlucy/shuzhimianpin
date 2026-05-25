package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_interview_answers")
public class AIInterviewAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("interview_id")
    private Long interviewId;

    @TableField("question_id")
    private Long questionId;

    @TableField("content")
    private String content;

    @TableField("transcript_text")
    private String transcriptText;

    @TableField("input_mode")
    private String inputMode;

    @TableField("duration")
    private Integer duration;

    @TableField("confidence_level")
    private Integer confidenceLevel;

    @TableField("score")
    private Double score;

    @TableField("feedback")
    private String feedback;

    @TableField("keyword_analysis")
    private String keywordAnalysis;

    @TableField("expression_analysis")
    private String expressionAnalysis;

    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @TableField("answer_text")
    private String answerText;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
