// controller/UserController.java
package com.lingshu.controller;

import com.lingshu.dto.request.DeleteAccountRequest;
import com.lingshu.dto.request.UserProfileRequest;
import com.lingshu.dto.response.ApiResponse;
import com.lingshu.dto.response.UserProfileResponse;
import com.lingshu.entity.VerificationCode;
import com.lingshu.service.EmailService;
import com.lingshu.service.UserService;
import com.lingshu.service.MessageService;
import com.lingshu.service.UploadStorageService;

import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final MessageService messageService;
    private final SecurityUtil securityUtil;
    private final UploadStorageService uploadStorageService;

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        String username = securityUtil.getCurrentUsername();
        UserProfileResponse profile = userService.getUserProfile(username);

        return ok(ApiResponse.success(profile));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestBody UserProfileRequest profile) {

        String username = securityUtil.getCurrentUsername();
        UserProfileResponse updated = userService.updateUserProfile(username, profile);

        return ok(ApiResponse.success("更新成功", updated));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = securityUtil.getCurrentUserId();
        UploadStorageService.StoredFile stored = uploadStorageService.storeAvatar(userId, file);
        UserProfileRequest profile = new UserProfileRequest();
        profile.setAvatar(stored.url());
        UserProfileResponse updated = userService.updateUserProfile(securityUtil.getCurrentUsername(), profile);
        return ok(ApiResponse.success("头像上传成功", updated));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        String username = securityUtil.getCurrentUsername();
        userService.changePassword(username, oldPassword, newPassword);

        return ok(ApiResponse.success("密码修改成功", null));
    }

    /**
     * 发送删除账号验证码
     */
    @PostMapping("/send-delete-code")
    public ResponseEntity<ApiResponse<Void>> sendDeleteCode(HttpServletRequest request) {
        String username = securityUtil.getCurrentUsername();
        String email = userService.getUserEmail(username);
        emailService.sendVerificationCode(email, VerificationCode.CodeType.ACCOUNT_DELETE, request);

        ApiResponse<Void> response = ApiResponse.success("验证码已发送到您的邮箱", null);
        return ok(response);
    }

    /**
     * 删除账号
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request) {

        String username = securityUtil.getCurrentUsername();
        String email = userService.getUserEmail(username);

        // 验证验证码
        boolean valid = emailService.validateCode(email, request.getCode(), VerificationCode.CodeType.ACCOUNT_DELETE);
        if (!valid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("验证码错误"));
        }

        // 删除账号
        userService.deleteAccount(username);

        ApiResponse<Void> response = ApiResponse.success("账号删除成功", null);
        return ok(response);
    }

    /**
     * 获取消息列表
     */
    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<?>> getMessages(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = securityUtil.getCurrentUserId();
        var messages = messageService.getUserMessages(userId, page, size);
        return ok(ApiResponse.success(messages));
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/messages/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount() {

        Long userId = securityUtil.getCurrentUserId();
        Long count = messageService.getUserUnreadMessageCount(userId);
        return ok(ApiResponse.success(count));
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable Long messageId) {

        messageService.markMessageAsRead(messageId);
        return ok(ApiResponse.success("消息已标记为已读", null));
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/messages/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllMessagesAsRead() {

        Long userId = securityUtil.getCurrentUserId();
        messageService.markAllMessagesAsRead(userId);
        return ok(ApiResponse.success("所有消息已标记为已读", null));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {

        messageService.deleteMessage(messageId);
        return ok(ApiResponse.success("消息删除成功", null));
    }

    /**
     * 清空消息
     */
    @DeleteMapping("/messages")
    public ResponseEntity<ApiResponse<Void>> clearMessages() {

        Long userId = securityUtil.getCurrentUserId();
        messageService.clearUserMessages(userId);
        return ok(ApiResponse.success("消息清空成功", null));
    }

    /**
     * 获取排行榜
     */
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<?>> getRankings(
            @RequestParam(defaultValue = "points") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        List<Map<String, Object>> rankings = userService.getUserRankings(type, page, size);
        return ok(ApiResponse.success(rankings));
    }


}
