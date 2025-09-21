package com.gengzi.sftp.monitor.config;

import com.gengzi.sftp.monitor.webSocket.SftpWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket  // 启用WebSocket支持
public class WebSocketConfig implements WebSocketConfigurer {

    private final SftpWebSocketHandler sftpWebSocketHandler;

    // 注入自定义的消息处理器
    public WebSocketConfig(SftpWebSocketHandler sftpWebSocketHandler) {
        this.sftpWebSocketHandler = sftpWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket端点，允许跨域访问
        registry.addHandler(sftpWebSocketHandler, "/ws/sftp-monitor")
                .setAllowedOrigins("*");  // 生产环境需指定具体域名，而非*
    }
}
