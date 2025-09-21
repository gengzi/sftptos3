package com.gengzi.sftp.nio;


import com.gengzi.sftp.cache.DirectoryContentsNamesCacheUtil;
import com.gengzi.sftp.cache.UserPathFileAttributesCacheUtil;
import com.gengzi.sftp.nio.constans.Constants;
import com.gengzi.sftp.s3.client.S3SftpClient;
import com.gengzi.sftp.s3.client.entity.ListObjectsResponse;
import com.gengzi.sftp.s3.client.entity.ObjectHeadResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 *  s3sftp文件属性
 *
 */
public class S3SftpBasicFileAttributes implements BasicFileAttributes {

    private static final Logger logger = LoggerFactory.getLogger(S3SftpBasicFileAttributes.class.getName());
    // 修改声明：为目录和文件都添加了权限，支持用户用户组，和其他用户组的可读可写
    private static final Set<PosixFilePermission> posixFilePermissions;
    static {
        posixFilePermissions = new HashSet<>();
        posixFilePermissions.add(PosixFilePermission.OWNER_READ);
        posixFilePermissions.add(PosixFilePermission.OWNER_WRITE);
        posixFilePermissions.add(PosixFilePermission.OTHERS_READ);
        posixFilePermissions.add(PosixFilePermission.OTHERS_WRITE);
        posixFilePermissions.add(PosixFilePermission.GROUP_READ);
        posixFilePermissions.add(PosixFilePermission.GROUP_WRITE);
    }
    /**
     * 目录属性
     */
    private static final S3SftpBasicFileAttributes DIRECTORY_ATTRIBUTES = new S3SftpBasicFileAttributes(
            FileTime.fromMillis(0),
            0L,
            null,
            true,
            false,
            posixFilePermissions
    );



    private final FileTime lastModifiedTime;
    private final Long size;
    private final Object eTag;
    private final boolean isDirectory;
    private final boolean isRegularFile;
    private final Set<PosixFilePermission> permissions;
    private Boolean isDirEmpty = false;

    public S3SftpBasicFileAttributes(FileTime lastModifiedTime,
                                     Long size,
                                     Object eTag,
                                     boolean isDirectory,
                                     boolean isRegularFile,
                                     Set<PosixFilePermission> permissions) {
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
        this.eTag = eTag;
        this.isDirectory = isDirectory;
        this.isRegularFile = isRegularFile;
        this.permissions = permissions;

    }

    public static S3SftpBasicFileAttributes get(S3SftpPath path) throws IOException {
        String key = path.getKey();
        ObjectHeadResponse cacheValue = UserPathFileAttributesCacheUtil.getCacheValue(path);
        if (cacheValue != null) {
            return getS3SftpBasicFileAttributes(cacheValue);
        } else {
            S3SftpClient client = path.getFileSystem().client();
            ObjectHeadResponse objectHeadResponse = client.headFileOrDirObject(path.bucketName(), key);
            // 处理空字节对象目录
            objectHeadResponse = execZeroObject(key,objectHeadResponse);
            putChache(path, objectHeadResponse);
            return getS3SftpBasicFileAttributes(objectHeadResponse);
        }
    }

