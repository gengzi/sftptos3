package com.gengzi.sftp.usermodel.monitor.controller;


import com.gengzi.sftp.usermodel.monitor.service.SftpAuditService;
import com.gengzi.sftp.usermodel.monitor.service.SftpConnectionAuditService;
import com.gengzi.sftp.usermodel.monitor.service.StatisticsRecordService;
import com.gengzi.sftp.usermodel.response.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "审计", description = "审计")
public class AuditController {


    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;

    @Autowired
    private SftpAuditService sftpAuditService;

    @Autowired
    private StatisticsRecordService statisticsRecordService;

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
    public Result<?> clientList(@RequestParam(required = false) String username,
                          @PageableDefault(page = 0, size = 10 , sort = "createTime", direction = Sort.Direction.DESC ) Pageable pageable) {
        return Result.success(sftpConnectionAuditService.list(username, pageable));
    }

    @GetMapping("/opt/list")
    @ResponseBody
    public Result<?> optList(@RequestParam(required = false) String clientName,
                          @PageableDefault(page = 0, size = 10 , sort = "createTime", direction = Sort.Direction.DESC ) Pageable pageable) {
        return Result.success(sftpAuditService.list(clientName, pageable));
    }


    @GetMapping("/statistics/now")
    @ResponseBody
    public Result<?> statisticsNow() {
        return Result.success(statisticsRecordService.statisticsNow());
    }

    @GetMapping("/statistics/traffic")
    @ResponseBody
    public Result<?> statisticsTraffic(String timeType) {
        return Result.success(statisticsRecordService.statisticsTraffic(timeType));
    }



}
