package com.dxsoft.rsgzgl.ops.monitor;

public record ServiceStatus(
        String name,
        String active,
        String sub,
        long restarts,
        String status,
        String message
) {
}
