package com.dxsoft.rsgzgl.ops.monitor;

import java.time.LocalDateTime;

public record MonitorTargetView(
        Long id,
        String name,
        String kind,
        String url,
        int timeoutMs,
        boolean enabled,
        LocalDateTime createdAt
) {
}
