package com.gengzi.sftp.config;


import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.context.ServerSessionUserInfoContext;
import com.gengzi.sftp.dao.S3Storage;
import com.gengzi.sftp.dao.S3StorageRepository;
import com.gengzi.sftp.dao.User;
import com.gengzi.sftp.enums.AuthFailureReason;
import com.gengzi.sftp.enums.StorageType;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServerSession {

    private static final Logger logger = LoggerFactory.getLogger(UserServerSession.class);

    @Autowired
    private S3StorageRepository storageRepository;


    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;

    public boolean addUserInfoToServerSession(User userByUsername, ServerSession serverSession){
        Long attributeId = serverSession.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
        String accessStorageType = userByUsername.getAccessStorageType();
        String accessStorageInfo = userByUsername.getAccessStorageInfo();
        if(StorageType.S3.type().equals(accessStorageType)){
            // 获取s3的存储信息
            Optional<S3Storage> storageOptional = storageRepository.findById(Long.valueOf(accessStorageInfo));
            if(storageOptional.isPresent()){
                S3Storage s3Storage = storageOptional.get();
                String s3SftpSchemeUri = s3SftpSchemeUri(s3Storage.getAccessKey(),
                        s3Storage.getAccessSecret(),
                        s3Storage.getEndpoint(),
                        s3Storage.getBucket());

                ServerSessionUserInfoContext serverSessionUserInfoContext = new ServerSessionUserInfoContext(userByUsername.getId(),
                        userByUsername.getUsername(),
                        userByUsername.getUserRootPath(),
                        userByUsername.getAccessStorageType(),
                        s3SftpSchemeUri,
                        s3Storage.getRegion()
                );
                serverSession.setAttribute(Constans.SERVERSESSIONUSERINFOCONTEXT,serverSessionUserInfoContext);
                return true;
            }else{
                logger.error("s3Storage is empty");
                sftpConnectionAuditService.authFailReasonEvent(attributeId,userByUsername.getUsername(), AuthFailureReason.SYS_NO_SUCH_USER.getReasonKey());
                return false;
            }
        }
        ServerSessionUserInfoContext serverSessionUserInfoContext = new ServerSessionUserInfoContext(userByUsername.getId(),
                userByUsername.getUsername(),
                userByUsername.getUserRootPath(),
                userByUsername.getAccessStorageType(),
                "",""
        );
        serverSession.setAttribute(Constans.SERVERSESSIONUSERINFOCONTEXT,serverSessionUserInfoContext);
        return true;
    }

    public static String s3SftpSchemeUri(String accessKey,String accessSecret,String endpoint,String bucket) {
        String endpointFormat = endpoint;
        if(endpoint.startsWith("http://") || endpoint.startsWith("https://")){
            String result = endpoint.replaceFirst("^https://", "");
            endpointFormat = result.replaceFirst("^http://", "");
        }
        return String.format("s3sftp://%s:%s@%s/%s", accessKey, accessSecret, endpointFormat, bucket);
    }


}
