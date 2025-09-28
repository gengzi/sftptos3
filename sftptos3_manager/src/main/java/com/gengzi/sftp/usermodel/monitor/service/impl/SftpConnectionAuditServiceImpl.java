package com.gengzi.sftp.usermodel.monitor.service.impl;


import com.gengzi.sftp.usermodel.dao.audit.SftpConnectionAudit;
import com.gengzi.sftp.usermodel.dao.audit.SftpConnectionAuditRepository;
import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord;
import com.gengzi.sftp.usermodel.enums.AuthFailureReason;
import com.gengzi.sftp.usermodel.enums.AuthStatus;
import com.gengzi.sftp.usermodel.monitor.service.SftpConnectionAuditService;
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
                predicates.add(cb.like(root.get("username"), username + "%"));
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


        Page<SftpConnectionAudit> sftpConnectionAudits = sftpConnectionAuditRepository.findAll(spec, pageable);

        sftpConnectionAudits.stream().forEach(audit -> {
            AuthFailureReason authFailureReasonByReasonKey = AuthFailureReason.getAuthFailureReasonByReasonKey(audit.getAuthFailureReason());
            if (authFailureReasonByReasonKey == null) {
                return;
            }
            audit.setAuthFailureReason(authFailureReasonByReasonKey.getReason());
        });
        return sftpConnectionAudits;
    }

    /**
     * 获取时间范围内的文件操作信息
     *
     * @param record
     * @return
     */
    @Override
    public StatisticsRecord statistics(StatisticsRecord record) {
        LocalDateTime startTime = record.getStartTime();
        LocalDateTime endTime = record.getEndTime();
        List<String> list = sftpConnectionAuditRepository.findAuthStatusByCreateTimeBetween(startTime, endTime);
        record.setAuthCountVal((long) list.size());
        record.setAuthSuccessVal(0L);
        record.setAuthFailureVal(0L);
        for (String auditStatus : list) {
            AuthStatus authStatusByStatus = AuthStatus.getAuthStatusByStatus(Byte.valueOf(auditStatus));
            switch (authStatusByStatus) {
                case AUTH_SUCCESS:
                    record.setAuthSuccessVal(record.getAuthSuccessVal() + 1);
                    break;
                case AUTH_FAIL:
                    record.setAuthFailureVal(record.getAuthFailureVal() + 1);
                    break;
                case NO_AUTH:
                    record.setAuthFailureVal(record.getAuthFailureVal() + 1);
                    break;
                default:
                    break;
            }
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clientClose(String id) {
        sftpConnectionAuditRepository.updateMultiCloseEventById(Long.parseLong(id));
    }
}
