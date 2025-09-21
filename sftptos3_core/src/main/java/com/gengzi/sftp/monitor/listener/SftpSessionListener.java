package com.gengzi.sftp.monitor.listener;

import com.gengzi.sftp.config.SftpPublicKeyAuthenticator;
import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.dao.SftpConnectionAudit;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * sftpsession监听器
 */
@Component
public class SftpSessionListener implements SessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SftpPublicKeyAuthenticator.class);

    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;

    /**
     * 监听异常情况
     *
     * @param session The referenced {@link Session}
     * @param t       The caught exception
     */
    @Override
    public void sessionException(Session session, Throwable t) {
        session.setAttribute(Constans.SERVERSESSION_THROWABLE, t);
        SessionListener.super.sessionException(session, t);
    }

    /**
     * 监听事件，主要是认证成功事件
     *
     * @param session The referenced {@link Session}
     * @param event   The generated {@link Event}
     */
    @Override
    public void sessionEvent(Session session, Event event) {
        logger.debug("sftp sessionEvent event:{},session:{},sessionId:{}", event, session, session.getSessionId());
        if (Event.KeyEstablished.equals(event)) {
            if (session instanceof ServerSession) {
                ServerSession serverSession = (ServerSession) session;
                SocketAddress clientAddress = serverSession.getClientAddress();
                if (clientAddress instanceof InetSocketAddress) {
                    InetSocketAddress inetSocketAddress = (InetSocketAddress) clientAddress;
                    String hostAddress = inetSocketAddress.getAddress().getHostAddress();
                    SftpConnectionAudit sftpConnectionAudit = new SftpConnectionAudit();
                    sftpConnectionAudit.setSessionId(BufferUtils.toHex(session.getSessionId()));
                    sftpConnectionAudit.setClientIp(hostAddress);
                    sftpConnectionAudit.setClientPort(inetSocketAddress.getPort());
                    Long id = sftpConnectionAuditService.KeyEstablishedEvent(sftpConnectionAudit);
                    session.setAttribute(Constans.SERVERSESSION_DB_IDKEY, id);
                }
            }
        }

        if(Event.Authenticated.equals(event)){
            if (session instanceof ServerSession) {
                ServerSession serverSession = (ServerSession) session;
                String username = serverSession.getUsername();
                Long attributeId = serverSession.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
                sftpConnectionAuditService.authSuccessEvent(attributeId,username);
            }
        }

        SessionListener.super.sessionEvent(session, event);
    }

    /**
     * 当会话完全关闭后（TCP 连接断开，资源释放）
     *
     * @param session The closed {@link Session}
     */
    @Override
    public void sessionClosed(Session session) {
        logger.debug("sftp sessionClosed session:{},sessionId:{}", session, session.getSessionId());
        if (session instanceof ServerSession) {
            Long id = session.getAttribute(Constans.SERVERSESSION_DB_IDKEY);
            Throwable throwable = session.getAttribute(Constans.SERVERSESSION_THROWABLE);
            sftpConnectionAuditService.sessionClosedEvent(id,throwable == null ? "Normal shutdown" : throwable.getMessage()  );
        }
        SessionListener.super.sessionClosed(session);
    }
}
