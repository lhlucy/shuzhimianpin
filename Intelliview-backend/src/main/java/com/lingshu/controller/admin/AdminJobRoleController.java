package com.lingshu.controller.admin;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.entity.JobRole;
import com.lingshu.service.admin.AdminJobRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/job-roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminJobRoleController {

    private final AdminJobRoleService adminJobRoleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobRole>>> listRoles(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(adminJobRoleService.listRoles(activeOnly, keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobRole>> createRole(@RequestBody JobRole request) {
        return ResponseEntity.ok(ApiResponse.success("岗位创建成功", adminJobRoleService.createRole(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobRole>> updateRole(@PathVariable Long id, @RequestBody JobRole request) {
        return ResponseEntity.ok(ApiResponse.success("岗位更新成功", adminJobRoleService.updateRole(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deactivateRole(@PathVariable Long id) {
        long usageCount = adminJobRoleService.countUsage(id);
        adminJobRoleService.deactivateRole(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("archived", true);
        data.put("usageCount", usageCount);
        return ResponseEntity.ok(ApiResponse.success("岗位已停用", data));
    }
}
