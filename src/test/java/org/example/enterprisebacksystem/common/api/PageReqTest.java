package org.example.enterprisebacksystem.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageReqTest {

    @Test
    void shouldUseDefaultPageAndSize() {
        PageReq req = new PageReq();

        assertEquals(1, req.getPage());
        assertEquals(10, req.getSize());
    }
}
