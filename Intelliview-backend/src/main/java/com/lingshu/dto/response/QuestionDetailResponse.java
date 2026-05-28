// dto/response/QuestionDetailResponse.java
package com.lingshu.dto.response;

import com.lingshu.dto.KeyPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class QuestionDetailResponse {

    // 分类对象（兼容前端）
    public static class Category {
        private Long id;
        private String name;

        public Category() {
        }

        public Category(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String questionText;
    private String answerText;
    private String difficulty;
    private String difficultyLabel;

    // 统计信息
    private Integer markCount = 0;      // 收藏数
    private Integer shareCount = 0;     // 分享数
    private Integer browseCount = 0;    // 浏览数
    private Integer viewCount = 0;      // 查看数
    private Integer likeCount = 0;      // 点赞数
    private Integer answerCount = 0;    // 回答数
    private LocalDateTime lastBrowseTime;

    // 知识点（用于AI评估）
    private List<KeyPoint> keyPoints = new ArrayList<>();

    // 相关问题
    private List<QuestionSimpleResponse> relatedQuestions = new ArrayList<>();

    // 分类和标签
    private Long categoryId;
    private String categoryName;
    private Long primaryJobRoleId;
    private List<QuestionResponse.TagResponse> tags = new ArrayList<>();

    // 分类对象（兼容前端）
    private Category category;

    // 元数据
    private Map<String, Object> metadata = new HashMap<>();

    // 创建信息
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 其他
    private Integer submitCount = 0;
    private Integer acceptCount = 0;
    private BigDecimal acceptRate = BigDecimal.ZERO;
    private Boolean isVisible = true;
    private Integer sortOrder = 0;
    private Boolean isFavorited = false;
    private Boolean isLiked = false;

    // 无参构造函数
    public QuestionDetailResponse() {
    }

    // 全参构造函数
    public QuestionDetailResponse(Long id, String title, String slug, String description, String questionText, String answerText, String difficulty, String difficultyLabel, Integer markCount, Integer shareCount, Integer browseCount, Integer viewCount, Integer likeCount, Integer answerCount, LocalDateTime lastBrowseTime, List<KeyPoint> keyPoints, List<QuestionSimpleResponse> relatedQuestions, Long categoryId, String categoryName, List<QuestionResponse.TagResponse> tags, Category category, Map<String, Object> metadata, Long createdBy, String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt, Integer submitCount, Integer acceptCount, BigDecimal acceptRate, Boolean isVisible, Integer sortOrder, Boolean isFavorited, Boolean isLiked) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.questionText = questionText;
        this.answerText = answerText;
        this.difficulty = difficulty;
        this.difficultyLabel = difficultyLabel;
        this.markCount = markCount != null ? markCount : 0;
        this.shareCount = shareCount != null ? shareCount : 0;
        this.browseCount = browseCount != null ? browseCount : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.answerCount = answerCount != null ? answerCount : 0;
        this.lastBrowseTime = lastBrowseTime;
        this.keyPoints = keyPoints != null ? keyPoints : new ArrayList<>();
        this.relatedQuestions = relatedQuestions != null ? relatedQuestions : new ArrayList<>();
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.category = category;
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submitCount = submitCount != null ? submitCount : 0;
        this.acceptCount = acceptCount != null ? acceptCount : 0;
        this.acceptRate = acceptRate != null ? acceptRate : BigDecimal.ZERO;
        this.isVisible = isVisible != null ? isVisible : true;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.isFavorited = isFavorited != null ? isFavorited : false;
        this.isLiked = isLiked != null ? isLiked : false;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficultyLabel() {
        return difficultyLabel;
    }

    public void setDifficultyLabel(String difficultyLabel) {
        this.difficultyLabel = difficultyLabel;
    }

    public Integer getMarkCount() {
        return markCount;
    }

    public void setMarkCount(Integer markCount) {
        this.markCount = markCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Integer getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(Integer browseCount) {
        this.browseCount = browseCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(Integer answerCount) {
        this.answerCount = answerCount;
    }

    public LocalDateTime getLastBrowseTime() {
        return lastBrowseTime;
    }

    public void setLastBrowseTime(LocalDateTime lastBrowseTime) {
        this.lastBrowseTime = lastBrowseTime;
    }

    public List<KeyPoint> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<KeyPoint> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public List<QuestionSimpleResponse> getRelatedQuestions() {
        return relatedQuestions;
    }

    public void setRelatedQuestions(List<QuestionSimpleResponse> relatedQuestions) {
        this.relatedQuestions = relatedQuestions;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getPrimaryJobRoleId() {
        return primaryJobRoleId;
    }

    public void setPrimaryJobRoleId(Long primaryJobRoleId) {
        this.primaryJobRoleId = primaryJobRoleId;
    }

    public List<QuestionResponse.TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<QuestionResponse.TagResponse> tags) {
        this.tags = tags;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getSubmitCount() {
        return submitCount;
    }

    public void setSubmitCount(Integer submitCount) {
        this.submitCount = submitCount;
    }

    public Integer getAcceptCount() {
        return acceptCount;
    }

    public void setAcceptCount(Integer acceptCount) {
        this.acceptCount = acceptCount;
    }

    public BigDecimal getAcceptRate() {
        return acceptRate;
    }

    public void setAcceptRate(BigDecimal acceptRate) {
        this.acceptRate = acceptRate;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }

    // Builder内部类
    public static class Builder {
        private Long id;
        private String title;
        private String slug;
        private String description;
        private String questionText;
        private String answerText;
        private String difficulty;
        private String difficultyLabel;
        private Integer markCount = 0;
        private Integer shareCount = 0;
        private Integer browseCount = 0;
        private Integer viewCount = 0;
        private Integer likeCount = 0;
        private Integer answerCount = 0;
        private LocalDateTime lastBrowseTime;
        private List<KeyPoint> keyPoints = new ArrayList<>();
        private List<QuestionSimpleResponse> relatedQuestions = new ArrayList<>();
        private Long categoryId;
        private String categoryName;
        private List<QuestionResponse.TagResponse> tags = new ArrayList<>();
        private Category category;
        private Map<String, Object> metadata = new HashMap<>();
        private Long createdBy;
        private String createdByName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer submitCount = 0;
        private Integer acceptCount = 0;
        private BigDecimal acceptRate = BigDecimal.ZERO;
        private Boolean isVisible = true;
        private Integer sortOrder = 0;
        private Boolean isFavorited = false;
        private Boolean isLiked = false;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder questionText(String questionText) {
            this.questionText = questionText;
            return this;
        }

        public Builder answerText(String answerText) {
            this.answerText = answerText;
            return this;
        }

        public Builder difficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder difficultyLabel(String difficultyLabel) {
            this.difficultyLabel = difficultyLabel;
            return this;
        }

        public Builder markCount(Integer markCount) {
            this.markCount = markCount;
            return this;
        }

        public Builder shareCount(Integer shareCount) {
            this.shareCount = shareCount;
            return this;
        }

        public Builder browseCount(Integer browseCount) {
            this.browseCount = browseCount;
            return this;
        }

        public Builder viewCount(Integer viewCount) {
            this.viewCount = viewCount;
            return this;
        }

        public Builder likeCount(Integer likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder answerCount(Integer answerCount) {
            this.answerCount = answerCount;
            return this;
        }

        public Builder lastBrowseTime(LocalDateTime lastBrowseTime) {
            this.lastBrowseTime = lastBrowseTime;
            return this;
        }

        public Builder keyPoints(List<KeyPoint> keyPoints) {
            this.keyPoints = keyPoints;
            return this;
        }

        public Builder relatedQuestions(List<QuestionSimpleResponse> relatedQuestions) {
            this.relatedQuestions = relatedQuestions;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder tags(List<QuestionResponse.TagResponse> tags) {
            this.tags = tags;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder createdBy(Long createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdByName(String createdByName) {
            this.createdByName = createdByName;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder submitCount(Integer submitCount) {
            this.submitCount = submitCount;
            return this;
        }

        public Builder acceptCount(Integer acceptCount) {
            this.acceptCount = acceptCount;
            return this;
        }

        public Builder acceptRate(BigDecimal acceptRate) {
            this.acceptRate = acceptRate;
            return this;
        }

        public Builder isVisible(Boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder isFavorited(Boolean isFavorited) {
            this.isFavorited = isFavorited;
            return this;
        }

        public Builder isLiked(Boolean isLiked) {
            this.isLiked = isLiked;
            return this;
        }

        public QuestionDetailResponse build() {
            return new QuestionDetailResponse(id, title, slug, description, questionText, answerText, difficulty, difficultyLabel, markCount, shareCount, browseCount, viewCount, likeCount, answerCount, lastBrowseTime, keyPoints, relatedQuestions, categoryId, categoryName, tags, category, metadata, createdBy, createdByName, createdAt, updatedAt, submitCount, acceptCount, acceptRate, isVisible, sortOrder, isFavorited, isLiked);
        }
    }

    // builder()方法
    public static Builder builder() {
        return new Builder();
    }

    // Stats内部类
    public static class Stats {
        private Integer markCount;
        private Integer browseCount;
        private Integer viewCount;
        private Integer shareCount;
        private Integer submitCount;
        private Integer acceptCount;
        private BigDecimal acceptRate;

        // 无参构造函数
        public Stats() {
        }

        // 全参构造函数
        public Stats(Integer markCount, Integer browseCount, Integer viewCount, Integer shareCount, Integer submitCount, Integer acceptCount, BigDecimal acceptRate) {
            this.markCount = markCount;
            this.browseCount = browseCount;
            this.viewCount = viewCount;
            this.shareCount = shareCount;
            this.submitCount = submitCount;
            this.acceptCount = acceptCount;
            this.acceptRate = acceptRate;
        }

        // Getter和Setter方法
        public Integer getMarkCount() {
            return markCount;
        }

        public void setMarkCount(Integer markCount) {
            this.markCount = markCount;
        }

        public Integer getBrowseCount() {
            return browseCount;
        }

        public void setBrowseCount(Integer browseCount) {
            this.browseCount = browseCount;
        }

        public Integer getViewCount() {
            return viewCount;
        }

        public void setViewCount(Integer viewCount) {
            this.viewCount = viewCount;
        }

        public Integer getShareCount() {
            return shareCount;
        }

        public void setShareCount(Integer shareCount) {
            this.shareCount = shareCount;
        }

        public Integer getSubmitCount() {
            return submitCount;
        }

        public void setSubmitCount(Integer submitCount) {
            this.submitCount = submitCount;
        }

        public Integer getAcceptCount() {
            return acceptCount;
        }

        public void setAcceptCount(Integer acceptCount) {
            this.acceptCount = acceptCount;
        }

        public BigDecimal getAcceptRate() {
            return acceptRate;
        }

        public void setAcceptRate(BigDecimal acceptRate) {
            this.acceptRate = acceptRate;
        }

        // Builder内部类
        public static class Builder {
            private Integer markCount;
            private Integer browseCount;
            private Integer viewCount;
            private Integer shareCount;
            private Integer submitCount;
            private Integer acceptCount;
            private BigDecimal acceptRate;

            public Builder markCount(Integer markCount) {
                this.markCount = markCount;
                return this;
            }

            public Builder browseCount(Integer browseCount) {
                this.browseCount = browseCount;
                return this;
            }

            public Builder viewCount(Integer viewCount) {
                this.viewCount = viewCount;
                return this;
            }

            public Builder shareCount(Integer shareCount) {
                this.shareCount = shareCount;
                return this;
            }

            public Builder submitCount(Integer submitCount) {
                this.submitCount = submitCount;
                return this;
            }

            public Builder acceptCount(Integer acceptCount) {
                this.acceptCount = acceptCount;
                return this;
            }

            public Builder acceptRate(BigDecimal acceptRate) {
                this.acceptRate = acceptRate;
                return this;
            }

            public Stats build() {
                return new Stats(markCount, browseCount, viewCount, shareCount, submitCount, acceptCount, acceptRate);
            }
        }

        // builder()方法
        public static Builder builder() {
            return new Builder();
        }
    }

    /**
     * 将BigDecimal类型的acceptRate转换为Double
     */
    public Double getAcceptRateAsDouble() {
        if (acceptRate != null) {
            return acceptRate.doubleValue();
        }
        return 0.0;
    }
}
