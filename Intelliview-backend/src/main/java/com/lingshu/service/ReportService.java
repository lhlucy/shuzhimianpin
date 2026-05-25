// service/ReportService.java
package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.request.ReportRequest;
import com.lingshu.dto.response.ReportResponse;
import com.lingshu.entity.Report;
import com.lingshu.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportMapper reportMapper;

    /**
     * 创建举报
     */
    @Transactional
    public ReportResponse createReport(Long userId, ReportRequest request) {
        // 创建举报
        Report report = new Report();
        report.setUserId(userId);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus("PENDING");

        reportMapper.insert(report);

        log.info("举报创建成功: userId={}, targetType={}, targetId={}",
                userId, request.getTargetType(), request.getTargetId());

        return convertToResponse(report);
    }

    /**
     * 管理员获取举报列表
     */
    public IPage<ReportResponse> getReports(String status, int page, int size) {
        IPage<Report> pageInfo = new Page<>(page, size);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status)
                .orderByDesc("created_at");

        IPage<Report> reports = reportMapper.selectPage(pageInfo, queryWrapper);

        return reports.convert(this::convertToResponse);
    }

    /**
     * 获取用户的举报列表
     */
    public IPage<ReportResponse> getUserReports(Long userId, int page, int size) {
        IPage<Report> pageInfo = new Page<>(page, size);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("created_at");

        IPage<Report> reports = reportMapper.selectPage(pageInfo, queryWrapper);

        return reports.convert(this::convertToResponse);
    }

    /**
     * 处理举报（接受）
     */
    @Transactional
    public ReportResponse processReport(Long reportId, Long adminId, String result) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }

        report.setStatus("RESOLVED");
        report.setAdminId(adminId);
        report.setProcessedAt(LocalDateTime.now());
        report.setResult(result);

        reportMapper.updateById(report);

        log.info("举报处理成功: adminId={}, reportId={}", adminId, reportId);

        return convertToResponse(report);
    }

    /**
     * 处理举报（拒绝）
     */
    @Transactional
    public ReportResponse rejectReport(Long reportId, Long adminId, String result) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }

        report.setStatus("REJECTED");
        report.setAdminId(adminId);
        report.setProcessedAt(LocalDateTime.now());
        report.setResult(result);

        reportMapper.updateById(report);

        log.info("举报拒绝成功: adminId={}, reportId={}", adminId, reportId);

        return convertToResponse(report);
    }

    /**
     * 获取目标的待处理举报数量
     */
    public Long getPendingReportCount(String targetType, Long targetId) {
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", targetType)
                .eq("target_id", targetId)
                .eq("status", "PENDING");

        return reportMapper.selectCount(queryWrapper);
    }

    /**
     * 获取目标的举报列表
     */
    public IPage<ReportResponse> getReportsByTarget(String targetType, Long targetId, int page, int size) {
        IPage<Report> pageInfo = new Page<>(page, size);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", targetType)
                .eq("target_id", targetId)
                .orderByDesc("created_at");

        IPage<Report> reports = reportMapper.selectPage(pageInfo, queryWrapper);

        return reports.convert(this::convertToResponse);
    }

    /**
     * 转换Report为ReportResponse
     */
    private ReportResponse convertToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .result(report.getResult())
                .createdAt(report.getCreatedAt())
                .processedAt(report.getProcessedAt())
                .userId(report.getUserId())
                .adminId(report.getAdminId())
                .build();
    }

    /**
     * 获取举报详情
     */
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }
        return convertToResponse(report);
    }
}
