package com.gengzi.sftp.usermodel.dao.audit;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * SFTP客户端连接审计表
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "sftp_connection_audit", schema = "sftptos3")
public class SftpConnectionAudit {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    /**
     * 连接用户名
     */
    @Size(max = 64)
    @NotNull
    @Column(name = "username", nullable = false, length = 64)
    private String username;

    /**
     * 客户端IP
     */
    @Size(max = 32)
    @NotNull
    @Column(name = "client_ip", nullable = false, length = 32)
    private String clientIp;

    /**
     * 客户端端口
     */
    @Column(name = "client_port")
    private Integer clientPort;

    /**
     * 连接建立时间
     */
    @NotNull
    @Column(name = "connect_time", nullable = false)
    private LocalDateTime connectTime;

    /**
     * 连接断开时间
     */
    @Column(name = "disconnect_time")
    private LocalDateTime disconnectTime;

    /**
     * 认证状态：0未认证，1成功，2失败
     */
    @NotNull
    @Column(name = "auth_status", nullable = false)
    private Byte authStatus;

    /**
     * 认证失败原因
     */
    @Size(max = 128)
    @Column(name = "auth_failure_reason", length = 128)
    private String authFailureReason;

    /**
     * 断开原因
     */
    @Size(max = 1024)
    @Column(name = "disconnect_reason", length = 1024)
    private String disconnectReason;

    /**
     * 审计记录创建时间
     */
    @NotNull
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "manually_close_client")
    private Byte manuallyCloseClient;

    @Size(max = 32)
    @NotNull
    @Column(name = "auth_type", nullable = false, length = 32)
    private String authType;

}