package com.lingshu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("login_records")
@Data
public class LoginRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("login_type")
    private LoginType loginType;

    @TableField("login_ip")
    private String loginIp;

    @TableField("login_location")
    private String loginLocation;

    @TableField("user_agent")
    private String userAgent;

    @TableField
    private Boolean success = true;

    @TableField("fail_reason")
    private String failReason;

    @TableField("login_time")
    private LocalDateTime loginTime;

    public enum LoginType {
        PASSWORD,      // 密码登录
        EMAIL_CODE,    // 邮箱验证码登录
        GITHUB         // GitHub登录
    }
}
