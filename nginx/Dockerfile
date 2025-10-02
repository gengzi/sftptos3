FROM nginx:1.29.1-alpine

# 安装 setcap 依赖（Alpine 需手动安装，Debian/Ubuntu 可能已自带）
RUN apk add --no-cache libcap

# 确认 Nginx 可执行文件路径（不同镜像可能不同，可通过 which nginx 查看）
# 为 Nginx 添加 cap_net_raw 权限
RUN setcap cap_net_raw+ep /usr/sbin/nginx

# 验证权限是否生效（可选，用于构建时检查）
RUN getcap /usr/sbin/nginx