package com.gengzi.sftp.usermodel.enums;

/**
 * 任务执行操作状态
 */
public enum OperateStatus {

    NOT_EXEC(Byte.valueOf("4")),
    PROCESS(Byte.valueOf("3")),
    SUCCESS(Byte.valueOf("1")),
    FAILURE(Byte.valueOf("2"));

    private Byte status;

    OperateStatus(Byte status) {
        this.status = status;
    }

    public Byte getStatus() {
        return status;
    }
}
