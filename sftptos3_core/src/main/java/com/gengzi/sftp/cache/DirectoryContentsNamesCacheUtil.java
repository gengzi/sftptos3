package com.gengzi.sftp.cache;


import com.gengzi.sftp.nio.S3SftpFileSystem;
import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;
import com.gengzi.sftp.nio.constans.Constants;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DirectoryContentsNamesCacheUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserPathFileAttributesCacheUtil.class);
    private static final CacheManager cacheManager = CacheManager.getInstance();


    /**
     * 获取
     *
     * @param sftpFileSystem
     * @return
     * @parparam path
     */
    public static List<String> getCacheValue(S3SftpFileSystem sftpFileSystem, String path) {
        try {
            String cacheKey = getCacheKey(sftpFileSystem, path);
            List<String> dirs = (List<String>) cacheManager.getDirectoryContentsNamesCache().getIfPresent(cacheKey);
            if (logger.isTraceEnabled()) {
                String directoriesStr = (dirs != null) ? dirs.stream().collect(Collectors.joining("\n")) : "null";
                logger.trace("DirectoryContentsNamesCache getCacheValue cacheKey:{}, directories:{}", cacheKey, directoriesStr);
            }
            return dirs;
        } catch (Exception e) {
            logger.error("DirectoryContentsNamesCache getCacheValue error !!! exception:{}", e.getMessage(), e);
        }
        return null;
    }

    private static String getCacheKey(S3SftpFileSystem sftpFileSystem, String path) {
        S3SftpNioSpiConfiguration configuration = sftpFileSystem.configuration();
        String cacheKey = configuration.getDirectoryContentsNamesCacheKey(path);
        logger.debug("Generated cache key for path '{}': {}", path, cacheKey);
        return cacheKey;
    }

    public static void putCacheValue(S3SftpFileSystem sftpFileSystem, String path, List<String> directories) {
        try {
            String cacheKey = getCacheKey(sftpFileSystem, path);
            cacheManager.getDirectoryContentsNamesCache().put(cacheKey, directories);
            if (logger.isDebugEnabled()) {
                String directoriesStr = (directories != null) ? directories.stream().collect(Collectors.joining("\n")) : "null";
                logger.debug("DirectoryContentsNamesCache putCacheValue cacheKey:{}, directories:{}", cacheKey, directoriesStr);
            }
        } catch (Exception e) {
            logger.error("DirectoryContentsNamesCache putCacheValue error !!! exception:{}", e.getMessage(), e);
        }

    }

    public static void removeCacheValue(S3SftpFileSystem sftpFileSystem, String path) {
        try {
            // 处理下path，如果是目录需要获取父目录，如果是文件，则获取文件所在目录
            String removePathKey = path;
            if (path == null || path.isEmpty() || Constants.PATH_SEPARATOR.equals(path)) {
                removePathKey = Constants.PATH_SEPARATOR;
            } else {
                if (path.endsWith("/")) {
                    removePathKey = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR, path.lastIndexOf(Constants.PATH_SEPARATOR) - 1) + 1);
                } else {
                    removePathKey = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR) + 1);
                }
            }
            String cacheKey = getCacheKey(sftpFileSystem, removePathKey);
            cacheManager.getDirectoryContentsNamesCache().invalidate(cacheKey);
            logger.debug("DirectoryContentsNamesCache removeCacheValue cacheKey:{}, original path:{}, processed path:{}", cacheKey, path, removePathKey);
        } catch (Exception e) {
            logger.error("DirectoryContentsNamesCache removeCacheValue error !!! exception:{}", e.getMessage(), e);
        }

    }

    public static String getCacheStats() {
        CacheStats stats = cacheManager.getDirectoryContentsNamesCache().stats();
        return String.format(
                "DirectoryContentsNamesCache 缓存统计 - 命中率: %.2f%%, 加载次数: %d, 命中次数: %d, 未命中次数: %d",
                stats.hitRate() * 100,
                stats.loadCount(),
                stats.hitCount(),
                stats.missCount()
        );
    }

}
