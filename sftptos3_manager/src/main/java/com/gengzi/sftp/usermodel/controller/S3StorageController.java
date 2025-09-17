package com.gengzi.sftp.usermodel.controller;


import com.gengzi.sftp.usermodel.dto.S3StorageRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import com.gengzi.sftp.usermodel.response.Result;
import com.gengzi.sftp.usermodel.service.S3StorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/s3/storage")
@Tag(name = "s3存储管理", description = "s3存储管理")
public class S3StorageController {


    @Autowired
    private S3StorageService storageService;

    @PostMapping("/create")
    @ResponseBody
    public Result<?> create(@Valid @RequestBody S3StorageRequest s3StorageRequest) {
        storageService.createS3Storage(s3StorageRequest);
        return Result.success(null);
    }


    /**
     * 获取现在已存在的s3存储信息名称
     */
    @PostMapping("/get/s3names")
    @ResponseBody
    public Result<?> s3names() {
        return Result.success(storageService.s3names());
    }

    @GetMapping("/list")
    @ResponseBody
    public Result<?> list(@RequestParam(required = false) String s3Name,
                          @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return Result.success(storageService.list(s3Name, pageable));
    }


}
