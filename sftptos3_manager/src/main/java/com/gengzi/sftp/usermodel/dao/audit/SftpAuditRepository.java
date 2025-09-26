package com.gengzi.sftp.usermodel.dao.audit;

import com.gengzi.sftp.usermodel.dto.SftpAuditDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SftpAuditRepository extends JpaRepository<SftpAudit, Long>, JpaSpecificationExecutor<SftpAudit> {

    @Query("update SftpAudit s set s.fileSize= :fileSize ,s.operateResult = :operateResult,s.errorMsg = :errorMsg,s.completionTime = :completionTime  where s.id = :sftpAuditDbId")
    @Modifying
    @Transactional
    int updateReadEvent(String fileSize, Byte operateResult, String errorMsg, LocalDateTime completionTime, long sftpAuditDbId);

    @Query("select new com.gengzi.sftp.usermodel.dto.SftpAuditDto( s.type,s.operateResult,s.fileSize)  from SftpAudit s where s.createTime between :startTime and :endTime")
    List<SftpAuditDto> findOptTypeResultBetween(LocalDateTime startTime, LocalDateTime endTime);
}