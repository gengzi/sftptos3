package com.gengzi.sftp.monitor.service.impl;

import com.gengzi.sftp.dao.SftpAuditRepository;
import com.gengzi.sftp.dao.SftpConnectionAudit;
import com.gengzi.sftp.dao.SftpConnectionAuditRepository;
import com.gengzi.sftp.enums.AuthStatus;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SftpConnectionAuditServiceImpl implements SftpConnectionAuditService {


    @Autowired
    private SftpConnectionAuditRepository sftpConnectionAuditRepository;

    @Autowired
    private SftpAuditRepository sftpAuditRepository;
    /**
     * 更新审计表：认证失败事件
     *
     * @param id                主键
     * @param username          用户名
     * @param authFailureReason 认证失败原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void authFailReasonEvent(Long id, String username, String authFailureReason) {
        sftpConnectionAuditRepository.updateAuthFailReasonEventById(username,
                AuthStatus.AUTH_FAIL.getStatus(), authFailureReason, id);
    }

    /**
     * 更新审计表：认证成功事件
     *
     * @param id       主键
     * @param username 用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void authSuccessEvent(Long id, String username) {
        sftpConnectionAuditRepository.updateAuthSuccessEventById(username,
                AuthStatus.AUTH_SUCCESS.getStatus(), id);
    }

    /**
     * 标记会话进入 “安全数据传输阶段”，下一步进入认证用户阶段
     *
     * @param sftpConnectionAudit
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long KeyEstablishedEvent(SftpConnectionAudit sftpConnectionAudit) {
        sftpConnectionAudit.setUsername("");
        sftpConnectionAudit.setConnectTime(LocalDateTime.now());
        sftpConnectionAudit.setCreateTime(LocalDateTime.now());
        sftpConnectionAudit.setAuthStatus(AuthStatus.NO_AUTH.getStatus());
        SftpConnectionAudit save = sftpConnectionAuditRepository.save(sftpConnectionAudit);
        return save.getId();
    }

    /**
     * 断开会话
     *
     * @param id               主键
     * @param throwable 断开原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sessionClosedEvent(Long id, Throwable throwable) {
        if (throwable != null) {
            sftpConnectionAuditRepository.updateSessionClosedEventById(LocalDateTime.now(), throwable.getMessage(), id);
        }
        sftpConnectionAuditRepository.updateSessionClosedEventById(LocalDateTime.now(), "Normal shutdown", id);
    }
}
