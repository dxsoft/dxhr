package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record RankRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String jx,
        String sysj,
        String syyy,
        String rmwh,
        Integer xrjxbz,
        String lb,
        String approvalStatus,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt,
        int attachmentCount
) {
}
