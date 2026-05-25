package com.lingshu.controller.admin;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.service.admin.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * 管理端仪表盘控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取系统概览统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getSystemStats() {
        var result = dashboardService.getSystemStats();
        return ok(ApiResponse.success(result));
    }

    /**
     * 获取最近活动日志
     */
    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<?>> getRecentActivities() {
        var result = dashboardService.getRecentActivities();
        return ok(ApiResponse.success(result));
    }

    /**
     * 获取题目难度分布
     */
    @GetMapping("/stats/questions/difficulty")
    public ResponseEntity<ApiResponse<?>> getQuestionDifficultyDistribution() {
        var result = dashboardService.getQuestionDifficultyDistribution();
        return ok(ApiResponse.success(result));
    }

    /**
     * 获取题目类型分布
     */
    @GetMapping("/stats/questions/type")
    public ResponseEntity<ApiResponse<?>> getQuestionTypeDistribution() {
        var result = dashboardService.getQuestionTypeDistribution();
        return ok(ApiResponse.success(result));
    }

    /**
     * 获取 AI 面试高频短板统计
     */
    @GetMapping("/weakness-tags")
    public ResponseEntity<ApiResponse<?>> getWeaknessTags() {
        var result = dashboardService.getWeaknessTags();
        return ok(ApiResponse.success(result));
    }

}
