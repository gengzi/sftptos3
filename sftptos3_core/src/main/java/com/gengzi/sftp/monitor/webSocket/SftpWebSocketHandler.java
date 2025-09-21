package com.gengzi.sftp.monitor.webSocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SftpWebSocketHandler extends TextWebSocketHandler {

    // 存储所有活跃的WebSocket会话（线程安全）
    private final Set<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();

    // 连接建立时触发
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
        // 发送连接成功消息
        session.sendMessage(new TextMessage("{\"type\":\"connect\",\"message\":\"WebSocket连接成功\"}"));
    }

    // 连接关闭时触发
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        activeSessions.remove(session);
    }

    // 接收客户端消息（如果需要处理客户端请求）
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // 处理客户端发送的消息（如订阅特定指标）
        session.sendMessage(new TextMessage("{\"type\":\"response\",\"message\":\"已收到: " + payload + "\"}"));
    }

    // 向所有客户端广播消息
    public void broadcast(String message) {
        activeSessions.forEach(session -> {
            try {
                if (session.isOpen()) {  // 确保会话处于打开状态
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                // 处理发送异常（如会话已关闭）
            }
        });
    }
}
