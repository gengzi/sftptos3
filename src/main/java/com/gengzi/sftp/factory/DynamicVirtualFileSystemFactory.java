package com.gengzi.sftp.factory;

import com.gengzi.sftp.nio.S3SftpFileSystemProvider;
import com.gengzi.sftp.s3.client.S3ClientNameEnum;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.session.SessionContext;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DynamicVirtualFileSystemFactory implements  FileSystemFactory {

    @Override
    public Path getUserHomeDir(SessionContext sessionContext) throws IOException {

        return null;
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
        if("admin".equals(username)){
            Map<String, Object> env = new HashMap<>();
            // 路径风格访问（MinIO 通常需要）
            env.put("s3sftp.pathStyleAccess", true);
            env.put("s3sftp.userRootPath", "ss.ss/1.xx.gg/");
            env.put("s3sftp.clientName", S3ClientNameEnum.DEFAULT_AWS_S3);
            env.put("s3sftp.sessionContext", sessionContext);
            URI s3Urix = URI.create("s3sftp://minioadmin:minioadmin@127.0.0.1:9000/image");
            S3SftpFileSystemProvider s3FileSystemProvider = new S3SftpFileSystemProvider();
            FileSystem fileSystem = s3FileSystemProvider.newFileSystem(s3Urix, env);
            return fileSystem;
        }



//        // 配置 S3 连接参数

        ;        Map<String, Object> env = new HashMap<>();
//        // 访问密钥（生产环境建议使用 IAM 角色，避免硬编码）
//        env.put("aws.accessKeyId", "YOUR_ACCESS_KEY");
//        env.put("aws.secretAccessKey", "YOUR_SECRET_KEY");
//        // 区域（如 AWS 中国区为 "cn-north-1"，MinIO 可省略）
//        env.put("aws.region", "us-east-1");
//        // 对接非 AWS 服务（如 MinIO、阿里云 OSS）时需指定端点
        // MinIO 本地示例
//        env.put("s3.endpointOverride", "http://localhost:9000");
        env.put("s3.pathStyleAccess", true); // 路径风格访问（MinIO 通常需要）
//        FileSystem s3Fs = FileSystems.newFileSystem(
//                URI.create("s3://my-bucket"), // 桶名直接包含在 URI 中
//                env,
//                Thread.currentThread().getContextClassLoader() // 类加载器
//        );
//        return s3Fs;
        URI s3Urix = URI.create("s3sftp://minioadmin:minioadmin@127.0.0.1:9000/image");

        S3SftpFileSystemProvider s3FileSystemProvider = new S3SftpFileSystemProvider();
        FileSystem fileSystem = s3FileSystemProvider.newFileSystem(s3Urix, env);

//        S3XFileSystemProvider s3xFileSystemProvider = new S3XFileSystemProvider();
//        S3NioSpiConfiguration s3NioSpiConfiguration = new S3NioSpiConfiguration(env);
//        s3xFileSystemProvider.setConfiguration(s3NioSpiConfiguration);
        return fileSystem;


    }
}