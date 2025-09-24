package com.gengzi.sftp.cache;


import com.gengzi.sftp.nio.S3SftpFileSystem;
import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;
import com.gengzi.sftp.nio.S3SftpPath;
import com.gengzi.sftp.s3.client.entity.ObjectHeadResponse;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 针对ls 等命令频繁查询s3获取对象属性的缓存
 */
public class UserPathFileAttributesCacheUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserPathFileAttributesCacheUtil.class);
    private static final CacheManager cacheManager = CacheManager.getInstance();

    /**
     * 获取文件属性缓存
     *
     * @param s3SftpPath
     * @return
     */
    public static ObjectHeadResponse getCacheValue(S3SftpPath s3SftpPath) {
        try {
            String cacheKey = getCacheKey(s3SftpPath);
            ObjectHeadResponse objectHeadResponse = (ObjectHeadResponse) cacheManager.getUserPathFileAttributesCache().getIfPresent(cacheKey);
            if (logger.isTraceEnabled()) {
                String valueStr = (objectHeadResponse == null) ? "null" : objectHeadResponse.toString();
                logger.trace("UserPathFileAttributesCache getCacheValue cacheKey:{}, value:{}", cacheKey, valueStr);
            }
            return objectHeadResponse;
        } catch (Exception e) {
            logger.error("UserPathFileAttributesCache getCacheValue error !!! exception:{}", e.getMessage(), e);
        }
        return null;
    }


    private static String getCacheKey(S3SftpPath s3SftpPath) {
        S3SftpNioSpiConfiguration configuration = s3SftpPath.getFileSystem().configuration();
        String cacheKey = configuration.getUserPathFileAttributesCacheKey(s3SftpPath.getKey());
        return cacheKey;
    }

    private static String getCacheKey(S3SftpFileSystem s3SftpFileSystem, String s3SftpPath) {
        S3SftpNioSpiConfiguration configuration = s3SftpFileSystem.configuration();
        String cacheKey = configuration.getUserPathFileAttributesCacheKey(s3SftpPath);
        return cacheKey;
    }

    public static void putCacheValue(S3SftpPath s3SftpPath, ObjectHeadResponse value) {
        try {
            String cacheKey = getCacheKey(s3SftpPath);
            cacheManager.getUserPathFileAttributesCache().put(cacheKey, value);
            if (logger.isDebugEnabled()) {
                String valueStr = (value == null) ? "null" : value.toString();
                logger.debug("UserPathFileAttributesCache putCacheValue cacheKey:{}, value:{}", cacheKey, valueStr);
            }
        } catch (Exception e) {
            logger.error("UserPathFileAttributesCache putCacheValue error !!! exception:{}", e.getMessage(), e);
        }

    }

    public static void putCacheValue(S3SftpFileSystem s3SftpFileSystem, String s3SftpPath, ObjectHeadResponse value) {
        try {
            String cacheKey = getCacheKey(s3SftpFileSystem, s3SftpPath);
            cacheManager.getUserPathFileAttributesCache().put(cacheKey, value);
            if (logger.isDebugEnabled()) {
                String valueStr = (value == null) ? "null" : value.toString();
                logger.debug("UserPathFileAttributesCache putCacheValue cacheKey:{}, value:{}", cacheKey, valueStr);
            }
        } catch (Exception e) {
            logger.error("UserPathFileAttributesCache putCacheValue error !!! exception:{}", e.getMessage(), e);
        }

    }

    public static void removeCacheValue(S3SftpPath s3SftpPath) {
        try {
            String cacheKey = getCacheKey(s3SftpPath);
            cacheManager.getUserPathFileAttributesCache().invalidate(cacheKey);
            logger.debug("UserPathFileAttributesCache removeCacheValue cacheKey:{}", cacheKey);
        } catch (Exception e) {
            logger.error("UserPathFileAttributesCache removeCacheValue error !!!exception:{}", e.getMessage(), e);
        }

    }

    public static String getCacheStats() {
        CacheStats stats = cacheManager.getUserPathFileAttributesCache().stats();
        return String.format(
                "UserPathFileAttributesCache 缓存统计 - 命中率: %.2f%%, 加载次数: %d, 命中次数: %d, 未命中次数: %d",
                stats.hitRate() * 100,
                stats.loadCount(),
                stats.hitCount(),
                stats.missCount()
        );
    }


}
