//package com.gengzi;
//
//import io.minio.BucketExistsArgs;
//import io.minio.MakeBucketArgs;
//import io.minio.MinioClient;
//import io.minio.PutObjectArgs;
//import io.minio.RemoveObjectArgs;
//import io.minio.StatObjectArgs;
//import io.minio.errors.MinioException;
//
//import java.io.ByteArrayInputStream;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
//public class MinioDeleteTest {
//    public static void main(String[] args) {
//        // MinIO服务器配置
//        String endpoint = "http://localhost:9000"; // 默认MinIO端口
//        String accessKey = "minioadmin";          // 访问密钥
//        String secretKey = "minioadmin";          // 密钥
//        String bucketName = "image"; // 测试桶名
//        String objectKey = "321/3211";  // 测试对象名
//
//        try {
//            // 初始化MinIO客户端
//            MinioClient minioClient = MinioClient.builder()
//                    .endpoint(endpoint)
//                    .credentials(accessKey, secretKey)
//                    .build();
//
//
//            // 执行删除操作
//            minioClient.removeObject(RemoveObjectArgs.builder()
//                    .bucket(bucketName)
//                    .object(objectKey)
//                    .build());
//            System.out.println("已执行删除操作");
//
//            // 验证对象已删除
//            boolean existsAfter = checkObjectExists(minioClient, bucketName, objectKey);
//            System.out.println("删除后对象是否存在: " + (existsAfter ? "是" : "否"));
//
//
//        } catch (Exception e) {
//            System.err.println("操作过程中发生错误: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 检查对象是否存在
//     */
//    private static boolean checkObjectExists(MinioClient client, String bucket, String key)
//            throws MinioException, InvalidKeyException, NoSuchAlgorithmException, Exception {
//        try {
//            client.statObject(StatObjectArgs.builder()
//                    .bucket(bucket)
//                    .object(key)
//                    .build());
//            return true;
//        } catch (MinioException e) {
//            // 捕获"对象不存在"的错误
//            throw e;
//        }
//    }
//
//}
