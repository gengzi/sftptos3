package com.gengzi.sftp.config;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.context.ServerSessionUserInfoContext;
import com.gengzi.sftp.dao.S3Storage;
import com.gengzi.sftp.dao.S3StorageRepository;
import com.gengzi.sftp.dao.User;
import com.gengzi.sftp.dao.UserRepository;
import com.gengzi.sftp.enums.StorageTypeEnum;
import com.gengzi.sftp.util.PasswordEncoderUtil;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    @Override
    public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException, AsyncAuthException {
        User userByUsername = userRepository.findUserByUsername(username);
        if(userByUsername == null){
            return false;
        }
        if(!PasswordEncoderUtil.matchesPassword(password, userByUsername.getPasswd())){
            return false;
        }
       return userServerSession.addUserInfoToServerSession(userByUsername,serverSession);
    }



}
