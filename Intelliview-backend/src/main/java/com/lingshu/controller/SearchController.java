package com.lingshu.controller;

import com.lingshu.dto.request.SearchRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.SearchResponse;
import com.lingshu.service.SearchService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;
    private final SecurityUtil securityUtil;

    /**
     * 搜索功能
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestBody SearchRequest request,
            HttpServletRequest httpRequest) {

        logSearchHistory(request, httpRequest);
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 快速搜索（GET方法）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponse>> quickSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest httpRequest) {

        SearchRequest request = new SearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);

        logSearchHistory(request, httpRequest);
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(
            @RequestParam String keyword) {

        List<String> suggestions = searchService.getSearchSuggestions(keyword);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    /**
     * 获取热门关键词
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<String>>> getPopularKeywords(
            @RequestParam(defaultValue = "10") Integer limit) {

        List<String> popularKeywords = searchService.getPopularKeywords(limit);
        return ResponseEntity.ok(ApiResponse.success(popularKeywords));
    }

    /**
     * 获取热门题目
     */
    @GetMapping("/hot-questions")
    public ResponseEntity<ApiResponse<SearchResponse>> getHotQuestions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        SearchResponse response = searchService.getHotQuestions(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 按标签搜索
     */
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<ApiResponse<SearchResponse>> searchByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        SearchResponse response = searchService.searchByTag(tagName, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 记录搜索历史
     */
    private void logSearchHistory(SearchRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = null;
            try {
                userId = securityUtil.getCurrentUserId();
            } catch (Exception e) {
                // 未登录用户userId为null
            }

            String keyword = request.getKeyword();
            String ipAddress = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");

            log.info("搜索记录: userId={}, keyword={}, ip={}", userId, keyword, ipAddress);

            // 保存搜索历史
            searchService.saveSearchHistory(userId, keyword, ipAddress, userAgent);

        } catch (Exception e) {
            log.error("记录搜索历史失败", e);
        }
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<SearchResponse.TagResponse>>> getAllTags() {
        List<SearchResponse.TagResponse> tags = searchService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 搜索标签
     */
    @GetMapping("/tags/search")
    public ResponseEntity<ApiResponse<List<SearchResponse.TagResponse>>> searchTags(
            @RequestParam(required = false) String keyword) {
        List<SearchResponse.TagResponse> tags = searchService.searchTags(keyword);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 获取搜索统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchStatistics() {
        Map<String, Object> statistics = searchService.getSearchStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * LeetCode高级搜索
     */
    @PostMapping("/advanced")
    public ResponseEntity<ApiResponse<SearchResponse>> advancedSearch(
            @RequestBody SearchRequest request,
            HttpServletRequest httpRequest) {

        logSearchHistory(request, httpRequest);
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据标签获取LeetCode题目
     */
    @GetMapping("/tag/{tagName}/questions")
    public ResponseEntity<ApiResponse<SearchResponse>> getQuestionsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        SearchResponse response = searchService.searchByTag(tagName, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取搜索历史
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<String>>> getSearchHistory(
            @RequestParam(defaultValue = "10") Integer limit) {

        Long userId = securityUtil.getCurrentUserId();
        List<String> history = searchService.getUserSearchHistory(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 清空搜索历史
     */
    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> clearSearchHistory() {

        Long userId = securityUtil.getCurrentUserId();
        searchService.clearSearchHistory(userId);
        return ResponseEntity.ok(ApiResponse.success("搜索历史已清空", null));
    }
}
