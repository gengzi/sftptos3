package com.gengzi.sftp.config;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.context.ServerSessionUserInfoContext;
import com.gengzi.sftp.dao.S3Storage;
import com.gengzi.sftp.dao.S3StorageRepository;
import com.gengzi.sftp.dao.User;
import com.gengzi.sftp.dao.UserRepository;
import com.gengzi.sftp.enums.StorageTypeEnum;
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
import java.util.Optional;

@Component
public class SftpPublicKeyAuthenticator implements PublickeyAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SftpPublicKeyAuthenticator.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3StorageRepository storageRepository;

    @Autowired
    private UserServerSession userServerSession;

    @Override
    public boolean authenticate(String username, PublicKey publicKey, ServerSession serverSession) throws AsyncAuthException {
        User userByUsername = userRepository.findUserByUsername(username);
        if(userByUsername == null){
            return false;
        }
        String keyData = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDz7mxtNKEHWE8Hq0vc2f90aIoT/mxfNDVRhdBya4FYAS28dbU1QD7v4lvzp7Ga/XvXnZCCTsHnO0puLKuzH8SBXLkMKGNQggtIkAGhKp/aykI9IbHqXE5ImnIcNYFVzGUIdhYvJVBRgUV8PS7pYeBzf5QiPQCwoIpFJsEe7ow5LCk6np2ZRVIgXvJWR1NIfSL3mFnSEHKcv+SUrvjbY5ezcKk4FFs7WbfcURpI7KAKjFiKqoH2V7KJRoTjXo6qID9H95rd6feBtKLiM5bI7bO4tJNQWfnzoGN9sAmxgWuhUhsbPR1/khJuKEXzZKxTP/zJ0t7V4kQbHfw9etZmwpHF gengzi@qq.com\n";
        // 从数据库读取公钥字符串并解析为 AuthorizedKeyEntry
        AuthorizedKeyEntry entry = AuthorizedKeyEntry.parseAuthorizedKeyEntry(keyData);
        // 使用指定的解析器将 entry 转换为 PublicKey
        try {
            PublicKey userPublicKey = entry.resolvePublicKey(serverSession, PublicKeyEntryResolver.IGNORING);
            if(!publicKey.equals(userPublicKey)){
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
       return userServerSession.addUserInfoToServerSession(userByUsername,serverSession);
    }

}
