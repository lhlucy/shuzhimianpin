package com.lingshu.service;

import com.lingshu.dto.request.UserProfileRequest;
import com.lingshu.dto.response.LoginResponse;
import com.lingshu.dto.response.UserProfileResponse;
import com.lingshu.entity.User;
import com.lingshu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserPracticeHistoryService userPracticeHistoryService;

    /**
     * 根据用户名获取用户信息
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        return convertToProfileResponse(user);
    }

    /**
     * 更新用户信息
     */
    public UserProfileResponse updateUserProfile(String username, UserProfileRequest profile) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户信息
        if (profile.getNickname() != null) {
            user.setNickname(profile.getNickname());
        }

        if (profile.getAvatar() != null) {
            user.setAvatar(profile.getAvatar());
        }

        if (profile.getEmail() != null) {
            user.setEmail(profile.getEmail());
        }

        if (profile.getPhone() != null) {
            user.setPhone(profile.getPhone());
        }

        if (profile.getBio() != null) {
            user.setBio(profile.getBio());
        }

        if (profile.getTargetJobRoleId() != null) {
            user.setTargetJobRoleId(profile.getTargetJobRoleId());
        }

        if (profile.getExperienceLevel() != null) {
            user.setExperienceLevel(profile.getExperienceLevel());
        }

        if (profile.getPreferredCompanyType() != null) {
            user.setPreferredCompanyType(profile.getPreferredCompanyType());
        }

        if (profile.getTargetCity() != null) {
            user.setTargetCity(profile.getTargetCity());
        }

        if (profile.getExpectedSalary() != null) {
            user.setExpectedSalary(profile.getExpectedSalary());
        }

        if (profile.getResumeSummary() != null) {
            user.setResumeSummary(profile.getResumeSummary());
        }

        userMapper.updateById(user);
        return convertToProfileResponse(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (user.getPassword() == null ||
                !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private UserProfileResponse convertToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .bio(user.getBio())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .githubLogin(user.getGithubLogin())
                .githubAvatar(user.getGithubAvatar())
                .githubName(user.getGithubName())
                .githubBio(user.getGithubBio())
                .githubCompany(user.getGithubCompany())
                .githubBlog(user.getGithubBlog())
                .githubLocation(user.getGithubLocation())
                .lastLoginTime(user.getLastLoginTime())
                .loginCount(user.getLoginCount())
                .points(user.getPoints())
                .level(user.getLevel())
                .experience(user.getExperience())
                .targetJobRoleId(user.getTargetJobRoleId())
                .experienceLevel(user.getExperienceLevel())
                .preferredCompanyType(user.getPreferredCompanyType())
                .targetCity(user.getTargetCity())
                .expectedSalary(user.getExpectedSalary())
                .resumeSummary(user.getResumeSummary())
                .latestOverallScore(user.getLatestOverallScore())
                .latestJobMatchScore(user.getLatestJobMatchScore())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 获取用户邮箱
     */
    public String getUserEmail(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getEmail();
    }

    /**
     * 将User转换为LoginResponse.UserResponse
     */
    public static LoginResponse.UserResponse convertToLoginUserResponse(User user) {
        return LoginResponse.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .githubLogin(user.getGithubLogin())
                .githubAvatar(user.getGithubAvatar())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 删除账号
     */
    public void deleteAccount(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 禁用账号（软删除）
        user.setEnabled(false);
        userMapper.updateById(user);

        log.info("账号删除: {}", username);
    }

    /**
     * 重置密码
     */
    public void resetPassword(String email, String newPassword) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("密码重置: {}", email);
    }

    /**
     * 更新用户角色
     */
    public void updateUserRole(String username, String newRole) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 不允许修改自己的角色
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("不能修改自己的角色");
        }

        user.setRole(newRole);
        userMapper.updateById(user);

        log.info("角色更新: {} -> {}", username, newRole);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 根据用户名搜索用户
     */
    public List<User> searchUsersByUsername(String username) {
        return userMapper.findByUsernameContainingIgnoreCase(username);
    }

    /**
     * 获取用户排行榜
     */
    public List<Map<String, Object>> getUserRankings(String type, int page, int size) {
        // 获取所有用户
        List<User> users = userMapper.selectList(null);
        
        // 构建排行榜数据
        List<Map<String, Object>> rankings = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> ranking = new HashMap<>();
            ranking.put("id", user.getId());
            ranking.put("name", user.getNickname() != null ? user.getNickname() : user.getUsername());
            ranking.put("avatar", user.getAvatar() != null ? user.getAvatar() : "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            ranking.put("score", user.getPoints());
            
            // 计算用户的刷题数据
            Map<String, Object> practiceStats = userPracticeHistoryService.getUserPracticeStats(user.getId());
            ranking.put("questionCount", practiceStats.get("totalCount"));
            ranking.put("completedCount", practiceStats.get("completedCount"));
            ranking.put("totalDuration", practiceStats.get("totalDuration"));
            
            rankings.add(ranking);
        }
        
        // 根据类型排序
        switch (type) {
            case "points":
                rankings.sort((a, b) -> ((Integer) b.get("score")).compareTo((Integer) a.get("score")));
                break;
            case "completed":
                rankings.sort((a, b) -> {
                    Integer completedA = (Integer) a.get("completedCount");
                    Integer completedB = (Integer) b.get("completedCount");
                    return completedB.compareTo(completedA);
                });
                break;
            case "duration":
                rankings.sort((a, b) -> {
                    Integer durationA = (Integer) a.get("totalDuration");
                    Integer durationB = (Integer) b.get("totalDuration");
                    return durationB.compareTo(durationA);
                });
                break;
            default:
                rankings.sort((a, b) -> ((Integer) b.get("score")).compareTo((Integer) a.get("score")));
                break;
        }
        
        // 分页
        int start = page * size;
        int end = Math.min(start + size, rankings.size());
        if (start >= rankings.size()) {
            return Collections.emptyList();
        }
        return rankings.subList(start, end);
    }
}
