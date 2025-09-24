package com.gengzi.sftp.listener;

import org.apache.sshd.server.session.ServerSession;

import java.util.Objects;

/**
 * 复合键：ServerSession + remoteHandle，用于唯一标识一个文件操作
 */
public class SessionHandleKey {
    private final ServerSession session;
    private final String remoteHandle;

    public SessionHandleKey(ServerSession session, String remoteHandle) {
        this.session = session;
        this.remoteHandle = remoteHandle;
    }

    // 关键：重写equals，确保两个键的session和remoteHandle都相等时才认为相等
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionHandleKey that = (SessionHandleKey) o;
        return Objects.equals(session, that.session) &&
                Objects.equals(remoteHandle, that.remoteHandle);
    }

    // 关键：重写hashCode，结合session和remoteHandle的哈希值
    @Override
    public int hashCode() {
        return Objects.hash(session, remoteHandle);
    }
}