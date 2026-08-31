package com.dxsoft.rsgzgl.ops.monitor;

import java.time.LocalDateTime;

public record MonitorAlertView(
        Long id,
        LocalDateTime createdAt,
        String level,
        String title,
        String message
) {
}