    public static ObjectHeadResponse execZeroObject(String key, ObjectHeadResponse objectHeadResponse) {
        if(!objectHeadResponse.isDirectory()){
            return objectHeadResponse;
        }



        // 提取路径分隔符常量，避免重复调用
        String pathSeparator = Constants.PATH_SEPARATOR;
        // 处理目标key（确保以路径分隔符结尾）
        String targetKey = key.endsWith(pathSeparator) ? key : key + pathSeparator;

        ListObjectsResponse listObjects = objectHeadResponse.getListObjects();

        // 外层条件判断：列表不为空且是大小为0的目录
        if (listObjects != null
                && objectHeadResponse.getSize() == 0
                && objectHeadResponse.isDirectory()) {

            Map<String, ObjectHeadResponse> prefixes = listObjects.getPrefixes();
            Map<String, ObjectHeadResponse> objects = listObjects.getObjects();

            // 校验前缀为空且对象集合符合条件（非空且仅有一个元素）
            boolean isPrefixesEmpty = prefixes == null || prefixes.isEmpty();
            boolean isSingleObject = objects != null && objects.size() == 1;

            if (isPrefixesEmpty && isSingleObject) {
                // 检查唯一对象是否匹配目标key（直接containsKey比流操作更高效）
                if (objects.containsKey(targetKey)) {
                    logger.debug("zero object file change dir:{}", key);

                    // 构建新的前缀映射
                    Map<String, ObjectHeadResponse> zeroObjectPrefixes = new HashMap<>();
                    zeroObjectPrefixes.put(key, new ObjectHeadResponse(
                            FileTime.fromMillis(0),
                            0L,
                            null,
                            true,
                            false
                    ));

                    // 构建并返回新的响应对象
                    return new ObjectHeadResponse(
                            FileTime.fromMillis(0),
                            0L,
                            null,
                            true,
                            false,
                            new ListObjectsResponse(zeroObjectPrefixes, new HashMap<>())
                    );
                }
            }
        }

        // 不满足条件时返回原始响应
        return objectHeadResponse;
    }


    public static S3SftpBasicFileAttributes getNoCache(S3SftpPath path) throws IOException {
        String key = path.getKey();
        S3SftpClient client = path.getFileSystem().client();
        ObjectHeadResponse objectHeadResponse = client.headFileOrDirObject(path.bucketName(), key);
        if (objectHeadResponse.isDirectory()) {
            S3SftpBasicFileAttributes fileAttributes = new S3SftpBasicFileAttributes(
                    FileTime.fromMillis(0),
                    0L,
                    null,
                    true,
                    false,
                    posixFilePermissions
            );
            ListObjectsResponse listObjects = objectHeadResponse.getListObjects();
            if (listObjects != null) {
                List<String> objectsNames = listObjects.getObjectsNames();
                if (objectsNames.stream().filter(objectName -> !objectName.equals(key.endsWith(Constants.PATH_SEPARATOR) ? key: key + Constants.PATH_SEPARATOR )).count() > 0) {
                    fileAttributes.setDirEmpty(false);
                } else {
                    fileAttributes.setDirEmpty(true);
                }
            }
            return fileAttributes;
        } else {
            return new S3SftpBasicFileAttributes(
                    objectHeadResponse.getLastModifiedTime(),
                    objectHeadResponse.getSize(),
                    objectHeadResponse.geteTag(),
                    false,
                    true,
                    posixFilePermissions
            );
        }

    }

    private static void putChache(S3SftpPath path, ObjectHeadResponse objectHeadResponse) {
        // 处理目录，如果当前路径是一个目录
        String pathKey = path.getKey();
        if (objectHeadResponse != null && objectHeadResponse.isDirectory() && objectHeadResponse.getListObjects() != null) {
            DirectoryContentsNamesCacheUtil.putCacheValue(path.getFileSystem(), path.getKey(), objectHeadResponse.getListObjects().getObjectsNames());

            ListObjectsResponse listObjects = objectHeadResponse.getListObjects();
            if (listObjects.getPrefixes() != null && !listObjects.getPrefixes().isEmpty()) {
                for (Map.Entry<String, ObjectHeadResponse> entry : listObjects.getPrefixes().entrySet()) {
                    String key = entry.getKey();
                    ObjectHeadResponse value = entry.getValue();
                    UserPathFileAttributesCacheUtil.putCacheValue(path.getFileSystem(), key, value);
                }
            }
            String directoryKey = pathKey.endsWith(Constants.PATH_SEPARATOR) ? pathKey : pathKey + Constants.PATH_SEPARATOR;
            if (listObjects.getObjects() != null && !listObjects.getObjects().isEmpty()) {
                    for (Map.Entry<String, ObjectHeadResponse> entry : listObjects.getObjects().entrySet()) {
                    logger.debug("put cache:{}, directoryKey:{}", entry.getKey(),directoryKey);
                    if(directoryKey.equals(entry.getKey())){
                        continue;
                    }
                    String key = entry.getKey();
                    ObjectHeadResponse value = entry.getValue();
                    UserPathFileAttributesCacheUtil.putCacheValue(path.getFileSystem(), key, value);
                }
            }
        }

        //TODO 可以把目录这部分删除掉
        UserPathFileAttributesCacheUtil.putCacheValue(path, objectHeadResponse);
    }

