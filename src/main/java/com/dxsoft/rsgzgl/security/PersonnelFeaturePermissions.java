package com.dxsoft.rsgzgl.security;

/** Fine-grained read/write permissions for personnel basic information. */
public final class PersonnelFeaturePermissions {

    public static final String LEGACY_READ = "PERSONNEL_READ";
    public static final String LEGACY_WRITE = "PERSONNEL_WRITE";
    public static final String BASIC_READ = "PERSONNEL_BASIC_READ";
    public static final String BASIC_WRITE = "PERSONNEL_BASIC_WRITE";
    public static final String KEY_FIELD_WRITE = "PERSONNEL_KEY_FIELD_WRITE";
    public static final String APPROVAL_WRITE = "PERSONNEL_APPROVAL_WRITE";

    private PersonnelFeaturePermissions() {
    }

    public static String[] basicReadAuthorities() {
        return new String[] {BASIC_READ, LEGACY_READ};
    }

    public static String[] basicWriteAuthorities() {
        return new String[] {BASIC_WRITE};
    }

    public static String[] approvalWriteAuthorities() {
        return new String[] {APPROVAL_WRITE};
    }

    public static String[] subrecordWriteAuthorities() {
        return new String[] {LEGACY_WRITE};
    }

    public static String[] approvalTrackingReadAuthorities() {
        return new String[] {APPROVAL_WRITE, BASIC_WRITE, LEGACY_WRITE, BASIC_READ, LEGACY_READ};
    }
}
