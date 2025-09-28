package com.gengzi.sftp.scheduled;


import com.gengzi.sftp.constans.Constans;
import com.gengzi.sftp.monitor.service.SftpConnectionAuditService;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientCloseTasks {

    private static final Logger logger = LoggerFactory.getLogger(ClientCloseTasks.class);

    // 存储所有活跃连接，key为会话ID，value为会话对象
    public static final Map<Long, ServerSession> activeSessions = new ConcurrentHashMap<>();

    @Autowired
    private SftpConnectionAuditService sftpConnectionAuditService;


    // 每1分钟执行一次
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processStatistics() {
        Set<Long> longs = activeSessions.keySet();
        if(longs == null || longs.isEmpty()){
            return;
        }
        Set<Long> closeClient = sftpConnectionAuditService.manuallyCloseClient(longs);
        for (Long close : closeClient) {
            ServerSession session = activeSessions.get(close);
            session.setAttribute(Constans.SERVERSESSION_THROWABLE,new Throwable("手动关闭"));
            session.close(true);
            activeSessions.remove(close);
        }
    }




}
