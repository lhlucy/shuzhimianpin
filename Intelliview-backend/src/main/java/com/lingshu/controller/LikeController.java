// controller/LikeController.java
package com.lingshu.controller;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.service.LikeService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Slf4j
public class LikeController {

    private final LikeService likeService;
    private final SecurityUtil securityUtil;

    /**
     * 点赞回答
     */
    @PostMapping("/answer/{answerId}")
    public ResponseEntity<ApiResponse<Void>> likeAnswer(@PathVariable Long answerId) {
        Long userId = securityUtil.getCurrentUserId();
        likeService.likeAnswer(userId, answerId);

        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    /**
     * 取消点赞回答
     */
    @DeleteMapping("/answer/{answerId}")
    public ResponseEntity<ApiResponse<Void>> unlikeAnswer(@PathVariable Long answerId) {
        Long userId = securityUtil.getCurrentUserId();
        likeService.unlikeAnswer(userId, answerId);

        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    /**
     * 点赞评论
     */
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ApiResponse<Void>> likeComment(@PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        likeService.likeComment(userId, commentId);

        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(@PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        likeService.unlikeComment(userId, commentId);

        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    /**
     * 点赞问题
     */
    @PostMapping("/question/{questionId}")
    public ResponseEntity<ApiResponse<Void>> likeQuestion(@PathVariable Long questionId) {
        Long userId = securityUtil.getCurrentUserId();
        likeService.likeQuestion(userId, questionId);

        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    /**
     * 取消点赞问题
     */
    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<ApiResponse<Void>> unlikeQuestion(@PathVariable Long questionId) {
        Long userId = securityUtil.getCurrentUserId();
        likeService.unlikeQuestion(userId, questionId);

        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    /**
     * 检查是否点赞
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkLike(
            @RequestParam String targetType,
            @RequestParam Long targetId) {

        Long userId = securityUtil.getCurrentUserId();
        boolean isLiked = likeService.isLiked(userId, targetType, targetId);

        return ResponseEntity.ok(ApiResponse.success(isLiked));
    }

    /**
     * 获取点赞数
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getLikeCount(
            @RequestParam String targetType,
            @RequestParam Long targetId) {

        Long count = likeService.getLikeCount(targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
