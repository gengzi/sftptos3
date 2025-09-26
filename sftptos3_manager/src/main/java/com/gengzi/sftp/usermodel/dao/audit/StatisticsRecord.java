package com.gengzi.sftp.usermodel.dao.audit;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 统计任务与结果综合表
 */
@Getter
@Setter
@Entity
@Table(name = "statistics_record", schema = "sftptos3")
public class StatisticsRecord {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 统计开始时间
     */
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 任务状态：0-未执行 1-执行中 2-已完成 3-失败
     */
    @NotNull
    @Column(name = "status", nullable = false)
    private Byte status;

    /**
     * 实际执行时间
     */
    @Column(name = "execute_time")
    private LocalDateTime executeTime;

    /**
     * 执行耗时(毫秒)
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * 指标1：总登录数
     */
    @NotNull
    @Column(name = "auth_count_val", nullable = false)
    private Long authCountVal;

    /**
     * 指标1：认证成功数
     */
    @NotNull
    @Column(name = "auth_success_val", nullable = false)
    private Long authSuccessVal;

    /**
     * 指标1：认证失败数
     */
    @NotNull
    @Column(name = "auth_failure_val", nullable = false)
    private Long authFailureVal;

    /**
     * 指标1：总登录数
     */
    @NotNull
    @Column(name = "download_count_val", nullable = false)
    private Long downloadCountVal;

    /**
     * 指标1：下载成功数
     */
    @NotNull
    @Column(name = "download_success_val", nullable = false)
    private Long downloadSuccessVal;

    /**
     * 指标1：下载失败数
     */
    @NotNull
    @Column(name = "download_failure_val", nullable = false)
    private Long downloadFailureVal;

    /**
     * 指标1：总登录数
     */
    @NotNull
    @Column(name = "upload_count_val", nullable = false)
    private Long uploadCountVal;

    /**
     * 指标1：下载成功数
     */
    @NotNull
    @Column(name = "upload_success_val", nullable = false)
    private Long uploadSuccessVal;

    /**
     * 指标1：下载失败数
     */
    @NotNull
    @Column(name = "upload_failure_val", nullable = false)
    private Long uploadFailureVal;

    /**
     * 指标1：总登录数
     */
    @NotNull
    @Column(name = "del_count_val", nullable = false)
    private Long delCountVal;

    /**
     * 指标1：下载成功数
     */
    @NotNull
    @Column(name = "del_success_val", nullable = false)
    private Long delSuccessVal;

    /**
     * 指标1：下载失败数
     */
    @NotNull
    @Column(name = "del_failure_val", nullable = false)
    private Long delFailureVal;

    /**
     * 备注信息
     */
    @Size(max = 255)
    @Column(name = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @NotNull
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 上传文件大小
     */
    @Size(max = 64)
    @NotNull
    @Column(name = "upload_size", nullable = false, length = 64)
    private String uploadSize;

    /**
     * 下载文件大小
     */
    @Size(max = 64)
    @NotNull
    @Column(name = "download_size", nullable = false, length = 64)
    private String downloadSize;

}