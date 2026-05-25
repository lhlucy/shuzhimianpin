package com.lingshu.controller;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.UserResumeResponse;
import com.lingshu.service.UserResumeService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user/resumes")
@RequiredArgsConstructor
public class UserResumeController {

    private final UserResumeService userResumeService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResumeResponse>>> listResumes() {
        return ResponseEntity.ok(ApiResponse.success(userResumeService.listResumes(securityUtil.getCurrentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResumeResponse>> uploadResume(@RequestParam("file") MultipartFile file) {
        UserResumeResponse resume = userResumeService.uploadResume(securityUtil.getCurrentUserId(), file);
        return ResponseEntity.ok(ApiResponse.success("简历上传成功", resume));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable Long resumeId) {
        userResumeService.deleteResume(securityUtil.getCurrentUserId(), resumeId);
        return ResponseEntity.ok(ApiResponse.success("简历已删除", null));
    }
}
