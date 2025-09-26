package com.gengzi.sftp.usermodel.monitor.service;


import com.gengzi.sftp.usermodel.dao.audit.SftpConnectionAudit;
import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计sftp链接信息
 */
public interface SftpConnectionAuditService {


    /**
     * 获取分页列表
     * @param username
     * @param pageable
     * @return
     */
    Page<SftpConnectionAudit> list(String username, Pageable pageable);


    /**
     * 获取时间范围内的文件操作信息
     * @return
     */
    StatisticsRecord statistics(StatisticsRecord record);



}
