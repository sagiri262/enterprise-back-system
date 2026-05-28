package org.example.enterprisebacksystem.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionTypeTest {

    @Test
    void shouldValidatePermissionTypeCode() {
        assertTrue(PermissionType.isValid(1));
        assertTrue(PermissionType.isValid(2));
        assertFalse(PermissionType.isValid(0));
        assertFalse(PermissionType.isValid(null));
    }
}
