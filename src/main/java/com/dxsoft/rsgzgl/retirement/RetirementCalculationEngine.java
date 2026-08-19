package com.dxsoft.rsgzgl.retirement;

import com.dxsoft.rsgzgl.payroll.PayrollRepository;
import com.dxsoft.rsgzgl.retirement.RetirementRepository.RetirementAllowanceStandardRow;
import com.dxsoft.rsgzgl.retirement.RetirementRepository.RetirementSeedRow;
import com.dxsoft.rsgzgl.statistics.RetirementMonthCalculator;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Ports VFP ltxbl06 calculation: jsgz → jsjbt → jshj.
 */
@Component
class RetirementCalculationEngine {

    private final RetirementRepository retirementRepository;
    private final PayrollRepository payrollRepository;

    RetirementCalculationEngine(
            RetirementRepository retirementRepository,
            PayrollRepository payrollRepository) {
        this.retirementRepository = retirementRepository;
        this.payrollRepository = payrollRepository;
    }

    RetirementWageCalculation calculate(
            RetirementSeedRow seed,
            String retirementDate,
            String retirementCategory) {
        String standardYm = digits(seed.salaryStandardYearMonth(), "200607");
        String allowanceYm = digits(seed.allowanceStandardYearMonth(), "201401");
        String category = blank(retirementCategory).isEmpty() ? "退休" : retirementCategory.trim();
        int salaryYears = Math.max(seed.salaryYears(), 0);
        int conversionRatio = retirementRepository.lookupConversionRatio(
                seed.postCategory(), salaryYears, category);
        int teachingPercent = Math.max(seed.raisePercentage(), 0);
        int feeIncreaseRatio = 0;

        WageAmounts wages = resolveWages(seed, standardYm);
        boolean wagesFromStandards = wages.fromStandards();
        int teachingRaise = Math.round((wages.positionSalary() + wages.gradeSalary()) * teachingPercent / 100.0f);
        int wageBase = wages.positionSalary() + wages.gradeSalary() + wages.technicalSalary()
                + seed.rankAllowance() + teachingRaise + seed.retainedSpecial();
        int effectiveRatio = Math.min(conversionRatio + feeIncreaseRatio, 100);
        int convertedWageBase = Math.round(wageBase * Math.max(effectiveRatio, 0) / 100.0f);
        int basicRetirementFee = convertedWageBase + seed.teachingAllowance();

        int educationCategory = retirementRepository.organizationEducationCategory(seed.organizationCode());
        if (RetirementMonthCalculator.compareYearMonth(allowanceYm, "200901") < 0) {
            educationCategory = 0;
        }

        Optional<RetirementAllowanceStandardRow> beforeStd = retirementRepository.findActiveAllowanceStandard(
                allowanceYm, seed.positionCode(), educationCategory);
        Optional<RetirementAllowanceStandardRow> afterStd = retirementRepository.findRetirementAllowanceStandard(
                allowanceYm, seed.positionCode(), category, educationCategory);

        AllowanceAmounts before = beforeStd
                .map(row -> fromStandard(row, seed, true))
                .orElseGet(() -> fromSeed(seed));
        AllowanceAmounts after = afterStd
                .map(row -> fromStandard(row, seed, false))
                .orElseGet(() -> fromSeed(seed));
        boolean allowancesFromStandards = beforeStd.isPresent() || afterStd.isPresent();

        int beforeAllowanceTotal = before.total();
        int afterAllowanceTotal = after.total();
        int beforeTotal = wageBase + seed.teachingAllowance() + beforeAllowanceTotal;
        int afterTotal = basicRetirementFee + afterAllowanceTotal; // lczjldxf = 0 on first seed

        String note = buildNote(
                wagesFromStandards,
                allowancesFromStandards,
                conversionRatio,
                feeIncreaseRatio,
                effectiveRatio,
                standardYm,
                allowanceYm);

        return new RetirementWageCalculation(
                salaryYears,
                conversionRatio,
                feeIncreaseRatio,
                effectiveRatio,
                wages.positionSalary(),
                wages.gradeSalary(),
                wages.technicalSalary(),
                teachingRaise,
                seed.rankAllowance(),
                seed.retainedSpecial(),
                seed.teachingAllowance(),
                wageBase,
                convertedWageBase,
                basicRetirementFee,
                before.retainedAllowance(),
                before.localAllowance(),
                before.postAllowance(),
                before.floatingSalary(),
                before.bonusBalance(),
                before.livingAllowance(),
                before.specialPostAllowance(),
                before.positionAllowance(),
                before.otherAllowance(),
                beforeAllowanceTotal,
                beforeTotal,
                after.retainedAllowance(),
                after.localAllowance(),
                after.postAllowance(),
                after.floatingSalary(),
                after.bonusBalance(),
                after.livingAllowance(),
                after.specialPostAllowance(),
                after.positionAllowance(),
                after.otherAllowance(),
                afterAllowanceTotal,
                afterTotal,
                wagesFromStandards,
                allowancesFromStandards,
                note);
    }

