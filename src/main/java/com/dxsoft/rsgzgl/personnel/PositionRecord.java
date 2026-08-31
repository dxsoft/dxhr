package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record PositionRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String currentPositionCode,
        String currentPosition,
        String positionLevel,
        String rankCode,
        String positionCode,
        String positionName,
        String startYearMonth,
        Integer intervalYears,
        String activeFlag,
        String promotionFlag,
        String positionChangeReason,
        Integer linkedAwardId,
        String approvalStatus,
        Boolean appCreated,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt,
        int attachmentCount
) {
}
