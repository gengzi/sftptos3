package com.gengzi.sftp.enums;

/**
 * 认证状态
 */
public enum AuthStatus {

    NO_AUTH(Byte.valueOf("0")),
    AUTH_SUCCESS(Byte.valueOf("1")),
    AUTH_FAIL(Byte.valueOf("2"));

    private Byte status;

    public Byte getStatus() {
        return status;
    }

    AuthStatus(Byte status) {
        this.status = status;
    }
}
