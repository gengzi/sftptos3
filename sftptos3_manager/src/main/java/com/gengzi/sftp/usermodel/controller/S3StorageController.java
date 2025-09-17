package com.gengzi.sftp.usermodel.controller;


import com.gengzi.sftp.usermodel.dto.S3StorageRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import com.gengzi.sftp.usermodel.response.Result;
import com.gengzi.sftp.usermodel.service.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/s3/storage")
public class S3StorageController {


    @Autowired
    private S3StorageService storageService;

    @PostMapping("/create")
    @ResponseBody
    public Result<?> authenticateUser(@Valid @RequestBody S3StorageRequest s3StorageRequest) {
        storageService.createS3Storage(s3StorageRequest);
        return Result.success(null);
    }

}
