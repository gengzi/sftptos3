package com.gengzi.sftp.nio;

import com.gengzi.sftp.cache.DirectoryContentsNamesCacheUtil;
import com.gengzi.sftp.cache.UserPathFileAttributesCacheUtil;
import com.gengzi.sftp.s3.client.entity.ListObjectsResponse;
import com.gengzi.sftp.s3.client.entity.ObjectHeadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class S3SftpDirectoryStream implements DirectoryStream {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private String bucketName;

    private S3SftpFileSystem fileSystem;
    // 目录路径
    private String path;

    private DirectoryStream.Filter<? super Path> filter;

    private Iterator<Path> dirs;


    public S3SftpDirectoryStream(S3SftpFileSystem fileSystem, String bucketName, String path, DirectoryStream.Filter<? super Path> filter) {
        this.path = path;
        this.bucketName = bucketName;
        this.fileSystem = fileSystem;
        this.filter = filter;

        List<String> cacheValue = DirectoryContentsNamesCacheUtil.getCacheValue(fileSystem, path);
        if (cacheValue != null) {
            filterFileNams(fileSystem, path, filter, cacheValue);
        } else {
            CompletableFuture<ListObjectsResponse> listObjects = fileSystem.client().getCurrentKeyDirAllListObjects(bucketName, path);
            try {
                ListObjectsResponse listObjectsResponse = listObjects.get(fileSystem.configuration().timeout(), fileSystem.configuration().timeoutUnit());
                // 设置缓存
                putCacheValue(fileSystem, path, listObjectsResponse);
                filterFileNams(fileSystem, path, filter, listObjectsResponse.getObjectsNames());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                logger.error("getCurrentKeyDirAllFileNames time out");
                throw new RuntimeException(e);
            }
        }

    }

    private static boolean isEqualToParent(String finalDirName, Path p) {
        return ((S3SftpPath) p).getKey().equals(finalDirName);
    }

    private void putCacheValue(S3SftpFileSystem fileSystem, String path, ListObjectsResponse listObjectsResponse) {
        // 如果响应为空，直接返回避免后续处理
        if (listObjectsResponse == null) {
            return;
        }
        DirectoryContentsNamesCacheUtil.putCacheValue(fileSystem, path, listObjectsResponse.getObjectsNames());
        // 提取对象和前缀集合（减少重复调用）
        Map<String, ObjectHeadResponse> objects = listObjectsResponse.getObjects();
        Map<String, ObjectHeadResponse> prefixes = listObjectsResponse.getPrefixes();

        // 检查前缀为空且对象集合只有一个元素
        boolean isPrefixesEmpty = prefixes == null || prefixes.isEmpty();
        boolean isSingleObject = objects != null && objects.size() == 1;

        if (isPrefixesEmpty && isSingleObject && objects.containsKey(path)) {
            ObjectHeadResponse objectHeadResponse = objects.get(path);
            // 验证对象不为空且大小为0
            if (objectHeadResponse != null && objectHeadResponse.getSize() == 0L) {
                logger.debug("zero object file change dir:{}", path);
                UserPathFileAttributesCacheUtil.putCacheValue(fileSystem, path,
                        new ObjectHeadResponse(
                                FileTime.fromMillis(0),
                                0L,
                                null,
                                true,
                                false
                        ));
                return;
            }
        }

        // 批量设置对象属性缓存
        cacheEntries(fileSystem, objects);

        // 批量设置前缀属性缓存
        cacheEntries(fileSystem, prefixes);
    }

    /**
     * 批量缓存条目到属性缓存
     */
    private void cacheEntries(S3SftpFileSystem fileSystem, Map<String, ObjectHeadResponse> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        entries.forEach((key, value) -> {
                    if (key.equals(path)) {
                        return;
                    }
                    UserPathFileAttributesCacheUtil.putCacheValue(fileSystem, key, value);
                }

        );
    }

    private void filterFileNams(S3SftpFileSystem fileSystem, String path, Filter<? super Path> filter, List<String> fileNames) {
        dirs = fileNames.stream()
                .map(fileName -> fileSystem.getPath(fileName))
                .filter(s3Sftppath -> !isEqualToParent(path, s3Sftppath))
                .filter(s3Sftppath -> tryAccept(filter, s3Sftppath))
                .iterator();
    }

    private boolean tryAccept(DirectoryStream.Filter<? super Path> filter, Path path) {
        try {
            return filter.accept(path);
        } catch (IOException e) {
            logger.warn("An IOException was thrown while filtering the path: {}." +
                    " Set log level to debug to show stack trace", path);
            logger.debug(e.getMessage(), e);
            return false;
        }
    }


    /**
     * Returns the iterator associated with this {@code DirectoryStream}.
     *
     * @return the iterator associated with this {@code DirectoryStream}
     * @throws IllegalStateException if this directory stream is closed or the iterator has already
     *                               been returned
     */
    @Override
    public Iterator iterator() {
        return dirs;
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {

    }
}
