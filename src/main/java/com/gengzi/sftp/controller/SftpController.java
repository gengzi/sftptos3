package com.gengzi.sftp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SftpController {


    @GetMapping("/sftp")
    public String getSftpInfo(){
        return "sftp";
    }

}
