CREATE DATABASE  IF NOT EXISTS `sftptos3` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `sftptos3`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: sftptos3
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0-禁用 1-正常）',
  `creater` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `username` varchar(128) NOT NULL COMMENT '管理员username',
  `passwd` varchar(128) NOT NULL COMMENT '管理员passwd',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `s3_storage`
--

DROP TABLE IF EXISTS `s3_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `s3_storage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0-禁用 1-正常）',
  `creator` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `bucket` varchar(64) NOT NULL COMMENT 's3桶',
  `endpoint` varchar(256) NOT NULL COMMENT 's3存储访问的地址',
  `access_key` varchar(256) NOT NULL COMMENT '账户',
  `access_secret` varchar(256) NOT NULL COMMENT '秘钥',
  `s3_name` varchar(45) NOT NULL COMMENT 's3存储名称',
  `region` varchar(256) NOT NULL COMMENT '地区',
  PRIMARY KEY (`id`),
  UNIQUE KEY `s3_name_UNIQUE` (`s3_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='s3存储表：s3链接桶等信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sftp_audit`
--

DROP TABLE IF EXISTS `sftp_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sftp_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `remark` varchar(1024) DEFAULT NULL COMMENT '备注信息',
  `client_address` varchar(256) NOT NULL COMMENT '客户端网络地址',
  `client_username` varchar(256) NOT NULL COMMENT '客户端用户名',
  `file_path` varchar(1024) NOT NULL COMMENT '文件路径',
  `type` varchar(32) NOT NULL COMMENT '对文件操作类型：UPLOAD（上传）、DOWNLOAD（下载）、DELETE（删除）、RENAME（重命名）',
  `file_stroage_info` varchar(1024) NOT NULL COMMENT '文件存储信息：本地文件（local），s3文件（对应的 用户名:请求地址/桶）',
  `opt_time` datetime NOT NULL COMMENT '操作时间',
  `file_size` varchar(64) NOT NULL COMMENT '文件大小',
  `operate_result` tinyint NOT NULL COMMENT '操作是否成功',
  `error_msg` varchar(512) NOT NULL COMMENT '错误信息',
  `client_audit_id` bigint NOT NULL COMMENT '客户端认证数据库表主键id',
  `completion_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remove_file_path` varchar(1024) DEFAULT NULL COMMENT '移动后的新的路径',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=231632 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='sftp审计表：记录客户端操作行为';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sftp_connection_audit`
--

DROP TABLE IF EXISTS `sftp_connection_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sftp_connection_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '连接用户名',
  `client_ip` varchar(32) NOT NULL COMMENT '客户端IP',
  `client_port` int DEFAULT NULL COMMENT '客户端端口',
  `connect_time` datetime NOT NULL COMMENT '连接建立时间',
  `disconnect_time` datetime DEFAULT NULL COMMENT '连接断开时间',
  `auth_status` tinyint NOT NULL DEFAULT '0' COMMENT '认证状态：0未认证，1成功，2失败',
  `auth_failure_reason` varchar(128) DEFAULT NULL COMMENT '认证失败原因',
  `disconnect_reason` varchar(1024) DEFAULT NULL COMMENT '断开原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计记录创建时间',
  `manually_close_client` tinyint DEFAULT NULL COMMENT '是否手动关闭正在链接client  1 是 2 否',
  `auth_type` varchar(32) NOT NULL COMMENT '认证类型：1 密码，2密钥',
  PRIMARY KEY (`id`),
  KEY `idx_client_ip` (`client_ip`),
  KEY `idx_username` (`username`),
  KEY `idx_connect_time` (`connect_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1172 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SFTP客户端连接审计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `statistics_record`
--

DROP TABLE IF EXISTS `statistics_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `statistics_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `start_time` datetime NOT NULL COMMENT '统计开始时间',
  `end_time` datetime NOT NULL COMMENT '统计结束时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '任务状态：0-未执行 1-执行中 2-已完成 3-失败',
  `execute_time` datetime DEFAULT NULL COMMENT '实际执行时间',
  `duration` int DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `auth_count_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：总登录数',
  `auth_success_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：认证成功数',
  `auth_failure_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：认证失败数',
  `download_count_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：总登录数',
  `download_success_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：下载成功数',
  `download_failure_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：下载失败数',
  `upload_count_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：总登录数',
  `upload_success_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：下载成功数',
  `upload_failure_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：下载失败数',
  `del_count_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：总登录数',
  `del_success_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：下载成功数',
  `del_failure_val` bigint NOT NULL DEFAULT '0' COMMENT '指标1：下载失败数',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `upload_size` varchar(64) NOT NULL DEFAULT '0' COMMENT '上传文件大小',
  `download_size` varchar(64) NOT NULL DEFAULT '0' COMMENT '下载文件大小',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_time_type` (`start_time`,`end_time`) COMMENT '时间范围+类型唯一约束'
) ENGINE=InnoDB AUTO_INCREMENT=560 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统计任务与结果综合表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(128) NOT NULL COMMENT '用户名',
  `passwd` varchar(128) NOT NULL COMMENT '密码',
  `user_root_path` varchar(1024) DEFAULT '' COMMENT '用户根目录',
  `access_storage_type` varchar(64) NOT NULL DEFAULT 'default_aws-s3' COMMENT '访问存储的类型',
  `access_storage_info` varchar(512) DEFAULT NULL COMMENT '访问存储的连接信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` varchar(128) NOT NULL COMMENT '创建人id',
  `updater` varchar(128) NOT NULL COMMENT '更新人id',
  `remark` varchar(128) DEFAULT '' COMMENT '备注',
  `secret_key` text COMMENT '客户端密钥ssh生成的',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表：管理访问sftp服务的用户信息';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-29 21:24:10


-- 插入一条数据
INSERT INTO `admin` VALUES (1,1,NULL,'2023-09-29 21:24:10',NULL,'2023-09-29 21:24:10','', 'admin', '$2a$10$x/.QhiVZK75kkIcfX5HBSe1VC2oGmCLwRinWrDNw/GzB9HRn1YiPu');