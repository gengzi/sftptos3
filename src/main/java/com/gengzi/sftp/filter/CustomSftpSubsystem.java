package com.gengzi.sftp.filter;

import com.gengzi.sftp.handle.S3DirectoryHandle;
import com.gengzi.sftp.handle.S3FileHandle;
import com.gengzi.sftp.process.S3Do;
import com.gengzi.sftp.process.S3DoStat;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.fs.SftpPath;
import org.apache.sshd.sftp.client.impl.SftpPathImpl;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.common.SftpException;
import org.apache.sshd.sftp.common.SftpHelper;
import org.apache.sshd.sftp.server.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 自定义SFTP文件系统视图，修改文件读取逻辑
 */
public class CustomSftpSubsystem extends SftpSubsystem {


    // 存储s3文件句柄对应的处理类
    protected final Map<String, Handle> s3Handles = new ConcurrentHashMap<>();

    /**
     * @param channel      The {@link ChannelSession} through which the command was received
     * @param configurator The {@link SftpSubsystemConfigurator} to use
     */
    public CustomSftpSubsystem(ChannelSession channel, SftpSubsystemConfigurator configurator) {
        super(channel, configurator);
    }

    @Override
    protected void process(Buffer buffer) throws IOException {
        super.process(buffer);
    }

    @Override
    protected void doProcess(Buffer buffer, int length, int type, int id) throws IOException {
        ServerSession serverSession = getServerSession();

        String statusName = SftpConstants.getCommandMessageName(type);
        log.info("doProcess :type={} typeName={} [id={}][length={}][buffer={}] ", type, statusName, id, length, buffer);
        super.doProcess(buffer, length, type, id);
    }


    @Override
    protected Map<String, Object> doLStat(int id, String path, int flags) throws IOException {
        // 根据路径获取对应目录下的 文件
        // 获取根据路径+文件名称 获取文件句柄
        log.info("doLStat :path={} [id={}][flags={}] ", path, id, flags);
        return S3DoStat.doStat(id, path, flags);
    }


    @Override
    protected Map<String, Object> doStat(int id, String path, int flags) throws IOException {
        System.out.println(id + "," + path + "," + flags);
        if (path != null && path.startsWith("/s3")) {
            return S3DoStat.doStat(id, path, flags);
        }
        return super.doStat(id, path, flags);
    }

    /**
     * 打开一个目录
     *
     * @param id
     * @param path
     * @param dir
     * @param options
     * @return
     * @throws IOException
     */
    @Override
    protected String doOpenDir(int id, String path, Path dir, LinkOption... options) throws IOException {

        if (true) {
            try {
                // 校验目录是否存在
                //TODO 先判断路径是否是一个目录
                S3Do s3Do = new S3Do();
                boolean status = s3Do.doesDirectoryExist(s3Do.getAmazonS3Config().getDefaultBucketName(), path);
                if (!status) {
                    throw signalOpenFailure(id, path, dir.toAbsolutePath(), true,
                            new NoSuchFileException(path, path, "Referenced target directory N/A"));
                }
            } catch (Exception e) {
                throw signalOpenFailure(id, path, dir.toAbsolutePath(), true,
                        new AccessDeniedException(dir.toAbsolutePath().toString(), dir.toAbsolutePath().toString(), "Cannot determine open-dir existence"));
            }
            String handle;
            try {
                synchronized (s3Handles) {
                    handle = generateFileHandle(dir);
                    S3DirectoryHandle s3DirectoryHandle = new S3DirectoryHandle(this, dir, handle);
                    s3Handles.put(handle, s3DirectoryHandle);
                }
            } catch (IOException e) {
                throw signalOpenFailure(id, path, dir, true, e);
            }

            return handle;
        }

        return super.doOpenDir(id, path, dir, options);
    }

    @Override
    protected void doLStat(Buffer buffer, int id) throws IOException {
        super.doLStat(buffer, id);
    }

    @Override
    protected String doOpen(int id, String path, int pflags, int access, Map<String, Object> attrs) throws IOException {
        // 打开文件，并返回文件句柄
        // 当前磁盘不存在该文件，从对象存储中获取文件
        if (path.toString().startsWith("/s3")) {
            ServerSession session = getServerSession();
            if (log.isInfoEnabled()) {
                log.info("doOpen({})[id={}] SSH_FXP_OPEN (path={}, access=0x{}, pflags=0x{}, attrs={})",
                        session, id, path, Integer.toHexString(access), Integer.toHexString(pflags), attrs);
            }

            Path file = resolveFile(path);
            int curHandleCount = s3Handles.size();
            int maxHandleCount = SftpModuleProperties.MAX_OPEN_HANDLES_PER_SESSION.getRequired(session);
            if (curHandleCount > maxHandleCount) {
                throw signalOpenFailure(id, path, file, false,
                        new SftpException(SftpConstants.SSH_FX_NO_SPACE_ON_FILESYSTEM,
                                "Too many open handles: current=" + curHandleCount + ", max.=" + maxHandleCount));
            }

            String handle;
            try {
                synchronized (s3Handles) {
                    handle = generateFileHandle(file);
                    S3FileHandle fileHandle = new S3FileHandle(this, file, pflags, handle, access);
                    s3Handles.put(handle, fileHandle);
                }
            } catch (IOException e) {
                throw signalOpenFailure(id, path, file, false, e);
            }

            return handle;
        }


        return super.doOpen(id, path, pflags, access, attrs);
    }

