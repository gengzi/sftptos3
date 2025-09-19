package com.gengzi.sftp.config;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * TODO 代完善
 */
@Component
public class SftpSessionListener implements SessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SftpPublicKeyAuthenticator.class);
    /**
     * 创建一个新的链接
     * @param session The created {@link Session}
     */
    @Override
    public void sessionCreated(Session session) {
        if(session instanceof ServerSession){
            ServerSession   serverSession  = (ServerSession) session;
            SocketAddress clientAddress = serverSession.getClientAddress();
            if(clientAddress instanceof InetSocketAddress){
                InetSocketAddress inetSocketAddress  = (InetSocketAddress) clientAddress;
                String hostAddress = inetSocketAddress.getAddress().getHostAddress();
                String hostName = inetSocketAddress.getHostName();
                int port = inetSocketAddress.getPort();
                boolean unresolved = inetSocketAddress.isUnresolved();

                logger.info("sftp session create,client hostAddress:{},hostName:{},port:{},unresolved:{}",hostAddress,hostName,port,unresolved);
            }

        }
    }


    @Override
    public void sessionClosed(Session session) {
        SessionListener.super.sessionClosed(session);
    }
}
