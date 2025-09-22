package com.gengzi.sftp.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 配置跨域支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭CSRF保护（如果不需要的话）
                .csrf(csrf -> csrf.disable())
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行所有/api/**路径的请求
                        .antMatchers("/api/**").permitAll()
                        // 其他所有请求都拒绝访问
                        .anyRequest().denyAll()
                )
                // 关闭默认的表单登录
                .formLogin(form -> form.disable())
                // 关闭HTTP基本认证
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    // 定义跨域配置源
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的源（生产环境建议指定具体域名，而非*）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET"));
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带cookie
        configuration.setAllowCredentials(true);
        // 预检请求的有效期（秒）
        configuration.setMaxAge(3600L);

        // 对所有路径应用跨域配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}


