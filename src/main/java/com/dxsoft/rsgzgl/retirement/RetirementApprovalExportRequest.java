package com.dxsoft.rsgzgl.retirement;

import java.util.List;

public record RetirementApprovalExportRequest(
        List<Integer> retireeIds,
        String style,
        String organizationNature) {
}
