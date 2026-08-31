package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record PersonnelFieldPolicyView(
        boolean canEditBasic,
        boolean canDelete,
        boolean canSave,
        String blockReason,
        String approvalStatus,
        boolean canChangeApproval,
        boolean canSubmit,
        boolean canApprove,
        boolean canReturnToDraft,
        boolean canCancelApproval,
        List<PersonnelFieldPolicyEntry> fields) {
}
