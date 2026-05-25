package com.lingshu.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SearchResponse {
    private List<QuestionResponse> questions;
    private List<UserProfileResponse> users;
    private List<TagResponse> tags;
    private List<AnswerResponse> answers;
    private Integer total;
    private Integer page;
    private Integer size;

    public SearchResponse() {
    }

    public SearchResponse(List<QuestionResponse> questions, List<UserProfileResponse> users, List<TagResponse> tags, List<AnswerResponse> answers, Integer total, Integer page, Integer size) {
        this.questions = questions;
        this.users = users;
        this.tags = tags;
        this.answers = answers;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }

    public List<UserProfileResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfileResponse> users) {
        this.users = users;
    }

    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }

    public List<AnswerResponse> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerResponse> answers) {
        this.answers = answers;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<QuestionResponse> questions;
        private List<UserProfileResponse> users;
        private List<TagResponse> tags;
        private List<AnswerResponse> answers;
        private Integer total;
        private Integer page;
        private Integer size;

        public Builder questions(List<QuestionResponse> questions) {
            this.questions = questions;
            return this;
        }

        public Builder users(List<UserProfileResponse> users) {
            this.users = users;
            return this;
        }

        public Builder tags(List<TagResponse> tags) {
            this.tags = tags;
            return this;
        }

        public Builder answers(List<AnswerResponse> answers) {
            this.answers = answers;
            return this;
        }

        public Builder total(Integer total) {
            this.total = total;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public SearchResponse build() {
            return new SearchResponse(questions, users, tags, answers, total, page, size);
        }
    }

    public static class QuestionResponse {
        private Long id;
        private String title;
        private String slug;
        private String description;
        private String difficulty;
        private String difficultyLabel;
        private Long categoryId;
        private Integer markCount;
        private Integer browseCount;
        private Integer viewCount;
        private Integer submitCount;
        private Integer acceptCount;
        private BigDecimal acceptRate;
        private LocalDateTime createdAt;

        // 扩展字段
        private String categoryName;
        private List<String> tags;
        private Integer answerCount;

        public QuestionResponse() {
        }

        public QuestionResponse(Long id, String title, String slug, String description, String difficulty, String difficultyLabel, Long categoryId, Integer markCount, Integer browseCount, Integer viewCount, Integer submitCount, Integer acceptCount, BigDecimal acceptRate, LocalDateTime createdAt, String categoryName, List<String> tags, Integer answerCount) {
            this.id = id;
            this.title = title;
            this.slug = slug;
            this.description = description;
            this.difficulty = difficulty;
            this.difficultyLabel = difficultyLabel;
            this.categoryId = categoryId;
            this.markCount = markCount;
            this.browseCount = browseCount;
            this.viewCount = viewCount;
            this.submitCount = submitCount;
            this.acceptCount = acceptCount;
            this.acceptRate = acceptRate;
            this.createdAt = createdAt;
            this.categoryName = categoryName;
            this.tags = tags;
            this.answerCount = answerCount;
        }

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

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public Integer getAnswerCount() {
            return answerCount;
        }

        public void setAnswerCount(Integer answerCount) {
            this.answerCount = answerCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String title;
            private String slug;
            private String description;
            private String difficulty;
            private String difficultyLabel;
            private Long categoryId;
            private Integer markCount;
            private Integer browseCount;
            private Integer viewCount;
            private Integer submitCount;
            private Integer acceptCount;
            private BigDecimal acceptRate;
            private LocalDateTime createdAt;
            private String categoryName;
            private List<String> tags;
            private Integer answerCount;

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

            public Builder createdAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder categoryName(String categoryName) {
                this.categoryName = categoryName;
                return this;
            }

            public Builder tags(List<String> tags) {
                this.tags = tags;
                return this;
            }

            public Builder answerCount(Integer answerCount) {
                this.answerCount = answerCount;
                return this;
            }

            public QuestionResponse build() {
                return new QuestionResponse(id, title, slug, description, difficulty, difficultyLabel, categoryId, markCount, browseCount, viewCount, submitCount, acceptCount, acceptRate, createdAt, categoryName, tags, answerCount);
            }
        }
    }

    public static class UserProfileResponse {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String bio;
        private Integer questionCount;
        private Integer answerCount;
        private Integer followerCount;
        private Integer followingCount;
        private LocalDateTime createdAt;
        private Boolean isFollowing; // 当前用户是否关注

        public UserProfileResponse() {
        }

        public UserProfileResponse(Long id, String username, String nickname, String avatar, String bio, Integer questionCount, Integer answerCount, Integer followerCount, Integer followingCount, LocalDateTime createdAt, Boolean isFollowing) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.avatar = avatar;
            this.bio = bio;
            this.questionCount = questionCount;
            this.answerCount = answerCount;
            this.followerCount = followerCount;
            this.followingCount = followingCount;
            this.createdAt = createdAt;
            this.isFollowing = isFollowing;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public Integer getQuestionCount() {
            return questionCount;
        }

        public void setQuestionCount(Integer questionCount) {
            this.questionCount = questionCount;
        }

        public Integer getAnswerCount() {
            return answerCount;
        }

        public void setAnswerCount(Integer answerCount) {
            this.answerCount = answerCount;
        }

        public Integer getFollowerCount() {
            return followerCount;
        }

        public void setFollowerCount(Integer followerCount) {
            this.followerCount = followerCount;
        }

        public Integer getFollowingCount() {
            return followingCount;
        }

        public void setFollowingCount(Integer followingCount) {
            this.followingCount = followingCount;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public Boolean getIsFollowing() {
            return isFollowing;
        }

        public void setIsFollowing(Boolean isFollowing) {
            this.isFollowing = isFollowing;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String username;
            private String nickname;
            private String avatar;
            private String bio;
            private Integer questionCount;
            private Integer answerCount;
            private Integer followerCount;
            private Integer followingCount;
            private LocalDateTime createdAt;
            private Boolean isFollowing;

            public Builder id(Long id) {
                this.id = id;
                return this;
            }

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder nickname(String nickname) {
                this.nickname = nickname;
                return this;
            }

            public Builder avatar(String avatar) {
                this.avatar = avatar;
                return this;
            }

            public Builder bio(String bio) {
                this.bio = bio;
                return this;
            }

            public Builder questionCount(Integer questionCount) {
                this.questionCount = questionCount;
                return this;
            }

            public Builder answerCount(Integer answerCount) {
                this.answerCount = answerCount;
                return this;
            }

            public Builder followerCount(Integer followerCount) {
                this.followerCount = followerCount;
                return this;
            }

            public Builder followingCount(Integer followingCount) {
                this.followingCount = followingCount;
                return this;
            }

            public Builder createdAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder isFollowing(Boolean isFollowing) {
                this.isFollowing = isFollowing;
                return this;
            }

            public UserProfileResponse build() {
                return new UserProfileResponse(id, username, nickname, avatar, bio, questionCount, answerCount, followerCount, followingCount, createdAt, isFollowing);
            }
        }
    }

    public static class TagResponse {
        private Long id;
        private String name;
        private String slug;
        private String color;
        private Integer questionCount;
        private String description;

        public TagResponse() {
        }

        public TagResponse(Long id, String name, String slug, String color, Integer questionCount, String description) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.color = color;
            this.questionCount = questionCount;
            this.description = description;
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

        public Integer getQuestionCount() {
            return questionCount;
        }

        public void setQuestionCount(Integer questionCount) {
            this.questionCount = questionCount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String name;
            private String slug;
            private String color;
            private Integer questionCount;
            private String description;

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

            public Builder questionCount(Integer questionCount) {
                this.questionCount = questionCount;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public TagResponse build() {
                return new TagResponse(id, name, slug, color, questionCount, description);
            }
        }
    }

    public static class AnswerResponse {
        private Long id;
        private String content;
        private Integer likeCount;
        private Integer commentCount;
        private String authorName;
        private String authorAvatar;
        private Long authorId;
        private Long questionId;
        private String questionTitle;
        private Boolean isLiked; // 当前用户是否点赞
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AnswerResponse() {
        }

        public AnswerResponse(Long id, String content, Integer likeCount, Integer commentCount, String authorName, String authorAvatar, Long authorId, Long questionId, String questionTitle, Boolean isLiked, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.content = content;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.authorId = authorId;
            this.questionId = questionId;
            this.questionTitle = questionTitle;
            this.isLiked = isLiked;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(Integer likeCount) {
            this.likeCount = likeCount;
        }

        public Integer getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(Integer commentCount) {
            this.commentCount = commentCount;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getAuthorAvatar() {
            return authorAvatar;
        }

        public void setAuthorAvatar(String authorAvatar) {
            this.authorAvatar = authorAvatar;
        }

        public Long getAuthorId() {
            return authorId;
        }

        public void setAuthorId(Long authorId) {
            this.authorId = authorId;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getQuestionTitle() {
            return questionTitle;
        }

        public void setQuestionTitle(String questionTitle) {
            this.questionTitle = questionTitle;
        }

        public Boolean getIsLiked() {
            return isLiked;
        }

        public void setIsLiked(Boolean isLiked) {
            this.isLiked = isLiked;
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

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long id;
            private String content;
            private Integer likeCount;
            private Integer commentCount;
            private String authorName;
            private String authorAvatar;
            private Long authorId;
            private Long questionId;
            private String questionTitle;
            private Boolean isLiked;
            private LocalDateTime createdAt;
            private LocalDateTime updatedAt;

            public Builder id(Long id) {
                this.id = id;
                return this;
            }

            public Builder content(String content) {
                this.content = content;
                return this;
            }

            public Builder likeCount(Integer likeCount) {
                this.likeCount = likeCount;
                return this;
            }

            public Builder commentCount(Integer commentCount) {
                this.commentCount = commentCount;
                return this;
            }

            public Builder authorName(String authorName) {
                this.authorName = authorName;
                return this;
            }

            public Builder authorAvatar(String authorAvatar) {
                this.authorAvatar = authorAvatar;
                return this;
            }

            public Builder authorId(Long authorId) {
                this.authorId = authorId;
                return this;
            }

            public Builder questionId(Long questionId) {
                this.questionId = questionId;
                return this;
            }

            public Builder questionTitle(String questionTitle) {
                this.questionTitle = questionTitle;
                return this;
            }

            public Builder isLiked(Boolean isLiked) {
                this.isLiked = isLiked;
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

            public AnswerResponse build() {
                return new AnswerResponse(id, content, likeCount, commentCount, authorName, authorAvatar, authorId, questionId, questionTitle, isLiked, createdAt, updatedAt);
            }
        }
    }
}
