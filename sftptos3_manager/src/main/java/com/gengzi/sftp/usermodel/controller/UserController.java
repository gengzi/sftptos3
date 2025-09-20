package com.gengzi.sftp.usermodel.controller;


import com.gengzi.sftp.usermodel.dto.UserInfoRequest;
import com.gengzi.sftp.usermodel.dto.UserInfoUpdateRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import com.gengzi.sftp.usermodel.response.Result;
import com.gengzi.sftp.usermodel.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户管理")
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ResponseBody
    public Result<?> authenticateUser(@Valid @RequestBody UserLoginRequest loginRequest) {
        JwtResponse longing = userService.longing(loginRequest);
        return Result.success(longing);
    }

    @GetMapping("/details")
    @ResponseBody
    public Result<?> details(@Valid @RequestParam Long id) {
        return Result.success(userService.details(id));
    }


    @GetMapping("/list")
    @ResponseBody
    public Result<?> list(@RequestParam(required = false) String username,
                          @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return Result.success(userService.list(username, pageable));
    }

    @PostMapping("/create")
    @ResponseBody
    public Result<?> createUser(@Valid @RequestBody UserInfoRequest userInfoRequest) {
        userService.createUser(userInfoRequest);
        return Result.success(null);
    }

    @PostMapping("/update")
    @ResponseBody
    public Result<?> updateUser(@Valid @RequestBody UserInfoUpdateRequest userInfoRequest) {
        userService.updateUser(userInfoRequest);
        return Result.success(null);
    }


    @PostMapping("/remove")
    @ResponseBody
    public Result<?> removeUser(@Valid @RequestParam Long id) {
        userService.removeUser(id);
        return Result.success(null);
    }






}
