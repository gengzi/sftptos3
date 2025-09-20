package com.gengzi.sftp.usermodel.dao.user.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户名
     */
    @Column(name = "username", nullable = false, length = 128)
    private String username;

    /**
     * 密码
     */
    @Column(name = "passwd", nullable = false, length = 128)
    private String passwd;

    /**
     * 用户根目录
     */
    @Column(name = "user_root_path", nullable = false, length = 1024)
    private String userRootPath;

    /**
     * 访问存储的类型
     */
    @Column(name = "access_storage_type", nullable = false, length = 64)
    private String accessStorageType;

    /**
     * 访问存储的连接信息
     */
    @Column(name = "access_storage_info", length = 512)
    private String accessStorageInfo;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建人id
     */
    @Column(name = "creater", nullable = false, length = 128)
    private String creater;

    /**
     * 更新人id
     */
    @Column(name = "updater", nullable = false, length = 128)
    private String updater;

    /**
     * 备注
     */
    @Column(name = "remark", length = 128)
    private String remark;

    /**
     * 客户端公钥
     */
    @Lob
    @Column(name = "secret_key")
    private String secretKey;

}