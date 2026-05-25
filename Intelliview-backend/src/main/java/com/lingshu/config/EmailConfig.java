package com.lingshu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email")
public class EmailConfig {
    private Boolean enabled = true;
    private String from;
    private String fromName;
    private Boolean mock = false;
    private String provider = "smtp";

    // SendGrid??
    private String sendgridApiKey;

    // 邮件主题
    private String loginSubject = "领书 - 登录验证码";
    private String registerSubject = "领书 - 注册验证码";
    private String resetPasswordSubject = "领书 - 重置密码验证码";
    private String deleteAccountSubject = "领书 - 删除账号验证码";

    private String codeTemplate = "<!DOCTYPE html>" +
        "<html>" +
        "<head>" +
        "    <meta charset=\"UTF-8\">" +
        "    <style>" +
        "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
        "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
        "        .header { background: #1890ff; color: white; padding: 20px; text-align: center; }" +
        "        .content { padding: 30px; background: #f9f9f9; }" +
        "        .code {" +
        "            font-size: 32px;" +
        "            font-weight: bold;" +
        "            color: #1890ff;" +
        "            letter-spacing: 5px;" +
        "            text-align: center;" +
        "            margin: 20px 0;" +
        "            padding: 10px;" +
        "            background: white;" +
        "            border: 2px dashed #1890ff;" +
        "            border-radius: 5px;" +
        "        }" +
        "        .footer {" +
        "            margin-top: 30px;" +
        "            padding-top: 20px;" +
        "            border-top: 1px solid #eee;" +
        "            font-size: 12px;" +
        "            color: #999;" +
        "            text-align: center;" +
        "        }" +
        "    </style>" +
        "</head>" +
        "<body>" +
        "    <div class=\"container\">" +
        "        <div class=\"header\">" +
        "            <h1>领书</h1>" +
        "        </div>" +
        "        <div class=\"content\">" +
        "            <h2>验证码</h2>" +
        "            <p>您正在进行{operation}操作，验证码如下：</p>" +
        "            <div class=\"code\">{code}</div>" +
        "            <p>验证码将在10分钟后过期</p>" +
        "            <p>请勿将验证码泄露给他人</p>" +
        "        </div>" +
        "        <div class=\"footer\">" +
        "            <p>此邮件由系统自动发送</p>" +
        "            <p>© 2024 领书 版权所有</p>" +
        "        </div>" +
        "    </div>" +
        "</body>" +
        "</html>";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public Boolean getMock() {
        return mock;
    }

    public void setMock(Boolean mock) {
        this.mock = mock;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSendgridApiKey() {
        return sendgridApiKey;
    }

    public void setSendgridApiKey(String sendgridApiKey) {
        this.sendgridApiKey = sendgridApiKey;
    }

    public String getLoginSubject() {
        return loginSubject;
    }

    public void setLoginSubject(String loginSubject) {
        this.loginSubject = loginSubject;
    }

    public String getRegisterSubject() {
        return registerSubject;
    }

    public void setRegisterSubject(String registerSubject) {
        this.registerSubject = registerSubject;
    }

    public String getResetPasswordSubject() {
        return resetPasswordSubject;
    }

    public void setResetPasswordSubject(String resetPasswordSubject) {
        this.resetPasswordSubject = resetPasswordSubject;
    }

    public String getDeleteAccountSubject() {
        return deleteAccountSubject;
    }

    public void setDeleteAccountSubject(String deleteAccountSubject) {
        this.deleteAccountSubject = deleteAccountSubject;
    }

    public String getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }
}
