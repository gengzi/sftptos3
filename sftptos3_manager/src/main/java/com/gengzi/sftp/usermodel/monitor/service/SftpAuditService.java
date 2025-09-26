package com.gengzi.sftp.usermodel.monitor.service;


import com.gengzi.sftp.usermodel.dao.audit.SftpAudit;
import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord;
import com.gengzi.sftp.usermodel.response.StatisticsRecordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户操作审计service
 *
 */
public interface SftpAuditService {


    /**
     * list
     *
     * @param clientName
     * @param pageable
     * @return
     */
    Page<SftpAudit> list(String clientName, Pageable pageable);

    /**
     * 获取时间范围内的文件操作信息
     * @return
     */
    StatisticsRecord statistics(StatisticsRecord record);

}
