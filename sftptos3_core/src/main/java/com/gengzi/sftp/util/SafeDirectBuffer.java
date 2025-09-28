package com.gengzi.sftp.util;

import com.gengzi.sftp.s3.client.DefaultAwsS3SftpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;

public class SafeDirectBuffer {
    private static final Logger logger = LoggerFactory.getLogger(SafeDirectBuffer.class);
    // 标准 Cleaner 实例（线程安全）
    private static final Cleaner CLEANER = Cleaner.create();
    // 堆外缓冲区
    private final ByteBuffer directBuffer;
    // 清理动作（释放堆外内存）
    private final Runnable cleanAction;

    // 构造方法：创建 DirectByteBuffer 并绑定清理逻辑
    public SafeDirectBuffer(int capacity) {
        this.directBuffer = ByteBuffer.allocateDirect(capacity);
        // 捕获 directBuffer 的引用，避免提前被 GC 回收
        ByteBuffer bufferToClean = this.directBuffer;
        // 定义清理动作（通过反射触发内部释放逻辑，兼容 JDK 17）
        this.cleanAction = () -> {
            try {
                // 反射调用 DirectByteBuffer 的 cleaner() 方法（JDK 17 需特殊处理）
                var cleanerMethod = bufferToClean.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true); // 允许访问私有方法
                Object cleaner = cleanerMethod.invoke(bufferToClean);
                // 调用 Cleaner 的 clean() 方法释放堆外内存
                var cleanMethod = cleaner.getClass().getMethod("clean");
                cleanMethod.invoke(cleaner);
                logger.debug("堆外内存已通过标准 Cleaner 释放");
            } catch (Exception e) {
                throw new RuntimeException("释放 DirectByteBuffer 失败", e);
            }
        };
        // 将清理动作注册到标准 Cleaner
        CLEANER.register(this, cleanAction);
    }

    // 获取缓冲区（供外部使用）
    public ByteBuffer getBuffer() {
        return directBuffer;
    }

    // 手动触发释放（可选，用于主动清理）
    public void free() {
        cleanAction.run();
    }
}