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
import java.util.Optional;
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


    private void putCacheValue(S3SftpFileSystem fileSystem, String path, ListObjectsResponse listObjectsResponse) {
        DirectoryContentsNamesCacheUtil.putCacheValue(fileSystem, path, listObjectsResponse.getObjectsNames());

        // 处理零字节对象路径
        if(listObjectsResponse != null && (listObjectsResponse.getPrefixes() == null || listObjectsResponse.getPrefixes().isEmpty()) &&
                (listObjectsResponse.getObjects() != null && listObjectsResponse.getObjects().size() == 1) ){
            if(listObjectsResponse.getObjects().keySet().stream().filter(key -> key.equals(path)).count() ==1){
                ObjectHeadResponse objectHeadResponse = listObjectsResponse.getObjects().get(path);
                if(objectHeadResponse.getSize() == 0L){
                    logger.debug("空字节对象处理:{}", path);
                    // 说明是零字节对象
                    UserPathFileAttributesCacheUtil.putCacheValue(fileSystem, path, new ObjectHeadResponse(
                            FileTime.fromMillis(0),
                            0L,
                            null,
                            true,
                            false,
                            null
                    ));
                    return;
                }
            }
        }



        // 设置文件属性
        if(listObjectsResponse.getObjects() != null && listObjectsResponse.getObjects().size() > 0){
             listObjectsResponse.getObjects().entrySet().forEach(entry -> {
                 UserPathFileAttributesCacheUtil.putCacheValue(fileSystem, entry.getKey(), entry.getValue());
             });
        }
        if (listObjectsResponse.getPrefixes() != null && listObjectsResponse.getPrefixes().size() > 0) {
            listObjectsResponse.getPrefixes().entrySet().forEach(entry -> {
                UserPathFileAttributesCacheUtil.putCacheValue(fileSystem, entry.getKey(), entry.getValue());
            });
        }
    }



    private static boolean isEqualToParent(String finalDirName, Path p) {
        return ((S3SftpPath) p).getKey().equals(finalDirName);
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
