package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchApprovalRequest(
        List<BatchApprovalItem> records
) {
}
