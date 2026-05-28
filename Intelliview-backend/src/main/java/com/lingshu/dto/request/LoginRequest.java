package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50位")
    private String password;

    @NotBlank(message = "图形验证码key不能为空")
    private String captchaKey;

    @NotBlank(message = "图形验证码不能为空")
    @Size(min = 4, max = 6, message = "图形验证码长度不正确")
    private String captchaCode;
}
