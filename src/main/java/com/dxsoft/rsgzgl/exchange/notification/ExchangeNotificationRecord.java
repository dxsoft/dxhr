package com.dxsoft.rsgzgl.exchange.notification;

import java.time.LocalDateTime;

public record ExchangeNotificationRecord(
        long id,
        String notificationType,
        String direction,
        String audienceScope,
        String sourceOrgCode,
        String targetOrgCode,
        String organizationCode,
        String organizationCodes,
        String packageType,
        String batchId,
        int personCount,
        String summary,
        String actionTab,
        String status,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        String readBy,
        Long workflowId,
        Integer personUid,
        Integer sourceId,
        String sourceType) {
}
