package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class SendCodeRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "类型不能为空")
    private String type; // LOGIN, REGISTER, RESET_PASSWORD

    @NotBlank(message = "图形验证码key不能为空")
    private String captchaKey;

    @NotBlank(message = "图形验证码不能为空")
    @Size(min = 4, max = 6, message = "图形验证码长度不正确")
    private String captchaCode;
}
