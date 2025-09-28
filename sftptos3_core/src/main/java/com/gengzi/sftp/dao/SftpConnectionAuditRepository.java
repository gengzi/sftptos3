package com.gengzi.sftp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface SftpConnectionAuditRepository extends JpaRepository<SftpConnectionAudit, Long>, JpaSpecificationExecutor<SftpConnectionAudit> {


    @Query("update SftpConnectionAudit s set s.username= :username ,s.authStatus = :authStatus,s.authType = :authType  where s.id = :id")
    @Modifying
    int updateAuthSuccessEventById(@Size(max = 64) @NotNull String username, Byte authStatus, String authType, Long id);

    @Query("update SftpConnectionAudit s set s.username= :username ,s.authStatus = :authStatus,s.authFailureReason = :authFailureReason,s.authType = :authType  where s.id = :id")
    @Modifying
    int updateAuthFailReasonEventById(@Size(max = 64) @NotNull String username, Byte authStatus, String authFailureReason, String authType, Long id);

    @Query("update SftpConnectionAudit s set s.disconnectTime= :disconnectTime ,s.disconnectReason = :disconnectReason  where s.id = :id")
    @Modifying
    int updateSessionClosedEventById(@Size(max = 64) @NotNull LocalDateTime disconnectTime, String disconnectReason, Long id);

    @Query("select s.id from SftpConnectionAudit s where s.manuallyCloseClient = :manuallyCloseClient and s.id in(:ids)")
    Set<Long> findIdByIdsAndManuallyCloseClient(Set<Long> ids, Byte manuallyCloseClient);
}