package com.dxsoft.rsgzgl.organization;

/** Classifies units for position dictionaries, field metadata, and payroll treatment. */
public final class UnitPayrollClassification {

    private UnitPayrollClassification() {
    }

    /** Effective 行政/事业 category for dictionaries and ryjbxx field metadata. */
    public static String effectiveUnitCategory(String unitCategory, String payrollCategory) {
        if (usesAdministrativeTreatment(unitCategory, payrollCategory)) {
            return "行政";
        }
        String normalized = normalize(unitCategory);
        return normalized.isEmpty() ? "事业" : normalized;
    }

    /** True when position/salary rules follow administrative (机关) units. */
    public static boolean usesAdministrativeTreatment(String unitCategory, String payrollCategory) {
        if ("行政".equals(normalize(unitCategory))) {
            return true;
        }
        return isCivilServiceManagedPayroll(payrollCategory);
    }

    /**
     * Legacy/VFP gzczbz: 公务员管理(0)、参照公务员(1)、依照公务员(2) use administrative rules;
     * 事业管理 uses institution rules even when dwbz is 事业.
     */
    public static boolean isCivilServiceManagedPayroll(String payrollCategory) {
        String value = normalize(payrollCategory);
        if (value.isEmpty()) {
            return false;
        }
        if ("0".equals(value) || "1".equals(value) || "2".equals(value)) {
            return true;
        }
        if (value.contains("事业管理")) {
            return false;
        }
        return value.contains("参照") || value.contains("依照") || value.contains("公务员");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
