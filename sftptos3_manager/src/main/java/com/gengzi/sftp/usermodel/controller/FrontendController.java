package com.gengzi.sftp.usermodel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    // 匹配所有前端路由（如 /s3Management、/admin）
    @GetMapping({
            "/s3Management",
            "/admin",
            "/monitor",
            "/userManagement",
            "/user",
            "/user/login",
            "/adminManagement"
    })
    public String forwardToIndex() {
        return "forward:/index.html"; // 转发到静态资源中的 index.html
    }
}