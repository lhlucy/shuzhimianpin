// dto/request/QuestionFilterRequest.java
package com.lingshu.dto.request;

import javax.validation.constraints.Min;
import java.util.List;

public class QuestionFilterRequest {
    private String keyword;
    private Long categoryId;
    private String difficulty;
    private Long primaryJobRoleId;
    private String primaryJobRoleCode;
    private String questionType;
    private Boolean isForInterview;
    private Boolean isForPractice;
    private List<Long> tagIds;

    @Min(value = 0, message = "页码不能小于0")
    private Integer page = 0;

    @Min(value = 1, message = "每页数量不能小于1")
    private Integer size = 20;

    private String sortBy = "createdAt"; // createdAt, markCount, browseCount, difficulty, sortOrder
    private String sortDirection = "desc"; // asc, desc

    // 高级过滤
    private Boolean includeSolution = false;
    private Boolean onlyMarked = false;
    private Boolean onlyUnanswered = false;

    // Getter?Setter??
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
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

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Boolean getIsForInterview() {
        return isForInterview;
    }

    public void setIsForInterview(Boolean isForInterview) {
        this.isForInterview = isForInterview;
    }

    public Boolean getIsForPractice() {
        return isForPractice;
    }

    public void setIsForPractice(Boolean isForPractice) {
        this.isForPractice = isForPractice;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Boolean getIncludeSolution() {
        return includeSolution;
    }

    public void setIncludeSolution(Boolean includeSolution) {
        this.includeSolution = includeSolution;
    }

    public Boolean getOnlyMarked() {
        return onlyMarked;
    }

    public void setOnlyMarked(Boolean onlyMarked) {
        this.onlyMarked = onlyMarked;
    }

    public Boolean getOnlyUnanswered() {
        return onlyUnanswered;
    }

    public void setOnlyUnanswered(Boolean onlyUnanswered) {
        this.onlyUnanswered = onlyUnanswered;
    }
}
