package com.gengzi.sftp.usermodel.dto;

import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage}
 */
@Data
public class S3StorageRequest implements Serializable {
    @NotBlank
    String bucket;
    @NotBlank
    String endpoint;
    @NotBlank
    String accessKey;
    @NotBlank
    String accessSecret;
    @NotBlank
    String s3Name;
}