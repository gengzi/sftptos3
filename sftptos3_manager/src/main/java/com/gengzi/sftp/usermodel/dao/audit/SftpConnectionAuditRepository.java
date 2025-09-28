package com.gengzi.sftp.usermodel.dao.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SftpConnectionAuditRepository extends JpaRepository<SftpConnectionAudit, Long>, JpaSpecificationExecutor<SftpConnectionAudit> {

    @Query("select s.authStatus from SftpConnectionAudit s where s.createTime between :startTime and :endTime")
    List<String> findAuthStatusByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query("update SftpConnectionAudit s set s.manuallyCloseClient= '1' where   s.disconnectTime = null and s.id= :id  ")
    @Modifying
    int updateMultiCloseEventById(Long id);
}