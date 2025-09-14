package com.gengzi.sftp.cache;


import com.gengzi.sftp.s3.client.entity.ObjectHeadResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 缓存工具类
 * 提供类型安全的缓存操作方法
 */
public class CacheUtil {

    @SuppressWarnings("unchecked")
    public static <T> T getCacheValue(Cache<String, Object> cache, String key) {
        return (T) cache.getIfPresent(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrLoadCacheValue(Cache<String, Object> cache, String key, Callable<T> loader)
            throws ExecutionException {
        return (T) cache.get(key, k -> {
            try {
                return loader.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void putCacheValue(Cache<String, Object> cache, String key, Object value) {
        cache.put(key, value);
    }

    public static void removeCacheValue(Cache<String, Object> cache, String key) {
        cache.invalidate(key);
    }

    public static String getCacheStats(Cache<String, Object> cache) {
        CacheStats stats = cache.stats();
        return String.format(
            "缓存统计 - 命中率: %.2f%%, 加载次数: %d, 命中次数: %d, 未命中次数: %d",
            stats.hitRate() * 100,
            stats.loadCount(),
            stats.hitCount(),
            stats.missCount()
        );
    }
}
