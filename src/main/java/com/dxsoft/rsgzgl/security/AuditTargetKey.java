package com.dxsoft.rsgzgl.security;

public record AuditTargetKey(String targetType, String targetId) {
    public AuditTargetKey {
        targetType = targetType == null ? "" : targetType.trim();
        targetId = targetId == null ? "" : targetId.trim();
    }
}
