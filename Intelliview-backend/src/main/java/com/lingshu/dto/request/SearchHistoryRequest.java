package com.lingshu.dto.request;

import lombok.Data;

@Data
public class SearchHistoryRequest {
    private String keyword;
    private String searchType;
    private String searchFilters; // JSON格式的搜索过滤条件
}
