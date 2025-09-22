package com.gengzi.sftp.usermodel.service;

import com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage;
import com.gengzi.sftp.usermodel.dto.S3StorageRequest;
import com.gengzi.sftp.usermodel.dto.S3StorageUpdateRequest;
import com.gengzi.sftp.usermodel.response.S3NamesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.List;

public interface S3StorageService {


    void createS3Storage(S3StorageRequest s3StorageRequest);


    List<S3NamesResponse> s3names();

    Page<S3Storage> list(String s3Name, Pageable pageable);

    void update(@Valid S3StorageUpdateRequest s3StorageRequest);

    void remove(@Valid Long id);
}
