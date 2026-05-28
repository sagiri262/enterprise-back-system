package org.example.enterprisebacksystem.service;

public interface TurnstileService {
    boolean verify(String token, String remoteIp);
}
