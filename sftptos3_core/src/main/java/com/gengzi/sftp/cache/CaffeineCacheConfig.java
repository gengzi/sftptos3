package com.gengzi.sftp.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine缓存配置类
 * 集中管理不同缓存区域的配置策略
 */
public class CaffeineCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineCacheConfig.class);

    private static final int DEFAULT_CACHE_SIZE = 100_000;
    private static final Long EXPIRE_AFTER_WRITE_TIMEOUT = 60L;
    private static final TimeUnit EXPIRE_AFTER_WRITE_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 文件和目录属性缓存
     * 过期时间为 5 秒
     *
     * @return
     */
    public static Cache<String, Object> getUserPathFileAttributesCache() {
        logger.debug("Creating UserPathFileAttributesCache with max size: {}, expire after write: {} {}",
                DEFAULT_CACHE_SIZE, EXPIRE_AFTER_WRITE_TIMEOUT, EXPIRE_AFTER_WRITE_TIME_UNIT);

        return Caffeine.newBuilder()
                .maximumSize(DEFAULT_CACHE_SIZE)
                .expireAfterWrite(EXPIRE_AFTER_WRITE_TIMEOUT, EXPIRE_AFTER_WRITE_TIME_UNIT)
                .recordStats()  // 开启统计
                .build();
    }

    /**
     * 当前目录文件列表缓存
     * 过期时间为 5 秒
     *
     * @return
     */
    public static Cache<String, Object> getDirectoryContentsNamesCache() {
        logger.debug("Creating DirectoryContentsNamesCache with max size: {}, expire after write: {} {}",
                DEFAULT_CACHE_SIZE, EXPIRE_AFTER_WRITE_TIMEOUT, EXPIRE_AFTER_WRITE_TIME_UNIT);

        return Caffeine.newBuilder()
                .maximumSize(DEFAULT_CACHE_SIZE)
                .expireAfterWrite(EXPIRE_AFTER_WRITE_TIMEOUT, EXPIRE_AFTER_WRITE_TIME_UNIT)
                .recordStats()  // 开启统计
                .build();
    }

}
