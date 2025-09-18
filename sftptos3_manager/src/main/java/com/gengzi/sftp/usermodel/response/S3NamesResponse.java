package com.gengzi.sftp.usermodel.response;


import lombok.Data;

@Data
public class S3NamesResponse {

    private Long id;

    private String s3Name;

    public S3NamesResponse(Long id, String s3Name) {
        this.id = id;
        this.s3Name = s3Name;
    }

    public Long getId() {
        return id;
    }

    public String getS3Name() {
        return s3Name;
    }
}
