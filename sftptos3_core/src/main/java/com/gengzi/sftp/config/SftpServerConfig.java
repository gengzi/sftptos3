package com.gengzi.sftp.config;


import com.gengzi.sftp.factory.DynamicVirtualFileSystemFactory;
import com.gengzi.sftp.listener.FileWriteListener;
import com.gengzi.sftp.listener.SftptoS3SftpEventListener;
import com.gengzi.sftp.util.ResourceUtils;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.sftp.server.UnsupportedAttributePolicy;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResourceAccessException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;


/**
 * 配置sftp相关配置项
 */
@Configuration
public class SftpServerConfig {

    private static FileKeyPairProvider fileKeyPairProvider;

    static {
        try {
            File file = ResourceUtils.readResource("hostkey.ser");
            if (file == null) {
                throw new ResourceAccessException("hostkey.ser not found");
            }
            fileKeyPairProvider = new FileKeyPairProvider(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${sftp.server.port}")
    private int sftpPort;
    @Autowired
    private FileWriteListener fileWriteListener;
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
        // 监听所有请求，默认支持
        // server.setHost("0.0.0.0");
        server.setPort(sftpPort);
        // 配置主机密钥
        server.setKeyPairProvider(fileKeyPairProvider);
        SftpSubsystemFactory factory = new SftpSubsystemFactory();
        // 设置监听器
        factory.addSftpEventListener(sftpEventListener);
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