    @Override
    protected int doRead(int id, String handle, long offset, int length, byte[] data, int doff, AtomicReference<Boolean> eof) throws IOException {
        System.out.println(handle);
        Handle h = s3Handles.get(handle);
        S3FileHandle s3FileHandle = validateHandle(handle, h, S3FileHandle.class);
        if (s3FileHandle != null) {

            ServerSession session = getServerSession();
            if (log.isInfoEnabled()) {
                log.info("doRead({})[id={}] SSH_FXP_READ (handle={}[{}], offset={}, length={})",
                        session, id, handle, s3FileHandle, offset, length);
            }
            // 校验文件长度
            ValidateUtils.checkTrue(length > 0L, "Invalid read length: %d", length);

            SftpEventListener listener = getSftpEventListenerProxy();
            int readLen;

            try {
                readLen = s3FileHandle.read(data, doff, length, offset, eof);
            } catch (RuntimeException | Error e) {
                throw e;
            }
            return readLen;

        }


        return super.doRead(id, handle, offset, length, data, doff, eof);
    }

    @Override
    protected void doReadDir(Buffer buffer, int id) throws IOException {
        String handle = buffer.getString();
        Handle h = s3Handles.get(handle);
        ServerSession session = getServerSession();
        boolean debugEnabled = log.isDebugEnabled();
        if (debugEnabled) {
            log.debug("doReadDir({})[id={}] SSH_FXP_READDIR (handle={}[{}])", session, id, handle, h);
        }

        Buffer reply = null;
        try {
            S3DirectoryHandle dh = validateHandle(handle, h, S3DirectoryHandle.class);
            if (dh.isDone()) {
                sendStatus(prepareReply(buffer), id, SftpConstants.SSH_FX_EOF, "Directory reading is done");
                return;
            }

            Path file = dh.getFile();

//            SftpEventListener listener = getSftpEventListenerProxy();
//            listener.readingEntries(session, handle, dh);

            if (dh.isSendDot() || dh.isSendDotDot() || dh.hasNext()) {
                // There is at least one file in the directory or we need to send the "..".
                // Send only a few files at a time to not create packets of a too
                // large size or have a timeout to occur.

                reply = prepareReply(buffer);
                reply.putByte((byte) SftpConstants.SSH_FXP_NAME);
                reply.putInt(id);

                int lenPos = reply.wpos();
                reply.putUInt(0L);  // save room for actual length

                int maxDataSize = SftpModuleProperties.MAX_READDIR_DATA_SIZE.getRequired(session);

                int count = doReadDir(id, handle, dh, reply, maxDataSize, false);

                BufferUtils.updateLengthPlaceholder(reply, lenPos, count);
                if ((!dh.isSendDot()) && (!dh.isSendDotDot()) && (!dh.hasNext())) {
                    dh.markDone();
                }

                int sftpVersion = getVersion();
                Boolean indicator = SftpHelper.indicateEndOfNamesList(reply, sftpVersion, session, dh.isDone());
                if (debugEnabled) {
                    log.debug("doReadDir({})({})[{}] - sending {} entries - eol={} (SFTP version {})", session, handle, h,
                            count, indicator, sftpVersion);
                }
            } else {
                // empty directory
                dh.markDone();
                sendStatus(prepareReply(buffer), id, SftpConstants.SSH_FX_EOF, "Empty directory");
                return;
            }

            Objects.requireNonNull(reply, "No reply buffer created");
        } catch (IOException | RuntimeException | Error e) {
            sendStatus(prepareReply(buffer), id, e, SftpConstants.SSH_FXP_READDIR, handle);
            return;
        }

        send(reply);
    }


    protected int doReadDir(int id, String handle, S3DirectoryHandle dir, Buffer buffer, int maxSize, boolean followLinks) throws IOException {

        ServerSession session = getServerSession();
        SftpFileSystemAccessor accessor = getFileSystemAccessor();
        LinkOption[] options = accessor.resolveFileAccessLinkOptions(
                this, dir.getFile(), SftpConstants.SSH_FXP_READDIR, "", followLinks);
        int nb = 0;
        Map<String, Path> entries = new TreeMap<>(Comparator.naturalOrder());

        while ((dir.isSendDot() || dir.isSendDotDot() || dir.hasNext()) && (buffer.wpos() < maxSize)) {
            if (dir.isSendDot()) {
                writeDirEntry(id, dir, entries, buffer, nb, dir.getFile(), ".", options);
                dir.markDotSent(); // do not send it again
            } else if (dir.isSendDotDot()) {
                Path dirPath = dir.getFile();
                Path parentPath = dirPath.getParent();
                if (parentPath != null) {
                    writeDirEntry(id, dir, entries, buffer, nb, parentPath, "..", options);
                }
                dir.markDotDotSent(); // do not send it again
            } else {
                Path f = dir.next();
                String shortName = getShortName(f);
                if (f instanceof SftpPath) {
                    SftpClient.Attributes attributes = ((SftpPath) f).getAttributes();
                    if (attributes != null) {
                        entries.put(shortName, f);
                        writeDirEntry(session, id, buffer, nb, f, shortName, attributes);
                        nb++;
                        continue;
                    }
                }
                writeDirEntry(id, dir, entries, buffer, nb, f, shortName, options);
            }

            nb++;
        }

//        SftpEventListener listener = getSftpEventListenerProxy();
//        listener.readEntries(session, handle, dir, entries);
        return nb;
    }


