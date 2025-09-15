package com.gengzi.sftp.s3.client;

import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;
import com.gengzi.sftp.nio.constans.Constants;
import com.gengzi.sftp.s3.client.entity.ObjectHeadResponse;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultAwsS3SftpClient extends AbstractS3SftpClient<S3AsyncClient> {

    private static final char PATH_SEPARATOR_CHAR = Constants.PATH_SEPARATOR.charAt(0);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultAwsS3SftpClient(S3SftpNioSpiConfiguration s3SftpNioSpiConfiguration) {
        super(s3SftpNioSpiConfiguration);
    }

    private static boolean isDirectoryString(String path) {
        return path.isEmpty()
                || hasTrailingSeparatorString(path)
                || ".".equals(path)
                || "..".equals(path)
                || path.endsWith(PATH_SEPARATOR_CHAR + ".")
                || path.endsWith(PATH_SEPARATOR_CHAR + "..");
    }

    private static boolean hasTrailingSeparatorString(String path) {
        if (path.isEmpty()) {
            return false;
        }
        return path.charAt(path.length() - 1) == PATH_SEPARATOR_CHAR;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            // 空路径表示根目录
            return "";
        }
        return path.endsWith("/") ? path : path + "/";
    }

    @NotNull
    private static ObjectHeadResponse getDirHeadResponse(String key, ListObjectsV2Publisher objectsAttributes) throws NoSuchFileException {
        SdkPublisher<S3Object> contents = objectsAttributes.contents();
        SdkPublisher<CommonPrefix> commonPrefixSdkPublisher = objectsAttributes.commonPrefixes();
        Single<Boolean> empty = Flowable.concat(commonPrefixSdkPublisher, contents).isEmpty();
        if (!empty.blockingGet()) {
            boolean isEmptyDirectory = false;
            List<String> directoryNames = null;
            Flowable<CommonPrefix> commonPrefixFlowable = Flowable.fromPublisher(commonPrefixSdkPublisher);
            Flowable<S3Object> contentsFlowable = Flowable.fromPublisher(contents);
            // 判断是否为空目录
            if (commonPrefixFlowable.isEmpty().blockingGet() && contentsFlowable.count().blockingGet() == 1L) {
                Maybe<S3Object> s3ObjectMaybe = contentsFlowable.take(1).singleElement();
                S3Object s3Object = s3ObjectMaybe.blockingGet();
                String objectKey = s3Object.key();
                if (objectKey.equals(key)) {
                    isEmptyDirectory = true;
                }
            }else{
                Flux<String> prefixMap = Flux.from(commonPrefixFlowable).map(commonPrefix -> commonPrefix.prefix());
                Flux<String> keyMap = Flux.from(contents).map(s3Object -> s3Object.key());
                directoryNames = Flux.concat(prefixMap, keyMap).collectList().block();
            }
            return new ObjectHeadResponse(
                    FileTime.fromMillis(0),
                    0L,
                    null,
                    true,
                    false,
                    isEmptyDirectory,
                    directoryNames
            );
        } else {
            throw new NoSuchFileException("no such file,path:" + key);
        }
    }

    private static CompletableFuture<List<String>> listObjectsRecursively(
            S3AsyncClient client, ListObjectsV2Request request, List<String> allFiles) {

        return client.listObjectsV2(request)
                .thenCompose(response -> {
                    // 处理当前页文件
                    allFiles.addAll(response.contents().stream().map(S3Object::key).toList());
                    allFiles.addAll(response.commonPrefixes().stream().map(CommonPrefix::prefix).toList());
                    // 若有更多结果，继续异步获取下一页
                    if (response.isTruncated()) {
                        ListObjectsV2Request nextRequest = request.toBuilder()
                                .continuationToken(response.nextContinuationToken())
                                .build();
                        return listObjectsRecursively(client, nextRequest, allFiles);
                    }
                    return CompletableFuture.completedFuture(allFiles);
                });
    }

    /**
     * s3客户端名称
     *
     * @return
     */
    @Override
    public String clietName() {
        return S3ClientNameEnum.DEFAULT_AWS_S3.name();
    }

    @Override
    public S3AsyncClient createClient(S3SftpNioSpiConfiguration s3SftpNioSpiConfiguration) {

        return S3AsyncClient.builder()
                .endpointOverride(s3SftpNioSpiConfiguration.endpointUri())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3SftpNioSpiConfiguration.accessKey(), s3SftpNioSpiConfiguration.secretKey())
                ))
                .serviceConfiguration(service -> service
                        .pathStyleAccessEnabled(true)
                )
                .build();
    }

    /**
     * 从对象存储中获取一个文件内容并返回到ByteBuffer
     *
     * @param bucketName 桶
     * @param key        对象key
     * @param offset     偏移量
     * @param length     长度
     * @return
     */
    @Override
    public CompletableFuture<ByteBuffer> getObject(String bucketName, String key, long offset, long length) {
        logger.debug("getObject bucketName:{},key:{},offset:{},length:{}", bucketName, key, offset, length);
        long readFrom = offset;
        long readTo = offset + length - 1;
        String range = "bytes=" + readFrom + "-" + readTo;
        logger.debug("byte range for {} is '{}'", key, range);
        try (S3AsyncClient s3AsyncClient = this.s3Client) {
            return s3AsyncClient.getObject(
                            builder -> builder
                                    .bucket(bucketName)
                                    .key(key)
                                    .range(range),
                            AsyncResponseTransformer.toBytes())
                    .thenApply(BytesWrapper::asByteBuffer);
        }

    }

    /**
     * 调用s3创建一个空目录
     *
     * @param bucketName
     * @param directoryKey
     * @return
     */
    @Override
    public CompletableFuture putObjectToCreateDirectory(String bucketName, String directoryKey) {
        logger.debug("getObject bucketName:{},directoryKey:{}", bucketName, directoryKey);
        return this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(directoryKey)
                        .build(),
                AsyncRequestBody.empty()
        );
    }

    /**
     * 删除一个对象
     *
     * @param bucketName
     * @param deletePathKey
     * @return
     */
    @Override
    public CompletableFuture<?> deleteObject(String bucketName, String deletePathKey) {
        logger.debug("deleteObject bucketName:{},deletePathKey:{}", bucketName, deletePathKey);
        return this.s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(deletePathKey)
                        .build()
        );
    }

    /**
     * copy到目标对象
     *
     * @param sourceBucketName
     * @param sourceKey
     * @param destinationBucketName
     * @param destinationKey
     * @return
     */
    @Override
    public CompletableFuture<?> copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        try (S3TransferManager s3TransferManager = S3TransferManager.builder().s3Client(this.s3Client).build()) {
            return s3TransferManager.copy(CopyRequest.builder()
                    .copyObjectRequest(CopyObjectRequest.builder()
                            .checksumAlgorithm(ChecksumAlgorithm.SHA256)
                            .sourceBucket(sourceBucketName)
                            .sourceKey(sourceKey)
                            .destinationBucket(destinationBucketName)
                            .destinationKey(destinationKey)
                            .build())
                    .build()).completionFuture();
        }
    }

    /**
     * 从对象存储中获取文件内容并保存到本地文件中
     *
     * @param bucketName
     * @param key
     * @param destination
     * @return
     */
    @Override
    public void getObjectAndWriteToLocalFile(String bucketName, String key, Path destination) throws IOException {
        logger.info("getObjectAndWriteToLocalFile bucketName:{},key:{},Path:{} ", bucketName, key, destination);
        try (S3TransferManager s3TransferManager = S3TransferManager.builder().s3Client(this.s3Client).build()) {
            CompletableFuture<CompletedFileDownload> downloadCompletableFuture = s3TransferManager.downloadFile(
                    DownloadFileRequest.builder()
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(key)
                                    .build())
                            .destination(destination)
                            .build()
            ).completionFuture();

            if (false) {
                try {
                    downloadCompletableFuture.get(600L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Could not open the path:" + key, e);
                } catch (TimeoutException | ExecutionException e) {
                    throw new IOException("Could not open the path:" + key, e);
                }
            } else {
                downloadCompletableFuture.join();
            }


        }
    }

    /**
     * 上传本地文件到对象存储中
     *
     * @param bucketName
     * @param key
     * @param localFile
     * @return
     */
    @Override
    public void putObjectByLocalFile(String bucketName, String key, Path localFile) throws IOException {
        logger.info("putObjectByLocalFile bucketName:{},key:{},Path:{} ", bucketName, key, localFile);
        try (S3TransferManager s3TransferManager = S3TransferManager.builder().s3Client(this.s3Client).build()) {
            CompletableFuture<CompletedFileUpload> uploadCompletableFuture = s3TransferManager.uploadFile(
                    UploadFileRequest.builder()
                            .putObjectRequest(PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(key)
                                    .contentType(Files.probeContentType(localFile))
                                    .build())
                            .source(localFile)
                            .build()
            ).completionFuture();

            // TODO: 上传文件超时配置新增
            if (false) {
                // 在超时时间内等待获取结果
                uploadCompletableFuture.get(600L, TimeUnit.MILLISECONDS);
            } else {
                // 会阻塞当前线程，直到异步任务执行完成
                uploadCompletableFuture.join();
            }
        } catch (InterruptedException e) {
            // 如果是中断异常，表示有其他线程调用了该线程的 interrupt 方法，会把中断标志改为false，清除中断标志位信息。
            // 调用此方法，重新设置中断标志位为 true
            Thread.currentThread().interrupt();
            throw new IOException("Could not write to path:" + key, e);
        } catch (TimeoutException | ExecutionException | IOException e) {
            throw new IOException("Could not write to path:" + key, e);
        }
    }

    /**
     * 获取当前key目录下的所有文件或者子目录名称
     *
     * @param bucketName
     * @param prefixKey
     * @return
     */
    @Override
    public CompletableFuture<List<String>> getCurrentKeyDirAllFileNames(String bucketName, String prefixKey) {
        logger.debug("getCurrentKeyDirAllFileNames bucketName:{},key:{},Path:{} ", bucketName, prefixKey);
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefixKey)
                .delimiter("/")
                .maxKeys(1000)
                .build();
        S3AsyncClient client = this.s3Client;
        ArrayList<String> allFiles = new ArrayList<>();
        return listObjectsRecursively(client, request, allFiles);
    }

    /**
     * 获取对象的基础元信息（支持对象文件或者对象"目录"）
     *
     * @param bucketName
     * @param key
     * @return 如果无此对象，必须返回 null
     */
    @Override
    public ObjectHeadResponse headFileOrDirObject(String bucketName, String key) throws IOException {
        logger.debug("headFileOrDirObject bucketName:{},key:{} ", bucketName, key);
        if (isDirectoryString(key)) {
            ListObjectsV2Publisher objectsAttributes = getObjectsAttributes(bucketName, key);
            return getDirHeadResponse(key, objectsAttributes);
        }
        HeadObjectResponse response = getObjectAttributes(bucketName, key);
        // 判断key是否为目录对象
        if (response == null) {
            ListObjectsV2Publisher objectsAttributes = getObjectsAttributes(bucketName, key);
            return getDirHeadResponse(key, objectsAttributes);
        }
        return new ObjectHeadResponse(
                FileTime.from(response.lastModified()),
                response.contentLength(),
                response.eTag(),
                false,
                true,
                false,
                null
        );

    }

    /**
     * 获取对象的基础元信息
     *
     * @param bucketName
     * @param key
     * @return 如果无此对象，必须返回 null
     */
    @Override
    public ObjectHeadResponse headObject(String bucketName, String key) throws IOException {
        logger.debug("headObject bucketName:{},key:{} ", bucketName, key);
        HeadObjectResponse response = getObjectAttributes(bucketName, key);
        if (response == null) {
            return null;
        }
        return new ObjectHeadResponse(
                FileTime.from(response.lastModified()),
                response.contentLength(),
                response.eTag(),
                false,
                true,
                false,
                null
        );
    }


    private HeadObjectResponse getObjectAttributes(String bucketName, String key) throws IOException {
        Long timeout = this.configuration.timeout();
        TimeUnit timeUnit = this.configuration.timeoutUnit();
        try {
            return this.s3Client.headObject(req -> req
                    .bucket(bucketName)
                    .key(key)
            ).get(timeout, timeUnit);
        } catch (NoSuchKeyException e) {
            return null;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchKeyException) {
                return null;
            }
            String errMsg = String.format("path: %s getFileAttributes error!!! req s3 server :%s",
                    key, e.getCause().toString());
            logger.error(errMsg);
            throw new IOException(errMsg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (TimeoutException e) {
            throw new IOException("path " + key + "getFileAttributes timeout " + timeout + ",timeUnit" + timeUnit.toString(), e);
        }
    }

    private ListObjectsV2Publisher getObjectsAttributes(String bucketName, String key) {
        String keyDir = normalizePath(key);
        return this.s3Client.listObjectsV2Paginator(req -> req
                .bucket(bucketName)
                .prefix(keyDir)
                .delimiter(Constants.PATH_SEPARATOR));
    }

}
