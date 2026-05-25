// dto/request/SearchRequest.java
package com.lingshu.dto.request;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class SearchRequest {
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    // LeetCode???????
    private List<String> difficultyList;  // ?????EASY, MEDIUM, HARD
    private List<String> tagList;         // ????
    private List<Long> categoryIds;       // ??ID??
    private String status;                // ???ALL, SOLVED, ATTEMPTED, UNSOLVED (??????)

    // ??
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "relevance";  // relevance, newest, oldest, hot
    private String order = "desc";        // asc, desc

    // ?????LeetCode?????????
    private Boolean searchTitle = true;
    private Boolean searchDescription = true;
    private Boolean searchContent = false;

    // ????????
    private Boolean recordHistory = true;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getDifficultyList() {
        return difficultyList;
    }

    public void setDifficultyList(List<String> difficultyList) {
        this.difficultyList = difficultyList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Boolean getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(Boolean searchTitle) {
        this.searchTitle = searchTitle;
    }

    public Boolean getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(Boolean searchDescription) {
        this.searchDescription = searchDescription;
    }

    public Boolean getSearchContent() {
        return searchContent;
    }

    public void setSearchContent(Boolean searchContent) {
        this.searchContent = searchContent;
    }

    public Boolean getRecordHistory() {
        return recordHistory;
    }

    public void setRecordHistory(Boolean recordHistory) {
        this.recordHistory = recordHistory;
    }
}
