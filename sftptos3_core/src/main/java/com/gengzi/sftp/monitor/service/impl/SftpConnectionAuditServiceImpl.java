package com.gengzi.sftp.monitor.service.impl;

import com.gengzi.sftp.dao.SftpConnectionAudit;
import com.gengzi.sftp.dao.SftpConnectionAuditRepository;
import com.gengzi.sftp.enums.AuthStatus;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SftpConnectionAuditServiceImpl implements SftpConnectionAuditService {


    @Autowired
    private SftpConnectionAuditRepository sftpConnectionAuditRepository;

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
     * @param disconnectReason 断开原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sessionClosedEvent(Long id, String disconnectReason) {
        sftpConnectionAuditRepository.updateSessionClosedEventById(LocalDateTime.now(), disconnectReason, id);
    }

    /**
     * 获取分页列表
     *
     * @param username
     * @param pageable
     * @return
     */
    @Override
    public Page<SftpConnectionAudit> list(String username, Pageable pageable) {
        Specification<SftpConnectionAudit> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 条件1：名称模糊匹配（如果name不为空）
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"),  username + "%"));
            }
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 计算7天前的时间
            LocalDateTime sevenDaysAgo = now.minusDays(7);
            predicates.add(cb.between(root.get("createTime"), sevenDaysAgo, now));

            // 添加排序条件
            query.orderBy(cb.desc(root.get("createTime")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };


        return sftpConnectionAuditRepository.findAll(spec,pageable);

    }
}
