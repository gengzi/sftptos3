package com.gengzi.sftp.config;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.dao.User;
import com.gengzi.sftp.dao.UserRepository;
import com.gengzi.sftp.enums.AuthFailureReason;
import com.gengzi.sftp.enums.AuthType;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

@Component
public class SftpPublicKeyAuthenticator implements PublickeyAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SftpPublicKeyAuthenticator.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServerSession userServerSession;

    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;

    @Override
    public boolean authenticate(String username, PublicKey publicKey, ServerSession serverSession) throws AsyncAuthException {
        Long attributeId = serverSession.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
        User userByUsername = userRepository.findUserByUsername(username);
        if (userByUsername == null) {
            sftpConnectionAuditService.authFailReasonEvent(attributeId, username, AuthFailureReason.SYS_NO_SUCH_USER.getReasonKey(), AuthType.PUBLIC_KEY.getType());
            return false;
        }

        String secretKey = userByUsername.getSecretKey();
        if (secretKey == null || "".equals(secretKey)) {
            sftpConnectionAuditService.authFailReasonEvent(attributeId, username, AuthFailureReason.NOT_CONFIGURED_CLIENT_PUBLIC_KEY.getReasonKey(), AuthType.PUBLIC_KEY.getType());
            return false;
        }

        // 从数据库读取公钥字符串并解析为 AuthorizedKeyEntry
        AuthorizedKeyEntry entry = AuthorizedKeyEntry.parseAuthorizedKeyEntry(secretKey);
        // 使用指定的解析器将 entry 转换为 PublicKey
        try {
            PublicKey userPublicKey = entry.resolvePublicKey(serverSession, PublicKeyEntryResolver.IGNORING);
            if (!publicKey.equals(userPublicKey)) {
                sftpConnectionAuditService.authFailReasonEvent(attributeId, username, AuthFailureReason.PUBLIC_KEY_FAILED_TO_MATCH.getReasonKey(), AuthType.PUBLIC_KEY.getType());
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        // 设置认证方式
        serverSession.setAttribute(Constans.AUTHTYPE, AuthType.PUBLIC_KEY);
        return userServerSession.addUserInfoToServerSession(userByUsername, serverSession);
    }

}
