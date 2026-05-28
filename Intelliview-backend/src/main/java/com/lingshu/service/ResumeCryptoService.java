package com.lingshu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class ResumeCryptoService {

    private static final String PREFIX = "AESGCM:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;

    public ResumeCryptoService(@Value("${app.security.resume-encryption-key:}") String configuredKey,
                               @Value("${app.jwt.secret:}") String jwtSecret) {
        String keyMaterial = StringUtils.hasText(configuredKey) ? configuredKey : jwtSecret;
        if (!StringUtils.hasText(keyMaterial)) {
            keyMaterial = "intelliview-local-resume-encryption-key";
            log.warn("未配置 app.security.resume-encryption-key，当前使用本地开发兜底密钥。生产环境请通过环境变量 RESUME_ENCRYPTION_KEY 注入。");
        }
        this.keySpec = new SecretKeySpec(sha256(keyMaterial), "AES");
    }

    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText) || plainText.startsWith(PREFIX)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new IllegalStateException("简历内容加密失败", ex);
        }
    }

    public String decrypt(String storedText) {
        if (!StringUtils.hasText(storedText) || !storedText.startsWith(PREFIX)) {
            return storedText;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(storedText.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.warn("简历内容解密失败，将按空内容处理", ex);
            return "";
        }
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("初始化简历加密密钥失败", ex);
        }
    }
}
