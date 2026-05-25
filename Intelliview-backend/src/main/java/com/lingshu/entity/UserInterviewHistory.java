package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@TableName("user_interview_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterviewHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("interview_id")
    private Long interviewId;

    @TableField("interview_type")
    private String interviewType;

    @TableField("score")
    private Double score;

    @TableField("interview_date")
    private LocalDateTime interviewDate;

    @TableField("position")
    private String position;

    @TableField("skill_tags")
    private String skillTags; // JSON格式

    @TableField("strengths")
    private String strengths; // JSON格式

    @TableField("weaknesses")
    private String weaknesses; // JSON格式

    @TableField("duration")
    private Integer duration;

    @TableField("created_at")
    private LocalDateTime createdAt;

    // 关联关系
    @TableField(exist = false)
    private User user;

}
