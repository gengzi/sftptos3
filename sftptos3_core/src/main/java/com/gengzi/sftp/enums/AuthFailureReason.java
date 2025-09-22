package com.gengzi.sftp.enums;

/**
 * 认证失败原因
 */
public enum AuthFailureReason {

    SYS_NO_SUCH_USER("SYS_NO_SUCH_USER", "系统中不存在此用户"),
    NOT_CONFIGURED_CLIENT_PUBLIC_KEY("NOT_CONFIGURED_CLIENT_PUBLIC_KEY", "此用户未配置客户端公钥"),
    PUBLIC_KEY_FAILED_TO_MATCH("PUBLIC_KEY_FAILED_TO_MATCH", "此用户客户端公钥与系统中存放的客户端公钥匹配失败"),
    PASSWD_FAILED_TO_MATCH("PASSWD_FAILED_TO_MATCH", "此用户密码与系统中存放的用户密码匹配失败"),
    SYS_ERROR_S3_STORAGE_CONFIG("SYS_ERROR_S3_STORAGE_CONFIG", "系统错误S3存储配置错误"),
    ;

    private String reasonKey;
    private String reason;

    AuthFailureReason(String reasonKey, String reason) {
        this.reason = reason;
        this.reasonKey = reasonKey;
    }

    public String getReason() {
        return reason;
    }

    public String getReasonKey() {
        return reasonKey;
    }
}
