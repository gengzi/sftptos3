package com.gengzi.sftp.usermodel.scheduled;

import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord;
import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecordRepository;
import com.gengzi.sftp.usermodel.enums.OperateStatus;
import com.gengzi.sftp.usermodel.monitor.service.SftpAuditService;
import com.gengzi.sftp.usermodel.monitor.service.SftpConnectionAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 定时任务：定时跑批将审计表中的数据统计
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    @Autowired
    private final StatisticsRecordRepository recordRepository;

    @Autowired
    private SftpAuditService sftpAuditService;

    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;

    public ScheduledTasks(StatisticsRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    // 每10分钟执行一次
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processStatistics() {
        logger.info("--- scheduled processStatistics start ---");
        // 1. 获取最后一次成功执行的记录
        StatisticsRecord lastSuccessRecord = recordRepository.findTopByStatusOrderByEndTimeDesc(OperateStatus.SUCCESS.getStatus());

        // 2. 确定起始时间
        LocalDateTime startTime;
        if (lastSuccessRecord == null) {
            // 首次执行，从系统设定的初始时间开始（例如今天0点）
            startTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        } else {
            // 从上次结束时间开始
            startTime = lastSuccessRecord.getEndTime();
        }

        // 3. 计算当前需要处理的结束时间（不超过当前时间）
        LocalDateTime endTime = startTime.plusMinutes(10);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // 4. 处理所有未执行的时间段
        while (endTime.isBefore(now) || endTime.isEqual(now)) {
            // 检查该时间段是否已存在记录
            List<StatisticsRecord> existingRecords = recordRepository.findByStartTimeAndEndTime(startTime, endTime);

            if (existingRecords.isEmpty()) {
                // 创建新记录
                StatisticsRecord newRecord = createNewRecord(startTime, endTime);
                recordRepository.save(newRecord);

                // 执行统计任务
                executeStatistics(newRecord);
            } else {
                // 处理已存在但未成功执行的记录
                for (StatisticsRecord record : existingRecords) {
                    if (record.getStatus() != OperateStatus.SUCCESS.getStatus()) { // 不是已完成状态
                        executeStatistics(record);
                    }
                }
            }

            // 移动到下一个时间段
            startTime = endTime;
            endTime = startTime.plusMinutes(10);
        }
    }

    // 创建新的统计记录
    private StatisticsRecord createNewRecord(LocalDateTime startTime, LocalDateTime endTime) {
        StatisticsRecord record = new StatisticsRecord();
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setAuthCountVal(0L);
        record.setAuthSuccessVal(0L);
        record.setAuthFailureVal(0L);
        record.setDownloadCountVal(0L);
        record.setDownloadSuccessVal(0L);
        record.setDownloadFailureVal(0L);
        record.setUploadCountVal(0L);
        record.setUploadSuccessVal(0L);
        record.setUploadFailureVal(0L);
        record.setDelCountVal(0L);
        record.setDelSuccessVal(0L);
        record.setDelFailureVal(0L);
        record.setUploadSize("");
        record.setDownloadSize("");

        // 未执行
        record.setStatus(OperateStatus.NOT_EXEC.getStatus());
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    // 执行统计逻辑

    private void executeStatistics(StatisticsRecord record) {
        long startTime = System.currentTimeMillis();

        try {
            // 更新状态为执行中
            record.setStatus(OperateStatus.PROCESS.getStatus());
            record.setExecuteTime(LocalDateTime.now());
            recordRepository.save(record);


            performActualStatistics(record);

            // 统计完成，更新状态
            record.setStatus(OperateStatus.SUCCESS.getStatus()); // 已完成
            record.setDuration((int)(System.currentTimeMillis() - startTime));
            recordRepository.save(record);

        } catch (Exception e) {
            logger.error("统计异常: ", e);
            // 处理异常，更新状态为失败
            record.setStatus(OperateStatus.FAILURE.getStatus()); // 失败
            record.setRemark("统计失败: " + e.getMessage());
            record.setDuration((int)(System.currentTimeMillis() - startTime));
            recordRepository.save(record);
        }
    }

    // 实际的统计逻辑
    private void performActualStatistics(StatisticsRecord record) {
        // 获取客户端链接信息
        StatisticsRecord connectionRecord = sftpConnectionAuditService.statistics(record);
        // 获取文件操作信息
        sftpAuditService.statistics(connectionRecord);
    }



}
