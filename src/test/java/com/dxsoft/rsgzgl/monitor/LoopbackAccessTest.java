package com.dxsoft.rsgzgl.monitor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoopbackAccessTest {

    @Test
    void acceptsLoopbackAddresses() {
        assertTrue(LoopbackAccess.isLoopback("127.0.0.1"));
        assertTrue(LoopbackAccess.isLoopback("::1"));
    }

    @Test
    void rejectsPublicAddresses() {
        assertFalse(LoopbackAccess.isLoopback("101.201.76.253"));
        assertFalse(LoopbackAccess.isLoopback(""));
    }
}
