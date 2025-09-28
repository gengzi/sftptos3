package com.gengzi.sftp.enums;

/**
 * 操作状态
 */
public enum AuthType {


    PASSWD("passwd"),
    PUBLIC_KEY("publicKey"),
    ;

    private String type;

    AuthType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
