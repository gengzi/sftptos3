package com.gengzi.sftp.config;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.dao.User;
import com.gengzi.sftp.dao.UserRepository;
import com.gengzi.sftp.enums.AuthFailureReason;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import com.gengzi.sftp.util.PasswordEncoderUtil;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户认证
 */
@Component
public class SftpUserPasswordAuthenticator implements PasswordAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SftpUserPasswordAuthenticator.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServerSession userServerSession;

    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;

    @Override
    public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException, AsyncAuthException {
        Long attributeId = serverSession.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
        User userByUsername = userRepository.findUserByUsername(username);
        if(userByUsername == null){
            sftpConnectionAuditService.authFailReasonEvent(attributeId,username, AuthFailureReason.SYS_NO_SUCH_USER.getReasonKey());
            return false;
        }
        if(!PasswordEncoderUtil.matchesPassword(password, userByUsername.getPasswd())){
            sftpConnectionAuditService.authFailReasonEvent(attributeId,username, AuthFailureReason.SYS_NO_SUCH_USER.getReasonKey());
            return false;
        }
       return userServerSession.addUserInfoToServerSession(userByUsername,serverSession);
    }



}
