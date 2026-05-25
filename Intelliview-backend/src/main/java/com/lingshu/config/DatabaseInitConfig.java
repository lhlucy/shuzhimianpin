package com.lingshu.config;

import com.lingshu.entity.User;
import com.lingshu.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Configuration
@Profile("!test")
public class DatabaseInitConfig {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initUsers() {
        // 添加管理员用户 (admin/123456)
        User admin = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", "admin"));
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@example.com");
            admin.setEmailVerified(true);
            admin.setNickname("管理员");
            admin.setAvatar("https://ui-avatars.com/api/?name=A&background=random");
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setLoginCount(0);
            admin.setPoints(0);
            admin.setLevel(1);
            admin.setExperience(0);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.insert(admin);
        } else {
            // 更新密码和角色
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(admin);
        }

        // 添加普通用户 (user/123456)
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", "user"));
        if (user == null) {
            user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setEmail("user@example.com");
            user.setEmailVerified(true);
            user.setNickname("普通用户");
            user.setAvatar("https://ui-avatars.com/api/?name=U&background=random");
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setLoginCount(0);
            user.setPoints(0);
            user.setLevel(1);
            user.setExperience(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 更新密码和角色
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }
}
