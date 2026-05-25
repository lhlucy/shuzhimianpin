package com.lingshu.dto.response;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String phone;
    private String bio;
    private String role;
    private Boolean emailVerified;
    private String githubLogin;
    private String githubAvatar;
    private String githubName;
    private String githubBio;
    private String githubCompany;
    private String githubBlog;
    private String githubLocation;
    private LocalDateTime lastLoginTime;
    private Integer loginCount;
    private Integer points;
    private Integer level;
    private Integer experience;
    private Long targetJobRoleId;
    private String experienceLevel;
    private String preferredCompanyType;
    private String targetCity;
    private String expectedSalary;
    private String resumeSummary;
    private Double latestOverallScore;
    private Double latestJobMatchScore;
    private LocalDateTime createTime;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String username, String email, String nickname, String avatar, String phone, String bio, String role, Boolean emailVerified, String githubLogin, String githubAvatar, String githubName, String githubBio, String githubCompany, String githubBlog, String githubLocation, LocalDateTime lastLoginTime, Integer loginCount, Integer points, Integer level, Integer experience, LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
        this.phone = phone;
        this.bio = bio;
        this.role = role;
        this.emailVerified = emailVerified;
        this.githubLogin = githubLogin;
        this.githubAvatar = githubAvatar;
        this.githubName = githubName;
        this.githubBio = githubBio;
        this.githubCompany = githubCompany;
        this.githubBlog = githubBlog;
        this.githubLocation = githubLocation;
        this.lastLoginTime = lastLoginTime;
        this.loginCount = loginCount;
        this.points = points;
        this.level = level;
        this.experience = experience;
        this.createTime = createTime;
    }

    public UserProfileResponse(Long id, String username, String email, String nickname, String avatar, String phone, String bio, String role, Boolean emailVerified, String githubLogin, String githubAvatar, String githubName, String githubBio, String githubCompany, String githubBlog, String githubLocation, LocalDateTime lastLoginTime, Integer loginCount, Integer points, Integer level, Integer experience, Long targetJobRoleId, String experienceLevel, String preferredCompanyType, String targetCity, String expectedSalary, String resumeSummary, Double latestOverallScore, Double latestJobMatchScore, LocalDateTime createTime) {
        this(id, username, email, nickname, avatar, phone, bio, role, emailVerified, githubLogin, githubAvatar, githubName, githubBio, githubCompany, githubBlog, githubLocation, lastLoginTime, loginCount, points, level, experience, createTime);
        this.targetJobRoleId = targetJobRoleId;
        this.experienceLevel = experienceLevel;
        this.preferredCompanyType = preferredCompanyType;
        this.targetCity = targetCity;
        this.expectedSalary = expectedSalary;
        this.resumeSummary = resumeSummary;
        this.latestOverallScore = latestOverallScore;
        this.latestJobMatchScore = latestJobMatchScore;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String role;
        private Boolean emailVerified;
        private String githubLogin;
        private String githubAvatar;
        private String githubName;
        private String githubBio;
        private String githubCompany;
        private String githubBlog;
        private String githubLocation;
        private LocalDateTime lastLoginTime;
        private Integer loginCount;
        private Integer points;
        private Integer level;
        private Integer experience;
        private Long targetJobRoleId;
        private String experienceLevel;
        private String preferredCompanyType;
        private String targetCity;
        private String expectedSalary;
        private String resumeSummary;
        private Double latestOverallScore;
        private Double latestJobMatchScore;
        private LocalDateTime createTime;
        private String phone;
        private String bio;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder githubLogin(String githubLogin) {
            this.githubLogin = githubLogin;
            return this;
        }

        public Builder githubAvatar(String githubAvatar) {
            this.githubAvatar = githubAvatar;
            return this;
        }

        public Builder githubName(String githubName) {
            this.githubName = githubName;
            return this;
        }

        public Builder githubBio(String githubBio) {
            this.githubBio = githubBio;
            return this;
        }

        public Builder githubCompany(String githubCompany) {
            this.githubCompany = githubCompany;
            return this;
        }

        public Builder githubBlog(String githubBlog) {
            this.githubBlog = githubBlog;
            return this;
        }

        public Builder githubLocation(String githubLocation) {
            this.githubLocation = githubLocation;
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }

        public Builder loginCount(Integer loginCount) {
            this.loginCount = loginCount;
            return this;
        }

        public Builder points(Integer points) {
            this.points = points;
            return this;
        }

        public Builder level(Integer level) {
            this.level = level;
            return this;
        }

        public Builder experience(Integer experience) {
            this.experience = experience;
            return this;
        }

        public Builder targetJobRoleId(Long targetJobRoleId) {
            this.targetJobRoleId = targetJobRoleId;
            return this;
        }

        public Builder experienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
            return this;
        }

        public Builder preferredCompanyType(String preferredCompanyType) {
            this.preferredCompanyType = preferredCompanyType;
            return this;
        }

        public Builder targetCity(String targetCity) {
            this.targetCity = targetCity;
            return this;
        }

        public Builder expectedSalary(String expectedSalary) {
            this.expectedSalary = expectedSalary;
            return this;
        }

        public Builder resumeSummary(String resumeSummary) {
            this.resumeSummary = resumeSummary;
            return this;
        }

        public Builder latestOverallScore(Double latestOverallScore) {
            this.latestOverallScore = latestOverallScore;
            return this;
        }

        public Builder latestJobMatchScore(Double latestJobMatchScore) {
            this.latestJobMatchScore = latestJobMatchScore;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public UserProfileResponse build() {
            return new UserProfileResponse(id, username, email, nickname, avatar, phone, bio, role, emailVerified, githubLogin, githubAvatar, githubName, githubBio, githubCompany, githubBlog, githubLocation, lastLoginTime, loginCount, points, level, experience, targetJobRoleId, experienceLevel, preferredCompanyType, targetCity, expectedSalary, resumeSummary, latestOverallScore, latestJobMatchScore, createTime);
        }
    }
}
