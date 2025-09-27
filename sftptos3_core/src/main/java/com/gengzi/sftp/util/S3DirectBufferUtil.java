package com.gengzi.sftp.util;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class S3DirectBufferUtil {

    /**
     * 将 ResponseBytes<GetObjectResponse> 转换为直接内存缓冲区（Direct ByteBuffer）
     * @param responseBytes S3客户端返回的字节响应
     * @return 直接内存缓冲区（需手动释放，或依赖GC自动清理）
     */
    public static ByteBuffer toDirectBuffer(ResponseBytes<GetObjectResponse> responseBytes) {
        if (responseBytes == null) {
            throw new IllegalArgumentException("ResponseBytes cannot be null");
        }

        // 1. 获取堆内存中的字节数组（ResponseBytes内部存储的是堆内存数组）
        byte[] heapBytes = responseBytes.asByteArray();
        if (heapBytes == null || heapBytes.length == 0) {
            return ByteBuffer.allocateDirect(0); // 空数据返回空缓冲区
        }

        // 2. 分配与字节数组大小相同的直接内存缓冲区
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(heapBytes.length);

        // 3. 将堆内存数据复制到直接内存（此操作会产生一次内存拷贝）
        directBuffer.put(heapBytes);

        // 4. 切换为读模式（可选，根据后续使用场景决定）
        directBuffer.flip();

        // 5. 可选：手动释放堆内存数组的引用，帮助GC尽早回收
        heapBytes = null;

        return directBuffer;
    }

    /**
     * 手动释放直接内存（通过sun.misc.Cleaner，需注意兼容性）
     * @param directBuffer 直接内存缓冲区
     */
    public static void freeDirectBuffer(ByteBuffer directBuffer) {
        if (directBuffer == null || !directBuffer.isDirect()) {
            return;
        }

        try {
            // 通过反射获取直接缓冲区的Cleaner，主动触发清理
            Method cleanerMethod = directBuffer.getClass().getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            Object cleaner = cleanerMethod.invoke(directBuffer);
            Method cleanMethod = cleaner.getClass().getMethod("clean");
            cleanMethod.invoke(cleaner);
        } catch (Exception e) {
            // 忽略反射异常（不同JDK版本可能有差异）
            e.printStackTrace();
        }
    }
}
    