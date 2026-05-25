package com.lingshu.exception;

public enum ErrorCode {
    // 通用错误
    SUCCESS("0000", "成功"),
    SYSTEM_ERROR("1000", "系统错误"),
    PARAM_ERROR("1001", "参数错误"),
    BAD_REQUEST("1002", "请求错误"),

    // 认证错误
    AUTH_ERROR("2000", "认证错误"),
    USER_NOT_FOUND("2001", "用户不存在"),
    PASSWORD_ERROR("2002", "密码错误"),
    TOKEN_EXPIRED("2003", "Token过期"),
    TOKEN_INVALID("2004", "Token无效"),

    // 业务错误
    USER_EXISTS("3001", "用户已存在"),
    EMAIL_EXISTS("3002", "邮箱已存在"),
    VERIFICATION_CODE_ERROR("3003", "验证码错误"),
    VERIFICATION_CODE_EXPIRED("3004", "验证码过期"),
    CATEGORY_NOT_FOUND("3005", "分类不存在"),
    QUESTION_NOT_FOUND("3006", "问题不存在"),

    // 权限错误
    PERMISSION_DENIED("4001", "权限不足"),
    NOT_LOGIN("4002", "未登录"),

    // 面试相关错误
    INTERVIEW_NOT_FOUND("5001", "面试不存在"),
    INTERVIEW_STATUS_INVALID("5002", "面试状态无效"),
    ACCESS_DENIED("5003", "访问拒绝");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
