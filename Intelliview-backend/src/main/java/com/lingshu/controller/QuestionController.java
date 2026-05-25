// controller/QuestionController.java
package com.lingshu.controller;

import com.lingshu.dto.request.QuestionFilterRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.QuestionDetailResponse;
import com.lingshu.dto.response.QuestionResponse;
import com.lingshu.service.QuestionService;
import com.lingshu.service.FavoriteService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionService questionService;
    private final FavoriteService favoriteService;
    private final SecurityUtil securityUtil;

    /**
     * 获取题目列表（POST请求）
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getQuestions(
            @Valid @RequestBody
            QuestionFilterRequest request) {

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getQuestions(request);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 获取题目列表（GET请求）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getQuestionsByGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long primaryJobRoleId,
            @RequestParam(required = false) String primaryJobRoleCode,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Boolean isForInterview,
            @RequestParam(required = false) Boolean isForPractice,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        QuestionFilterRequest request = new QuestionFilterRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setDifficulty(difficulty);
        request.setPrimaryJobRoleId(primaryJobRoleId);
        request.setPrimaryJobRoleCode(primaryJobRoleCode);
        request.setQuestionType(questionType);
        request.setIsForInterview(isForInterview);
        request.setIsForPractice(isForPractice);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getQuestions(request);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 快速获取题目列表（GET请求）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> quickGetQuestions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long primaryJobRoleId,
            @RequestParam(required = false) String primaryJobRoleCode,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Boolean isForInterview,
            @RequestParam(required = false) Boolean isForPractice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        QuestionFilterRequest request = new QuestionFilterRequest();
        request.setPage(page);
        request.setSize(size);
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setDifficulty(difficulty);
        request.setPrimaryJobRoleId(primaryJobRoleId);
        request.setPrimaryJobRoleCode(primaryJobRoleCode);
        request.setQuestionType(questionType);
        request.setIsForInterview(isForInterview);
        request.setIsForPractice(isForPractice);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getQuestions(request);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionDetail(
            @PathVariable Long id) {

        QuestionDetailResponse question = questionService.getQuestionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    /**
     * 通过slug获取题目详情
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionBySlug(
            @PathVariable String slug) {

        QuestionDetailResponse question = questionService.getQuestionBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    /**
     * 获取相关题目
     */
    @GetMapping("/detail/{id}/related")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getRelatedQuestions(
            @PathVariable Long id) {

        List<QuestionResponse> relatedQuestions = questionService.getRelatedQuestions(id);
        return ResponseEntity.ok(ApiResponse.success(relatedQuestions));
    }

    /**
     * 获取推荐题目
     */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getRecommendedQuestions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getRecommendedQuestions(page, size);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 获取热门题目
     */
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getHotQuestions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getHotQuestions(page, size);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 按分类获取题目
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getQuestionsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        QuestionFilterRequest request = new QuestionFilterRequest();
        request.setPage(page);
        request.setSize(size);
        request.setCategoryId(categoryId);

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getQuestions(request);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 按难度获取题目
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getQuestionsByDifficulty(
            @PathVariable String difficulty,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        QuestionFilterRequest request = new QuestionFilterRequest();
        request.setPage(page);
        request.setSize(size);
        request.setDifficulty(difficulty);

        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> questions = questionService.getQuestions(request);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 增加题目分享数
     */
    @PostMapping("/detail/{id}/share")
    public ResponseEntity<ApiResponse<Void>> incrementShareCount(@PathVariable Long id) {
        questionService.incrementShareCount(id);
        return ResponseEntity.ok(ApiResponse.success("分享成功", null));
    }

    /**
     * 增加题目点赞数
     */
    @PostMapping("/detail/{id}/like")
    public ResponseEntity<ApiResponse<Void>> incrementLikeCount(@PathVariable Long id) {
        questionService.incrementLikeCount(id);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    /**
     * 减少题目点赞数
     */
    @PostMapping("/detail/{id}/unlike")
    public ResponseEntity<ApiResponse<Void>> decrementLikeCount(@PathVariable Long id) {
        questionService.decrementLikeCount(id);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    /**
     * 增加题目浏览量
     */
    @PostMapping("/detail/{id}/view")
    public ResponseEntity<ApiResponse<Void>> incrementViewCount(@PathVariable Long id) {
        questionService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success("浏览量增加成功", null));
    }

    /**
     * 获取题目统计信息
     */
    @GetMapping("/detail/{id}/stats")
    public ResponseEntity<ApiResponse<QuestionDetailResponse.Stats>> getQuestionStats(
            @PathVariable Long id) {

        QuestionDetailResponse question = questionService.getQuestionDetail(id);

        // 构建统计信息
        QuestionDetailResponse.Stats stats = new QuestionDetailResponse.Stats();
        stats.setMarkCount(question.getMarkCount());
        stats.setBrowseCount(question.getBrowseCount());
        stats.setViewCount(question.getViewCount());
        stats.setShareCount(question.getShareCount());
        stats.setSubmitCount(question.getSubmitCount());
        stats.setAcceptCount(question.getAcceptCount());

        // 处理 BigDecimal 类型
        BigDecimal acceptRate = question.getAcceptRate();
        if (acceptRate != null) {
            stats.setAcceptRate(acceptRate);
        } else {
            stats.setAcceptRate(BigDecimal.ZERO);
        }

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取难度统计信息
     */
    @GetMapping("/stats/difficulty")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDifficultyStatistics() {
        Map<String, Long> stats = questionService.getDifficultyStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取用户收藏的题目（调用FavoriteService）
     */
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse>>> getUserFavorites(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = securityUtil.getCurrentUserId();
        com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> favorites = favoriteService.getUserFavorites(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }
}
