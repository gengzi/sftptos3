package com.gengzi.sftp.usermodel.monitor.entity;

import java.time.LocalDateTime;

/**
 * 客户端连接信息实体
 */
public class ClientConnection {

    // 客户端唯一标识（如 username@ip）
    String clientId;

    String username;
    // 客户端IP地址
    String ipAddress;
    // 客户端


    // 连接建立时间
    LocalDateTime connectTime;

}