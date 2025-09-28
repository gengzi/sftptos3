package com.gengzi.sftp.nio;

import com.gengzi.sftp.s3.client.S3SftpClient;
import com.gengzi.sftp.util.S3DirectBufferUtil;
import com.gengzi.sftp.util.SafeDirectBuffer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 定义s3 sftp 读通道
 */
public class S3SftpReadableByteChannel implements ReadableByteChannel {


    private static final Logger logger = LoggerFactory.getLogger(S3SftpReadableByteChannel.class);
    private final S3SftpClient client;
    private final S3SftpPath path;
    private final S3SftpSeekableByteChannel delegator;
    private final int maxFragmentSize;
    private final int maxNumberFragments;
    private final int numFragmentsInObject;
    private final long size;
    private final Long timeout;
    private final TimeUnit timeUnit;
    private final Cache<Integer, CompletableFuture<ByteBuffer>> readAheadBuffersCache;
    private boolean open;


    /**
     * @param path
     * @param maxFragmentSize
     * @param maxNumberFragments
     * @param client
     * @param delegator
     * @param timeout
     * @param timeUnit
     * @throws IOException
     */
    S3SftpReadableByteChannel(S3SftpPath path, int maxFragmentSize, int maxNumberFragments, S3SftpClient client,
                              S3SftpSeekableByteChannel delegator, Long timeout, TimeUnit timeUnit) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(client);
        Objects.requireNonNull(delegator);
        if (maxFragmentSize < 1) {
            throw new IllegalArgumentException("maxFragmentSize must be >= 1");
        }
        if (maxNumberFragments < 2) {
            throw new IllegalArgumentException("maxNumberFragments must be >= 2");
        }

