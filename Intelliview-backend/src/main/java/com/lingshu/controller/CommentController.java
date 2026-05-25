// controller/CommentController.java
package com.lingshu.controller;

import com.lingshu.dto.request.CommentRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.CommentResponse;
import com.lingshu.service.CommentService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtil securityUtil;

    /**
     * 创建评论
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @RequestParam Long answerId,
            @Valid @RequestBody CommentRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        CommentResponse comment = commentService.createComment(userId, answerId, request);

        return ResponseEntity.ok(ApiResponse.success("评论创建成功", comment));
    }

    /**
     * 根据回答获取评论列表
     */
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<CommentResponse>>> getCommentsByAnswer(
            @PathVariable Long answerId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        com.baomidou.mybatisplus.core.metadata.IPage<CommentResponse> comments = commentService.getCommentsByAnswer(answerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable Long commentId) {
        CommentResponse comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    /**
     * 更新评论
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        CommentResponse comment = commentService.updateComment(userId, commentId, request);

        return ResponseEntity.ok(ApiResponse.success("评论更新成功", comment));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        commentService.deleteComment(userId, commentId);

        return ResponseEntity.ok(ApiResponse.success("评论删除成功", null));
    }

    /**
     * 获取评论的回复列表
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<Object>> getRepliesByComment(@PathVariable Long commentId) {
        Object replies = commentService.getRepliesByComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(replies));
    }
}
