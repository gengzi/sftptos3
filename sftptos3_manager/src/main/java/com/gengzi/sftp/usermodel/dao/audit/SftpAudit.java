package com.gengzi.sftp.usermodel.dao.audit;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * sftp审计表：记录客户端操作行为
 */
@Getter
@Setter
@Entity
@Data
@Table(name = "sftp_audit", schema = "sftptos3")
public class SftpAudit {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 创建时间
     */
    @NotNull
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 备注信息
     */
    @Size(max = 1024)
    @Column(name = "remark", length = 1024)
    private String remark;

    /**
     * 客户端网络地址
     */
    @Size(max = 256)
    @NotNull
    @Column(name = "client_address", nullable = false, length = 256)
    private String clientAddress;

    /**
     * 客户端用户名
     */
    @Size(max = 256)
    @NotNull
    @Column(name = "client_username", nullable = false, length = 256)
    private String clientUsername;

    /**
     * 文件路径
     */
    @Size(max = 1024)
    @NotNull
    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    /**
     * 对文件操作类型：UPLOAD（上传）、DOWNLOAD（下载）、DELETE（删除）、RENAME（重命名）
     */
    @Size(max = 32)
    @NotNull
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    /**
     * 文件存储信息：本地文件（local），s3文件（对应的 用户名:请求地址/桶）
     */
    @Size(max = 1024)
    @NotNull
    @Column(name = "file_stroage_info", nullable = false, length = 1024)
    private String fileStroageInfo;

    /**
     * 操作时间
     */
    @NotNull
    @Column(name = "opt_time", nullable = false)
    private LocalDateTime optTime;

    /**
     * 文件大小
     */
    @Size(max = 64)
    @NotNull
    @Column(name = "file_size", nullable = false, length = 64)
    private String fileSize;

    /**
     * 操作是否成功
     */
    @NotNull
    @Column(name = "operate_result", nullable = false)
    private Byte operateResult;

    /**
     * 错误信息
     */
    @Size(max = 512)
    @NotNull
    @Column(name = "error_msg", nullable = false, length = 512)
    private String errorMsg;

    /**
     * 客户端认证数据库表主键id
     */
    @NotNull
    @Column(name = "client_audit_id", nullable = false)
    private Long clientAuditId;

    /**
     * 完成时间
     */
    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    /**
     * 移动文件路径
     */
    @Size(max = 1024)
    @Column(name = "remove_file_path", length = 1024)
    private String removeFilePath;






}