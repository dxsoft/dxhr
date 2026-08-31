package com.dxsoft.rsgzgl.backup;

public record HisbaseSidChainRepairSummary(
        String tableName,
        int multiTipPersonsBefore,
        int brokenSidRefsBefore,
        int nullSidTipsBefore,
        int rowsRebuilt,
        int nullTipsNormalized,
        int multiTipPersonsAfter,
        int brokenSidRefsAfter,
        int nullSidTipsAfter
) {
    boolean hadChainIssuesBefore() {
        return multiTipPersonsBefore > 0 || brokenSidRefsBefore > 0;
    }

    boolean isHealthyAfter() {
        return multiTipPersonsAfter == 0 && brokenSidRefsAfter == 0 && nullSidTipsAfter == 0;
    }
}
