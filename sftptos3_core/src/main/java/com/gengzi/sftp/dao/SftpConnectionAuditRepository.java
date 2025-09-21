package com.gengzi.sftp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Repository
public interface SftpConnectionAuditRepository extends JpaRepository<SftpConnectionAudit, Long>, JpaSpecificationExecutor<SftpConnectionAudit> {


    @Query("update SftpConnectionAudit s set s.username= :username ,s.authStatus = :authStatus  where s.id = :id")
    @Modifying
    int updateAuthSuccessEventById(@Size(max = 64) @NotNull String username, Byte authStatus, Long id);

    @Query("update SftpConnectionAudit s set s.username= :username ,s.authStatus = :authStatus,s.authFailureReason = :authFailureReason  where s.id = :id")
    @Modifying
    int updateAuthFailReasonEventById(@Size(max = 64) @NotNull String username, Byte authStatus, String authFailureReason, Long id);

    @Query("update SftpConnectionAudit s set s.disconnectTime= :disconnectTime ,s.disconnectReason = :disconnectReason  where s.id = :id")
    @Modifying
    int updateSessionClosedEventById(@Size(max = 64) @NotNull LocalDateTime disconnectTime, String disconnectReason, Long id);


}