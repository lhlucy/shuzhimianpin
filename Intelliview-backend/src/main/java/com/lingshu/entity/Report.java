// entity/Report.java
package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("reports")
@Data
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField(exist = false)
    private User user;

    @TableField("target_type")
    private String targetType; // QUESTION, ANSWER, COMMENT, USER

    @TableField("target_id")
    private Long targetId;

    @TableField("reason")
    private String reason; // SPAM, ABUSE, INAPPROPRIATE, COPYRIGHT, OTHER

    @TableField
    private String description;

    @TableField("status")
    private String status = "PENDING"; // PENDING, PROCESSING, RESOLVED, REJECTED

    @TableField("admin_id")
    private Long adminId;

    @TableField(exist = false)
    private User admin;

    @TableField("processed_at")
    private LocalDateTime processedAt;

    @TableField
    private String result;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
