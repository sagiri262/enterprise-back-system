package org.example.enterprisebacksystem.common.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilsTest {

    @Test
    void shouldGenerateAndParseToken() {
        String token = JwtUtils.generateToken(1L, "admin");

        Claims claims = JwtUtils.parseToken(token);

        assertEquals("admin", claims.getSubject());
        assertEquals(1, claims.get("userId", Integer.class));
    }
}
