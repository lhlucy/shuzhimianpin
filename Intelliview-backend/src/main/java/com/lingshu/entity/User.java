package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "password")
    private String password;

    @TableField(value = "email")
    private String email;

    @TableField(value = "email_verified")
    private Boolean emailVerified = false;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "avatar")
    private String avatar;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "bio")
    private String bio;

    @TableField(value = "role")
    private String role = "ROLE_USER";

    @TableField(value = "enabled")
    private Boolean enabled = true;

    // GitHub相关信息
    @TableField(value = "github_id")
    private String githubId;

    @TableField(value = "github_login")
    private String githubLogin;

    @TableField(value = "github_avatar")
    private String githubAvatar;

    @TableField(value = "github_name")
    private String githubName;

    @TableField(value = "github_bio")
    private String githubBio;

    @TableField(value = "github_company")
    private String githubCompany;

    @TableField(value = "github_blog")
    private String githubBlog;

    @TableField(value = "github_location")
    private String githubLocation;

    // 登录信息
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField(value = "last_login_ip")
    private String lastLoginIp;

    @TableField(value = "login_count")
    private Integer loginCount = 0;

    // 用户统计信息
    @TableField(value = "points")
    private Integer points = 0;

    @TableField(value = "level")
    private Integer level = 1;

    @TableField(value = "experience")
    private Integer experience = 0;

    @TableField(value = "target_job_role_id")
    private Long targetJobRoleId;

    @TableField(value = "experience_level")
    private String experienceLevel;

    @TableField(value = "preferred_company_type")
    private String preferredCompanyType;

    @TableField(value = "target_city")
    private String targetCity;

    @TableField(value = "expected_salary")
    private String expectedSalary;

    @TableField(value = "resume_summary")
    private String resumeSummary;

    @TableField(value = "latest_overall_score")
    private Double latestOverallScore;

    @TableField(value = "latest_job_match_score")
    private Double latestJobMatchScore;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getGithubLogin() {
        return githubLogin;
    }

    public void setGithubLogin(String githubLogin) {
        this.githubLogin = githubLogin;
    }

    public String getGithubAvatar() {
        return githubAvatar;
    }

    public void setGithubAvatar(String githubAvatar) {
        this.githubAvatar = githubAvatar;
    }

    public String getGithubName() {
        return githubName;
    }

    public void setGithubName(String githubName) {
        this.githubName = githubName;
    }

    public String getGithubBio() {
        return githubBio;
    }

    public void setGithubBio(String githubBio) {
        this.githubBio = githubBio;
    }

    public String getGithubCompany() {
        return githubCompany;
    }

    public void setGithubCompany(String githubCompany) {
        this.githubCompany = githubCompany;
    }

    public String getGithubBlog() {
        return githubBlog;
    }

    public void setGithubBlog(String githubBlog) {
        this.githubBlog = githubBlog;
    }

    public String getGithubLocation() {
        return githubLocation;
    }

    public void setGithubLocation(String githubLocation) {
        this.githubLocation = githubLocation;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Long getTargetJobRoleId() {
        return targetJobRoleId;
    }

    public void setTargetJobRoleId(Long targetJobRoleId) {
        this.targetJobRoleId = targetJobRoleId;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getPreferredCompanyType() {
        return preferredCompanyType;
    }

    public void setPreferredCompanyType(String preferredCompanyType) {
        this.preferredCompanyType = preferredCompanyType;
    }

    public String getTargetCity() {
        return targetCity;
    }

    public void setTargetCity(String targetCity) {
        this.targetCity = targetCity;
    }

    public String getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(String expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getResumeSummary() {
        return resumeSummary;
    }

    public void setResumeSummary(String resumeSummary) {
        this.resumeSummary = resumeSummary;
    }

    public Double getLatestOverallScore() {
        return latestOverallScore;
    }

    public void setLatestOverallScore(Double latestOverallScore) {
        this.latestOverallScore = latestOverallScore;
    }

    public Double getLatestJobMatchScore() {
        return latestJobMatchScore;
    }

    public void setLatestJobMatchScore(Double latestJobMatchScore) {
        this.latestJobMatchScore = latestJobMatchScore;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
