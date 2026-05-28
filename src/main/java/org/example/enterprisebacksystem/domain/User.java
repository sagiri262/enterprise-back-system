package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    private Integer siteOwner;
    private Integer status; // 1:启用 0:禁用
    @TableLogic
    private Integer deleted;
}
