package org.example.enterprisebacksystem.common.enums;

import lombok.Getter;

@Getter
public enum PermissionType {
    MENU(1, "菜单"),
    BUTTON(2, "按钮");

    private final int code;
    private final String desc;

    PermissionType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

