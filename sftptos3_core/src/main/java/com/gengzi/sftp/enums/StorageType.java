package com.gengzi.sftp.enums;


public enum StorageType {

    S3("s3"),
    LOCAL("local"),
    ;

    private String type;

    StorageType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

}
