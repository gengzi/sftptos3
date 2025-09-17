package com.gengzi.sftp.usermodel.dao.s3.repository;

import com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface S3StorageRepository extends JpaRepository<S3Storage, Long>, JpaSpecificationExecutor<S3Storage> {



}