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

    @GetMapping("/client/list")
    @ResponseBody
    public Result<?> clientList(@RequestParam(required = false) String username,
                          @PageableDefault(page = 0, size = 10 , sort = "createTime", direction = Sort.Direction.DESC ) Pageable pageable) {
        return Result.success(sftpConnectionAuditService.list(username, pageable));
    }

    @PostMapping("/client/close")
    @ResponseBody
    public Result<?> clientClose(@RequestParam(required = true) String id) {
        sftpConnectionAuditService.clientClose(id);
        return Result.success(null);
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