    private WageAmounts resolveWages(RetirementSeedRow seed, String standardYm) {
        String positionCode = blank(seed.positionCode());
        if (positionCode.isEmpty() || standardYm.isBlank()) {
            return new WageAmounts(
                    seed.positionSalary(), seed.gradeSalary(), seed.technicalSalary(), false);
        }
        int positionSalary = payrollRepository.positionSalary(positionCode, standardYm)
                + payrollRepository.positionGradeSalary(
                        positionCode, seed.gradeStep(), seed.gradeStepExtra(), standardYm);
        int gradeSalary;
        if (isAgencyCadre(seed)) {
            int combinedStep = parseInt(seed.gradeStep()) + parseInt(seed.gradeStepExtra());
            gradeSalary = payrollRepository.gradeSalary(
                    seed.gradeLevel(),
                    String.valueOf(Math.max(combinedStep, 1)),
                    standardYm);
        } else {
            gradeSalary = payrollRepository.salaryLevelSalary(
                    seed.gradeStep(), seed.gradeStepExtra(), standardYm, positionCode);
        }
        int technicalSalary = payrollRepository.technicalGradeSalary(positionCode, standardYm);
        boolean lookedUp = positionSalary > 0 || gradeSalary > 0 || technicalSalary > 0;
        if (!lookedUp) {
            return new WageAmounts(
                    seed.positionSalary(), seed.gradeSalary(), seed.technicalSalary(), false);
        }
        return new WageAmounts(
                positionSalary > 0 ? positionSalary : seed.positionSalary(),
                gradeSalary > 0 ? gradeSalary : seed.gradeSalary(),
                technicalSalary > 0 ? technicalSalary : seed.technicalSalary(),
                true);
    }

    private boolean isAgencyCadre(RetirementSeedRow seed) {
        String orgType = blank(seed.organizationType());
        String postCategory = blank(seed.postCategory());
        if ("机关技术工人".equals(postCategory) || "机关普通工人".equals(postCategory)) {
            return false;
        }
        if (orgType.contains("事业")) {
            return false;
        }
        return orgType.contains("行政") || "行政管理人员".equals(postCategory);
    }

    private AllowanceAmounts fromStandard(
            RetirementAllowanceStandardRow row,
            RetirementSeedRow seed,
            boolean before) {
        // Standards cover core jbt fields; keep floating/bonus from seed when missing in table.
        return new AllowanceAmounts(
                row.retainedAllowance(),
                row.localAllowance(),
                row.postAllowance(),
                before ? seed.floatingSalary() : seed.floatingSalary(),
                before ? seed.bonusBalance() : seed.bonusBalance(),
                row.livingAllowance(),
                row.specialPostAllowance(),
                row.positionAllowance(),
                row.otherAllowance());
    }

    private AllowanceAmounts fromSeed(RetirementSeedRow seed) {
        return new AllowanceAmounts(
                seed.retainedAllowance(),
                seed.localAllowance(),
                seed.postAllowance(),
                seed.floatingSalary(),
                seed.bonusBalance(),
                seed.livingAllowance(),
                seed.specialPostAllowance(),
                seed.positionAllowance(),
                seed.otherAllowance());
    }

    private String buildNote(
            boolean wagesFromStandards,
            boolean allowancesFromStandards,
            int conversionRatio,
            int feeIncreaseRatio,
            int effectiveRatio,
            String standardYm,
            String allowanceYm) {
        StringBuilder note = new StringBuilder("计发对标 ltxbl06：");
        note.append(wagesFromStandards ? "工资已按标准 " + standardYm + " 重算" : "工资沿用在职执行额");
        note.append("；");
        note.append(allowancesFromStandards
                ? "津补贴已按 " + allowanceYm + "（jbtbz06/zzjbtbz06）重算"
                : "津补贴暂沿用在职额（未命中 jbtbz06）");
        note.append("；zsbl06=").append(conversionRatio).append("%");
        if (feeIncreaseRatio > 0) {
            note.append(" + zjbl=").append(feeIncreaseRatio).append("%");
        }
        note.append(" → 有效 ").append(effectiveRatio).append("%");
        note.append("；jbldxf=折算基数+教护龄，hj2=jbldxf+津补贴合计。");
        return note.toString();
    }

    private String digits(String yearMonth, String fallback) {
        String normalized = RetirementMonthCalculator.normalizeYearMonth(yearMonth);
        if (normalized.length() >= 6) {
            return normalized.substring(0, 6);
        }
        return fallback;
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseInt(String value) {
        String digits = blank(value).replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private record WageAmounts(
            int positionSalary,
            int gradeSalary,
            int technicalSalary,
            boolean fromStandards) {
    }

    private record AllowanceAmounts(
            int retainedAllowance,
            int localAllowance,
            int postAllowance,
            int floatingSalary,
            int bonusBalance,
            int livingAllowance,
            int specialPostAllowance,
            int positionAllowance,
            int otherAllowance) {
        int total() {
            return retainedAllowance + localAllowance + postAllowance + floatingSalary + bonusBalance
                    + livingAllowance + specialPostAllowance + positionAllowance + otherAllowance;
        }
    }
}
