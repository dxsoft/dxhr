package com.dxsoft.rsgzgl.statistics;

import java.util.Set;

public final class RetirementMonthCalculator {

    public enum Category {
        MALE("男职工", 60, "1965.01"),
        FEMALE_CADRE("女干部", 55, "1970.01"),
        FEMALE_WORKER("女工勤", 50, "1975.01");

        private final String label;
        private final int baseRetirementAgeYears;
        private final String delayThresholdYearMonth;

        Category(String label, int baseRetirementAgeYears, String delayThresholdYearMonth) {
            this.label = label;
            this.baseRetirementAgeYears = baseRetirementAgeYears;
            this.delayThresholdYearMonth = delayThresholdYearMonth;
        }

        public String label() {
            return label;
        }

        int baseRetirementAgeYears() {
            return baseRetirementAgeYears;
        }

        String delayThresholdYearMonth() {
            return delayThresholdYearMonth;
        }
    }

    public record CalculationResult(
            String retirementYearMonth,
            int delayMonths,
            Category category) {
    }

    private static final Set<String> WORKER_POSITION_PREFIXES = Set.of("05", "06", "08", "09");

    private RetirementMonthCalculator() {
    }

    static Category resolveCategory(String gender, String positionCode) {
        if (isMale(gender)) {
            return Category.MALE;
        }
        if (isWorkerPosition(positionCode)) {
            return Category.FEMALE_WORKER;
        }
        return Category.FEMALE_CADRE;
    }

    public static CalculationResult calculate(String birthYearMonth, String gender, String positionCode) {
        Category category = resolveCategory(gender, positionCode);
        String normalizedBirth = normalizeYearMonth(birthYearMonth);
        if (normalizedBirth.isBlank()) {
            return new CalculationResult("", 0, category);
        }
        int delayMonths = delayMonths(normalizedBirth, category);
        return new CalculationResult(
                retirementYearMonth(normalizedBirth, delayMonths, category.baseRetirementAgeYears()),
                delayMonths,
                category);
    }

    public static boolean isRetirementDue(String birthYearMonth, String gender, String positionCode, String referencePeriod) {
        CalculationResult calculation = calculate(birthYearMonth, gender, positionCode);
        if (calculation.retirementYearMonth().isBlank()) {
            return false;
        }
        String reference = normalizeYearMonth(referencePeriod);
        if (reference.isBlank()) {
            return false;
        }
        return compareYearMonth(calculation.retirementYearMonth(), reference) <= 0;
    }

    public static boolean isRetirementWithinOneMonth(
            String birthYearMonth,
            String gender,
            String positionCode,
            String referencePeriod) {
        CalculationResult calculation = calculate(birthYearMonth, gender, positionCode);
        if (calculation.retirementYearMonth().isBlank()) {
            return false;
        }
        String reference = normalizeYearMonth(referencePeriod);
        if (reference.isBlank()) {
            return false;
        }
        if (compareYearMonth(calculation.retirementYearMonth(), reference) <= 0) {
            return false;
        }
        String upperBound = yearMonthPlusMonths(reference, 1);
        if (upperBound.isBlank()) {
            return false;
        }
        return compareYearMonth(calculation.retirementYearMonth(), upperBound) <= 0;
    }

    public static String yearMonthPlusMonths(String yearMonth, int months) {
        String normalized = normalizeYearMonth(yearMonth);
        if (normalized.length() < 6 || months < 0) {
            return "";
        }
        try {
            int year = Integer.parseInt(normalized.substring(0, 4));
            int month = Integer.parseInt(normalized.substring(4, 6));
            int totalMonths = year * 12 + (month - 1) + months;
            if (totalMonths < 0) {
                return "";
            }
            int resultYear = totalMonths / 12;
            int resultMonth = totalMonths % 12 + 1;
            return String.format("%04d%02d", resultYear, resultMonth);
        } catch (NumberFormatException ex) {
            return "";
        }
    }

    public static int compareYearMonth(String left, String right) {
        int leftValue = yearMonthValue(left);
        int rightValue = yearMonthValue(right);
        return Integer.compare(leftValue, rightValue);
    }

    public static String formatYearMonth(String yearMonth) {
        String normalized = normalizeYearMonth(yearMonth);
        if (normalized.length() == 6) {
            return normalized.substring(0, 4) + "." + normalized.substring(4, 6);
        }
        return normalized;
    }

    public static String normalizeYearMonth(String yearMonth) {
        if (yearMonth == null) {
            return "";
        }
        String trimmed = yearMonth.trim();
        if (trimmed.isBlank()) {
            return "";
        }
        String digits = trimmed.replace(".", "");
        if (digits.length() >= 6) {
            return digits.substring(0, 6);
        }
        if (digits.length() == 4) {
            return digits + "01";
        }
        return digits;
    }

