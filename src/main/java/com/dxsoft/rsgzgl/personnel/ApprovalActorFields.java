package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record ApprovalActorFields(
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt) {

    public static ApprovalActorFields empty() {
        return new ApprovalActorFields(null, null, null, null);
    }
}
