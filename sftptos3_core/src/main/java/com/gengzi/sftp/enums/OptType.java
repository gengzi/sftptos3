package com.gengzi.sftp.enums;

/**
 * 操作状态
 */
public enum OptType {


    UPLOAD("upload"),
    DOWNLOAD("download"),
    DELETE_FILE("delete_file"),
    DELETE_DIR("delete_dir"),
    RENAME("rename"),
    ;

    private String type;

    OptType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
