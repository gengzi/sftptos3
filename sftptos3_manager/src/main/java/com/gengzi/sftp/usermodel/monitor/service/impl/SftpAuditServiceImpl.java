package com.gengzi.sftp.usermodel.monitor.service.impl;

import com.gengzi.sftp.usermodel.dao.audit.SftpAudit;
import com.gengzi.sftp.usermodel.dao.audit.SftpAuditRepository;
import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord;
import com.gengzi.sftp.usermodel.dto.SftpAuditDto;
import com.gengzi.sftp.usermodel.enums.OperateStatus;
import com.gengzi.sftp.usermodel.enums.OptType;
import com.gengzi.sftp.usermodel.monitor.service.SftpAuditService;
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
        List<SftpAuditDto> optTypeResultBetween = sftpAuditRepository.findOptTypeResultBetween(startTime, endTime);
        record.setUploadCountVal(0L);
        record.setUploadSuccessVal(0L);
        record.setUploadFailureVal(0L);
        record.setDownloadCountVal(0L);
        record.setDownloadSuccessVal(0L);
        record.setDownloadFailureVal(0L);
        record.setDelCountVal(0L);
        record.setDelSuccessVal(0L);
        record.setDelFailureVal(0L);
        record.setUploadSize("0");
        record.setDownloadSize("0");
        for (SftpAuditDto audit : optTypeResultBetween) {
            OptType optTypeByType = OptType.getOptTypeByType(audit.getType());
            switch (optTypeByType) {
                case UPLOAD:
                    record.setUploadCountVal(record.getUploadCountVal() + 1);
                    if (OperateStatus.SUCCESS.getStatus().equals(audit.getOperateResult())) {
                        record.setUploadSuccessVal(record.getUploadSuccessVal() + 1);
                        record.setUploadSize(String.valueOf(Long.valueOf(record.getUploadSize()) + Long.valueOf("".equals(audit.getFileSize()) ? "0" : audit.getFileSize())));
                    } else {
                        record.setUploadFailureVal(record.getUploadFailureVal() + 1);
                    }
                    break;
                case DOWNLOAD:
                    record.setDownloadCountVal(record.getDownloadCountVal() + 1);
                    if (OperateStatus.SUCCESS.getStatus().equals(audit.getOperateResult())) {
                        record.setDownloadSuccessVal(record.getDownloadSuccessVal() + 1);
                        record.setDownloadSize(String.valueOf(Long.valueOf(record.getDownloadSize()) + Long.valueOf("".equals(audit.getFileSize()) ? "0" : audit.getFileSize())));
                    } else {
                        record.setDownloadFailureVal(record.getDownloadFailureVal() + 1);
                    }
                    break;
                case DELETE_FILE:
                    record.setDelCountVal(record.getDelCountVal() + 1);
                    if (OperateStatus.SUCCESS.getStatus().equals(audit.getOperateResult())) {
                        record.setDelSuccessVal(record.getDelSuccessVal() + 1);
                    } else {
                        record.setDelFailureVal(record.getDelFailureVal() + 1);
                    }
                case DELETE_DIR:
                    record.setDelCountVal(record.getDelCountVal() + 1);
                    if (OperateStatus.SUCCESS.getStatus().equals(audit.getOperateResult())) {
                        record.setDelSuccessVal(record.getDelSuccessVal() + 1);
                    } else {
                        record.setDelFailureVal(record.getDelFailureVal() + 1);
                    }
                    break;
                default:
                    break;
            }
        }
        return record;
    }
}
