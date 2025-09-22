package com.gengzi.sftp.monitor.response;

/**
 * 业务状态码枚举
 * 规范：
 * - 2xx：成功
 * - 4xx：客户端错误
 * - 5xx：服务端错误
 * - 1xxx：自定义业务异常
 */
public enum ResultCode {
    // 成功
    SUCCESS(200, "操作成功"),
    // 客户端错误
    PARAM_ERROR(400, "参数格式错误"),
    AUTH_ERROR(401, "未登录或令牌过期"),
    PERMISSION_ERROR(403, "无操作权限"),
    RESOURCE_NOT_FOUND(404, "请求资源不存在"),
    // 服务端错误
    SYSTEM_ERROR(500, "服务端异常，请联系管理员"),
    DB_ERROR(501, "数据库操作异常"),
    // 自定义业务异常
    USER_EXIST(1001, "用户名已存在"),
    USER_NOT_EXIST(1002, "用户不存在"),

    ORDER_CANCELED(1003, "订单已取消"),
    CONFIG_EXIST(1004, "当前配置存在"),
    ADMIN_USER_PROHIBIT_DEL(1005, "admin管理员禁止删除"),
    ;

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getter
    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}