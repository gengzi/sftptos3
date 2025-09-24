package com.gengzi.sftp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface SftpAuditRepository extends JpaRepository<SftpAudit, Long>, JpaSpecificationExecutor<SftpAudit> {

    @Query("update SftpAudit s set s.fileSize= :fileSize ,s.operateResult = :operateResult,s.errorMsg = :errorMsg,s.completionTime = :completionTime  where s.id = :sftpAuditDbId")
    @Modifying
    @Transactional
    int updateReadEvent(String fileSize, Byte operateResult, String errorMsg, LocalDateTime completionTime, long sftpAuditDbId);
}