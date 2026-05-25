package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 根据用户名查询用户
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    // 根据邮箱查询用户
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);
    
    // 根据GitHub ID查询用户
    @Select("SELECT * FROM users WHERE github_id = #{githubId}")
    User findByGithubId(@Param("githubId") String githubId);
    
    // 检查用户名是否存在
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);
    
    // 检查邮箱是否存在
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);
    
    // 检查GitHub ID是否存在
    @Select("SELECT COUNT(*) > 0 FROM users WHERE github_id = #{githubId}")
    boolean existsByGithubId(@Param("githubId") String githubId);
    
    // 根据用户名模糊查询用户（忽略大小写）
    @Select("SELECT * FROM users WHERE LOWER(username) LIKE LOWER(CONCAT('%', #{username}, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);
}

