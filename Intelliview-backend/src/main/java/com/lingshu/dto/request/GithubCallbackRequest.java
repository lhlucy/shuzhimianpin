package com.lingshu.dto.request;

import javax.validation.constraints.*;

public class GithubCallbackRequest {
    @NotBlank(message = "code不能为空")
    private String code;

    private String state;

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

