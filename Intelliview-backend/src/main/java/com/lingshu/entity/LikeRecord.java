// entity/LikeRecord.java
package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("like_records")
@Data
public class LikeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField(exist = false)
    private User user;

    @TableField("target_type")
    private String targetType; // ANSWER, COMMENT, QUESTION

    @TableField("target_id")
    private Long targetId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

