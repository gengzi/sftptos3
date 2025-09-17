package com.gengzi;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class Hs512SecretGenerator {
    public static void main(String[] args) {
        // 生成HS512算法专用密钥（自动确保≥512位）
        SecretKey secretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        // 转为Base64编码（便于存储在配置文件中，避免特殊字符问题）
        String base64Secret = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        
        System.out.println("HS512专用密钥（Base64编码）：");
        System.out.println(base64Secret);
        System.out.println("密钥原始长度：" + secretKey.getEncoded().length + "字节（" + secretKey.getEncoded().length * 8 + "位）");
    }
}