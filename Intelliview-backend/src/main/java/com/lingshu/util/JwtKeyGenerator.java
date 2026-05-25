package com.lingshu.util;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtKeyGenerator {

    public static void main(String[] args) {
        // 生成HS512密钥
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);

        // 转换为Base64编码
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("生成的Base64密钥:");
        System.out.println(base64Key);
        System.out.println("\n长度: " + base64Key.length() + " 字符");
        System.out.println("密钥长度: " + key.getEncoded().length + " 字节 (512位密钥)");
    }
}
