// entity/Question.java
package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.KeyPoint;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@TableName("questions")
@Data
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String title;

    @TableField
    private String slug;

    @TableField
    private String description;

    @TableField("question_text")
    private String questionText;

    @TableField("answer_text")
    private String answerText;

    @TableField
    private Difficulty difficulty = Difficulty.MEDIUM;

    @TableField("category_id")
    private Long categoryId;

    @TableField("question_type")
    private String questionType;

    @TableField("primary_job_role_id")
    private Long primaryJobRoleId;

    @TableField("skill_dimension_summary")
    private String skillDimensionSummary;

    @TableField("standard_answer_points")
    private String standardAnswerPoints;

    @TableField("common_mistakes")
    private String commonMistakes;

    @TableField("follow_up_prompts")
    private String followUpPrompts;

    @TableField("scoring_points")
    private String scoringPoints;

    @TableField("recommended_resources")
    private String recommendedResources;

    @TableField("is_for_interview")
    private Boolean isForInterview = true;

    @TableField("is_for_practice")
    private Boolean isForPractice = true;

    @TableField("interview_frequency")
    private Integer interviewFrequency = 0;

    @TableField("source_type")
    private String sourceType = "ADMIN";

    @TableField(exist = false)
    private Category category;

    // === 统计字段 ===
    @TableField("mark_count")
    private Integer markCount = 0;  // 收藏数

    @TableField("share_count")
    private Integer shareCount = 0; // 分享数

    @TableField("browse_count")
    private Integer browseCount = 0; // 浏览数

    @TableField("last_browse_time")
    private LocalDateTime lastBrowseTime; // 最后浏览时间

    @TableField("view_count")
    private Integer viewCount = 0; // 查看数

    @TableField("like_count")
    private Integer likeCount = 0; // 点赞数

    @TableField(exist = false)
    private List<KeyPoint> keyPoints = new ArrayList<>();

    @TableField("related_questions")
    private String relatedQuestions; // 相关问题JSON

    @TableField("metadata")
    private String metadata; // 元数据

    @TableField
    private String source = "ADMIN"; // ADMIN, USER, INTERVIEW

    // 提交统计
    @TableField("submit_count")
    private Integer submitCount = 0;

    @TableField("accept_count")
    private Integer acceptCount = 0;

    @TableField("accept_rate")
    private BigDecimal acceptRate = BigDecimal.ZERO;

    @TableField("is_visible")
    private Boolean isVisible = true;

    @TableField("sort_order")
    private Integer sortOrder = 0;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Set<Tag> tags = new HashSet<>();

    // JSON处理方法
    public List<Long> getRelatedQuestionIdList() {
        if (relatedQuestions == null || relatedQuestions.trim().isEmpty() || relatedQuestions.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(relatedQuestions, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setRelatedQuestionIdList(List<Long> ids) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.relatedQuestions = mapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            this.relatedQuestions = "[]";
        }
    }

    public Map<String, Object> getMetadataMap() {
        if (metadata == null || metadata.trim().isEmpty() || metadata.equals("{}")) {
            return new HashMap<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(metadata, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    public void setMetadataMap(Map<String, Object> metadataMap) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.metadata = mapper.writeValueAsString(metadataMap);
        } catch (JsonProcessingException e) {
            this.metadata = "{}";
        }
    }

    public enum Difficulty {
        EASY("简单"),
        MEDIUM("中等"),
        HARD("困难");

        private final String label;

        Difficulty(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
