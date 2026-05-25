package com.lingshu.dto.request;

public class UserProfileRequest {
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String bio;
    private Long targetJobRoleId;
    private String experienceLevel;
    private String preferredCompanyType;
    private String targetCity;
    private String expectedSalary;
    private String resumeSummary;

    public UserProfileRequest() {
    }

    public UserProfileRequest(String nickname, String avatar, String email, String phone, String bio) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nickname;
        private String avatar;
        private String email;
        private String phone;
        private String bio;

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
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

        public UserProfileRequest build() {
            return new UserProfileRequest(nickname, avatar, email, phone, bio);
        }
    }
}
