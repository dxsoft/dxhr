package com.dxsoft.rsgzgl.security;

import java.time.LocalDateTime;

public record SecurityAuditLog(
        Long id,
        String actorUsername,
        String action,
        String targetType,
        String targetId,
        String summary,
        LocalDateTime createdAt
) {
}
