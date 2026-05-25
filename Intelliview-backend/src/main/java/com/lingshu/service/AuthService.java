package com.lingshu.service;

import com.lingshu.dto.request.LoginRequest;
import com.lingshu.dto.response.LoginResponse;
import com.lingshu.entity.User;
import com.lingshu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public LoginResponse loginWithPassword(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userMapper.findByUsername(request.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            String token = jwtService.generateToken(user.getUsername());

            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)
                    .user(UserService.convertToLoginUserResponse(user))
                    .build();

        } catch (Exception e) {
            log.error("登录失败", e);
            throw new RuntimeException("登录失败，请检查用户名和密码");
        }
    }
}
