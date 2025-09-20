package com.gengzi.sftp.factory;

import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.context.ServerSessionUserInfoContext;
import com.gengzi.sftp.enums.StorageTypeEnum;
import com.gengzi.sftp.nio.S3SftpFileSystemProvider;
import com.gengzi.sftp.nio.S3SftpNioSpiConfiguration;
import com.gengzi.sftp.s3.client.S3ClientNameEnum;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.SessionContext;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DynamicVirtualFileSystemFactory implements FileSystemFactory {


    private final VirtualFileSystemFactory virtualFileSystemFactory = new VirtualFileSystemFactory();


    @Override
    public Path getUserHomeDir(SessionContext sessionContext) throws IOException {
        ServerSessionUserInfoContext serverSessionUserInfoContext =
                sessionContext.getAttribute(Constans.SERVERSESSIONUSERINFOCONTEXT);

        if (StorageTypeEnum.LOCAL.type().equals(serverSessionUserInfoContext.getAccessStorageType())) {
           return virtualFileSystemFactory.getDefaultHomeDir();
//            return NativeFileSystemFactory.INSTANCE.getUserHomeDir(sessionContext);
        }

        if (StorageTypeEnum.S3.type().equals(serverSessionUserInfoContext.getAccessStorageType())) {
            return Paths.get(serverSessionUserInfoContext.getUserRootPath()).normalize().toAbsolutePath();
        }
        throw new IOException("不支持的存储类型");
    }

    /**
     * 在这里处理虚拟文件系统创建逻辑
     * 是本地文件系统
     * 还是s3文件系统
     *
     * @param sessionContext
     * @return
     * @throws IOException
     */
    @Override
    public FileSystem createFileSystem(SessionContext sessionContext) throws IOException {
        String username = sessionContext.getUsername();
        ServerSessionUserInfoContext serverSessionUserInfoContext =
                sessionContext.getAttribute(Constans.SERVERSESSIONUSERINFOCONTEXT);

        if (StorageTypeEnum.LOCAL.type().equals(serverSessionUserInfoContext.getAccessStorageType())) {
            virtualFileSystemFactory.setDefaultHomeDir(Path.of(serverSessionUserInfoContext.getUserRootPath()));
            return virtualFileSystemFactory.createFileSystem(sessionContext);

//            return NativeFileSystemFactory.INSTANCE.createFileSystem(sessionContext);

        }

        if (StorageTypeEnum.S3.type().equals(serverSessionUserInfoContext.getAccessStorageType())) {
            Map<String, Object> env = new HashMap<>();
            env.put("s3sftp.pathStyleAccess", true);
            env.put(S3SftpNioSpiConfiguration.USER_ROOT_PATH, serverSessionUserInfoContext.getUserRootPath());
            env.put(S3SftpNioSpiConfiguration.CLIENT_NAME, S3ClientNameEnum.DEFAULT_AWS_S3);
            env.put(S3SftpNioSpiConfiguration.SESSION_CONTEXT, sessionContext);
            env.put(S3SftpNioSpiConfiguration.REGION, serverSessionUserInfoContext.getS3Region());
            URI s3Urix = URI.create(serverSessionUserInfoContext.getS3SftpSchemeUri());
            S3SftpFileSystemProvider s3FileSystemProvider = new S3SftpFileSystemProvider();
            FileSystem fileSystem = s3FileSystemProvider.newFileSystem(s3Urix, env);
            return fileSystem;
        }

        throw new IOException("不支持的存储类型");

    }
}