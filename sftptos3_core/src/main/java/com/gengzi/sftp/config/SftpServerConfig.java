package com.gengzi.sftp.config;


import com.gengzi.sftp.factory.DynamicVirtualFileSystemFactory;
import com.gengzi.sftp.listener.SftpSessionListener;
import com.gengzi.sftp.listener.SftptoS3SftpEventListener;
import com.gengzi.sftp.sshd.AuditSftpSubsystemFactory;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.netty.NettyIoServiceFactoryFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.session.SessionFactory;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.sftp.server.UnsupportedAttributePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


/**
 * 配置sftp相关配置项
 */
@Configuration
public class SftpServerConfig {

    private static ClassLoadableResourceKeyPairProvider resourceKeyPairProvider;

    static {
        resourceKeyPairProvider = new ClassLoadableResourceKeyPairProvider("hostkey.ser");
    }

    @Value("${sftp.server.port}")
    private int sftpPort;
    @Value("${sftp.server.customerOptAuditRecord}")
    private boolean customerOptAuditRecord;
    @Autowired
    private SftpUserPasswordAuthenticator passwordAuthenticator;
    @Autowired
    private SftptoS3SftpEventListener sftpEventListener;
    @Autowired
    private SftpPublicKeyAuthenticator sftpPublicKeyAuthenticator;
    @Autowired
    private SftpSessionListener sftpSessionListener;

    @Bean
    public SshServer sftpServer() throws IOException {
        SshServer server = SshServer.setUpDefaultServer();
        server.setIoServiceFactoryFactory(new NettyIoServiceFactoryFactory());
        // 监听所有请求，默认支持
        // server.setHost("0.0.0.0");
        server.setPort(sftpPort);
        // 启用PROXY协议支持
        server.setServerProxyAcceptor(new ProxyProtocolAcceptor());
        SftpModuleProperties.COPY_BUF_SIZE.set(server,65536);
        CoreModuleProperties.IDLE_TIMEOUT.set(server, Duration.ofMinutes(15));
        CoreModuleProperties.NIO2_READ_TIMEOUT.set(server, Duration.ofMinutes(20));

        // 修正点：设置心跳机制
        // HeartbeatType.KEEP_ALIVE : 发送 SSH_MSG_IGNORE 或类似包保持连接
        // 60, TimeUnit.SECONDS   : 每 60 秒发一次
        server.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, TimeUnit.SECONDS, 60);

        // 5. [新增] 窗口大小 (Window Size) - 核心性能参数
        // SSH 协议有自己的流控窗口。默认值较小，对于 S3 这种高延迟写入
        // 需要调大窗口，让客户端在等待服务器 ACK 时能继续发数据，提高吞吐量。
        // 建议设置 2MB ~ 4MB (太大会导致内存溢出，太小速度跑不起来)
        CoreModuleProperties.WINDOW_SIZE.set(server, 1 * 1024 * 1024L); // 1MB

        // 6. [新增] 最大包大小 (Max Packet Size)
        // 配合上面的 Copy Buffer，允许客户端发送更大的数据包
        CoreModuleProperties.MAX_PACKET_SIZE.set(server, 65536L);

        // 配置主机密钥
        server.setKeyPairProvider(resourceKeyPairProvider);
        SftpSubsystemFactory factory;
        // 设置监听器
        if (customerOptAuditRecord) {
            factory = new AuditSftpSubsystemFactory();
        } else {
            factory = new SftpSubsystemFactory();
        }
        // 设置不支持属性打印日志
        factory.setUnsupportedAttributePolicy(UnsupportedAttributePolicy.Warn);
        server.setSubsystemFactories(Collections.singletonList(factory));
        // 配置密码认证器
        server.setPasswordAuthenticator(passwordAuthenticator);
        // 配置秘钥认证器
        server.setPublickeyAuthenticator(sftpPublicKeyAuthenticator);
        // 设置文件系统根目录
        server.setFileSystemFactory(new DynamicVirtualFileSystemFactory());
        // 设置session监听器
        server.addSessionListener(sftpSessionListener);
        server.start();
        return server;
    }


}

