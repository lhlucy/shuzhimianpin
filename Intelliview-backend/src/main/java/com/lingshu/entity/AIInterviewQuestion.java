package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_interview_questions")
public class AIInterviewQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("interview_id")
    private Long interviewId;

    @TableField("content")
    private String content;

    @TableField("type")
    private String type;

    @TableField("difficulty")
    private String difficulty;

    @TableField("topic")
    private String topic;

    @TableField("estimated_time")
    private Integer estimatedTime;

    @TableField("question_order")
    private Integer questionOrder;

    @TableField("context")
    private String context;

    @TableField("source_question_id")
    private Long sourceQuestionId;

    @TableField("parent_question_id")
    private Long parentQuestionId;

    @TableField("question_text")
    private String questionText;

    @TableField("question_type")
    private String questionType;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("time_limit")
    private Integer timeLimit;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
