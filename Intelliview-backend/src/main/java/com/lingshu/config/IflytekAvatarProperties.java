package com.lingshu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.avatar.iflytek")
public class IflytekAvatarProperties {

    private boolean enabled = false;

    private String wsUrl = "ws://avatar.cn-huadong-1.xf-yun.com/v1/interact";

    private String appId = "";

    private String apiKey = "";

    private String apiSecret = "";

    private String sceneId = "";

    private String avatarId = "";

    private String vcn = "";

    /**
     * RTCPlayer 使用的 appid 与开放平台 app_id 不是同一个字段。
     * 参考讯飞示例播放器，默认值通常为 1000000001。
     */
    private String playerAppId = "1000000001";

    private int width = 720;

    private int height = 1280;

    private int fps = 25;

    private int bitrate = 5000;

    private int alpha = 0;

    private int speed = 50;

    private int pitch = 50;

    private int volume = 50;

    private int connectTimeoutMs = 10000;

    private int initTimeoutMs = 15000;

    private int heartbeatSeconds = 5;
}
