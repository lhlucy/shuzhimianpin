// entity/Comment.java
package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("comments")
@Data
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField(exist = false)
    private User user;

    @TableField("answer_id")
    private Long answerId;

    @TableField(exist = false)
    private Answer answer;

    @TableField
    private String content;

    @TableField("parent_id")
    private Long parentId;

    @TableField(exist = false)
    private Comment parent;

    @TableField("like_count")
    private Integer likeCount = 0;

    @TableField("is_approved")
    private Boolean isApproved = true;

    @TableField("is_deleted")
    private Boolean isDeleted = false;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

