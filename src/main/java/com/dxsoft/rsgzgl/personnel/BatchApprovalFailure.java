package com.dxsoft.rsgzgl.personnel;

public record BatchApprovalFailure(
        String personCode,
        String personName,
        String recordType,
        Integer recordId,
        String message
) {
}
