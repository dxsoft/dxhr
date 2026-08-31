package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record AwardRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String hjmc,
        String sjdw,
        String jllx,
        String hjsj,
        String tqyjjssj,
        String qtqk,
        Integer jldc,
        Integer jljb,
        String approvalStatus,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt,
        int attachmentCount
) {
}
