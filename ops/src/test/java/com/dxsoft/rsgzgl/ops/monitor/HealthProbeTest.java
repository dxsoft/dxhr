package com.dxsoft.rsgzgl.ops.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HealthProbeTest {

    @Test
    void redirectToLoginIsCrit() {
        ProbeResult result = HealthProbe.classify(
                1L, "主实例", "http://127.0.0.1:8080/actuator/health",
                302, 20, "http://127.0.0.1:8080/login.html", "", null);
        assertEquals("CRIT", result.status());
        assertTrue(result.message().contains("登录"));
    }

    @Test
    void actuatorUpIsOk() {
        ProbeResult result = HealthProbe.classify(
                1L, "pq", "http://127.0.0.1:18082/actuator/health",
                200, 15, "", "{\"status\":\"UP\"}", "UP");
        assertEquals("OK", result.status());
        assertEquals("UP", result.message());
    }

    @Test
    void runtimeUrlFromHealth() {
        assertEquals(
                "http://127.0.0.1:18081/internal/runtime",
                HealthProbe.runtimeUrl("http://127.0.0.1:18081/actuator/health"));
    }

    @Test
    void htmlLoginPageIsCrit() {
        ProbeResult result = HealthProbe.classify(
                1L, "demo", "http://127.0.0.1:18081/actuator/health",
                200, 10, "", "<form><input type=\"password\"></form>", null);
        assertEquals("CRIT", result.status());
    }
}
