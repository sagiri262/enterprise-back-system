package org.example.enterprisebacksystem.dto.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssignRolePermissionsReqTest {

    @Test
    void shouldHoldPermissionIdsForBatchGrant() {
        AssignRolePermissionsReq req = new AssignRolePermissionsReq();
        req.setPermissionIds(List.of(1L, 2L, 3L));

        assertEquals(3, req.getPermissionIds().size());
    }
}
