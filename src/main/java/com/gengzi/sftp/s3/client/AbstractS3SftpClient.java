package com.gengzi.sftp.s3.client;

import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;

public abstract class AbstractS3SftpClient<T> implements S3SftpClient{

    public S3SftpNioSpiConfiguration configuration;

    public T s3Client;

    public AbstractS3SftpClient(S3SftpNioSpiConfiguration s3SftpNioSpiConfiguration) {
        this.configuration = s3SftpNioSpiConfiguration;
        this.s3Client = (T) this.createClient(s3SftpNioSpiConfiguration);
    }


}
