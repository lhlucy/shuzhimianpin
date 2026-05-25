package com.lingshu.dto.response;

import java.time.LocalDateTime;

public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tokenType, Long expiresIn, UserResponse user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String tokenType;
        private Long expiresIn;
        private UserResponse user;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder user(UserResponse user) {
            this.user = user;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(token, tokenType, expiresIn, user);
        }
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String role;
        private Boolean emailVerified;
        private String githubLogin;
        private String githubAvatar;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createTime;

        public UserResponse() {
        }

        public UserResponse(Long id, String username, String email, String nickname, String avatar, String role, Boolean emailVerified, String githubLogin, String githubAvatar, LocalDateTime lastLoginTime, LocalDateTime createTime) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.nickname = nickname;
            this.avatar = avatar;
            this.role = role;
            this.emailVerified = emailVerified;
            this.githubLogin = githubLogin;
            this.githubAvatar = githubAvatar;
            this.lastLoginTime = lastLoginTime;
            this.createTime = createTime;
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

        public LocalDateTime getLastLoginTime() {
            return lastLoginTime;
        }

        public void setLastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
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
            private LocalDateTime lastLoginTime;
            private LocalDateTime createTime;

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

            public Builder lastLoginTime(LocalDateTime lastLoginTime) {
                this.lastLoginTime = lastLoginTime;
                return this;
            }

            public Builder createTime(LocalDateTime createTime) {
                this.createTime = createTime;
                return this;
            }

            public UserResponse build() {
                return new UserResponse(id, username, email, nickname, avatar, role, emailVerified, githubLogin, githubAvatar, lastLoginTime, createTime);
            }
        }
    }
}
