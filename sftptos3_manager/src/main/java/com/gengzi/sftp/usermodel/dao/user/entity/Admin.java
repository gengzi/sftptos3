package com.gengzi.sftp.usermodel.dao.user.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 管理员
 */
@Getter
@Setter
@Entity
@Data
@Table(name = "admin", schema = "sftptos3")
public class Admin {
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
    @Column(name = "creater")
    private Long creater;

    /**
     * 创建时间
     */
    @NotNull
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

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
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    @Size(max = 500)
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 管理员username
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "username", nullable = false, length = 128)
    private String username;

    /**
     * 管理员passwd
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "passwd", nullable = false, length = 128)
    private String passwd;

}