    protected void writeDirEntry(int id, S3DirectoryHandle dir, Map<String, Path> entries, Buffer buffer, int index, Path f, String shortName, LinkOption... options) throws IOException {

        // 返回该文件的各种属性信息
        Map<String, ?> attrs = new TreeMap<>();
        // 针对目录

        // 针对文件


        entries.put(shortName, f);

        SftpFileSystemAccessor accessor = getFileSystemAccessor();
        ServerSession session = getServerSession();
        accessor.putRemoteFileName(this, f, buffer, shortName, true);

        int version = getVersion();
        if (version == SftpConstants.SFTP_V3) {
            String longName = getLongName(f, shortName, options);
            accessor.putRemoteFileName(this, f, buffer, longName, false);

            if (log.isTraceEnabled()) {
                log.trace("writeDirEntry({} id={})[{}] - {} [{}]: {}", session, id, index, shortName, longName, attrs);
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("writeDirEntry({} id={})[{}] - {}: {}", session, id, index, shortName, attrs);
            }
        }

        writeAttrs(buffer, attrs);
    }

    @Override
    protected NavigableMap<String, Object> resolveFileAttributes(Path path, int flags, boolean neverFollowSymLinks, LinkOption... options) throws IOException {
        return SftpPathImpl.withAttributeCache(path, file -> {
            return getAttributes(file, flags, options);
        });
    }


    @Override
    protected void doClose(int id, String handle) throws IOException {
        if (s3Handles.containsKey(handle)) {
            Handle h = s3Handles.get(handle);
            // 判断是文件句柄，不是目录句柄
            if (h instanceof S3FileHandle) {
                S3FileHandle s3FileHandle = (S3FileHandle) h;
                Boolean fileIsUploaded = s3FileHandle.getFileIsUploaded();
                if (!fileIsUploaded) {
                    // 异步上传
                    s3FileHandle.asyncPut();
                    // 写数据库操作，如果上传失败或者其他的信息
                }
            }
            Handle nodeHandle = s3Handles.remove(handle);
            ServerSession session = getServerSession();
            SftpEventListener listener = getSftpEventListenerProxy();
            try {
                listener.closing(session, handle, nodeHandle);
                nodeHandle.close();
                listener.closed(session, handle, nodeHandle, null);
            } catch (IOException | RuntimeException | Error e) {
                listener.closed(session, handle, nodeHandle, e);
                throw e;
            } finally {
                nodeHandle.clearAttributes();
            }
            return;
        }

        // 执行既往逻辑
        super.doClose(id, handle);
    }


    @Override
    protected void doWrite(int id, String handle, long offset, int length, byte[] data, int doff, int remaining) throws IOException {

        S3FileHandle s3FileHandle = (S3FileHandle) s3Handles.get(handle);
        ServerSession session = getServerSession();
        int maxAllowed = SftpModuleProperties.MAX_WRITEDATA_PACKET_LENGTH.getRequired(session);
        if (log.isTraceEnabled()) {
            log.trace("doWrite({})[id={}] SSH_FXP_WRITE (handle={}[{}], offset={}, length={}, maxAllowed={})",
                    session, id, handle, s3FileHandle, offset, length, maxAllowed);
        }

        if (length < 0) {
            throw new IllegalStateException("Bad length (" + length + ") for writing to " + s3FileHandle);
        }

        if (remaining < length) {
            throw new IllegalStateException("Not enough buffer data for writing to " + s3FileHandle
                    + ": required=" + length + ", available=" + remaining);
        }

        if (length > maxAllowed) {
            throw new IOException("Reuested write size (" + length + ") exceeds max. allowed (" + maxAllowed + ")");
        }
        try {
            if (s3FileHandle.isOpenAppend()) {
                // 使用临时文件，存入部分数据后，开始往s3写入
                s3FileHandle.append(data, doff, length);
            } else {
                // 使用临时文件目录下载文件后，随机写入，在colse 时，将整个文件上传到s3 存储覆盖原文件
                s3FileHandle.write(data, doff, length, offset);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        }


        //super.doWrite(id, handle, offset, length, data, doff, remaining);
    }
}
    