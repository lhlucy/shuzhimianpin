package com.lingshu.dto.request;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class DeleteAccountRequest {
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位")
    @Pattern(regexp = "\\d{6}", message = "验证码必须是6位数字")
    private String code;
}

