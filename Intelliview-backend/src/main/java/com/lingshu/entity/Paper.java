package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 试卷实体类
 */
@TableName("papers")
@Data
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String title;

    @TableField
    private String description;

    @TableField
    private Integer duration;

    @TableField("question_count")
    private Integer questionCount;

    @TableField
    private Boolean status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
