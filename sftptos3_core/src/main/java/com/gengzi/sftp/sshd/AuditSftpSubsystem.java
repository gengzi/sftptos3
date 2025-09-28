package com.gengzi.sftp.sshd;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.dao.SftpAudit;
import com.gengzi.sftp.dao.SftpAuditRepository;
import com.gengzi.sftp.enums.OperateStatus;
import com.gengzi.sftp.enums.OptType;
import com.gengzi.sftp.enums.StorageType;
import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;
import com.gengzi.sftp.nio.S3SftpPath;
import com.gengzi.sftp.util.SpringContextUtil;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.apache.sshd.sftp.server.SftpSubsystem;
import org.apache.sshd.sftp.server.SftpSubsystemConfigurator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class AuditSftpSubsystem extends SftpSubsystem {

    // 存放操作审计事件监听器
    private final Collection<AuditEventListener> optAuditEventListeners = new CopyOnWriteArraySet<>();


    // 存放读取文件的信息
    private final Map<String, AuditContext> auditMap = new ConcurrentHashMap<>();


    /**
     * @param channel      The {@link ChannelSession} through which the command was received
     * @param configurator The {@link SftpSubsystemConfigurator} to use
     */
    public AuditSftpSubsystem(ChannelSession channel, SftpSubsystemConfigurator configurator) {
        super(channel, configurator);
    }

    @NotNull
    private static SftpAuditRepository sftpAuditRepository() {
        SftpAuditRepository sftpAuditRepository = SpringContextUtil.getBean(SftpAuditRepository.class);
        return sftpAuditRepository;
    }

    private static void initBaseAudit(SftpAudit sftpAudit, ServerSession session) {
        LocalDateTime now = LocalDateTime.now();
        sftpAudit.setCreateTime(now);
        sftpAudit.setClientAddress(session.getClientAddress().toString());
        sftpAudit.setClientUsername(session.getUsername());
        sftpAudit.setOptTime(now);
        sftpAudit.setFileSize("");
        Long attribute = session.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
        sftpAudit.setClientAuditId(attribute);
        sftpAudit.setOperateResult(OperateStatus.FAILURE.getStatus());
        sftpAudit.setErrorMsg("");
    }

    /**
     * 截取字符串的前maxChars个字符
     *
     * @param str      原始字符串
     * @param maxChars 最大字符数
     * @return 截取后的字符串
     */
    public static String substringByChars(String str, int maxChars) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // 如果字符串长度小于等于最大字符数，直接返回原字符串
        if (str.length() <= maxChars) {
            return str;
        }
        // 否则截取前maxChars个字符
        return str.substring(0, maxChars);
    }

    /**
     * 重写doRead 方法，用于向审计表中插入读取文件的信息
     *
     * @param id
     * @param handle
     * @param offset
     * @param length
     * @param data
     * @param doff
     * @param eof
     * @return
     * @throws IOException
     */
    @Override
    protected int doRead(int id, String handle, long offset, int length, byte[] data, int doff, AtomicReference<Boolean> eof) throws IOException {
        Handle h = handles.get(handle);
        FileHandle fh = validateHandle(handle, h, FileHandle.class);
        auditMap.computeIfAbsent(handle, k -> {
            Result result = initReadOrWriteAudit(fh, OptType.DOWNLOAD);
            AuditContext readContext = new AuditContext(System.currentTimeMillis(),
                    result.sftpAudit().getId(), OptType.DOWNLOAD);
            return readContext;
        });
        int readLen;
        try {
            readLen = super.doRead(id, handle, offset, length, data, doff, eof);
        } catch (Exception e) {
            long size = fh.getFileChannel().size();
            long sftpAuditDbId = auditMap.get(handle).sftpAuditDbId;
            sftpAuditRepository().updateReadEvent(String.valueOf(size), OperateStatus.FAILURE.getStatus(),
                    substringByChars(e.getMessage(), 200), LocalDateTime.now(), sftpAuditDbId);
            throw e;
        }
        if (readLen <= 0) {
            AuditContext readContext = auditMap.get(handle);
            if (!readContext.isComplete) {
                readContext.isComplete = true;
                long size = fh.getFileChannel().size();
                long sftpAuditDbId = auditMap.get(handle).sftpAuditDbId;
                sftpAuditRepository().updateReadEvent(String.valueOf(size), OperateStatus.SUCCESS.getStatus(), "", LocalDateTime.now(), sftpAuditDbId);
            }
        }
        return readLen;
    }

    @Override
    protected void doWrite(int id, String handle, long offset, int length, byte[] data, int doff, int remaining) throws IOException {
        Handle h = handles.get(handle);
        FileHandle fh = validateHandle(handle, h, FileHandle.class);
        auditMap.computeIfAbsent(handle, k -> {
            Result result = initReadOrWriteAudit(fh, OptType.UPLOAD);
            AuditContext readContext = new AuditContext(System.currentTimeMillis(),
                    result.sftpAudit().getId(), OptType.UPLOAD);
            return readContext;
        });
        //TODO APPEND 还没有验证呢。。。
        try {
            super.doWrite(id, handle, offset, length, data, doff, remaining);
            auditMap.get(handle).writeBytes.addAndGet(length);
        } catch (Exception e) {
            long size = fh.getFileChannel().size();
            long sftpAuditDbId = auditMap.get(handle).sftpAuditDbId;
            sftpAuditRepository().updateReadEvent(String.valueOf(size), OperateStatus.FAILURE.getStatus(),
                    substringByChars(e.getMessage(), 200), LocalDateTime.now(), sftpAuditDbId);
            throw e;
        }

    }

    @Override
    protected void doClose(int id, String handle) throws IOException {
        AuditContext auditContext = auditMap.get(handle);
        synchronized (auditMap) {
            auditMap.remove(handle);
        }
        super.doClose(id, handle);
        if (auditContext != null && auditContext.optType.equals(OptType.UPLOAD)) {
            sftpAuditRepository().updateReadEvent(String.valueOf(auditContext.writeBytes.get()), OperateStatus.SUCCESS.getStatus(), "", LocalDateTime.now(), auditContext.sftpAuditDbId);
        }
    }

    /**
     * 删除一个目录
     * 在现有策略中，禁止删除一个非空目录
     *
     * @param id
     * @param path
     * @throws IOException
     */
    @Override
    protected void doRemoveDirectory(int id, String path) throws IOException {
        Result result = initRemoveOptAudit(path, OptType.DELETE_DIR);
        try {
            super.doRemoveDirectory(id, path);
        } catch (Exception e) {
            sftpAuditRepository().updateReadEvent("", OperateStatus.FAILURE.getStatus(),
                    substringByChars(e.getMessage(), 200), LocalDateTime.now(), result.sftpAudit.getId());
            throw e;
        }
        sftpAuditRepository().updateReadEvent("", OperateStatus.SUCCESS.getStatus(),
                "", LocalDateTime.now(), result.sftpAudit.getId());
    }

    @Override
    protected void doRemoveFile(int id, String path) throws IOException {
        Result result = initRemoveOptAudit(path, OptType.DELETE_FILE);
        try {
            super.doRemoveFile(id, path);
        } catch (Exception e) {
            sftpAuditRepository().updateReadEvent("", OperateStatus.FAILURE.getStatus(),
                    substringByChars(e.getMessage(), 200), LocalDateTime.now(), result.sftpAudit.getId());
            throw e;
        }
        sftpAuditRepository().updateReadEvent("", OperateStatus.SUCCESS.getStatus(),
                "", LocalDateTime.now(), result.sftpAudit.getId());
    }

    @Override
    protected void doRename(int id, String oldPath, String newPath, int flags) throws IOException {
        Result result = initRenameOptAudit(oldPath, newPath, OptType.RENAME);
        try {
            super.doRename(id, oldPath, newPath, flags);
        } catch (Exception e) {
            sftpAuditRepository().updateReadEvent("", OperateStatus.FAILURE.getStatus(),
                    substringByChars(e.getMessage(), 200), LocalDateTime.now(), result.sftpAudit.getId());
            throw e;
        }
        sftpAuditRepository().updateReadEvent("", OperateStatus.SUCCESS.getStatus(),
                "", LocalDateTime.now(), result.sftpAudit.getId());

    }

    @NotNull
    private Result initReadOrWriteAudit(FileHandle localHandle, OptType optType) {
        ServerSession session = getServerSession();
        // 数据库审计记录初始化
        SftpAudit sftpAudit = new SftpAudit();
        initBaseAudit(sftpAudit, session);
        Path file = localHandle.getFile();
        Path root = file.getRoot();
        sftpAudit.setFilePath(root.toString() + file.toString());
        sftpAudit.setType(optType.getType());
        if (file instanceof S3SftpPath) {
            S3SftpPath s3SftpPath = (S3SftpPath) file;
            S3SftpNioSpiConfiguration configuration = s3SftpPath.getFileSystem().configuration();
            sftpAudit.setFileStroageInfo(configuration.getStroageInfo());
        } else {
            sftpAudit.setFileStroageInfo(StorageType.LOCAL.type());
        }
        SftpAudit save = sftpAuditRepository().save(sftpAudit);
        Result result = new Result(save);
        return result;
    }

    @NotNull
    private Result initRemoveOptAudit(String removePath, OptType optType) throws IOException {
        ServerSession session = getServerSession();
        Path path = resolveFile(removePath);
        // 数据库审计记录初始化
        SftpAudit sftpAudit = new SftpAudit();
        initBaseAudit(sftpAudit, session);
        sftpAudit.setFilePath(path.toString());
        sftpAudit.setType(optType.getType());
        if (path instanceof S3SftpPath) {
            S3SftpPath s3SftpPath = (S3SftpPath) path;
            S3SftpNioSpiConfiguration configuration = s3SftpPath.getFileSystem().configuration();
            sftpAudit.setFileStroageInfo(configuration.getStroageInfo());
        } else {
            sftpAudit.setFileStroageInfo(StorageType.LOCAL.type());
        }
        SftpAudit save = sftpAuditRepository().save(sftpAudit);
        Result result = new Result(save);
        return result;
    }

    @NotNull
    private Result initRenameOptAudit(String oldPath, String newPath, OptType optType) throws IOException {
        ServerSession session = getServerSession();
        Path path = resolveFile(oldPath);
        // 数据库审计记录初始化
        SftpAudit sftpAudit = new SftpAudit();
        initBaseAudit(sftpAudit, session);
        sftpAudit.setFilePath(path.toString());
        sftpAudit.setRemoveFilePath(newPath);
        sftpAudit.setType(optType.getType());
        if (path instanceof S3SftpPath) {
            S3SftpPath s3SftpPath = (S3SftpPath) path;
            S3SftpNioSpiConfiguration configuration = s3SftpPath.getFileSystem().configuration();
            sftpAudit.setFileStroageInfo(configuration.getStroageInfo());
        } else {
            sftpAudit.setFileStroageInfo(StorageType.LOCAL.type());
        }
        SftpAudit save = sftpAuditRepository().save(sftpAudit);
        Result result = new Result(save);
        return result;
    }

    private record Result(SftpAudit sftpAudit) {
    }

    class AuditContext {
        /**
         * 读取内容长度
         */
        AtomicLong readBytes;
        /**
         * 写入内容长度
         */
        AtomicLong writeBytes;
        /**
         * 操作时间
         */
        long startTime;

        /**
         * 数据源ID
         */
        long sftpAuditDbId;

        /**
         * 是否完成
         */
        boolean isComplete;

        /**
         * 本次操作的类型
         */
        OptType optType;


        public AuditContext(long startTime, long sftpAuditDbId, OptType optType) {
            this.readBytes = new AtomicLong(0L);
            this.writeBytes = new AtomicLong(0L);
            this.startTime = startTime;
            this.sftpAuditDbId = sftpAuditDbId;
            this.isComplete = false;
            this.optType = optType;
        }


    }
}
