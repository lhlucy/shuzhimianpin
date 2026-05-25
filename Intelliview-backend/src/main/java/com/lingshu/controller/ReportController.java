// controller/ReportController.java
package com.lingshu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingshu.dto.request.ReportRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.ReportResponse;
import com.lingshu.service.ReportService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtil securityUtil;

    /**
     * 创建举报
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody ReportRequest request) {

        Long userId = securityUtil.getCurrentUserId();
        ReportResponse report = reportService.createReport(userId, request);

        return ResponseEntity.ok(ApiResponse.success("举报成功", report));
    }

    /**
     * 获取我的举报列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<IPage<ReportResponse>>> getMyReports(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = securityUtil.getCurrentUserId();
        IPage<ReportResponse> reports = reportService.getUserReports(userId, page, size);

        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable Long reportId) {
        // 实现举报详情获取逻辑 getReportById
        ReportResponse report = reportService.getReportById(reportId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * 获取举报列表（管理员）
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<IPage<ReportResponse>>> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        IPage<ReportResponse> reports = reportService.getReports(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    /**
     * 处理举报（管理员）
     */
    @PostMapping("/admin/{reportId}/process")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> processReport(
            @PathVariable Long reportId,
            @RequestParam String result) {

        Long adminId = securityUtil.getCurrentUserId();
        ReportResponse report = reportService.processReport(reportId, adminId, result);

        return ResponseEntity.ok(ApiResponse.success("举报处理成功", report));
    }

    /**
     * 拒绝举报（管理员）
     */
    @PostMapping("/admin/{reportId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> rejectReport(
            @PathVariable Long reportId,
            @RequestParam String result) {

        Long adminId = securityUtil.getCurrentUserId();
        ReportResponse report = reportService.rejectReport(reportId, adminId, result);

        return ResponseEntity.ok(ApiResponse.success("举报已拒绝", report));
    }

    /**
     * 根据目标获取举报列表
     */
    @GetMapping("/target")
    public ResponseEntity<ApiResponse<IPage<ReportResponse>>> getReportsByTarget(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        IPage<ReportResponse> reports = reportService.getReportsByTarget(targetType, targetId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
}
