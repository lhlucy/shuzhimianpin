// ?????????????
//package com.lingshu.service;
//
//import com.google.code.kaptcha.Producer;
//import com.lingshu.dto.response.CaptchaResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Base64;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@Slf4j
//public class CaptchaService {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    // ??kaptcha?Producer
//    @Autowired
//    private Producer captchaProducer;
//
//    @Value("${app.captcha.expire-seconds:300}")
//    private long captchaExpireSeconds;
//
//    private static final String CAPTCHA_PREFIX = "captcha:";
//
//    public CaptchaResponse generateCaptcha() {
//        // ???????
//        String code = captchaProducer.createText();
//
//        // ????key
//        String captchaKey = UUID.randomUUID().toString();
//
//        // ???Redis?5????
//        redisTemplate.opsForValue().set(
//                CAPTCHA_PREFIX + captchaKey,
//                code.toLowerCase(),
//                captchaExpireSeconds,
//                TimeUnit.SECONDS
//        );
//
//        // ???????
//        String base64Image = generateCaptchaImage(code);
//
//        return CaptchaResponse.builder()
//                .captchaKey(captchaKey)
//                .captchaImage("data:image/png;base64," + base64Image)
//                .expireTime(captchaExpireSeconds * 1000)
//                .build();
//    }
//
//    public boolean validateCaptcha(String captchaKey, String captchaCode) {
//        if (captchaKey == null || captchaCode == null) {
//            return false;
//        }
//
//        String key = CAPTCHA_PREFIX + captchaKey;
//        String storedCode = redisTemplate.opsForValue().get(key);
//
//        if (storedCode == null) {
//            return false;
//        }
//
//        // ???????
//        redisTemplate.delete(key);
//
//        return storedCode.equalsIgnoreCase(captchaCode.trim());
//    }
//
//    /**
//     * ??kaptcha???????????base64???
//     */
//    private String generateCaptchaImage(String captchaText) {
//        try {
//            // ??captchaProducer????
//            BufferedImage bi = captchaProducer.createImage(captchaText);
//
//            // ???base64
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(bi, "png", baos);
//            byte[] bytes = baos.toByteArray();
//
//            return Base64.getEncoder().encodeToString(bytes);
//        } catch (IOException e) {
//            log.error("?????????", e);
//            // ?????????????????
//            return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
//        }
//    }
//}
