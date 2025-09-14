package com.gengzi.sftp.listener;

import com.gengzi.sftp.cache.CacheManager;
import com.gengzi.sftp.cache.CacheUtil;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.SftpEventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FileWriteListener implements SftpEventListener {

    @Override
    public void writing(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data, int dataOffset, int dataLen) throws IOException {

        String username = session.getUsername();

        System.out.printf(username + "写入" );

        try {
            String cacheStats = CacheUtil.getCacheStats(CacheManager.getInstance().getUserPathFileAttributesCache());
            System.out.println(cacheStats );
        }catch (Exception e){
            System.out.println("统计出错了");
        }


        SftpEventListener.super.writing(session, remoteHandle, localHandle, offset, data, dataOffset, dataLen);
    }
}
