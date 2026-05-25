// util/SecurityUtil.java
package com.lingshu.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lingshu.entity.User;
import com.lingshu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final UserMapper userMapper;

    /**
     * 获取当前用户
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return Optional.of((User) principal);
        } else if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return Optional.ofNullable(userMapper.selectOne(new QueryWrapper<User>().eq("username", username)));
        } else if (principal instanceof String) {
            String username = (String) principal;
            return Optional.ofNullable(userMapper.selectOne(new QueryWrapper<User>().eq("username", username)));
        }

        return Optional.empty();
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("用户未登录或不存在"));
    }

    /**
     * 获取当前用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getUsername();
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        throw new RuntimeException("无法获取用户名");
    }

    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.getRole().equals(role))
                .orElse(false);
    }

    /**
     * 检查是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * 检查是否为教师
     */
    public boolean isTeacher() {
        return hasRole("ROLE_TEACHER");
    }

    /**
     * 检查是否为普通用户
     */
    public boolean isUser() {
        return hasRole("ROLE_USER");
    }
}
