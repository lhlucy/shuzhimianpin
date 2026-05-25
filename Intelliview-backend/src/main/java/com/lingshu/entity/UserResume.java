package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_resumes")
public class UserResume {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("file_name")
    private String fileName;

    @TableField("original_file_name")
    private String originalFileName;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("intention_job")
    private String intentionJob;

    @TableField("recruitment_type")
    private String recruitmentType;

    @TableField("intention_city")
    private String intentionCity;

    @TableField("expected_salary")
    private String expectedSalary;

    @TableField("parsed_at")
    private LocalDateTime parsedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
