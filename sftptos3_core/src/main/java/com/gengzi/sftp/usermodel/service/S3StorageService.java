package com.gengzi.sftp.usermodel.service;

import com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage;
import com.gengzi.sftp.usermodel.dto.S3StorageRequest;

public interface S3StorageService {


    void createS3Storage(S3StorageRequest s3StorageRequest);

}
