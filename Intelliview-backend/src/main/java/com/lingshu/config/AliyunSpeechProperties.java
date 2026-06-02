package com.lingshu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.llm.aliyun.speech")
public class AliyunSpeechProperties {

    private boolean enabled = true;

    private String apiKey = "";

    private String asrBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String asrModel = "qwen3-asr-flash";

    private String realtimeAsrUrl = "wss://dashscope.aliyuncs.com/api-ws/v1/realtime";

    private String realtimeAsrModel = "qwen3-asr-flash-realtime-2026-02-10";

    private int realtimeSampleRate = 16000;

    private String ttsUrl = "https://dashscope.aliyuncs.com/api/v1/services/audio/tts/SpeechSynthesizer";

    private String ttsModel = "cosyvoice-v3-flash";

    private String chineseVoice = "longanyang";

    private String englishVoice = "longanyang";

    private String audioFormat = "mp3";

    private int sampleRate = 24000;

    private int connectTimeoutMs = 5000;

    private int readTimeoutMs = 30000;
}
