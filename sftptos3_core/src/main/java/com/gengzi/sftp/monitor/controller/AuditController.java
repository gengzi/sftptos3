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

    /**
     * 1, 登录数，认证成功，认证失败
     * 2，下载文件数，下载成功，下载失败
     * 3，上传文件数，上传成功，上传失败
     * 4，删除文件数，删除成功，删除失败
     * 5，创建目录数，创建成功，创建失败
     *
     * 流量：一小时统计一次 上传，下载，总流量
     */


    @GetMapping("/client/list")
    @ResponseBody
    public Result<?> list(@RequestParam(required = false) String username,
                          @PageableDefault(page = 0, size = 10 , sort = "createTime", direction = Sort.Direction.DESC ) Pageable pageable) {
        return Result.success(sftpConnectionAuditService.list(username, pageable));
    }






}
