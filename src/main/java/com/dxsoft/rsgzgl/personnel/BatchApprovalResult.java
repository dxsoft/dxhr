package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchApprovalResult(
        int approved,
        int skipped,
        List<BatchApprovalFailure> failures
) {
}
