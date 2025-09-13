package com.gengzi.sftp.s3.client;


import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * s3工程类，根据不同的s3系统创建对应的客户端
 */
public class S3ClientFactory {
    static final Map<String, Class<? extends S3SftpClient>> allS3Client = new HashMap<>();
    static {
        allS3Client.put(S3ClientNameEnum.DEFAULT_AWS_S3.name(), DefaultAwsS3SftpClient.class);
    }

    public static S3SftpClient getS3Client(String s3ClientName, S3SftpNioSpiConfiguration configuration) {
        Class<? extends S3SftpClient> implClass = allS3Client.get(s3ClientName);
        if (implClass == null) {
            throw new IllegalArgumentException("未找到名称为[" + s3ClientName + "]的实现类");
        }
        try {
            Constructor<? extends S3SftpClient> constructor = implClass.getConstructor(configuration.getClass());
            S3SftpClient instance = constructor.newInstance(configuration);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("实现类[" + implClass.getName() + "]不存在匹配的构造方法", e);
        } catch (Exception e) {
            throw new RuntimeException("创建实例失败", e);
        }
    }

    public static void addS3Client(String s3ClientName, Class<? extends S3SftpClient> implClass) {
        allS3Client.put(s3ClientName, implClass);
    }





}
