package com.lingshu.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateUserRoleRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotNull(message = "角色不能为空")
    private String role;
}

