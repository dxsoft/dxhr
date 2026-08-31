package com.dxsoft.rsgzgl.ops.monitor;

public record MonitorTargetRequest(
        String name,
        String url,
        Integer timeoutMs,
        Boolean enabled
) {
}
