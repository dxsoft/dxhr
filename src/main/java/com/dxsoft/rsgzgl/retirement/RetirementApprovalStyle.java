package com.dxsoft.rsgzgl.retirement;

/**
 * 退休审批表年款样式，对标 VFP {@code dy.Optiongroup2}：2006 / 2021 / 2025。
 * 2006 机关/事业分 xz/sy；2021/2025 为机关事业通用表样。
 */
public enum RetirementApprovalStyle {
    STYLE_2006("2006", "2006版", "", "txspbxz", "txspbsy"),
    STYLE_2021("2021", "2021版（机关事业通用）", "21", "txspb21", "txspb21"),
    STYLE_2025("2025", "2025版（机关事业通用）", "25", "txspb25", "txspb25");

    private final String code;
    private final String label;
    private final String fileSuffix;
    private final String agencyTemplate;
    private final String institutionTemplate;

    RetirementApprovalStyle(
            String code,
            String label,
            String fileSuffix,
            String agencyTemplate,
            String institutionTemplate) {
        this.code = code;
        this.label = label;
        this.fileSuffix = fileSuffix;
        this.agencyTemplate = agencyTemplate;
        this.institutionTemplate = institutionTemplate;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String fileSuffix() {
        return fileSuffix;
    }

    public String agencyTemplate() {
        return agencyTemplate;
    }

    public String institutionTemplate() {
        return institutionTemplate;
    }

    public static RetirementApprovalStyle fromCode(String code) {
        if (code == null || code.isBlank()) {
            return STYLE_2025;
        }
        String normalized = code.trim();
        for (RetirementApprovalStyle style : values()) {
            if (style.code.equals(normalized) || style.name().equalsIgnoreCase(normalized)) {
                return style;
            }
        }
        return STYLE_2025;
    }
}