    @NotNull
    private static S3SftpBasicFileAttributes getS3SftpBasicFileAttributes(ObjectHeadResponse objectHeadResponse) {
        if (objectHeadResponse.isDirectory()) {
            return DIRECTORY_ATTRIBUTES;
        } else {
            return new S3SftpBasicFileAttributes(
                    objectHeadResponse.getLastModifiedTime(),
                    objectHeadResponse.getSize(),
                    objectHeadResponse.geteTag(),
                    false,
                    true,
                    posixFilePermissions
            );
        }
    }

    public Boolean getDirEmpty() {
        return isDirEmpty;
    }

    public void setDirEmpty(Boolean dirEmpty) {
        isDirEmpty = dirEmpty;
    }

    /**
     * Returns the time of last modification.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time of last modification then this method returns an
     * implementation specific default value, typically a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time the file was last
     * modified
     */
    @Override
    public FileTime lastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Returns the time of last access.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time of last access then this method returns
     * an implementation specific default value, typically the {@link
     * #lastModifiedTime() last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time of last access
     */
    @Override
    public FileTime lastAccessTime() {
        return lastModifiedTime();
    }

    /**
     * Returns the creation time. The creation time is the time that the file
     * was created.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time when the file was created then this method returns
     * an implementation specific default value, typically the {@link
     * #lastModifiedTime() last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time the file was created
     */
    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return {@code true} if the file is a regular file with opaque content
     */
    @Override
    public boolean isRegularFile() {
        return isRegularFile;
    }

    /**
     * Tells whether the file is a directory.
     *
     * @return {@code true} if the file is a directory
     */
    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Tells whether the file is a symbolic link.
     *
     * @return {@code true} if the file is a symbolic link
     */
    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    /**
     * Tells whether the file is something other than a regular file, directory,
     * or symbolic link.
     *
     * @return {@code true} if the file something other than a regular file,
     * directory or symbolic link
     */
    @Override
    public boolean isOther() {
        return false;
    }

    /**
     * Returns the size of the file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons. The size of files that are not {@link
     * #isRegularFile regular} files is implementation specific and
     * therefore unspecified.
     *
     * @return the file size, in bytes
     */
    @Override
    public long size() {
        return size;
    }

    /**
     * Returns an object that uniquely identifies the given file, or {@code
     * null} if a file key is not available. On some platforms or file systems
     * it is possible to use an identifier, or a combination of identifiers to
     * uniquely identify a file. Such identifiers are important for operations
     * such as file tree traversal in file systems that support <a
     * href="../package-summary.html#links">symbolic links</a> or file systems
     * that allow a file to be an entry in more than one directory. On UNIX file
     * systems, for example, the <em>device ID</em> and <em>inode</em> are
     * commonly used for such purposes.
     *
     * <p> The file key returned by this method can only be guaranteed to be
     * unique if the file system and files remain static. Whether a file system
     * re-uses identifiers after a file is deleted is implementation dependent and
     * therefore unspecified.
     *
     * <p> File keys returned by this method can be compared for equality and are
     * suitable for use in collections. If the file system and files remain static,
     * and two files are the {@link Files#isSameFile same} with
     * non-{@code null} file keys, then their file keys are equal.
     *
     * @return an object that uniquely identifies the given file, or {@code null}
     * @see Files#walkFileTree
     */
    @Override
    public Object fileKey() {
        return eTag;
    }

    public Map<String, Object> toMap() {
        return new HashMap<String, Object>() {{
            put("lastModifiedTime", lastModifiedTime());
            put("lastAccessTime", lastAccessTime());
            put("creationTime", creationTime());
            put("isRegularFile", isRegularFile());
            put("isDirectory", isDirectory());
            put("isSymbolicLink", isSymbolicLink());
            put("isOther", isOther());
            put("size", size);
            put("fileKey", fileKey());
            put("permissions", permissions());
        }};
    }


    private Set<PosixFilePermission> permissions() {
        return permissions;
    }
}
