package com.gengzi.sftp.listener;

import org.apache.sshd.sftp.server.AbstractSftpEventListenerAdapter;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class SftptoS3SftpEventListener extends AbstractSftpEventListenerAdapter {


}
