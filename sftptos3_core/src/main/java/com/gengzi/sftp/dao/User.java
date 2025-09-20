package com.gengzi.sftp.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

/**
 * 用户表：管理访问sftp服务的用户信息
 */
@Getter
@Setter
@Entity
@Table(name = "user", schema = "sftptos3")
public class User {
    /**
     * 主键
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户名
     */
    @javax.validation.constraints.Size(max = 128)
    @javax.validation.constraints.NotNull
    @Column(name = "username", nullable = false, length = 128)
    private String username;

    /**
     * 密码
     */
    @javax.validation.constraints.Size(max = 128)
    @javax.validation.constraints.NotNull
    @Column(name = "passwd", nullable = false, length = 128)
    private String passwd;

    /**
     * 用户根目录
     */
    @javax.validation.constraints.Size(max = 1024)
    @javax.validation.constraints.NotNull
    @Column(name = "user_root_path", nullable = false, length = 1024)
    private String userRootPath;

    /**
     * 访问存储的类型
     */
    @javax.validation.constraints.Size(max = 64)
    @javax.validation.constraints.NotNull
    @Column(name = "access_storage_type", nullable = false, length = 64)
    private String accessStorageType;

    /**
     * 访问存储的连接信息
     */
    @javax.validation.constraints.Size(max = 512)
    @javax.validation.constraints.NotNull
    @Column(name = "access_storage_info", nullable = false, length = 512)
    private String accessStorageInfo;

    /**
     * 创建时间
     */
    @javax.validation.constraints.NotNull
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    /**
     * 更新时间
     */
    @javax.validation.constraints.NotNull
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    /**
     * 创建人id
     */
    @javax.validation.constraints.Size(max = 128)
    @javax.validation.constraints.NotNull
    @Column(name = "creater", nullable = false, length = 128)
    private String creater;

    /**
     * 更新人id
     */
    @javax.validation.constraints.Size(max = 128)
    @javax.validation.constraints.NotNull
    @Column(name = "updater", nullable = false, length = 128)
    private String updater;

    /**
     * 备注
     */
    @javax.validation.constraints.Size(max = 128)
    @Column(name = "remark", length = 128)
    private String remark;

    @Lob
    @Column(name = "secret_key")
    private String secretKey;

}