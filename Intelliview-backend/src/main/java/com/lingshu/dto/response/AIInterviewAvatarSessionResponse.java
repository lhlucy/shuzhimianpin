package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIInterviewAvatarSessionResponse {

    private boolean enabled;

    private boolean connected;

    private Integer playerType;

    private String sid;

    private String server;

    private String auth;

    private String appid;

    private String userId;

    private String roomId;

    private String timeStr;

    private String streamUrl;

    private Integer width;

    private Integer height;

    private String message;
}
