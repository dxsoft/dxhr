package com.dxsoft.rsgzgl.ops.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MonitorServiceTest {

    @Test
    void acceptsHttpHealthUrl() {
        assertEquals("127.0.0.1", MonitorService.validateHttpUrl("http://127.0.0.1:18081/actuator/health").getHost());
    }

    @Test
    void rejectsNonHttpUrl() {
        assertThrows(IllegalArgumentException.class, () -> MonitorService.validateHttpUrl("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> MonitorService.validateHttpUrl("not-a-url"));
    }
}
