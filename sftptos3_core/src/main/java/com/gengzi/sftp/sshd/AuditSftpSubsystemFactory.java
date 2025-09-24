package com.gengzi.sftp.sshd;

import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import java.io.IOException;

/**
 * 用于创建带审计功能的sftp系统工厂类
 *
 */
public class AuditSftpSubsystemFactory extends SftpSubsystemFactory {


    /**
     * 只重写创建子系统类的方法
     * @param channel     The {@link ChannelSession} through which the command has been received
     * @return
     * @throws IOException
     */
    @Override
    public Command createSubsystem(ChannelSession channel) throws IOException {
        AuditSftpSubsystem subsystem = new AuditSftpSubsystem(channel, this);
        GenericUtils.forEach(getRegisteredListeners(), subsystem::addSftpEventListener);
        return subsystem;
    }
}
