package com.lingshu.controller.admin;

import com.lingshu.dto.request.UpdateUserRoleRequest;
import com.lingshu.entity.User;
import com.lingshu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lingshu.dto.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserManagementController {

    private final UserService userService;

    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @RequestBody UpdateUserRoleRequest request) {

        userService.updateUserRole(request.getUsername(), request.getRole());
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", null));
    }
    /**
     * 根据用户名搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(
            @RequestParam String username) {
        List<User> users = userService.searchUsersByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
