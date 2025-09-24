package com.gengzi.sftp.listener;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.dao.SftpAudit;
import com.gengzi.sftp.dao.SftpAuditRepository;
import com.gengzi.sftp.enums.OperateStatus;
import com.gengzi.sftp.enums.OptType;
import com.gengzi.sftp.enums.StorageType;
import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;
import com.gengzi.sftp.nio.S3SftpPath;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.AbstractSftpEventListenerAdapter;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Service
public class SftptoS3SftpEventListener extends AbstractSftpEventListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SftptoS3SftpEventListener.class);
    // 存放读信息
    private final Map<SessionHandleKey, ReadContext> readMap = new ConcurrentHashMap<>();
    @Autowired
    private SftpAuditRepository sftpAuditRepository;

    private static String remoteHandleStr(String remoteHandle) {
        return BufferUtils.toHex(BufferUtils.EMPTY_HEX_SEPARATOR, remoteHandle.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * &#x51C6;&#x5907;&#x8BFB;&#x53D6;
     *
     * @param session      The {@link ServerSession} through which the request was handled
     * @param remoteHandle The (opaque) assigned handle for the file
     * @param localHandle  The associated {@link FileHandle}
     * @param offset       Offset in file from which to read
     * @param data         Buffer holding the read data
     * @param dataOffset   Offset of read data in buffer
     * @param dataLen      Requested read length
     * @throws IOException
     */
    @Override
    public void reading(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data, int dataOffset, int dataLen) throws IOException {
        // 仅记录首次准备读取时间
        log.debug("reading: [session object={}], remote handle: [{}]", session, remoteHandleStr(remoteHandle));
        SessionHandleKey key = new SessionHandleKey(session, remoteHandle);
//        boolean containsKey = readMap.containsKey(key);
//        if (containsKey) {
//            throw new IllegalStateException("session and remoteHandle is exist !");
//        }
        readMap.computeIfAbsent(key, k -> {
            Result result = initReadAudit(session, localHandle);
            ReadContext readContext = new ReadContext(System.currentTimeMillis(), result.save().getId(), result.file().toString());
            return readContext;
        });
        super.reading(session, remoteHandle, localHandle, offset, data, dataOffset, dataLen);
    }

    @NotNull
    private Result initReadAudit(ServerSession session, FileHandle localHandle) {
        // 数据库审计记录初始化
        SftpAudit sftpAudit = new SftpAudit();
        sftpAudit.setCreateTime(LocalDateTime.now());
        sftpAudit.setClientAddress(session.getClientAddress().toString());
        sftpAudit.setClientUsername(session.getUsername());
        Path file = localHandle.getFile();
        Path root = file.getRoot();
        sftpAudit.setFilePath(file.toString());
        sftpAudit.setType(OptType.DOWNLOAD.getType());
        if (file instanceof S3SftpPath) {
            S3SftpPath s3SftpPath = (S3SftpPath) file;
            S3SftpNioSpiConfiguration configuration = s3SftpPath.getFileSystem().configuration();
            sftpAudit.setFileStroageInfo(configuration.getStroageInfo());
        } else {
            sftpAudit.setFileStroageInfo(StorageType.LOCAL.type());
        }
        sftpAudit.setOptTime(LocalDateTime.now());
        sftpAudit.setFileSize("");
        Long attribute = session.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
        sftpAudit.setClientAuditId(attribute);
        sftpAudit.setOperateResult(OperateStatus.FAILURE.getStatus());
        sftpAudit.setErrorMsg("");
        SftpAudit save = sftpAuditRepository.save(sftpAudit);
        Result result = new Result(file, save);
        return result;
    }

    /**
     * 读取
     *
     * @param session      The {@link ServerSession} through which the request was handled
     * @param remoteHandle The (opaque) assigned handle for the file
     * @param localHandle  The associated {@link FileHandle}
     * @param offset       Offset in file from which to read
     * @param data         Buffer holding the read data
     * @param dataOffset   Offset of read data in buffer
     * @param dataLen      Requested read length
     * @param readLen      Actual read length - negative if thrown exception provided
     * @param thrown       Non-{@code null} if read failed due to this exception
     * @throws IOException
     */
    @Override
    public void read(ServerSession session, String remoteHandle, FileHandle localHandle,
                     long offset, byte[] data, int dataOffset, int dataLen, int readLen, Throwable thrown) throws IOException {
        SessionHandleKey key = new SessionHandleKey(session, remoteHandle);

        log.debug("read session: {}, remoteHandle: {}, readLen: {}, thrown: {}", session, remoteHandleStr(remoteHandle), readLen, thrown);
        ReadContext readContext = readMap.get(key);
        if (readContext == null) {
            throw new IllegalStateException("session and remoteHandle is not exist !");
        }
        // 1. 读取失败
        if (thrown != null) {

            long size = localHandle.getFileChannel().size();
            long sftpAuditDbId = readContext.sftpAuditDbId;
            sftpAuditRepository.updateReadEvent(String.valueOf(size), OperateStatus.FAILURE.getStatus(), thrown.getMessage(), sftpAuditDbId);
            synchronized (readMap) {
                readMap.remove(key);
            }
            super.read(session, remoteHandle, localHandle, offset, data, dataOffset, dataLen, readLen, thrown);
            return;
        }
        if (readLen <= 0) {

            long size = localHandle.getFileChannel().size();
            if (readContext.readBytes.get() == size) {
                // read complete
            }
            long sftpAuditDbId = readContext.sftpAuditDbId;
            sftpAuditRepository.updateReadEvent(String.valueOf(size), OperateStatus.SUCCESS.getStatus(), "", sftpAuditDbId);
            synchronized (readMap) {
                readMap.remove(key);
            }
            return;
        }
        readContext.readBytes.addAndGet(readLen);
        super.read(session, remoteHandle, localHandle, offset, data, dataOffset, dataLen, readLen, thrown);
    }



    // ------------------------------ 2. 文件读取：监控下载行为（客户端读服务端文件） ------------------------------

    @Override
    public void closing(ServerSession session, String remoteHandle, Handle localHandle) throws IOException {
        SessionHandleKey key = new SessionHandleKey(session, remoteHandle);
        synchronized (readMap) {
            readMap.remove(key);
        }
        super.closing(session, remoteHandle, localHandle);
    }

    private record Result(Path file, SftpAudit save) {
    }

    class ReadContext {
        /**
         * 读取内容长度
         */
        AtomicLong readBytes;
        /**
         * 操作时间
         */
        long startTime;

        /**
         * 数据源ID
         */
        long sftpAuditDbId;


        public ReadContext(long startTime, long sftpAuditDbId, String path) {
            this.readBytes = new AtomicLong(0L);
            this.startTime = startTime;
            this.sftpAuditDbId = sftpAuditDbId;
        }


    }

    //
//    // ------------------------------ 3. 文件写入：监控上传行为（客户端写服务端文件） ------------------------------
//    @Override
//    public void written(ServerSession session, String remoteHandle, FileHandle localHandle,
//                        long offset, byte[] data, int dataOffset, int dataLen, Throwable thrown) throws IOException {
//        // 1. 过滤无效场景：会话无上下文、写入失败
//        TransferContext context = sessionTransferContexts.get(session);
//        if (context == null || thrown != null) {
//            if (thrown != null) {
//                log.error("[SFTP上传失败] 用户名: {}, 客户端IP: {}, 文件路径: {}, 已传字节: {} bytes, 错误: {}",
//                        session.getUsername(), session.getClientAddress().getHostString(),
//                        context != null ? context.remoteFilePath : "未知",
//                        context != null ? context.uploadBytes.get() : 0, thrown.getMessage());
//            }
//            return;
//        }
//
//        // 2. 累计上传字节数（dataLen为实际写入的字节数，与请求一致）
//        long currentTotal = context.uploadBytes.addAndGet(dataLen);
//
//        // 3. 计算上传进度（上传时文件总大小可能动态增长，此处用已传字节估算，或重新获取文件大小）
//        // 优化：上传时实时更新文件总大小（避免客户端声明大小与实际不一致）
//        Path localFilePath = localHandle.getFile().toPath();
//        long realTimeFileSize = Files.size(localFilePath);
//        context.fileTotalSize = realTimeFileSize; // 更新上下文的总大小
//
//        String progress = realTimeFileSize <= 0
//                ? "未知"
//                : String.format("%.2f%%", (currentTotal * 100.0) / realTimeFileSize);
//
//        // 4. 日志：实时进度
//        log.debug("[SFTP上传进度] 用户名: {}, 客户端IP: {}, 文件路径: {}, 当前累计: {} bytes, 实时文件大小: {} bytes, 进度: {}, 偏移量: {} bytes",
//                context.username, context.clientIp, context.remoteFilePath,
//                currentTotal, realTimeFileSize, progress, offset);
//    }
//
//    // ------------------------------ 4. 文件关闭：结束监控并统计 ------------------------------
//    @Override
//    public void closed(ServerSession session, String remoteHandle, Handle localHandle, Throwable thrown) throws IOException {
//        // 1. 过滤目录、无上下文的场景
//        if (!(localHandle instanceof FileHandle) || !sessionTransferContexts.containsKey(session)) {
//            return;
//        }
//
//        // 2. 获取并移除上下文（避免内存泄漏）
//        TransferContext context = sessionTransferContexts.remove(session);
//        long totalTime = System.currentTimeMillis() - context.startTime; // 计算总耗时
//        String transferType = "";
//        long totalBytes = 0;
//        String status = thrown == null ? "SUCCESS" : "FAILED";
//
//        // 3. 区分上传/下载（根据计数器判断：哪个计数器有值就是哪种操作）
//        if (context.uploadBytes.get() > 0) {
//            transferType = "UPLOAD";
//            totalBytes = context.uploadBytes.get();
//        } else if (context.downloadBytes.get() > 0) {
//            transferType = "DOWNLOAD";
//            totalBytes = context.downloadBytes.get();
//        }
//
//        // 4. 最终统计日志（可扩展持久化到数据库）
//        log.info("[SFTP传输结束] 类型: {}, 状态: {}, 用户名: {}, 客户端IP: {}, 文件路径: {}, 总字节: {} bytes, 总耗时: {} ms, 平均速度: {} KB/s",
//                transferType, status, context.username, context.clientIp,
//                context.remoteFilePath, totalBytes, totalTime,
//                totalTime > 0 ? String.format("%.2f", (totalBytes / 1024.0) / (totalTime / 1000.0)) : "0.00");
//    }
//
//    // ------------------------------ 5. 会话销毁：清理残留上下文 ------------------------------
//    @Override
//    public void destroying(ServerSession session) throws IOException {
//        // 防止客户端异常断开（未触发closed）导致上下文残留，在会话销毁时清理
//        if (sessionTransferContexts.remove(session) != null) {
//            log.warn("[SFTP会话销毁] 用户名: {}, 客户端IP: {}, 清理残留的传输上下文（可能客户端异常断开）",
//                    session.getUsername(), session.getClientAddress().getHostString());
//        }
//    }


}
