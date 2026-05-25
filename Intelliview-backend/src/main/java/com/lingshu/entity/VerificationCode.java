package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("verification_codes")
public class VerificationCode {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String email;

    @TableField
    private String code;

    @TableField
    private CodeType type;

    @TableField
    private String ip;

    @TableField("user_agent")
    private String userAgent;

    @TableField
    private Boolean used = false;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CodeType getType() {
        return type;
    }

    public void setType(CodeType type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public enum CodeType {
        LOGIN,           // 登录
        REGISTER,        // 注册
        RESET_PASSWORD,  // 重置密码
        BIND_EMAIL,       // 绑定邮箱
        ACCOUNT_DELETE  // 删除账号
    }
}
