package com.gengzi.sftp.usermodel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage}
 */
@Data
public class S3StorageUpdateRequest implements Serializable {
    @NotNull
    Long id;
    @NotBlank
    String bucket;
    @NotBlank
    String endpoint;
    @NotBlank
    String accessKey;
    @NotBlank
    String accessSecret;
    @NotBlank
    String region;

}