package com.gengzi.sftp.usermodel.controller;


import com.gengzi.sftp.usermodel.dto.AdminInfoRequest;
import com.gengzi.sftp.usermodel.dto.AdminInfoUpdateRequest;
import com.gengzi.sftp.usermodel.response.Result;
import com.gengzi.sftp.usermodel.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员管理", description = "管理员管理")
public class AdminController {


    @Autowired
    private AdminService adminService;


    @GetMapping("/list")
    @ResponseBody
    public Result<?> list(@RequestParam(required = false) String username,
                          @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return Result.success(adminService.list(username, pageable));
    }

    @PostMapping("/create")
    @ResponseBody
    public Result<?> createUser(@Valid @RequestBody AdminInfoRequest adminInfoRequest) {
        adminService.createUser(adminInfoRequest);
        return Result.success(null);
    }

    @PostMapping("/update")
    @ResponseBody
    public Result<?> updateUser(@Valid @RequestBody AdminInfoUpdateRequest adminInfoUpdateRequest) {
        adminService.updateUser(adminInfoUpdateRequest);
        return Result.success(null);
    }


    @PostMapping("/remove")
    @ResponseBody
    public Result<?> removeUser(@Valid @RequestParam Long id) {
        adminService.removeUser(id);
        return Result.success(null);
    }






}
