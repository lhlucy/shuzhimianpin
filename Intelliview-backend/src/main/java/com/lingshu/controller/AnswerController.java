// controller/AnswerController.java
package com.lingshu.controller;

import com.lingshu.dto.request.AnswerRequest;
import com.lingshu.dto.response.AnswerResponse;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.service.AnswerService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
@Slf4j
public class AnswerController {

    private final AnswerService answerService;
    private final SecurityUtil securityUtil;

    /**
     * 创建回答
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AnswerResponse>> createAnswer(
            @RequestParam Long questionId,
            @Valid @RequestBody AnswerRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        AnswerResponse answer = answerService.createAnswer(userId, questionId, request);

        return ResponseEntity.ok(ApiResponse.success("回答创建成功", answer));
    }

    /**
     * 根据问题获取回答列表
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<AnswerResponse>>> getAnswersByQuestion(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        com.baomidou.mybatisplus.core.metadata.IPage<AnswerResponse> answers = answerService.getAnswersByQuestion(questionId, page, size);
        return ResponseEntity.ok(ApiResponse.success(answers));
    }

    /**
     * 根据用户获取回答列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<AnswerResponse>>> getAnswersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        com.baomidou.mybatisplus.core.metadata.IPage<AnswerResponse> answers = answerService.getAnswersByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(answers));
    }

    /**
     * 获取回答详情
     */
    @GetMapping("/{answerId}")
    public ResponseEntity<ApiResponse<AnswerResponse>> getAnswerById(@PathVariable Long answerId) {
        AnswerResponse answer = answerService.getAnswerById(answerId);
        return ResponseEntity.ok(ApiResponse.success(answer));
    }

    /**
     * 更新回答
     */
    @PutMapping("/{answerId}")
    public ResponseEntity<ApiResponse<AnswerResponse>> updateAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        AnswerResponse answer = answerService.updateAnswer(userId, answerId, request);

        return ResponseEntity.ok(ApiResponse.success("回答更新成功", answer));
    }

    /**
     * 删除回答
     */
    @DeleteMapping("/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(@PathVariable Long answerId) {
        Long userId = securityUtil.getCurrentUserId();
        answerService.deleteAnswer(userId, answerId);

        return ResponseEntity.ok(ApiResponse.success("回答删除成功", null));
    }

    /**
     * 获取问题的最佳回答
     */
    @GetMapping("/question/{questionId}/top")
    public ResponseEntity<ApiResponse<Object>> getTopAnswers(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "5") Integer limit) {

        Object topAnswers = answerService.getTopAnswersByQuestion(questionId, limit);
        return ResponseEntity.ok(ApiResponse.success(topAnswers));
    }
}
