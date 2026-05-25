package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_banks")
public class QuestionBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Integer questionCount;

    private String icon;

    private Boolean popular;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
