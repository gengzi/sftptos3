# 多阶段构建：第一阶段编译
## 使用1.6.1jdk17镜像版本进行编辑构建
FROM  gradle:7.6.1-jdk17 AS builder
# 指定工作目录（Gradle 镜像默认的工作目录是 /home/gradle，使用该目录可避免权限问题）
WORKDIR /home/gradle/project
# 复制 Gradle 配置文件（优先复制以利用缓存）
# 复制父工程和子工程的 Gradle 配置（关键：多模块需包含父工程配置）
RUN mkdir -p ./sftptos3_core

COPY settings.gradle ./
COPY build.gradle  ./
COPY ./sftptos3_core/build.gradle ./sftptos3_core
# 复制源代码
COPY ./sftptos3_core/src ./sftptos3_core/src
# 在需要重新执行的步骤前添加以下行（每次构建前修改内容即可打破缓存）
RUN echo "Force rebuild at $(date)"  # 利用时间戳变化打破缓存
# 构建当前子工程（--project-dir 指定子工程目录）
RUN gradle :sftptos3_core:bootJar --no-daemon -x test

# 第二阶段：运行环境
FROM eclipse-temurin:17.0.16_8-jre
WORKDIR /app
# 关键步骤1：创建日志目录（/app/logs）
RUN mkdir -p /app/logs && \
    # 关键步骤2：设置目录权限（避免权限不足）
    chmod 755 /app/logs
# 从构建阶段复制 JAR 包
COPY --from=builder /home/gradle/project/sftptos3_core/build/libs/*.jar sftptos3_core_nosql.jar
# 创建非 root 用户（安全最佳实践）
RUN groupadd -r appgroup && \
    useradd -r -g appgroup -d /home/appuser -s /bin/bash appuser && \
    # 确认目录和文件存在后再授权
    chown -R appuser:appgroup /app/logs /app/sftptos3_core_nosql.jar
USER appuser
# 暴露端口（根据实际应用需要）
EXPOSE 9988
# 启动命令
# 在启动命令中添加JVM参数
# 示例：设置堆内存、GC日志、编码等参数
CMD ["java","-jar", "sftptos3_core_nosql.jar"]

