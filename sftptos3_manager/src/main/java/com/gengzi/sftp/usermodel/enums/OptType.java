package com.gengzi.sftp.usermodel.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final static Map<String, OptType> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(k -> k.type, OptType -> OptType));


    private String type;

    OptType(String type) {
        this.type = type;
    }

    public static OptType getOptTypeByType(String type) {
        return MAP.get(type);
    }

    public String getType() {
        return type;
    }
}
