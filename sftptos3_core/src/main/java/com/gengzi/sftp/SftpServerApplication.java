package com.gengzi.sftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// 开启定时任务
@EnableScheduling
public class SftpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SftpServerApplication.class, args);
    }
}