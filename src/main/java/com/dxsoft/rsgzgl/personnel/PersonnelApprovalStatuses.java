package com.dxsoft.rsgzgl.personnel;

final class PersonnelApprovalStatuses {

    static final String DRAFT = "草稿";
    static final String SUBMITTED = "申报";
    static final String APPROVED = "审批通过";
    private static final String LEGACY_INITIAL = "初始建库";

    private PersonnelApprovalStatuses() {
    }

    static String defaultStatus() {
        return DRAFT;
    }

    static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return DRAFT;
        }
        String trimmed = raw.trim();
        if (LEGACY_INITIAL.equals(trimmed)) {
            return DRAFT;
        }
        if (DRAFT.equals(trimmed) || SUBMITTED.equals(trimmed) || APPROVED.equals(trimmed)) {
            return trimmed;
        }
        return DRAFT;
    }

    static boolean isDraft(String raw) {
        return DRAFT.equals(normalize(raw));
    }

    static boolean isSubmitted(String raw) {
        return SUBMITTED.equals(normalize(raw));
    }

    static boolean isApproved(String raw) {
        return APPROVED.equals(normalize(raw));
    }
}
