package com.lingshu.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomUtil {

    private static final String DIGITS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC = LETTERS + DIGITS;

    private static final Random RANDOM = new SecureRandom();

    // 生成数字验证码
    public static String generateDigitCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return sb.toString();
    }

    // 生成随机字符串
    public static String generateString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    // 生成随机用户名
    public static String generateUsername() {
        return "user_" + System.currentTimeMillis() + "_" + generateDigitCode(4);
    }
}
