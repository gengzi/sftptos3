package com.gengzi.sftp.usermodel.dao.s3.repository;

import com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage;
import com.gengzi.sftp.usermodel.response.S3NamesResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface S3StorageRepository extends JpaRepository<S3Storage, Long>, JpaSpecificationExecutor<S3Storage> {

    @Query("SELECT new com.gengzi.sftp.usermodel.response.S3NamesResponse(s.id, s.s3Name) FROM S3Storage s")
    List<S3NamesResponse> findAllS3Name();



}