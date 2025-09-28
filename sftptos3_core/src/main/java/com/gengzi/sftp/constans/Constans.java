package com.gengzi.sftp.constans;

import com.gengzi.sftp.context.ServerSessionUserInfoContext;
import org.apache.sshd.common.AttributeRepository;


public class Constans {

    public static final AttributeRepository.AttributeKey<ServerSessionUserInfoContext> SERVERSESSIONUSERINFOCONTEXT =
           new AttributeRepository.AttributeKey<>();
    public static final String SERVERSESSIONUSERINFOCONTEXT_STR = "serverSessionUserInfoContext";

    public static final AttributeRepository.AttributeKey<Long> SERVERSESSION_DB_IDKEY =
            new AttributeRepository.AttributeKey<>();
    public static final AttributeRepository.AttributeKey<Throwable> SERVERSESSION_THROWABLE =
            new AttributeRepository.AttributeKey<>();
    public static final AttributeRepository.AttributeKey<Boolean> DOWNLOADFILEUSERDIRECTBUFFER =
            new AttributeRepository.AttributeKey<>();

}
