package com.gengzi.sftp.usermodel.controller;


import com.gengzi.sftp.usermodel.dto.UserInfoRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import com.gengzi.sftp.usermodel.response.Result;
import com.gengzi.sftp.usermodel.response.ResultCode;
import com.gengzi.sftp.usermodel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ResponseBody
    public Result<?> authenticateUser(@Valid @RequestBody UserLoginRequest loginRequest) {
        JwtResponse longing = userService.longing(loginRequest);
        return Result.success(longing);
    }

    @PostMapping("/create")
    @ResponseBody
    public Result<?> createUser(@Valid @RequestBody UserInfoRequest userInfoRequest) {
        userService.createUser(userInfoRequest);
        return Result.success(null);
    }

    @PostMapping("/update")
    @ResponseBody
    public Result<?> updateUser(@Valid @RequestBody UserInfoRequest userInfoRequest) {

        return Result.success(null);
    }


    @PostMapping("/remove")
    @ResponseBody
    public Result<?> removeUser(@Valid @RequestBody Object loginRequest) {

        return Result.success(null);
    }






}
