package org.example.enterprisebacksystem.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.service.TurnstileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class TurnstileServiceImpl implements TurnstileService {
    private final RestClient.Builder restClientBuilder;

    @Value("${blog.turnstile.secret:}")
    private String secret;

    @Value("${blog.turnstile.enabled:true}")
    private boolean enabled;

    @Override
    public boolean verify(String token, String remoteIp) {
        if (!enabled) {
            return true;
        }
        if (secret == null || secret.isBlank() || token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secret);
        body.add("response", token);
        if (remoteIp != null && !remoteIp.isBlank()) {
            body.add("remoteip", remoteIp);
        }

        try {
            TurnstileResp resp = restClientBuilder.build()
                    .post()
                    .uri("https://challenges.cloudflare.com/turnstile/v0/siteverify")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(TurnstileResp.class);

            return resp != null && resp.isSuccess();
        } catch (RestClientException ex) {
            return false;
        }
    }

    @Data
    private static class TurnstileResp {
        private boolean success;
    }
}
