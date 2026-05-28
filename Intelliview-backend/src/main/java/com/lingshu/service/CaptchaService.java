package com.lingshu.service;

import com.google.code.kaptcha.Producer;
import com.lingshu.dto.response.CaptchaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptchaService {

    private static final String CAPTCHA_PREFIX = "captcha:";

    private final StringRedisTemplate redisTemplate;
    private final Producer captchaProducer;

    @Value("${app.captcha.expire-seconds:300}")
    private long captchaExpireSeconds;

    public CaptchaResponse generateCaptcha() {
        String code = captchaProducer.createText();
        String captchaKey = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaKey,
                code.toLowerCase(),
                captchaExpireSeconds,
                TimeUnit.SECONDS
        );

        return CaptchaResponse.builder()
                .captchaKey(captchaKey)
                .captchaImage("data:image/png;base64," + generateCaptchaImage(code))
                .expireTime(captchaExpireSeconds * 1000)
                .build();
    }

    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.trim().isEmpty()
                || captchaCode == null || captchaCode.trim().isEmpty()) {
            return false;
        }

        String key = CAPTCHA_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            return false;
        }

        redisTemplate.delete(key);
        return storedCode.equalsIgnoreCase(captchaCode.trim());
    }

    private String generateCaptchaImage(String captchaText) {
        try {
            BufferedImage image = captchaProducer.createImage(captchaText);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            log.error("生成图形验证码失败", e);
            return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        }
    }
}
