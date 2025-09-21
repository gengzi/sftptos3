package com.gengzi.sftp.monitor.cache;


import com.gengzi.sftp.cache.CaffeineCacheConfig;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理器
 * 单例模式，统一管理所有缓存实例
 */
public class MonitorCacheManager {


    // 单例实例
    private static final MonitorCacheManager INSTANCE = new MonitorCacheManager();
    // 缓存容器：存储所有缓存实例
    private final Map<String, Cache<String, Object>> caches = new HashMap<>();

    // 私有构造器，初始化所有缓存
    private MonitorCacheManager() {
        initCaches();
    }

    // 获取单例实例
    public static MonitorCacheManager getInstance() {
        return INSTANCE;
    }

    // 初始化所有缓存
    private void initCaches() {
        caches.put(CacheMode.CLIENT_CONNECTION_CACHE.cacheMode, MonitorCacheConfig.clientConnectionCache());
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
    public Cache<String, Object> getClientConnectionCache() {
        return getCache(CacheMode.CLIENT_CONNECTION_CACHE.cacheMode);
    }


    enum CacheMode {
        CLIENT_CONNECTION_CACHE("s3sftp.monitor.ClientConnection");


        String cacheMode;

        CacheMode(String cacheMode) {
            this.cacheMode = cacheMode;
        }

        public String getCacheMode() {
            return cacheMode;
        }
    }

}
