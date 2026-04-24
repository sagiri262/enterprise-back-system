package org.example.enterprisebacksystem.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
