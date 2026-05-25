package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户刷题记录实体类
 */
@TableName("user_practice_history")
@Data
public class UserPracticeHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("question_id")
    private Long questionId;

    @TableField
    private Boolean completed = false;

    @TableField("view_answer")
    private Boolean viewAnswer = false;

    @TableField
    private Integer duration = 0;

    @TableField("job_role_id")
    private Long jobRoleId;

    @TableField("question_type")
    private String questionType;

    @TableField("self_score")
    private Double selfScore;

    @TableField("system_score")
    private Double systemScore;

    @TableField("weakness_tags")
    private String weaknessTags;

    @TableField("practice_source")
    private String practiceSource;

    @TableField("linked_interview_id")
    private Long linkedInterviewId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 关联题目
    @TableField(exist = false)
    private Question question;

    // 关联用户
    @TableField(exist = false)
    private User user;
}
