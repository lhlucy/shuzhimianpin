package com.lingshu.controller.admin;

import com.lingshu.dto.response.AdminInterviewRecordResponse;
import com.lingshu.dto.response.AIInterviewSummaryResponse;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.service.admin.AdminInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/admin/interviews")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminInterviewController {

    private final AdminInterviewService adminInterviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminInterviewRecordResponse>>> listInterviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long jobRoleId) {
        return ResponseEntity.ok(ApiResponse.success(adminInterviewService.listInterviews(keyword, status, jobRoleId)));
    }

    @GetMapping("/{interviewId}/summary")
    public ResponseEntity<ApiResponse<AIInterviewSummaryResponse>> getInterviewSummary(@PathVariable Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(adminInterviewService.getInterviewSummary(interviewId)));
    }
}
