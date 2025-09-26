package com.gengzi.sftp.usermodel.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证状态
 */
public enum AuthStatus {

    NO_AUTH(Byte.valueOf("0")),
    AUTH_SUCCESS(Byte.valueOf("1")),
    AUTH_FAIL(Byte.valueOf("2"));


    private final static Map<Byte, AuthStatus> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(k -> k.status, AuthStatus -> AuthStatus));


    private Byte status;

    AuthStatus(Byte status) {

        this.status = status;
    }

    public Byte getStatus() {
        return status;
    }

    public static AuthStatus getAuthStatusByStatus(Byte status) {
        return MAP.get(status);
    }


}
