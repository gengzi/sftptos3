package com.gengzi.sftp.monitor.service;

import com.gengzi.sftp.dao.SftpConnectionAudit;

/**
 * 审计sftp链接信息
 */
public interface SftpConnectionAuditService {


    /**
     * 更新审计表：认证失败事件
     *
     * @param id                主键
     * @param username          用户名
     * @param authFailureReason 认证失败原因
     */
    void authFailReasonEvent(Long id, String username, String authFailureReason);


    /**
     * 更新审计表：认证成功事件
     *
     * @param id       主键
     * @param username 用户名
     */
    void authSuccessEvent(Long id, String username);


    /**
     * 标记会话进入 “安全数据传输阶段”，下一步进入认证用户阶段
     *
     * @param sftpConnectionAudit
     * @return 数据库主键
     */
    Long KeyEstablishedEvent(SftpConnectionAudit sftpConnectionAudit);


    /**
     * 断开会话
     * @param id 主键
     * @param disconnectReason 断开原因
     */
    void sessionClosedEvent(Long id, String disconnectReason);

}
