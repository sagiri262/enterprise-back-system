package org.example.enterprisebacksystem.dto.auth;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResp {
    /*
    * 1、令牌
    * 2、用户 ID
    * 3、用户名*/
    private String token;
    private Long userId;
    private String username;
}
