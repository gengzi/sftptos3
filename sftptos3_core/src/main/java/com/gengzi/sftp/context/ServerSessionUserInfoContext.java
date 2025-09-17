package com.gengzi.sftp.context;


public class ServerSessionUserInfoContext {

    private Long userId;

    private String username;

    private String userRootPath;

    private String s3SftpSchemeUri;

    private String accessStorageType;

    public ServerSessionUserInfoContext(Long userId, String username, String userRootPath,String accessStorageType, String s3SftpSchemeUri) {
        this.userId = userId;
        this.username = username;
        this.userRootPath = userRootPath;
        this.s3SftpSchemeUri = s3SftpSchemeUri;
        this.accessStorageType = accessStorageType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserRootPath() {
        return userRootPath;
    }

    public String getS3SftpSchemeUri() {
        return s3SftpSchemeUri;
    }

    public String getAccessStorageType() {
        return accessStorageType;
    }
}
