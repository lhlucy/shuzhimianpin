package com.lingshu.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class QuestionResponse {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String difficulty;
    private String difficultyLabel;
    private Long categoryId;
    private String categoryName;
    private Long primaryJobRoleId;
    private String primaryJobRoleCode;
    private String primaryJobRoleName;
    private List<TagResponse> tags;
    private Integer submitCount;
    private Integer acceptCount;
    private BigDecimal acceptRate;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime favoriteTime;

    // 无参构造方法
    public QuestionResponse() {
    }

    // 全参构造方法
    public QuestionResponse(Long id, String title, String slug, String description, String difficulty, String difficultyLabel, Long categoryId, String categoryName, List<TagResponse> tags, Integer submitCount, Integer acceptCount, BigDecimal acceptRate, Integer viewCount, Integer likeCount, Boolean isVisible, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime favoriteTime) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.difficulty = difficulty;
        this.difficultyLabel = difficultyLabel;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tags = tags;
        this.submitCount = submitCount;
        this.acceptCount = acceptCount;
        this.acceptRate = acceptRate;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.isVisible = isVisible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.favoriteTime = favoriteTime;
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

    public String getPrimaryJobRoleCode() {
        return primaryJobRoleCode;
    }

    public void setPrimaryJobRoleCode(String primaryJobRoleCode) {
        this.primaryJobRoleCode = primaryJobRoleCode;
    }

    public String getPrimaryJobRoleName() {
        return primaryJobRoleName;
    }

    public void setPrimaryJobRoleName(String primaryJobRoleName) {
        this.primaryJobRoleName = primaryJobRoleName;
    }

    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
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

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
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

    public LocalDateTime getFavoriteTime() {
        return favoriteTime;
    }

    public void setFavoriteTime(LocalDateTime favoriteTime) {
        this.favoriteTime = favoriteTime;
    }

    // Builder类
    public static class Builder {
        private Long id;
        private String title;
        private String slug;
        private String description;
        private String difficulty;
        private String difficultyLabel;
        private Long categoryId;
        private String categoryName;
        private Long primaryJobRoleId;
        private String primaryJobRoleCode;
        private String primaryJobRoleName;
        private List<TagResponse> tags;
        private Integer submitCount;
        private Integer acceptCount;
        private BigDecimal acceptRate;
        private Integer viewCount;
        private Integer likeCount;
        private Boolean isVisible;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime favoriteTime;

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

        public Builder difficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder difficultyLabel(String difficultyLabel) {
            this.difficultyLabel = difficultyLabel;
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

        public Builder primaryJobRoleId(Long primaryJobRoleId) {
            this.primaryJobRoleId = primaryJobRoleId;
            return this;
        }

        public Builder primaryJobRoleCode(String primaryJobRoleCode) {
            this.primaryJobRoleCode = primaryJobRoleCode;
            return this;
        }

        public Builder primaryJobRoleName(String primaryJobRoleName) {
            this.primaryJobRoleName = primaryJobRoleName;
            return this;
        }

        public Builder tags(List<TagResponse> tags) {
            this.tags = tags;
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

        public Builder viewCount(Integer viewCount) {
            this.viewCount = viewCount;
            return this;
        }

        public Builder likeCount(Integer likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder isVisible(Boolean isVisible) {
            this.isVisible = isVisible;
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

        public Builder favoriteTime(LocalDateTime favoriteTime) {
            this.favoriteTime = favoriteTime;
            return this;
        }

        public QuestionResponse build() {
            QuestionResponse response = new QuestionResponse(id, title, slug, description, difficulty, difficultyLabel, categoryId, categoryName, tags, submitCount, acceptCount, acceptRate, viewCount, likeCount, isVisible, createdAt, updatedAt, favoriteTime);
            response.setPrimaryJobRoleId(primaryJobRoleId);
            response.setPrimaryJobRoleCode(primaryJobRoleCode);
            response.setPrimaryJobRoleName(primaryJobRoleName);
            return response;
        }
    }

    // builder()方法
    public static Builder builder() {
        return new Builder();
    }

    // TagResponse类
    public static class TagResponse {
        private Long id;
        private String name;
        private String slug;
        private String color;

        // 无参构造方法
        public TagResponse() {
        }

        // 全参构造方法
        public TagResponse(Long id, String name, String slug, String color) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.color = color;
        }

        // Getter和Setter方法
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

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        // Builder类
        public static class Builder {
            private Long id;
            private String name;
            private String slug;
            private String color;

            public Builder id(Long id) {
                this.id = id;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder slug(String slug) {
                this.slug = slug;
                return this;
            }

            public Builder color(String color) {
                this.color = color;
                return this;
            }

            public TagResponse build() {
                return new TagResponse(id, name, slug, color);
            }
        }

        // builder()方法
        public static Builder builder() {
            return new Builder();
        }
    }
}
