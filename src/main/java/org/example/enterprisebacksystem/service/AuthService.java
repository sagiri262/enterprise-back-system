package org.example.enterprisebacksystem.service;

// 鉴权服务：要导入登陆的响应，即 LoginRequest 和 LoginResponse
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;

public interface AuthService {
    LoginResp login(LoginReq req);
}