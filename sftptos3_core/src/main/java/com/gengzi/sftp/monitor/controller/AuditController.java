package com.gengzi.sftp.monitor.controller;



import com.gengzi.sftp.monitor.response.Result;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {


    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;



    @GetMapping("/list")
    @ResponseBody
    public Result<?> list(@RequestParam(required = false) String username,
                          @PageableDefault(page = 0, size = 10 , sort = "createTime", direction = Sort.Direction.DESC ) Pageable pageable) {
        return Result.success(sftpConnectionAuditService.list(username, pageable));
    }






}
