package com.gengzi.sftp.monitor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class MonitorCacheConfig {

    // 配置客户端连接缓存：30分钟未活动自动过期，最大容量10_0000

    public static Cache<String, Object> clientConnectionCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES) // 基于访问的过期策略
                .maximumSize(10_000)                       // 最大缓存数量
                .recordStats()                            // 记录缓存统计（可选）
                .build();
    }
}