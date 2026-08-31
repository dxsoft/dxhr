package com.dxsoft.rsgzgl.dictionary;

/**
 * Maps unified rank dictionary codes ({@code 02301}–{@code 02304}) to jx-table lb values.
 */
public final class RankCategorySupport {

    public static final String BRANCH_JX = "02301";
    public static final String BRANCH_JC = "02302";
    public static final String BRANCH_SP = "02303";
    public static final String BRANCH_MT = "02304";

    private RankCategorySupport() {
    }

    public static String categoryFromCode(String code) {
        String normalized = code == null ? "" : code.trim();
        if (normalized.startsWith(BRANCH_JX)) {
            return "jx";
        }
        if (normalized.startsWith(BRANCH_JC)) {
            return "jc";
        }
        if (normalized.startsWith(BRANCH_SP)) {
            return "sp";
        }
        if (normalized.startsWith(BRANCH_MT)) {
            return "mt";
        }
        return "";
    }

    public static boolean isSelectableLeaf(String code) {
        String normalized = code == null ? "" : code.trim();
        return normalized.length() >= 7 && !categoryFromCode(normalized).isBlank();
    }
}
