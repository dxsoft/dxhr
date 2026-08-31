package com.dxsoft.rsgzgl.ops.monitor;

public record ProbeResult(
        Long targetId,
        String name,
        String url,
        String status,
        int httpStatus,
        long latencyMs,
        String message
) {
}
