package com.gengzi.sftp.cache;


import com.github.benmanes.caffeine.cache.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理器
 * 单例模式，统一管理所有缓存实例
 */
public class CacheManager {


    // 单例实例
    private static final CacheManager INSTANCE = new CacheManager();
    // 缓存容器：存储所有缓存实例
    private final Map<String, Cache<String, Object>> caches = new HashMap<>();

    // 私有构造器，初始化所有缓存
    private CacheManager() {
        initCaches();
    }

    // 获取单例实例
    public static CacheManager getInstance() {
        return INSTANCE;
    }

    // 初始化所有缓存
    private void initCaches() {
        caches.put(CacheMode.USER_PATH_FILE_ATTRIBUTES_CACHE.cacheMode, CaffeineCacheConfig.getUserPathFileAttributesCache());
        caches.put(CacheMode.DIRECTORY_CONTENTS_NAMES_CACHE.cacheMode, CaffeineCacheConfig.getDirectoryContentsNamesCache());
    }

    // 获取指定缓存
    public Cache<String, Object> getCache(String cacheName) {
        Cache<String, Object> cache = caches.get(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("未找到缓存配置: " + cacheName);
        }
        return cache;
    }


    // 获取用户缓存（简化API）
    public Cache<String, Object> getUserPathFileAttributesCache() {
        return getCache(CacheMode.USER_PATH_FILE_ATTRIBUTES_CACHE.cacheMode);
    }

    public Cache<String, Object> getDirectoryContentsNamesCache() {
        return getCache(CacheMode.DIRECTORY_CONTENTS_NAMES_CACHE.cacheMode);
    }

    enum CacheMode {
        USER_PATH_FILE_ATTRIBUTES_CACHE("s3sftp.userPathFileAttributes"),
        DIRECTORY_CONTENTS_NAMES_CACHE("s3sftp.directoryContentsNamesCache");


        String cacheMode;

        CacheMode(String cacheMode) {
            this.cacheMode = cacheMode;
        }

        public String getCacheMode() {
            return cacheMode;
        }
    }

}
