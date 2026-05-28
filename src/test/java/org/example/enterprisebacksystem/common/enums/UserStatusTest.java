package org.example.enterprisebacksystem.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserStatusTest {

    @Test
    void shouldValidateUserStatusCode() {
        assertTrue(UserStatus.isValid(0));
        assertTrue(UserStatus.isValid(1));
        assertFalse(UserStatus.isValid(2));
        assertFalse(UserStatus.isValid(null));
    }
}
