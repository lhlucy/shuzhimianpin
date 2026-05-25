package com.lingshu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lingshu.config.GithubConfig;
import com.lingshu.dto.response.LoginResponse;
import com.lingshu.entity.User;
import com.lingshu.mapper.UserMapper;
import com.lingshu.util.IpUtil;
import com.lingshu.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubAuthService {

    private static final Logger log = LoggerFactory.getLogger(GithubAuthService.class);
    private final GithubConfig githubConfig;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取GitHub授权URL
     */
    public String getAuthorizationUrl(String state) {
        return githubConfig.buildAuthorizeUrl(state);
    }

    /**
     * 处理GitHub回调
     */
    @Transactional
    public LoginResponse handleCallback(String code, HttpServletRequest request) {
        // 1. 获取access_token
        String accessToken = getAccessToken(code);

        // 2. 获取用户信息
        Map<String, Object> userInfo = getUserInfo(accessToken);

        // 3. 获取用户邮箱
        String email = getUserEmail(accessToken);

        // 4. 查找或创建用户
        User user = findOrCreateUser(userInfo, email);

        // 5. 更新登录信息
        updateLoginInfo(user, request);

        // 6. 生成JWT token
        String token = jwtService.generateToken(user.getUsername());

        // 7. 构建响应
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24小时
                .user(UserService.convertToLoginUserResponse(user))
                .build();
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", githubConfig.getClientId());
        params.add("client_secret", githubConfig.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", githubConfig.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GithubConfig.ACCESS_TOKEN_URL, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            log.error("获取GitHub access_token失败", e);
            throw new RuntimeException("GitHub授权失败");
        }
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GithubConfig.USER_API_URL, HttpMethod.GET, request, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            log.error("获取GitHub用户信息失败", e);
            throw new RuntimeException("获取用户信息失败");
        }
    }

    private String getUserEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GithubConfig.USER_EMAILS_URL, HttpMethod.GET, request, String.class);

            JsonNode emails = objectMapper.readTree(response.getBody());

            // 查找主邮箱
            for (JsonNode emailNode : emails) {
                if (emailNode.get("primary").asBoolean()) {
                    return emailNode.get("email").asText();
                }
            }

            // 如果没有主邮箱，返回第一个邮箱
            if (emails.isArray() && emails.size() > 0) {
                return emails.get(0).get("email").asText();
            }

            return null;

        } catch (Exception e) {
            log.warn("获取GitHub邮箱失败", e);
            return null;
        }
    }

    private User findOrCreateUser(Map<String, Object> userInfo, String email) {
        String githubId = userInfo.get("id").toString();
        String login = (String) userInfo.get("login");
        String name = (String) userInfo.get("name");
        String avatarUrl = (String) userInfo.get("avatar_url");
        String bio = (String) userInfo.get("bio");
        String company = (String) userInfo.get("company");
        String blog = (String) userInfo.get("blog");
        String location = (String) userInfo.get("location");

        // 查找现有用户
        User existingUser = userMapper.findByGithubId(githubId);

        if (existingUser != null) {
            return existingUser;
        }

        // 创建新用户
        User user = new User();
        user.setGithubId(githubId);
        user.setGithubLogin(login);
        user.setGithubName(name);
        user.setGithubAvatar(avatarUrl);
        user.setGithubBio(bio);
        user.setGithubCompany(company);
        user.setGithubBlog(blog);
        user.setGithubLocation(location);

        // 设置用户名（如果GitHub登录名未被使用）
        if (!userMapper.existsByUsername(login)) {
            user.setUsername(login);
        } else {
            user.setUsername(RandomUtil.generateUsername());
        }

        // 设置昵称
        if (name != null && !name.isEmpty()) {
            user.setNickname(name);
        } else {
            user.setNickname(login);
        }

        // 设置头像
        user.setAvatar(avatarUrl);

        // 设置邮箱
        if (email != null && !email.isEmpty() && !userMapper.existsByEmail(email)) {
            user.setEmail(email);
            user.setEmailVerified(true);
        }

        // 设置GitHub头像
        user.setGithubAvatar(avatarUrl);

        userMapper.insert(user);
        return user;
    }

    private void updateLoginInfo(User user, HttpServletRequest request) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getClientIp(request));
        user.setLoginCount(user.getLoginCount() + 1);

        userMapper.updateById(user);
    }
}
