package com.gengzi.sftp.enums;


public enum StorageTypeEnum {

    S3("s3"),
    LOCAL("local"),
    ;

    private String type;

    StorageTypeEnum(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

}
