package com.lingshu.controller;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.JobRoleConfigResponse;
import com.lingshu.dto.response.JobRoleOptionResponse;
import com.lingshu.service.JobRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-roles")
@RequiredArgsConstructor
public class JobRoleController {

    private final JobRoleService jobRoleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobRoleOptionResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.success(jobRoleService.listActiveRoles()));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<JobRoleConfigResponse>> getRoleConfig(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(jobRoleService.getRoleConfig(code)));
    }
}
