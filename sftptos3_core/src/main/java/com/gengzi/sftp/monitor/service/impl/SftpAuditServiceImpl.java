package com.gengzi.sftp.monitor.service.impl;

import com.gengzi.sftp.dao.SftpAudit;
import com.gengzi.sftp.dao.SftpAuditRepository;
import com.gengzi.sftp.monitor.service.SftpAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SftpAuditServiceImpl implements SftpAuditService {

    @Autowired
    private SftpAuditRepository sftpAuditRepository;

    /**
     * list
     *
     * @param clientName
     * @param pageable
     * @return
     */
    @Override
    public Page<SftpAudit> list(String clientName, Pageable pageable) {
        Specification<SftpAudit> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 条件1：名称模糊匹配（如果name不为空）
            if (clientName != null && !clientName.isEmpty()) {
                predicates.add(cb.equal(root.get("clientUsername"), clientName));
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
        return sftpAuditRepository.findAll(spec, pageable);
    }
}
