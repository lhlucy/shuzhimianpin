// entity/Answer.java
package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answers")
public class Answer {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(exist = false)
    private User user;

    @TableField(value = "question_id")
    private Long questionId;

    @TableField(exist = false)
    private Question question;

    @TableField(value = "content")
    private String content;

    @TableField(value = "like_count")
    private Integer likeCount = 0;

    @TableField(value = "view_count")
    private Integer viewCount = 0;

    @TableField(value = "is_approved")
    private Boolean isApproved = false;

    @TableField(value = "is_anonymous")
    private Boolean isAnonymous = false;

    @TableField(value = "is_deleted")
    private Boolean isDeleted = false;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
