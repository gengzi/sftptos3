package com.gengzi.sftp.nio;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.*;

/**
 * filechannel
 * 定制了大量文件专属操作能力
 */
public class S3SftpFileChannel extends FileChannel {

    private S3SftpSeekableByteChannel seekableChannel;


    public S3SftpFileChannel(S3SftpSeekableByteChannel seekableChannel) {
        this.seekableChannel = seekableChannel;
    }

    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> Bytes are read starting at this channel's current file position, and
     * then the file position is updated with the number of bytes actually
     * read.  Otherwise this method behaves exactly as specified in the {@link
     * ReadableByteChannel} interface. </p>
     *
     * @param dst
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        return seekableChannel.read(dst);
    }

    /**
     * 这段代码是Java NIO中FileChannel类的read方法注释，功能是从文件通道读取字节序列到指定的缓冲区数组中。主要特点：
     * 从通道当前文件位置开始读取数据
     * 读取后会更新文件位置指针
     * 支持将数据分散读取到多个缓冲区（Scattering读取）
     * 参数包括目标缓冲区数组、起始偏移量和长度
     * Reads a sequence of bytes from this channel into a subsequence of the
     * given buffers.
     *
     * <p> Bytes are read starting at this channel's current file position, and
     * then the file position is updated with the number of bytes actually
     * read.  Otherwise this method behaves exactly as specified in the {@link
     * ScatteringByteChannel} interface.  </p>
     *
     * @param dsts
     * @param offset
     * @param length
     */
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        // 从当前位置读取文件内容，到不同的缓冲区数组中
        long totalBytesRead = 0L;
        for (int i = offset; i < offset + length - 1; i++) {
            int read = read(dsts[i]);
            if (read == -1) {
                break;
            }
            totalBytesRead += read;
        }
        return totalBytesRead;
    }

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> Bytes are written starting at this channel's current file position
     * unless the channel is in append mode, in which case the position is
     * first advanced to the end of the file.  The file is grown, if necessary,
     * to accommodate the written bytes, and then the file position is updated
     * with the number of bytes actually written.  Otherwise this method
     * behaves exactly as specified by the {@link WritableByteChannel}
     * interface. </p>
     *
     * @param src
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        return seekableChannel.write(src);
    }

    /**
     * Writes a sequence of bytes to this channel from a subsequence of the
     * given buffers.
     *
     * <p> Bytes are written starting at this channel's current file position
     * unless the channel is in append mode, in which case the position is
     * first advanced to the end of the file.  The file is grown, if necessary,
     * to accommodate the written bytes, and then the file position is updated
     * with the number of bytes actually written.  Otherwise this method
     * behaves exactly as specified in the {@link GatheringByteChannel}
     * interface.  </p>
     *
     * @param srcs
     * @param offset
     * @param length
     */
    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        int bytesWritten = 0;
        for (int i = offset; i < offset + length; i++) {
            ByteBuffer src = srcs[i];
            bytesWritten += write(src);
        }
        return bytesWritten;
    }

    /**
     * Returns this channel's file position.
     *
     * @return This channel's file position,
     * a non-negative integer counting the number of bytes
     * from the beginning of the file to the current position
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException            If some other I/O error occurs
     */
    @Override
    public long position() throws IOException {
        return seekableChannel.position();
    }

    /**
     * Sets this channel's file position.
     *
     * <p> Setting the position to a value that is greater than the file's
     * current size is legal but does not change the size of the file.  A later
     * attempt to read bytes at such a position will immediately return an
     * end-of-file indication.  A later attempt to write bytes at such a
     * position will cause the file to be grown to accommodate the new bytes;
     * the values of any bytes between the previous end-of-file and the
     * newly-written bytes are unspecified.  </p>
     *
     * @param newPosition The new position, a non-negative integer counting
     *                    the number of bytes from the beginning of the file
     * @return This file channel
     * @throws ClosedChannelException   If this channel is closed
     * @throws IllegalArgumentException If the new position is negative
     * @throws IOException              If some other I/O error occurs
     */
    @Override
    public FileChannel position(long newPosition) throws IOException {
        seekableChannel.position(newPosition);
        return this;
    }

    /**
     * Returns the current size of this channel's file.
     *
     * @return The current size of this channel's file,
     * measured in bytes
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException            If some other I/O error occurs
     */
    @Override
    public long size() throws IOException {
        return seekableChannel.size();
    }

    /**
     * Truncates this channel's file to the given size.
     *
     * <p> If the given size is less than the file's current size then the file
     * is truncated, discarding any bytes beyond the new end of the file.  If
     * the given size is greater than or equal to the file's current size then
     * the file is not modified.  In either case, if this channel's file
     * position is greater than the given size then it is set to that size.
     * </p>
     *
     * @param size The new size, a non-negative byte count
     * @return This file channel
     * @throws NonWritableChannelException If this channel was not opened for writing
     * @throws ClosedChannelException      If this channel is closed
     * @throws IllegalArgumentException    If the new size is negative
     * @throws IOException                 If some other I/O error occurs
     */
    @Override
    public FileChannel truncate(long size) throws IOException {
        seekableChannel.truncate(size);
        return this;
    }

    /**
     * Forces any updates to this channel's file to be written to the storage
     * device that contains it.
     *
     * <p> If this channel's file resides on a local storage device then when
     * this method returns it is guaranteed that all changes made to the file
     * since this channel was created, or since this method was last invoked,
     * will have been written to that device.  This is useful for ensuring that
     * critical information is not lost in the event of a system crash.
     *
     * <p> If the file does not reside on a local device then no such guarantee
     * is made.
     *
     * <p> The {@code metaData} parameter can be used to limit the number of
     * I/O operations that this method is required to perform.  Passing
     * {@code false} for this parameter indicates that only updates to the
     * file's content need be written to storage; passing {@code true}
     * indicates that updates to both the file's content and metadata must be
     * written, which generally requires at least one more I/O operation.
     * Whether this parameter actually has any effect is dependent upon the
     * underlying operating system and is therefore unspecified.
     *
     * <p> Invoking this method may cause an I/O operation to occur even if the
     * channel was only opened for reading.  Some operating systems, for
     * example, maintain a last-access time as part of a file's metadata, and
     * this time is updated whenever the file is read.  Whether or not this is
     * actually done is system-dependent and is therefore unspecified.
     *
     * <p> This method is only guaranteed to force changes that were made to
     * this channel's file via the methods defined in this class.  It may or
     * may not force changes that were made by modifying the content of a
     * {@link MappedByteBuffer <i>mapped byte buffer</i>} obtained by
     * invoking the {@link #map map} method.  Invoking the {@link
     * MappedByteBuffer#force force} method of the mapped byte buffer will
     * force changes made to the buffer's content to be written.  </p>
     *
     * @param metaData If {@code true} then this method is required to force changes
     *                 to both the file's content and metadata to be written to
     *                 storage; otherwise, it need only force content changes to be
     *                 written
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException            If some other I/O error occurs
     */
    @Override
    public void force(boolean metaData) throws IOException {
        if (seekableChannel.getWritableByteChannel() != null) {
            ((S3SftpWritableByteChannel) seekableChannel.getWritableByteChannel()).force();
        }
    }

    /**
     * Transfers bytes from this channel's file to the given writable byte
     * channel.
     *
     * <p> An attempt is made to read up to {@code count} bytes starting at
     * the given {@code position} in this channel's file and write them to the
     * target channel.  An invocation of this method may or may not transfer
     * all of the requested bytes; whether or not it does so depends upon the
     * natures and states of the channels.  Fewer than the requested number of
     * bytes are transferred if this channel's file contains fewer than
     * {@code count} bytes starting at the given {@code position}, or if the
     * target channel is non-blocking and it has fewer than {@code count}
     * bytes free in its output buffer.
     *
     * <p> This method does not modify this channel's position.  If the given
     * position is greater than the file's current size then no bytes are
     * transferred.  If the target channel has a position then bytes are
     * written starting at that position and then the position is incremented
     * by the number of bytes written.
     *
     * <p> This method is potentially much more efficient than a simple loop
     * that reads from this channel and writes to the target channel.  Many
     * operating systems can transfer bytes directly from the filesystem cache
     * to the target channel without actually copying them.  </p>
     *
     * @param position The position within the file at which the transfer is to begin;
     *                 must be non-negative
     * @param count    The maximum number of bytes to be transferred; must be
     *                 non-negative
     * @param target   The target channel
     * @return The number of bytes, possibly zero,
     * that were actually transferred
     * @throws IllegalArgumentException    If the preconditions on the parameters do not hold
     * @throws NonReadableChannelException If this channel was not opened for reading
     * @throws NonWritableChannelException If the target channel was not opened for writing
     * @throws ClosedChannelException      If either this channel or the target channel is closed
     * @throws AsynchronousCloseException  If another thread closes either channel
     *                                     while the transfer is in progress
     * @throws ClosedByInterruptException  If another thread interrupts the current thread while the
     *                                     transfer is in progress, thereby closing both channels and
     *                                     setting the current thread's interrupt status
     * @throws IOException                 If some other I/O error occurs
     */
    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        // 零拷贝，用操作系统的方法，实现从一个文件通道，在内核态直接写入到另个文件通道中
        if (position < 0 || count < 0) {
            throw new IllegalArgumentException("position or count args exception");
        }
        if (seekableChannel.getReadableByteChannel() == null) {
            throw new NonReadableChannelException();
        }

        if (count == 0 || position > seekableChannel.size()) {
            return 0;
        }

        if (!seekableChannel.isOpen()) {
            throw new ClosedChannelException();
        }

        synchronized (seekableChannel) {
            long originalPosition = seekableChannel.position();
            seekableChannel.position(position);
            long bytesTransferred = 0;
            while (bytesTransferred < count) {
                int bytesToTransfer = (int) Math.min(count - bytesTransferred, Integer.MAX_VALUE);
                ByteBuffer buffer = ByteBuffer.allocate(bytesToTransfer);
                int read = seekableChannel.read(buffer);
                if (read == -1) {
                    break;
                }
                // 从写模式切换为读模式
                buffer.flip();
                target.write(buffer);
                bytesTransferred += read;
            }

            this.seekableChannel.position(originalPosition);
            return bytesTransferred;
        }
    }

    /**
     * Transfers bytes into this channel's file from the given readable byte
     * channel.
     *
     * <p> An attempt is made to read up to {@code count} bytes from the
     * source channel and write them to this channel's file starting at the
     * given {@code position}.  An invocation of this method may or may not
     * transfer all of the requested bytes; whether or not it does so depends
     * upon the natures and states of the channels.  Fewer than the requested
     * number of bytes will be transferred if the source channel has fewer than
     * {@code count} bytes remaining, or if the source channel is non-blocking
     * and has fewer than {@code count} bytes immediately available in its
     * input buffer. No bytes are transferred, and zero is returned, if the
     * source has reached end-of-stream.
     *
     * <p> This method does not modify this channel's position.  If the given
     * position is greater than the file's current size then no bytes are
     * transferred.  If the source channel has a position then bytes are read
     * starting at that position and then the position is incremented by the
     * number of bytes read.
     *
     * <p> This method is potentially much more efficient than a simple loop
     * that reads from the source channel and writes to this channel.  Many
     * operating systems can transfer bytes directly from the source channel
     * into the filesystem cache without actually copying them.  </p>
     *
     * @param src      The source channel
     * @param position The position within the file at which the transfer is to begin;
     *                 must be non-negative
     * @param count    The maximum number of bytes to be transferred; must be
     *                 non-negative
     * @return The number of bytes, possibly zero,
     * that were actually transferred
     * @throws IllegalArgumentException    If the preconditions on the parameters do not hold
     * @throws NonReadableChannelException If the source channel was not opened for reading
     * @throws NonWritableChannelException If this channel was not opened for writing
     * @throws ClosedChannelException      If either this channel or the source channel is closed
     * @throws AsynchronousCloseException  If another thread closes either channel
     *                                     while the transfer is in progress
     * @throws ClosedByInterruptException  If another thread interrupts the current thread while the
     *                                     transfer is in progress, thereby closing both channels and
     *                                     setting the current thread's interrupt status
     * @throws IOException                 If some other I/O error occurs
     */
    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException("file position must be non-negative");
        }

        if (count < 0) {
            throw new IllegalArgumentException("byte count must be non-negative");
        }

        if (count == 0 || position > seekableChannel.size()) {
            return 0;  // no op
        }

        if (seekableChannel.getWritableByteChannel() == null) {
            throw new NonWritableChannelException();
        }

        if (!seekableChannel.isOpen()) {
            throw new ClosedChannelException();
        }
        synchronized (seekableChannel) {
            var originalPosition = seekableChannel.position();
            seekableChannel.position(position);
            long bytesTransferred = 0;

            while (bytesTransferred < count) {
                int bytesToTransfer = ((int) Math.min(count - bytesTransferred, Integer.MAX_VALUE));
                var buffer = ByteBuffer.allocate(bytesToTransfer);
                int bytesRead = src.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                buffer.flip();
                seekableChannel.write(buffer);
                bytesTransferred += bytesRead;
            }

            // set the byte buffers position back to this channels position
            seekableChannel.position(originalPosition);
            return bytesTransferred;
        }
    }

    /**
     * Reads a sequence of bytes from this channel into the given buffer,
     * starting at the given file position.
     *
     * <p> This method works in the same manner as the {@link
     * #read(ByteBuffer)} method, except that bytes are read starting at the
     * given file position rather than at the channel's current position.  This
     * method does not modify this channel's position.  If the given position
     * is greater than the file's current size then no bytes are read.  </p>
     *
     * @param dst      The buffer into which bytes are to be transferred
     * @param position The file position at which the transfer is to begin;
     *                 must be non-negative
     * @return The number of bytes read, possibly zero, or {@code -1} if the
     * given position is greater than or equal to the file's current
     * size
     * @throws IllegalArgumentException    If the position is negative or the buffer is read-only
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
    public int read(ByteBuffer dst, long position) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException("file position must be non-negative");
        }
        synchronized (seekableChannel){
            long originalPosition = seekableChannel.position();
            try {
                seekableChannel.position(position);
                return seekableChannel.read(dst);
            } finally {
                seekableChannel.position(originalPosition);
            }
        }
    }

    /**
     * Writes a sequence of bytes to this channel from the given buffer,
     * starting at the given file position.
     *
     * <p> This method works in the same manner as the {@link
     * #write(ByteBuffer)} method, except that bytes are written starting at
     * the given file position rather than at the channel's current position.
     * This method does not modify this channel's position.  If the given
     * position is greater than the file's current size then the file will be
     * grown to accommodate the new bytes; the values of any bytes between the
     * previous end-of-file and the newly-written bytes are unspecified.  </p>
     *
     * @param src      The buffer from which bytes are to be transferred
     * @param position The file position at which the transfer is to begin;
     *                 must be non-negative
     * @return The number of bytes written, possibly zero
     * @throws IllegalArgumentException    If the position is negative
     * @throws NonWritableChannelException If this channel was not opened for writing
     * @throws ClosedChannelException      If this channel is closed
     * @throws AsynchronousCloseException  If another thread closes this channel
     *                                     while the write operation is in progress
     * @throws ClosedByInterruptException  If another thread interrupts the current thread
     *                                     while the write operation is in progress, thereby
     *                                     closing the channel and setting the current thread's
     *                                     interrupt status
     * @throws IOException                 If some other I/O error occurs
     */
    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        synchronized (seekableChannel) {
            var originalPosition = seekableChannel.position();
            seekableChannel.position(position);
            var bytesWritten = seekableChannel.write(src);
            seekableChannel.position(originalPosition);
            return bytesWritten;
        }
    }

    /**
     * Maps a region of this channel's file directly into memory.
     *
     * <p> The {@code mode} parameter specifies how the region of the file is
     * mapped and may be one of the following modes:
     *
     * <ul>
     *
     *   <li><p> <i>Read-only:</i> Any attempt to modify the resulting buffer
     *   will cause a {@link ReadOnlyBufferException} to be thrown.
     *   ({@link MapMode#READ_ONLY MapMode.READ_ONLY}) </p></li>
     *
     *   <li><p> <i>Read/write:</i> Changes made to the resulting buffer will
     *   eventually be propagated to the file; they may or may not be made
     *   visible to other programs that have mapped the same file.  ({@link
     *   MapMode#READ_WRITE MapMode.READ_WRITE}) </p></li>
     *
     *   <li><p> <i>Private:</i> Changes made to the resulting buffer will not
     *   be propagated to the file and will not be visible to other programs
     *   that have mapped the same file; instead, they will cause private
     *   copies of the modified portions of the buffer to be created.  ({@link
     *   MapMode#PRIVATE MapMode.PRIVATE}) </p></li>
     *
     * </ul>
     *
     * <p> An implementation may support additional map modes.
     *
     * <p> For a read-only mapping, this channel must have been opened for
     * reading; for a read/write or private mapping, this channel must have
     * been opened for both reading and writing.
     *
     * <p> The {@link MappedByteBuffer <i>mapped byte buffer</i>}
     * returned by this method will have a position of zero and a limit and
     * capacity of {@code size}; its mark will be undefined.  The buffer and
     * the mapping that it represents will remain valid until the buffer itself
     * is garbage-collected.
     *
     * <p> A mapping, once established, is not dependent upon the file channel
     * that was used to create it.  Closing the channel, in particular, has no
     * effect upon the validity of the mapping.
     *
     * <p> Many of the details of memory-mapped files are inherently dependent
     * upon the underlying operating system and are therefore unspecified.  The
     * behavior of this method when the requested region is not completely
     * contained within this channel's file is unspecified.  Whether changes
     * made to the content or size of the underlying file, by this program or
     * another, are propagated to the buffer is unspecified.  The rate at which
     * changes to the buffer are propagated to the file is unspecified.
     *
     * <p> For most operating systems, mapping a file into memory is more
     * expensive than reading or writing a few tens of kilobytes of data via
     * the usual {@link #read read} and {@link #write write} methods.  From the
     * standpoint of performance it is generally only worth mapping relatively
     * large files into memory.  </p>
     *
     * @param mode     One of the constants {@link MapMode#READ_ONLY READ_ONLY}, {@link
     *                 MapMode#READ_WRITE READ_WRITE}, or {@link MapMode#PRIVATE
     *                 PRIVATE} defined in the {@link MapMode} class, according to
     *                 whether the file is to be mapped read-only, read/write, or
     *                 privately (copy-on-write), respectively, or an implementation
     *                 specific map mode
     * @param position The position within the file at which the mapped region
     *                 is to start; must be non-negative
     * @param size     The size of the region to be mapped; must be non-negative and
     *                 no greater than {@link Integer#MAX_VALUE}
     * @return The mapped byte buffer
     * @throws NonReadableChannelException   If the {@code mode} is {@link MapMode#READ_ONLY READ_ONLY} or
     *                                       an implementation specific map mode requiring read access
     *                                       but this channel was not opened for reading
     * @throws NonWritableChannelException   If the {@code mode} is {@link MapMode#READ_WRITE READ_WRITE}.
     *                                       {@link MapMode#PRIVATE PRIVATE} or an implementation specific
     *                                       map mode requiring write access but this channel was not
     *                                       opened for both reading and writing
     * @throws IllegalArgumentException      If the preconditions on the parameters do not hold
     * @throws UnsupportedOperationException If an unsupported map mode is specified
     * @throws IOException                   If some other I/O error occurs
     * @see MapMode
     * @see MappedByteBuffer
     */
    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        throw new IOException("This library current doesn't support MappedByteBuffers");
    }

    /**
     * Acquires a lock on the given region of this channel's file.
     *
     * <p> An invocation of this method will block until the region can be
     * locked, this channel is closed, or the invoking thread is interrupted,
     * whichever comes first.
     *
     * <p> If this channel is closed by another thread during an invocation of
     * this method then an {@link AsynchronousCloseException} will be thrown.
     *
     * <p> If the invoking thread is interrupted while waiting to acquire the
     * lock then its interrupt status will be set and a {@link
     * FileLockInterruptionException} will be thrown.  If the invoker's
     * interrupt status is set when this method is invoked then that exception
     * will be thrown immediately; the thread's interrupt status will not be
     * changed.
     *
     * <p> The region specified by the {@code position} and {@code size}
     * parameters need not be contained within, or even overlap, the actual
     * underlying file.  Lock regions are fixed in size; if a locked region
     * initially contains the end of the file and the file grows beyond the
     * region then the new portion of the file will not be covered by the lock.
     * If a file is expected to grow in size and a lock on the entire file is
     * required then a region starting at zero, and no smaller than the
     * expected maximum size of the file, should be locked.  The zero-argument
     * {@link #lock()} method simply locks a region of size {@link
     * Long#MAX_VALUE}.
     *
     * <p> Some operating systems do not support shared locks, in which case a
     * request for a shared lock is automatically converted into a request for
     * an exclusive lock.  Whether the newly-acquired lock is shared or
     * exclusive may be tested by invoking the resulting lock object's {@link
     * FileLock#isShared() isShared} method.
     *
     * <p> File locks are held on behalf of the entire Java virtual machine.
     * They are not suitable for controlling access to a file by multiple
     * threads within the same virtual machine.  </p>
     *
     * @param position The position at which the locked region is to start; must be
     *                 non-negative
     * @param size     The size of the locked region; must be non-negative, and the sum
     *                 {@code position}&nbsp;+&nbsp;{@code size} must be non-negative
     * @param shared   {@code true} to request a shared lock, in which case this
     *                 channel must be open for reading (and possibly writing);
     *                 {@code false} to request an exclusive lock, in which case this
     *                 channel must be open for writing (and possibly reading)
     * @return A lock object representing the newly-acquired lock
     * @throws IllegalArgumentException      If the preconditions on the parameters do not hold
     * @throws ClosedChannelException        If this channel is closed
     * @throws AsynchronousCloseException    If another thread closes this channel while the invoking
     *                                       thread is blocked in this method
     * @throws FileLockInterruptionException If the invoking thread is interrupted while blocked in this
     *                                       method
     * @throws OverlappingFileLockException  If a lock that overlaps the requested region is already held by
     *                                       this Java virtual machine, or if another thread is already
     *                                       blocked in this method and is attempting to lock an overlapping
     *                                       region
     * @throws NonReadableChannelException   If {@code shared} is {@code true} this channel was not
     *                                       opened for reading
     * @throws NonWritableChannelException   If {@code shared} is {@code false} but this channel was not
     *                                       opened for writing
     * @throws IOException                   If some other I/O error occurs
     * @see #lock()
     * @see #tryLock()
     * @see #tryLock(long, long, boolean)
     */
    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        throw new IOException(new UnsupportedOperationException("S3 does not support file locks"));
    }

    /**
     * Attempts to acquire a lock on the given region of this channel's file.
     *
     * <p> This method does not block.  An invocation always returns
     * immediately, either having acquired a lock on the requested region or
     * having failed to do so.  If it fails to acquire a lock because an
     * overlapping lock is held by another program then it returns
     * {@code null}.  If it fails to acquire a lock for any other reason then
     * an appropriate exception is thrown.
     *
     * <p> The region specified by the {@code position} and {@code size}
     * parameters need not be contained within, or even overlap, the actual
     * underlying file.  Lock regions are fixed in size; if a locked region
     * initially contains the end of the file and the file grows beyond the
     * region then the new portion of the file will not be covered by the lock.
     * If a file is expected to grow in size and a lock on the entire file is
     * required then a region starting at zero, and no smaller than the
     * expected maximum size of the file, should be locked.  The zero-argument
     * {@link #tryLock()} method simply locks a region of size {@link
     * Long#MAX_VALUE}.
     *
     * <p> Some operating systems do not support shared locks, in which case a
     * request for a shared lock is automatically converted into a request for
     * an exclusive lock.  Whether the newly-acquired lock is shared or
     * exclusive may be tested by invoking the resulting lock object's {@link
     * FileLock#isShared() isShared} method.
     *
     * <p> File locks are held on behalf of the entire Java virtual machine.
     * They are not suitable for controlling access to a file by multiple
     * threads within the same virtual machine.  </p>
     *
     * @param position The position at which the locked region is to start; must be
     *                 non-negative
     * @param size     The size of the locked region; must be non-negative, and the sum
     *                 {@code position}&nbsp;+&nbsp;{@code size} must be non-negative
     * @param shared   {@code true} to request a shared lock,
     *                 {@code false} to request an exclusive lock
     * @return A lock object representing the newly-acquired lock,
     * or {@code null} if the lock could not be acquired
     * because another program holds an overlapping lock
     * @throws IllegalArgumentException     If the preconditions on the parameters do not hold
     * @throws ClosedChannelException       If this channel is closed
     * @throws OverlappingFileLockException If a lock that overlaps the requested region is already held by
     *                                      this Java virtual machine, or if another thread is already
     *                                      blocked in this method and is attempting to lock an overlapping
     *                                      region of the same file
     * @throws IOException                  If some other I/O error occurs
     * @see #lock()
     * @see #lock(long, long, boolean)
     * @see #tryLock()
     */
    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        throw new IOException(new UnsupportedOperationException("S3 does not support file locks"));
    }

    /**
     * Closes this channel.
     *
     * <p> This method is invoked by the {@link #close close} method in order
     * to perform the actual work of closing the channel.  This method is only
     * invoked if the channel has not yet been closed, and it is never invoked
     * more than once.
     *
     * <p> An implementation of this method must arrange for any other thread
     * that is blocked in an I/O operation upon this channel to return
     * immediately, either by throwing an exception or by returning normally.
     * </p>
     *
     * @throws IOException If an I/O error occurs while closing the channel
     */
    @Override
    protected void implCloseChannel() throws IOException {
        seekableChannel.close();
    }
}
