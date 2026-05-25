package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("categories")
@Data
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String name;

    @TableField
    private String slug;

    @TableField
    private String description;

    @TableField
    private String icon;

    @TableField("parent_id")
    private Long parentId;

    @TableField("sort_order")
    private Integer sortOrder = 0;

    @TableField("question_count")
    private Integer questionCount = 0;

    @TableField("is_visible")
    private Boolean isVisible = true;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

