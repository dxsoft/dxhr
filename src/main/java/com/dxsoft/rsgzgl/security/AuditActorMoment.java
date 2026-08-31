package com.dxsoft.rsgzgl.security;

import java.time.LocalDateTime;

public record AuditActorMoment(String actorUsername, LocalDateTime createdAt) {
}
