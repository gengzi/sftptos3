package com.gengzi.sftp.enums;


public enum ManuallyCloseStatus {

    YES(Byte.valueOf("1")),
    NO(Byte.valueOf("2")),;

    private Byte status;

    public Byte getStatus() {
        return status;
    }

    ManuallyCloseStatus(Byte status) {
        this.status = status;
    }
}
