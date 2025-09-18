package com.gengzi.sftp.usermodel.dto;


import lombok.Data;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotBlank;

@Data
public class S3StoragePageRequest {
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

    Pageable pageable;
}
