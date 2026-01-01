package com.gengzi.sftp.config;


import com.gengzi.sftp.s3.client.S3ClientFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class S3Config {


    @PreDestroy
    public void closeAll() {
        S3ClientFactory.closeClient();
    }

}