        logger.debug("max read ahead fragments '{}' with size '{}' bytes", maxNumberFragments, maxFragmentSize);
        this.client = client;
        this.path = path;
        this.delegator = delegator;
        this.size = delegator.size();
        // 设置当前每个分片的大小size
        this.maxFragmentSize = maxFragmentSize;
        // 根据整个文件大小除于分片大小，得到分片数量
        this.numFragmentsInObject = (int) Math.ceil((float) size / (float) maxFragmentSize);
        // 构建缓存，并设置最大缓存数量  如果一个分片是64kb 最大设置20个。 意味着缓存中能存储1280kb
        this.readAheadBuffersCache = Caffeine.newBuilder()
                .maximumSize(maxNumberFragments)
                .expireAfterAccess(5, TimeUnit.MINUTES) // 5分钟未访问自动淘汰
                .weakValues() // 无强引用时允许GC回收
                .recordStats()
                .removalListener((Integer key, CompletableFuture<ByteBuffer> value, RemovalCause cause) -> {
                    logger.debug("Removed from cache: {}", key);
                    if (value != null && value.isDone()) {
                        try {
                            S3DirectBufferUtil.freeDirectBuffer(value.get());
                        } catch (Exception e) {
                            value = null;
                            // 处理异常
                            logger.error("Error while cleaning up direct buffer: " + e.getMessage(), e);
                        }
                    }
                    logger.debug("value :{}",value);
                })
                .build();
        // 最大分片数
        this.maxNumberFragments = maxNumberFragments;
        this.open = true;
        // 调用s3的超时时间
        this.timeout = timeout != null ? timeout : 5L;
        this.timeUnit = timeUnit != null ? timeUnit : TimeUnit.MINUTES;
    }


    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> An attempt is made to read up to <i>r</i> bytes from the channel,
     * where <i>r</i> is the number of bytes remaining in the buffer, that is,
     * {@code dst.remaining()}, at the moment this method is invoked.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is read, where
     * {@code 0}&nbsp;{@code <=}&nbsp;<i>n</i>&nbsp;{@code <=}&nbsp;<i>r</i>.
     * This byte sequence will be transferred into the buffer so that the first
     * byte in the sequence is at index <i>p</i> and the last byte is at index
     * <i>p</i>&nbsp;{@code +}&nbsp;<i>n</i>&nbsp;{@code -}&nbsp;{@code 1},
     * where <i>p</i> is the buffer's position at the moment this method is
     * invoked.  Upon return the buffer's position will be equal to
     * <i>p</i>&nbsp;{@code +}&nbsp;<i>n</i>; its limit will not have changed.
     *
     * <p> A read operation might not fill the buffer, and in fact it might not
     * read any bytes at all.  Whether or not it does so depends upon the
     * nature and state of the channel.  A socket channel in non-blocking mode,
     * for example, cannot read any more bytes than are immediately available
     * from the socket's input buffer; similarly, a file channel cannot read
     * any more bytes than remain in the file.  It is guaranteed, however, that
     * if a channel is in blocking mode and there is at least one byte
     * remaining in the buffer then this method will block until at least one
     * byte is read.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a read operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. </p>
     *
     * @param dst The buffer into which bytes are to be transferred
     * @return The number of bytes read, possibly zero, or {@code -1} if the
     * channel has reached end-of-stream
     * @throws IllegalArgumentException    If the buffer is read-only
     * @throws NonReadableChannelException If this channel was not opened for reading
     * @throws ClosedChannelException      If this channel is closed
     * @throws AsynchronousCloseException  If another thread closes this channel
     *                                     while the read operation is in progress
     * @throws ClosedByInterruptException  If another thread interrupts the current thread
     *                                     while the read operation is in progress, thereby
     *                                     closing the channel and setting the current thread's
     *                                     interrupt status
     * @throws IOException                 If some other I/O error occurs
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        // 获取目标要读取的文件内容长度
        logger.info("dst read length:{}", dst.limit() - dst.position());
        Objects.requireNonNull(dst);
        // 获取的当前通道的位置
        var channelPosition = delegator.position();
        logger.debug("delegator position: {}", channelPosition);

        // 如果当前通道的位置已经超过size说明已经读取完毕了
        // if the position of the delegator is at the end (>= size) return -1. we're finished reading.
        if (channelPosition >= size) {
            return -1;
        }

        // 通过当前位置除于分片大小得到分片索引 （ 100 /  3kb = 33.33 向下取整 33）
        //figure out the index of the fragment the bytes would start in
        var fragmentIndex = fragmentIndexForByteNumber(channelPosition);
        logger.debug("fragment index: {}", fragmentIndex);
        // 通过当前位置减去分片索引乘以分片大小得到分片在文件中的偏移量(100- 33*3kb = 1)
        var fragmentOffset = (int) (channelPosition - (fragmentIndex.longValue() * maxFragmentSize));
        logger.debug("fragment {} offset: {}", fragmentIndex, fragmentOffset);

        try {
            final ByteBuffer fragment = Objects.requireNonNull(readAheadBuffersCache.get(fragmentIndex, this::computeFragmentFuture))
                    .get(timeout, timeUnit);

            fragment.position(fragmentOffset);
            logger.debug("fragment remaining: {}", fragment.remaining());
            logger.debug("dst remaining: {}", dst.remaining());

            //put the bytes from fragment from the offset upto the min of fragment remaining or dst remaining
            var limit = Math.min(fragment.remaining(), dst.remaining());
            logger.debug("byte limit: {}", limit);

            // 记录fragment的当前limit，避免修改原缓冲区
            int originalFragmentLimit = fragment.limit();
            // 限制拷贝长度
            fragment.limit(fragment.position() + limit);
            // 直接从fragment拷贝到dst，无临时数组
            dst.put(fragment);
            // 恢复fragment的limit
            fragment.limit(originalFragmentLimit);

            int copiedBytes = limit; // 直接使用limit作为拷贝字节数


            if (fragment.position() >= fragment.limit() / 2) {

                // clear any fragments in cache that are lower index than this one
                clearPriorFragments(fragmentIndex);

                // until available cache slots are filled or number of fragments in file
                var maxFragmentsToLoad = Math.min(maxNumberFragments - 1, numFragmentsInObject - fragmentIndex - 1);

                for (var i = 0; i < maxFragmentsToLoad; i++) {
                    final var idxToLoad = i + fragmentIndex + 1;

                    //  add the index if it's not already there
                    if (readAheadBuffersCache.asMap().containsKey(idxToLoad)) {
                        continue;
                    }

                    logger.debug("initiate pre-loading fragment with index '{}' from '{}'", idxToLoad, path.toUri());
                    readAheadBuffersCache.put(idxToLoad, computeFragmentFuture(idxToLoad));
                }
            }

            delegator.position(channelPosition + copiedBytes);
            logger.info("read data length:{}", copiedBytes);
            return copiedBytes;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // the async execution completed exceptionally.
            // not currently obvious when this will happen or if we can recover
            logger.error(
                    "an exception occurred while reading bytes from {} that was not recovered by the S3 Client RetryCondition(s)",
                    path.toUri(), e);
            throw new IOException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tells whether or not this channel is open.
     *
     * @return {@code true} if, and only if, this channel is open
     */
    @Override
    public boolean isOpen() {
        return open;
    }

    /**
     * Closes this channel.
     *
     * <p> After a channel is closed, any further attempt to invoke I/O
     * operations upon it will cause a {@link ClosedChannelException} to be
     * thrown.
     *
     * <p> If this channel is already closed then invoking this method has no
     * effect.
     *
     * <p> This method may be invoked at any time.  If some other thread has
     * already invoked it, however, then another invocation will block until
     * the first invocation is complete, after which it will return without
     * effect. </p>
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        open = false;
        readAheadBuffersCache.invalidateAll();
        readAheadBuffersCache.cleanUp();
    }


    private void clearPriorFragments(int currentFragIndx) {
        final Set<@NonNull Integer> priorIndexes = readAheadBuffersCache
                .asMap()
                .keySet().stream()
                .filter(idx -> idx < currentFragIndx)
                .collect(Collectors.toSet());

        if (!priorIndexes.isEmpty()) {
            logger.debug("invalidating fragment(s) '{}' from '{}'",
                    priorIndexes.stream().map(Objects::toString).collect(Collectors.joining(", ")), path.toUri());
            readAheadBuffersCache.invalidateAll(priorIndexes);
        }
    }

    /**
     * The number of fragments currently in the cache.
     *
     * @return the size of the cache after any async evictions or reloads have happened.
     */
    int numberOfCachedFragments() {
        readAheadBuffersCache.cleanUp();
        return (int) readAheadBuffersCache.estimatedSize();
    }

    /**
     * Obtain a snapshot of the statistics of the internal cache, provides information about
     * hits, misses, requests, evictions etc. that are useful for tuning.
     *
     * @return the statistics of the internal cache.
     */
    CacheStats cacheStatistics() {
        return readAheadBuffersCache.stats();
    }

    private CompletableFuture<ByteBuffer> computeFragmentFuture(int fragmentIndex) {
        var readFrom = (long) fragmentIndex * maxFragmentSize;
        var length = Math.min(readFrom + maxFragmentSize, size);
        logger.debug("byte range for {} readForm '{}' length '{}'", path.getKey(), readFrom, length);

        return client.getObject(path.bucketName(), path.getKey(), readFrom, length);
    }

    /**
     * Compute which buffer a byte should be in
     *
     * @param byteNumber the number of the byte in the object accessed by this channel
     * @return the index of the fragment in which {@code byteNumber} will be found.
     */
    Integer fragmentIndexForByteNumber(long byteNumber) {
        return Math.toIntExact(Math.floorDiv(byteNumber, (long) maxFragmentSize));
    }

    public int getMaxFragmentSize() {
        return maxFragmentSize;
    }

    public int getMaxNumberFragments() {
        return maxNumberFragments;
    }
}
