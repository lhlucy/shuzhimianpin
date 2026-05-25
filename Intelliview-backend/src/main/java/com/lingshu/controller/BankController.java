package com.lingshu.controller;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    /**
     * 获取题库列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getBanks(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        var result = bankService.getBanks(page, size, sortBy, sortDirection);
        return ok(ApiResponse.success(result));
    }

    /**
     * 创建题库
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createBank(@RequestBody com.lingshu.entity.QuestionBank bank) {
        var result = bankService.createBank(bank);
        return ok(ApiResponse.success(result));
    }

    /**
     * 更新题库
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<?>> updateBank(
            @PathVariable Long id,
            @RequestBody com.lingshu.entity.QuestionBank bank) {
        var result = bankService.updateBank(id, bank);
        return ok(ApiResponse.success(result));
    }

    /**
     * 删除题库
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBank(@PathVariable Long id) {
        var result = bankService.deleteBank(id);
        return ok(ApiResponse.success(result));
    }

    /**
     * 获取题库详情
     */
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<?>> getBankDetail(
            @RequestParam Long id) {

        var result = bankService.getBankDetail(id);
        return ok(ApiResponse.success(result));
    }

    /**
     * 获取题库的题目列表
     */
    @GetMapping("/{bankId}/questions")
    public ResponseEntity<ApiResponse<?>> getBankQuestions(
            @PathVariable Long bankId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        var result = bankService.getBankQuestions(bankId, page, size);
        return ok(ApiResponse.success(result));
    }

    /**
     * 将题目归入题库关联分类
     */
    @PostMapping("/{bankId}/questions")
    public ResponseEntity<ApiResponse<?>> addQuestionToBank(
            @PathVariable Long bankId,
            @RequestParam Long questionId) {

        var result = bankService.addQuestionToBank(bankId, questionId);
        return ok(ApiResponse.success(result));
    }

    /**
     * 根据标签筛选题库
     */
    @GetMapping("/by-tag")
    public ResponseEntity<ApiResponse<?>> getBanksByTag(
            @RequestParam Long tagId) {

        var result = bankService.getBanksByTag(tagId);
        return ok(ApiResponse.success(result));
    }
}
