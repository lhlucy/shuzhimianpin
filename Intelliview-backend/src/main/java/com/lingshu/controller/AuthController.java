package com.lingshu.controller;

import com.lingshu.dto.request.*;
import com.lingshu.dto.response.*;
import com.lingshu.entity.LoginRecord;
import com.lingshu.entity.VerificationCode;
import com.lingshu.entity.User;
import com.lingshu.mapper.UserMapper;
import com.lingshu.service.*;
import com.lingshu.util.IpUtil;
import com.lingshu.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    private final EmailService emailService;
    private final GithubAuthService githubAuthService;
    private final UserService userService;

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public ResponseEntity<ApiResponse<CaptchaResponse>> getCaptcha() {
        CaptchaResponse captcha = captchaService.generateCaptcha();
        return ResponseEntity.ok(ApiResponse.success(captcha));
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody
            SendCodeRequest request,
            HttpServletRequest httpRequest) {
        request.setEmail(normalizeEmail(request.getEmail()));

        if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("图形验证码错误或已过期"));
        }

        // 解析验证码类型
        VerificationCode.CodeType type;
        try {
            type = VerificationCode.CodeType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证码类型错误"));
        }

        // 检查邮箱是否存在，根据验证码类型判断
        boolean emailExists = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("email", request.getEmail())) != null;

        if (type == VerificationCode.CodeType.REGISTER && emailExists) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("邮箱已存在"));
        }

        if ((type == VerificationCode.CodeType.LOGIN ||
                type == VerificationCode.CodeType.RESET_PASSWORD) && !emailExists) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("邮箱不存在"));
        }

        // 发送验证码
        emailService.sendVerificationCode(request.getEmail(), type, httpRequest);

        return ResponseEntity.ok(ApiResponse.success("验证码发送成功", null));
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login/email")
    @Transactional
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithEmail(
            @Valid @RequestBody
            EmailLoginRequest request,
            HttpServletRequest httpRequest) {

        // 验证验证码
        if (!emailService.validateCode(request.getEmail(), request.getCode(),
                VerificationCode.CodeType.LOGIN)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证码错误或已过期"));
        }

        // 查询用户
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("email", request.getEmail()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新登录信息
        updateUserLoginInfo(user, httpRequest, LoginRecord.LoginType.EMAIL_CODE);

        // 生成JWT token
        String token = jwtService.generateToken(user.getUsername());

        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24小时
                .user(UserService.convertToLoginUserResponse(user))
                .build();

        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    /**
     * 密码登录
     */
    @PostMapping("/login/password")
    @Transactional
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithPassword(
            @Valid @RequestBody
            LoginRequest request,
        HttpServletRequest httpRequest) {

        try {
            if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("图形验证码错误或已过期"));
            }

            // 进行身份认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 查询用户信息
            User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", request.getUsername()));
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 更新用户登录信息
            updateUserLoginInfo(user, httpRequest, LoginRecord.LoginType.PASSWORD);

            // 生成JWT token
            String token = jwtService.generateToken(user.getUsername());

            // 构建登录响应
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(86400000L) // 24小时
                    .user(UserService.convertToLoginUserResponse(user))
                    .build();

            return ResponseEntity.ok(ApiResponse.success("登录成功", response));

        } catch (Exception e) {
            log.error("登录失败", e);
            // 打印具体的异常信息
            System.err.println("登录失败的具体原因：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登录失败，请检查用户名和密码: " + e.getMessage()));
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody
            RegisterRequest request,
            HttpServletRequest httpRequest) {
        request.setUsername(request.getUsername().trim());
        request.setEmail(normalizeEmail(request.getEmail()));
        request.setPhone(request.getPhone().trim());

        // 验证验证码
        if (!emailService.validateCode(request.getEmail(), request.getCode(),
                VerificationCode.CodeType.REGISTER)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证码错误或已过期"));
        }

        // 检查用户名是否存在
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> usernameWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        usernameWrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户名已存在"));
        }

        // 检查邮箱是否存在
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> emailWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        emailWrapper.eq("email", request.getEmail());
        if (userMapper.selectCount(emailWrapper) > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("邮箱已存在"));
        }

        if (userMapper.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("手机号已存在"));
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEmailVerified(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        } else {
            user.setNickname(request.getUsername());
        }

        // 生成默认头像
        user.setAvatar(generateDefaultAvatar(user.getEmail()));

        userMapper.insert(user);
        User savedUser = user;

        // 更新登录信息
        updateUserLoginInfo(savedUser, httpRequest, LoginRecord.LoginType.PASSWORD);

        // 生成JWT token
        String token = jwtService.generateToken(savedUser.getUsername());

        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24小时
                .user(UserService.convertToLoginUserResponse(savedUser))
                .build();

        log.info("用户注册: {}", savedUser.getUsername());

        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    /**
     * 获取GitHub授权URL
     */
    @GetMapping("/github/authorize")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGithubAuthUrl(
            @RequestParam(value = "state", required = false) String state) {

        if (state == null || state.isEmpty()) {
            state = RandomUtil.generateString(16);
        }

        String authUrl = githubAuthService.getAuthorizationUrl(state);

        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        response.put("state", state);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GitHub登录回调
     */
    @PostMapping("/github/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> githubCallback(
            @Valid @RequestBody
            GithubCallbackRequest request,
            HttpServletRequest httpRequest) {

        try {
            LoginResponse response = githubAuthService.handleCallback(
                    request.getCode(), httpRequest);

            return ResponseEntity.ok(ApiResponse.success("GitHub登录成功", response));

        } catch (Exception e) {
            log.error("GitHub登录失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("GitHub登录失败: " + e.getMessage()));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT是无状态的，客户端删除token即可
        // 服务端不需要做额外处理
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("缺少token"));
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("token已过期或无效"));
        }

        String username = jwtService.getUsernameFromToken(token);
        String newToken = jwtService.generateToken(username);

        Map<String, String> response = new HashMap<>();
        response.put("token", newToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", "86400000");

        return ResponseEntity.ok(ApiResponse.success("token刷新成功", response));
    }

    private void updateUserLoginInfo(User user, HttpServletRequest request,
                                     LoginRecord.LoginType loginType) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getClientIp(request));
        user.setLoginCount(user.getLoginCount() + 1);

        userMapper.updateById(user);

        // 记录登录日志
        log.info("用户登录: {}, IP: {}, 方式: {}",
                user.getUsername(), user.getLastLoginIp(), loginType);
    }

    private String generateDefaultAvatar(String email) {
        // 使用ui-avatars生成默认头像
        // 基于邮箱首字母生成
        return "https://ui-avatars.com/api/?name=" +
                (email != null ? email.substring(0, 1) : "U") +
                "&background=random";
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody
            ResetPasswordRequest request) {

        // 验证验证码
        if (!emailService.validateCode(request.getEmail(), request.getCode(),
                VerificationCode.CodeType.RESET_PASSWORD)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证码错误或已过期"));
        }

        // 重置密码
        userService.resetPassword(request.getEmail(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
    }
}
