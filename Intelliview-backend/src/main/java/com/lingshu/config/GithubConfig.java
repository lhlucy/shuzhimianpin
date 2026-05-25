package com.lingshu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "github")
public class GithubConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    // GitHub API地址
    public static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    public static final String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    public static final String USER_API_URL = "https://api.github.com/user";
    public static final String USER_EMAILS_URL = "https://api.github.com/user/emails";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    // 获取scope
    public String getScope() {
        return "user:email";
    }

    // 构建授权URL
    public String buildAuthorizeUrl(String state) {
        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                AUTHORIZE_URL, clientId, redirectUri, getScope(), state);
    }
}
