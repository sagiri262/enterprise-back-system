package org.example.enterprisebacksystem.common.api;

import lombok.Getter;

@Getter
public enum ErrorCode {
    OK(0, "OK"),

    // 通用错误
    BAD_REQUEST(40000, "参数错误"),
    UNAUTHORIZED(40100, "未认证"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    INTERNAL_ERROR(50000, "系统正繁忙，请稍后再试"),

    // 业务错误
    BIZ_ERROR(60000, "业务异常");
    // ...后续可以补充

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
