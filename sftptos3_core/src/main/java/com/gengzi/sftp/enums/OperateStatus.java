package com.gengzi.sftp.enums;

/**
 * 认证状态
 */
public enum OperateStatus {

    SUCCESS(Byte.valueOf("1")),
    FAILURE(Byte.valueOf("2")),
    PROCESS(Byte.valueOf("3")),
    PENDING(Byte.valueOf("4")),;

    private Byte status;

    public Byte getStatus() {
        return status;
    }

    OperateStatus(Byte status) {
        this.status = status;
    }
}
