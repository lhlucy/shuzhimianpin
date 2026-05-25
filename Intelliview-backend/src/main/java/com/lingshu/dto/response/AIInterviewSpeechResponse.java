package com.lingshu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIInterviewSpeechResponse {

    private String audioUrl;

    private Long expiresAt;

    private String voice;

    private String format;

    private String text;
}
