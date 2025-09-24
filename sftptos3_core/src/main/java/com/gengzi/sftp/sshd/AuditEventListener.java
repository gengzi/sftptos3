package com.gengzi.sftp.sshd;

import org.apache.sshd.common.util.SshdEventListener;

import java.util.EventListener;

/**
 * 审计事件监听器
 * 监控对应的操作，添加审计功能
 *
 * 此监听器执行时机
 * 1，在操作文件之前执行
 * 2，操作文件完成后执行
 * 3，如果操作文件过程有异常也会执行
 *
 */
public interface AuditEventListener extends SshdEventListener {





}
