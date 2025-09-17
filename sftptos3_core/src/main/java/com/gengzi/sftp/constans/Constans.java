package com.gengzi.sftp.constans;

import com.gengzi.sftp.context.ServerSessionUserInfoContext;
import org.apache.sshd.common.AttributeRepository;


public class Constans {

    public static final AttributeRepository.AttributeKey<ServerSessionUserInfoContext> SERVERSESSIONUSERINFOCONTEXT =
           new AttributeRepository.AttributeKey<>();
    public static final String SERVERSESSIONUSERINFOCONTEXT_STR = "serverSessionUserInfoContext";
}
