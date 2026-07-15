package com.dxsoft.rsgzgl.statistics;

import java.util.Set;

final class RetirementMonthCalculator {

    enum Category {
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

        String label() {
            return label;
        }

        int baseRetirementAgeYears() {
            return baseRetirementAgeYears;
        }

        String delayThresholdYearMonth() {
            return delayThresholdYearMonth;
        }
    }

    record CalculationResult(
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

    static CalculationResult calculate(String birthYearMonth, String gender, String positionCode) {
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

    static int compareYearMonth(String left, String right) {
        int leftValue = yearMonthValue(left);
        int rightValue = yearMonthValue(right);
        return Integer.compare(leftValue, rightValue);
    }

    static String formatYearMonth(String yearMonth) {
        String normalized = normalizeYearMonth(yearMonth);
        if (normalized.length() == 6) {
            return normalized.substring(0, 4) + "." + normalized.substring(4, 6);
        }
        return normalized;
    }

    static String normalizeYearMonth(String yearMonth) {
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

    static String storedRetirementYearMonth(int storedValue) {
        if (storedValue <= 0) {
            return "";
        }
        String digits = String.valueOf(storedValue);
        if (digits.length() < 6) {
            return "";
        }
        return formatYearMonth(digits.substring(0, 6));
    }

    private static boolean isMale(String gender) {
        if (gender == null || gender.isBlank()) {
            return false;
        }
        String value = gender.trim();
        return "男".equals(value) || "1".equals(value) || value.equalsIgnoreCase("M");
    }

    private static boolean isWorkerPosition(String positionCode) {
        if (positionCode == null || positionCode.length() < 2) {
            return false;
        }
        return WORKER_POSITION_PREFIXES.contains(positionCode.substring(0, 2));
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
