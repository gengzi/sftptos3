package com.gengzi.sftp.s3.client;


import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * s3工程类，根据不同的s3系统创建对应的客户端
 */
public class S3ClientFactory {
    static final ConcurrentHashMap<String, Class<? extends S3SftpClient>> allS3Client = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, S3SftpClient> CACHE = new ConcurrentHashMap();

    static {
        allS3Client.put(S3ClientNameEnum.DEFAULT_AWS_S3.name(), DefaultAwsS3SftpClient.class);
    }

    public static S3SftpClient getS3Client(String s3ClientName, S3SftpNioSpiConfiguration configuration) {
        // 当时同一个配置的s3系统，还是复用同一个实例
        String key = configuration.getS3ClientInfo() + "/" + s3ClientName;
        Class<? extends S3SftpClient> implClass = allS3Client.get(s3ClientName);
        if (implClass == null) {
            throw new IllegalArgumentException("未找到名称为[" + s3ClientName + "]的实现类");
        }
        // 原子操作，不能先使用 containsKey 再get 单个方法是原子的，组合在一起就是非原子的
        return CACHE.computeIfAbsent(key, k -> {
            try {
                Constructor<? extends S3SftpClient> c =
                        // 构造器用 S3SftpNioSpiConfiguration.class，避免子类导致找不到构造器。
                        implClass.getConstructor(S3SftpNioSpiConfiguration.class);
                return c.newInstance(configuration);
            } catch (Exception e) {
                throw new RuntimeException("创建实例失败", e);
            }
        });

    }

    public static void addS3Client(String s3ClientName, Class<? extends S3SftpClient> implClass) {
        allS3Client.put(s3ClientName, implClass);
    }


}
