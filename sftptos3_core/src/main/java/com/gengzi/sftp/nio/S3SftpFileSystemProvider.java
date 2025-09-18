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
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * s3sftp文件系统提供者
 * 创建和管理文件系统实例（FileSystem）
 * 将 URI 转换为文件系统路径（Path）
 * 执行具体的文件操作（创建、删除、复制、移动文件 / 目录等）
 */
public class S3SftpFileSystemProvider extends FileSystemProvider {
    // 文件系统前缀标识
    static final String SCHEME = "s3sftp";
    // 文件分隔符
    static final String PATH_SEPARATOR = Constants.PATH_SEPARATOR;
    // 缓存已经创建的S3SftpFileSystem
    private static final Map<String, S3SftpFileSystem> FS_CACHE = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static S3SftpPath checkPath(Path obj) {
        Objects.requireNonNull(obj);
        if (!(obj instanceof S3SftpPath)) {
            throw new ProviderMismatchException();
        }
        return (S3SftpPath) obj;
    }

    private static void delPath(S3SftpClient s3Client, String bucketName, String deletePathKey, Long timeout, TimeUnit timeUnit) {
        try {
            s3Client.deleteObject(bucketName, deletePathKey).get(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getContainedObjectBatches(
            S3SftpClient s3Client,
            String bucketName,
            String prefix,
            long timeOut,
            TimeUnit unit
    ) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<ListObjectsResponse> currentKeyDirAllFileNames = s3Client.getCurrentKeyDirAllListObjects(bucketName, prefix);
        return currentKeyDirAllFileNames.get(timeOut, unit).getObjectsNames();
    }

    @Override
    public String getScheme() {
        return SCHEME;
    }

    /**
     * 解析uri 创建filesystem
     *
     * @param uri URI reference  s3sftp://[key:secret@]endpoint[:port]/bucket/objectkey
     * @param env A map of provider specific properties to configure the file system;
     *            may be empty   环境变量
     * @return
     * @throws IOException
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        // 判断uri 的约束是否一致
        if (!uri.getScheme().equals(getScheme())) {
            throw new IllegalArgumentException("URI scheme must be " + getScheme());
        }
        // 解析uri
        S3SftpFileSystemInfo info = new S3SftpFileSystemInfo(uri);
        // 解析env
        S3SftpNioSpiConfiguration config = new S3SftpNioSpiConfiguration(env)
                .withEndpoint(info.endpoint())
                .withBucketName(info.bucket());
        if (info.accessKey() != null) {
            config.withCredentials(info.accessKey(), info.accessSecret());
        }
        S3SftpFileSystem s3SftpFileSystem = new S3SftpFileSystem(this, config);
        FS_CACHE.put(info.key(), s3SftpFileSystem);
        return s3SftpFileSystem;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        S3SftpFileSystemInfo info = new S3SftpFileSystemInfo(uri);
        S3SftpFileSystem s3SftpFileSystem = FS_CACHE.get(info.key());
        if (s3SftpFileSystem == null) {
            throw new FileSystemNotFoundException(info.key());
        }
        return s3SftpFileSystem;
    }

    //TODO 代验证？？
    @NotNull
    @Override
    public Path getPath(@NotNull URI uri) {
        Objects.requireNonNull(uri);
        return getFileSystem(uri)
                .getPath(uri.getScheme() + "://" + uri.getPath());
    }

    /**
     * 用于为指定路径创建一个 可随机访问的字节通道（SeekableByteChannel），支持读写文件数据并灵活控制文件打开模式与初始属性
     *
     * @param path    the path to the file to open or create
     * @param options options specifying how the file is opened
     * @param attrs   an optional list of file attributes to set atomically when
     *                creating the file
     * @return
     * @throws IOException
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        checkPath(path);
        S3SftpFileSystem fileSystem = (S3SftpFileSystem) path.getFileSystem();
        S3SftpSeekableByteChannel s3SftpSeekableByteChannel = new S3SftpSeekableByteChannel((S3SftpPath) path,
                fileSystem.client(), options);
        fileSystem.registerOpenChannel(s3SftpSeekableByteChannel);
        return s3SftpSeekableByteChannel;
    }

    /**
     * 创建一个目录流，遍历指定目录（dir）中符合过滤条件（filter）的文件和子目录
     *
     * @param dir    the path to the directory
     * @param filter the directory stream filter
     * @return
     * @throws IOException
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        // 检查路径是否为s3sftp路径
        S3SftpPath s3Path = checkPath(dir);
        // 获取一个绝对路径
        String path = s3Path.toAbsolutePath().getKey();
        if (!s3Path.isDirectory()) {
            path = path + PATH_SEPARATOR;
        }
        return new S3SftpDirectoryStream(s3Path.getFileSystem(), s3Path.bucketName(), path, filter);
    }

    /**
     * 用于在指定路径创建新目录并设置初始属性
     *
     * @param dir   the directory to create
     * @param attrs an optional list of file attributes to set atomically when
     *              creating the directory
     * @throws IOException
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        S3SftpPath s3Directory = checkPath(dir);
        if (s3Directory.toString().equals(PATH_SEPARATOR) || s3Directory.toString().isEmpty()) {
            throw new FileAlreadyExistsException("Root directory already exists");
        }
        String directoryKey = s3Directory.toRealPath(LinkOption.NOFOLLOW_LINKS).getKey();
        if (!directoryKey.endsWith(PATH_SEPARATOR) && !directoryKey.isEmpty()) {
            directoryKey = directoryKey + PATH_SEPARATOR;
        }
        // 移除缓存，移除父目录缓存
        DirectoryContentsNamesCacheUtil.removeCacheValue(s3Directory.getFileSystem(), directoryKey);
        try {
            S3SftpFileSystem fileSystem = s3Directory.getFileSystem();
            fileSystem.client().putObjectToCreateDirectory(s3Directory.bucketName(), directoryKey)
                    .get(fileSystem.configuration().timeout(), fileSystem.configuration().timeoutUnit());
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 用于删除指定路径对应的文件或目录，是文件系统操作中删除资源的核心方法
     *
     * @param path the path to the file to delete
     * @throws IOException
     */
    @Override
    public void delete(Path path) throws IOException {
        S3SftpPath deletePath = checkPath(path);
        String deletePathKey = deletePath.toRealPath(LinkOption.NOFOLLOW_LINKS).getKey();
        // 判断如果是根目录，不允许删除
        S3SftpClient s3Client = deletePath.getFileSystem().client();
        S3SftpBasicFileAttributes s3SftpBasicFileAttributes = S3SftpBasicFileAttributes.getNoCache(deletePath);
        boolean directory = s3SftpBasicFileAttributes.isDirectory();
        String bucketName = deletePath.bucketName();
        S3SftpNioSpiConfiguration configuration = deletePath.getFileSystem().configuration();
        Long timeout = configuration.timeout();
        TimeUnit timeoutUnit = configuration.timeoutUnit();

        if (!directory) {
            // 是文件，可以删除
            delPath(s3Client, bucketName, deletePathKey, timeout, timeoutUnit);
        }
        if (directory) {
            deletePathKey = deletePathKey.endsWith("/") ? deletePathKey : deletePathKey + PATH_SEPARATOR;
            boolean emptyDirectory = s3SftpBasicFileAttributes.getDirEmpty();
            if (emptyDirectory) {
                // 是空目录，可以删除
                delPath(s3Client, bucketName, deletePathKey, timeout, timeoutUnit);
            } else {
                throw new DirectoryNotEmptyException("dir is not empty");
            }
        }
        // 移除缓存
        UserPathFileAttributesCacheUtil.removeCacheValue(deletePath.toRealPath(LinkOption.NOFOLLOW_LINKS));
        DirectoryContentsNamesCacheUtil.removeCacheValue(deletePath.getFileSystem(), deletePathKey);
    }

    /**
     * 用于复制文件或目录
     *
     * @param source  the path to the file to copy
     * @param target  the path to the target file
     * @param options options specifying how the copy should be done
     * @throws IOException
     */
    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        // If both paths point to the same object, this is a no-op
        if (source.equals(target)) {
            return;
        }

        var s3SourcePath = checkPath(source);
        var s3TargetPath = checkPath(target);
        // 移除缓存
        DirectoryContentsNamesCacheUtil.removeCacheValue(s3TargetPath.getFileSystem(), s3TargetPath.getKey());

        final var s3Client = s3SourcePath.getFileSystem().client();
        final var sourceBucket = s3SourcePath.bucketName();

        final var timeOut = 1L;
        final var unit = MINUTES;

        var fileExistsAndCannotReplace = cannotReplaceAndFileExistsCheck(options, s3Client);

        try {
            var sourcePrefix = s3SourcePath.toRealPath(NOFOLLOW_LINKS).getKey();

            List<String> sourceKeys;
            String prefixWithSeparator;
            if (s3SourcePath.isDirectory()) {
                sourceKeys = getContainedObjectBatches(s3Client, sourceBucket, sourcePrefix, timeOut, unit);
                prefixWithSeparator = sourcePrefix;
            } else {
                sourceKeys = List.of(sourcePrefix);
                prefixWithSeparator = sourcePrefix.substring(0, sourcePrefix.lastIndexOf(PATH_SEPARATOR)) + PATH_SEPARATOR;
            }

            for (var key : sourceKeys) {
                copyKey(s3Client,key, prefixWithSeparator, sourceBucket, s3TargetPath, fileExistsAndCannotReplace).get(timeOut, unit);
            }

        } catch (TimeoutException e) {
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

    }

    private Function<S3SftpPath, Boolean> cannotReplaceAndFileExistsCheck(CopyOption[] options, S3SftpClient s3Client) {
        final var canReplaceFile = Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING);

        return (S3SftpPath destination) -> {
            if (canReplaceFile) {
                return false;
            }
            return exists(s3Client, destination);
        };
    }

    private CompletableFuture<CompletedCopy> copyKey(
            S3SftpClient s3Client,
            String sourceObjectIdentifierKey,
            String sourcePrefix,
            String sourceBucket,
            S3SftpPath targetPath,
            Function<S3SftpPath, Boolean> fileExistsAndCannotReplaceFn
    ) throws FileAlreadyExistsException {
        final var sanitizedIdKey = sourceObjectIdentifierKey.replaceFirst(sourcePrefix, "");

        // should resolve if the target path is a dir
        if (targetPath.isDirectory()) {
            targetPath = (S3SftpPath) targetPath.resolve(sanitizedIdKey);
        }

        if (fileExistsAndCannotReplaceFn.apply(targetPath)) {
            throw new FileAlreadyExistsException("File already exists at the target key");
        }

        return s3Client.copyObject(sourceBucket, sourceObjectIdentifierKey, targetPath.bucketName(), targetPath.getKey());
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        this.copy(source, target, options);
        this.delete(source);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return path.toRealPath(NOFOLLOW_LINKS).equals(path2.toRealPath(NOFOLLOW_LINKS));
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    /**
     * 检查当前程序对指定路径（文件 / 目录）是否拥有特定的访问权限，若权限不足则直接抛出异常，无异常则表示权限满足。
     * 对象存储通常不包含这些信息，文件或者目录是否可读可写可执行所以忽略访问权限检查，只检查对象是否存在
     *
     * @param path  the path to the file to check
     * @param modes The access modes to check; may have zero elements
     * @throws IOException
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        S3SftpPath checkPath = checkPath(path);
        S3SftpPath realPath = checkPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
        try {
            S3SftpBasicFileAttributes.get(realPath);
        } catch (NoSuchFileException e) {
            throw new NoSuchFileException(realPath.toString());
        } catch (IOException e) {
            throw new IOException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        Objects.requireNonNull(type, "the type of attribute view required cannot be null");
        S3SftpPath s3SftpPath = checkPath(path);
        if (type.equals(BasicFileAttributes.class)) {
            S3SftpFileAttributeView s3SftpFileAttributeView = new S3SftpFileAttributeView(s3SftpPath);
            return (V) s3SftpFileAttributeView;
        } else {
            return null;
        }
    }

    /**
     * 获取 path 的属性信息
     *
     * @param path    the path to the file
     * @param type    the {@code Class} of the file attributes required
     *                to read
     * @param options options indicating how symbolic links are handled
     * @param <A>
     * @return
     * @throws IOException
     */
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {

        Objects.requireNonNull(type);
        Path s3Path = checkPath(path);

        if (type.equals(BasicFileAttributes.class)) {
            @SuppressWarnings("unchecked")
            A a = (A) S3SftpBasicFileAttributes.get((S3SftpPath) s3Path);
            return a;
        } else {
            throw new UnsupportedOperationException("cannot read attributes of type: " + type);
        }

    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {

        Objects.requireNonNull(attributes);
        var s3Path = checkPath(path);

        if (attributes.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        return S3SftpBasicFileAttributes.get(s3Path).toMap();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("s3 file attributes cannot be modified by this class");
    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        S3SftpFileSystem fs = (S3SftpFileSystem) getFileSystem(path.toUri());
        S3SftpSeekableByteChannel s3SeekableByteChannel = new S3SftpSeekableByteChannel((S3SftpPath) path, fs.client(), options);
        return new S3SftpFileChannel(s3SeekableByteChannel);
    }

    /**
     * 判断文件是否存在
     *
     * @param s3SftpPath 文件
     */
    public Boolean exists(S3SftpClient s3Client, S3SftpPath s3SftpPath) {
        // TODO 增加缓存，耗时操作  ？？？
        try {
            ObjectHeadResponse objectHeadResponse = s3Client.headObject(s3SftpPath.bucketName(), s3SftpPath.getKey());
            if (objectHeadResponse == null) {
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.debug("Could not retrieve object head information", e);
            return false;
        }
    }

    void closeFileSystem(FileSystem fs) {
        for (var key : FS_CACHE.keySet()) {
            if (fs == FS_CACHE.get(key)) {
                try (FileSystem closeable = FS_CACHE.remove(key)) {
                    closeFileSystemIfOpen(closeable);
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            closeFileSystemIfOpen(fs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeFileSystemIfOpen(FileSystem fs) throws IOException {
        if (fs.isOpen()) {
            fs.close();
        }
    }
}
