package com.lingshu.controller;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.entity.Paper;
import com.lingshu.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * 试卷管理Controller
 */
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {
    
    private final PaperService paperService;
    
    /**
     * 获取试卷列表（分页）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getPapers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        var result = paperService.getPapers(page, size, sortBy, sortDirection);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 根据状态获取试卷列表
     */
    @GetMapping("/list-by-status")
    public ResponseEntity<ApiResponse<?>> getPapersByStatus(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Boolean status) {
        
        var result = paperService.getPapersByStatus(page, size, status);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 获取试卷详情
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<?>> getPaperDetail(
            @PathVariable Long id) {
        
        var result = paperService.getPaperDetail(id);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 创建试卷
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createPaper(
            @RequestBody Paper paper) {
        
        var result = paperService.createPaper(paper);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 更新试卷
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<?>> updatePaper(
            @PathVariable Long id,
            @RequestBody Paper paper) {
        
        var result = paperService.updatePaper(id, paper);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 删除试卷
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> deletePaper(
            @PathVariable Long id) {
        
        var result = paperService.deletePaper(id);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 切换试卷状态
     */
    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<ApiResponse<?>> togglePaperStatus(
            @PathVariable Long id) {
        
        var result = paperService.togglePaperStatus(id);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 获取试卷题目列表
     */
    @GetMapping("/{paperId}/questions")
    public ResponseEntity<ApiResponse<?>> getPaperQuestions(
            @PathVariable Long paperId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        var result = paperService.getPaperQuestions(paperId, page, size);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 向试卷添加题目
     */
    @PostMapping("/{paperId}/questions")
    public ResponseEntity<ApiResponse<?>> addQuestionToPaper(
            @PathVariable Long paperId,
            @RequestParam Long questionId,
            @RequestParam(defaultValue = "0") Integer sortOrder) {
        
        var result = paperService.addQuestionToPaper(paperId, questionId, sortOrder);
        return ok(ApiResponse.success(result));
    }
    
    /**
     * 从试卷移除题目
     */
    @DeleteMapping("/{paperId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<?>> removeQuestionFromPaper(
            @PathVariable Long paperId,
            @PathVariable Long questionId) {
        
        var result = paperService.removeQuestionFromPaper(paperId, questionId);
        return ok(ApiResponse.success(result));
    }
}
