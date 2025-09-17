package com.gengzi.sftp.usermodel.dao.s3.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * s3存储表：s3链接桶等信息
 */
@Getter
@Setter
@Entity
@Table(name = "s3_storage", schema = "sftptos3")
public class S3Storage {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 状态（0-禁用 1-正常）
     */
    @NotNull
    @Column(name = "status", nullable = false)
    private Boolean status = false;

    /**
     * 创建人ID
     */
    @Column(name = "creator")
    private Long creator;

    /**
     * 创建时间
     */
    @NotNull
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 更新人ID
     */
    @Column(name = "updater")
    private Long updater;

    /**
     * 更新时间
     */
    @NotNull
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    /**
     * 备注信息
     */
    @Size(max = 500)
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * s3桶
     */
    @Size(max = 64)
    @NotNull
    @Column(name = "bucket", nullable = false, length = 64)
    private String bucket;

    /**
     * s3存储访问的地址
     */
    @Size(max = 256)
    @NotNull
    @Column(name = "endpoint", nullable = false, length = 256)
    private String endpoint;

    /**
     * 账户
     */
    @Size(max = 256)
    @NotNull
    @Column(name = "access_key", nullable = false, length = 256)
    private String accessKey;

    /**
     * 秘钥
     */
    @Size(max = 256)
    @NotNull
    @Column(name = "access_secret", nullable = false, length = 256)
    private String accessSecret;

    /**
     * s3存储名称
     */
    @Size(max = 45)
    @NotNull
    @Column(name = "s3_name", nullable = false, length = 45)
    private String s3Name;

}