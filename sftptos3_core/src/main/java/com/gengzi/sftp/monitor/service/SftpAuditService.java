package com.gengzi.sftp.monitor.service;

import com.gengzi.sftp.dao.SftpAudit;
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
}