    public static String storedRetirementYearMonth(int storedValue) {
        if (storedValue <= 0) {
            return "";
        }
        String digits = String.valueOf(storedValue);
        if (digits.length() < 6) {
            return "";
        }
        return formatYearMonth(digits.substring(0, 6));
    }

    /**
     * SQL 预筛上界：男职工最早法定退休年龄 60，出生晚于此的不可能已达退休。
     * 延迟退休只会更晚退休，因此可安全用于缩小候选集。
     */
    public static String maleBirthUpperBound(String referencePeriod) {
        return yearMonthMinusYears(referencePeriod, Category.MALE.baseRetirementAgeYears());
    }

    /**
     * SQL 预筛上界：女工勤最早法定退休年龄 50（各类别中最早），出生晚于此的不可能已达退休。
     */
    public static String femaleBirthUpperBound(String referencePeriod) {
        return yearMonthMinusYears(referencePeriod, Category.FEMALE_WORKER.baseRetirementAgeYears());
    }

    static String yearMonthMinusYears(String yearMonth, int years) {
        String normalized = normalizeYearMonth(yearMonth);
        if (normalized.length() < 6 || years < 0) {
            return "";
        }
        try {
            int year = Integer.parseInt(normalized.substring(0, 4)) - years;
            int month = Integer.parseInt(normalized.substring(4, 6));
            if (year < 1 || month < 1 || month > 12) {
                return "";
            }
            return String.format("%04d%02d", year, month);
        } catch (NumberFormatException ex) {
            return "";
        }
    }

    private static boolean isMale(String gender) {
        if (gender == null || gender.isBlank()) {
            return false;
        }
        String value = gender.trim();
        return "男".equals(value) || "1".equals(value) || value.equalsIgnoreCase("M") || value.equalsIgnoreCase("male");
    }

    private static boolean isWorkerPosition(String positionCode) {
        if (positionCode == null || positionCode.isBlank()) {
            return false;
        }
        String code = positionCode.trim();
        if (code.length() < 2) {
            return false;
        }
        // 对齐 VFP：女工勤按岗位前缀 05/06/08/09（警员工勤序列）判定。
        return WORKER_POSITION_PREFIXES.contains(code.substring(0, 2));
    }

    private static int delayMonths(String birthYearMonth, Category category) {
        String comparableBirth = toComparableYearMonth(birthYearMonth);
        String threshold = toComparableYearMonth(category.delayThresholdYearMonth());
        if (comparableBirth.compareTo(threshold) < 0) {
            return 0;
        }
        int birthYear = Integer.parseInt(comparableBirth.substring(0, 4));
        int birthMonth = Integer.parseInt(comparableBirth.substring(4, 6));
        return switch (category) {
            case MALE, FEMALE_CADRE -> {
                int thresholdYear = Integer.parseInt(threshold.substring(0, 4));
                int months = ((birthYear - thresholdYear) * 12 + birthMonth + 3) / 4;
                yield Math.min(months, 36);
            }
            case FEMALE_WORKER -> {
                int thresholdYear = Integer.parseInt(threshold.substring(0, 4));
                int months = ((birthYear - thresholdYear) * 12 + birthMonth + 1) / 2;
                yield Math.min(months, 60);
            }
        };
    }

    private static String retirementYearMonth(String birthYearMonth, int delayMonths, int baseRetirementAgeYears) {
        String comparableBirth = toComparableYearMonth(birthYearMonth);
        int birthYear = Integer.parseInt(comparableBirth.substring(0, 4));
        int birthMonth = Integer.parseInt(comparableBirth.substring(4, 6));
        if (delayMonths % 12 == 0) {
            int retirementYear = birthYear + delayMonths / 12 + baseRetirementAgeYears;
            return formatYearMonth(String.format("%04d%02d", retirementYear, birthMonth));
        }
        int combinedMonth = delayMonths % 12 + birthMonth;
        if (combinedMonth > 12) {
            int retirementYear = birthYear + delayMonths / 12 + baseRetirementAgeYears + 1;
            return formatYearMonth(String.format("%04d%02d", retirementYear, combinedMonth - 12));
        }
        int retirementYear = birthYear + delayMonths / 12 + baseRetirementAgeYears;
        return formatYearMonth(String.format("%04d%02d", retirementYear, combinedMonth));
    }

    private static String toComparableYearMonth(String yearMonth) {
        String normalized = normalizeYearMonth(yearMonth);
        if (normalized.length() < 6) {
            return "000000";
        }
        return normalized.substring(0, 6);
    }

    private static int yearMonthValue(String yearMonth) {
        String normalized = normalizeYearMonth(yearMonth);
        if (normalized.length() < 6) {
            return 0;
        }
        try {
            return Integer.parseInt(normalized.substring(0, 6));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
