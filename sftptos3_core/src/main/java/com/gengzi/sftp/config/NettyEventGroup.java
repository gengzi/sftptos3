package com.gengzi.sftp.config;

import io.netty.channel.nio.NioEventLoopGroup;

public class NettyEventGroup {

    // 自定义 Netty EventLoopGroup（IO 线程池）
    public static final NioEventLoopGroup CUSTOMEVENTLOOPGROUP = new NioEventLoopGroup(100,
            r -> {
                return new Thread(r, "sftptos3-netty-io-thread");
            }
    );
}
