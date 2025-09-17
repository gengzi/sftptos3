package com.gengzi;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密密码
    public static String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 验证密码
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        // 示例：加密密码
        String rawPassword = "admin123";
        String encodedPassword = encodePassword(rawPassword);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt加密后: " + encodedPassword);
        
        // 验证密码
        boolean isMatch = matchesPassword(rawPassword, encodedPassword);
        System.out.println("密码匹配: " + isMatch);
    }
}