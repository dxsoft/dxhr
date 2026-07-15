package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.common.SensitiveData;
import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import com.dxsoft.rsgzgl.payroll.PayrollChangeComponentComparison;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
class PayrollChangeReportLayoutService {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    record ApprovalRow(String label, String beforeText, String afterText, String differenceText, boolean highlight) {
    }

    record ApprovalTotals(BigDecimal beforeAmount, BigDecimal afterAmount, BigDecimal difference) {
    }

    record ApprovalSheetModel(
            String reportTitle,
            String organizationCode,
            String personCode,
            String archiveNumber,
            String name,
            String gender,
            String birthDate,
            String education,
            String organizationName,
            String workStartDate,
            String workYears,
            String currentPositionName,
            String positionStartDate,
            String previousChangeText,
            String stepYear,
            String levelYear,
            String basisTitle,
            String executionPeriod,
            List<String> basisDetailLines,
            String executionYear,
            String executionMonth,
            boolean institution,
            String performanceRatio,
            List<ApprovalRow> rows,
            ApprovalTotals totals) {
    }

    record RegisterColumnLabels(String position, String level, String positionSalary, String gradeSalary) {
    }

    record RegisterPersonRow(
            String name,
            String personCode,
            String maskedIdCard,
            String beforeTotal,
            String afterTotal,
            String difference,
            String executePeriod,
            String beforePosition,
            String afterPosition,
            String beforeLevel,
            String afterLevel,
            String beforePositionSalary,
            String afterPositionSalary,
            String beforeGradeSalary,
            String afterGradeSalary,
            String beforeTechnicalSalary,
            String afterTechnicalSalary,
            String beforeBonus,
            String afterBonus,
            String beforeRetained,
            String afterRetained,
            String beforeRankAllowance,
            String afterRankAllowance,
            String beforeRetainedReformAllowance,
            String afterRetainedReformAllowance,
            String beforeWorkAllowance,
            String afterWorkAllowance,
            String beforePerformance,
            String afterPerformance,
            String beforePositionAllowance,
            String afterPositionAllowance,
            String beforeRetainedReformSalary,
            String afterRetainedReformSalary,
            String beforeOtherAllowance,
            String afterOtherAllowance,
            String beforeRuralTeacher,
            String afterRuralTeacher) {
    }

    record RegisterTotalsRow(
            int personCount,
            String beforeTotal,
            String afterTotal,
            String difference,
            String beforePositionSalary,
            String afterPositionSalary,
            String beforeGradeSalary,
            String afterGradeSalary,
            String beforeRetained,
            String afterRetained,
            String beforeRankAllowance,
            String afterRankAllowance,
            String beforeWorkAllowance,
            String afterWorkAllowance,
            String beforePerformance,
            String afterPerformance,
            String beforeRetainedReformSalary,
            String afterRetainedReformSalary,
            String beforeRuralTeacher,
            String afterRuralTeacher) {
    }

    record RegisterPageModel(
            String reportTitle,
            String organizationName,
            String organizationCode,
            int pageNumber,
            int pageCount,
            RegisterColumnLabels labels,
            List<RegisterPersonRow> people,
            RegisterTotalsRow totals) {
    }

    List<ApprovalSheetModel> buildApprovalSheets(
            List<PayrollChangeComparison> reports,
            String selectedTitle,
            Boolean institutionOverride) {
        List<ApprovalSheetModel> sheets = new ArrayList<>();
        for (PayrollChangeComparison report : reports) {
            boolean institution = resolveInstitution(report, selectedTitle, institutionOverride);
            String reportTitle = resolveApprovalTitle(report, selectedTitle);
            String period = safe(report.calculationPeriod());
            sheets.add(new ApprovalSheetModel(
                    reportTitle,
                    blank(report.organizationCode()),
                    blank(report.personCode()),
                    safe(report.archiveNumber()),
                    blank(report.name()),
                    blank(report.gender()),
                    blank(report.birthDate()),
                    blank(report.education()),
                    blankDash(report.organizationName(), report.organizationCode()),
                    blank(report.workStartDate()),
                    report.workYears() == null ? "-" : String.valueOf(report.workYears()),
                    blank(report.currentPositionName()),
                    blank(report.positionStartDate()),
                    previousChangeText(report),
                    displayApprovalYear(report.nextStepAssessmentYear(), period),
                    displayApprovalYear(report.nextLevelAssessmentYear(), ""),
                    approvalBasisTitle(report.changeType(), institution),
                    formatApprovalPeriod(period),
                    basisDetailLines(report),
                    period.length() >= 4 ? period.substring(0, 4) : "-",
                    period.length() >= 6 ? period.substring(4, 6) : "-",
                    institution,
                    safe(report.performanceRatio()),
                    approvalRows(report, institution),
                    approvalTotals(report.components())));
        }
        return sheets;
    }

    List<RegisterPageModel> buildRegisterPages(
            List<PayrollChangeComparison> reports,
            String selectedTitle,
            Boolean institutionOverride) {
        boolean institution = reports.stream()
                .anyMatch(report -> resolveInstitution(report, selectedTitle, institutionOverride));
        RegisterColumnLabels labels = registerColumnLabels(institution);
        String reportTitle = selectedTitle == null || selectedTitle.isBlank()
                ? "工资变动花名册"
                : selectedTitle.trim();
        List<List<PayrollChangeComparison>> pages = chunk(reports, 10);
        List<RegisterPageModel> models = new ArrayList<>();
        for (int index = 0; index < pages.size(); index++) {
            List<PayrollChangeComparison> pageReports = pages.get(index);
            PayrollChangeComparison first = pageReports.getFirst();
            models.add(new RegisterPageModel(
                    reportTitle,
                    blankDash(first.organizationName(), first.organizationCode()),
                    blank(first.organizationCode()),
                    index + 1,
                    pages.size(),
                    labels,
                    pageReports.stream()
                            .map(report -> registerPersonRow(report, institution))
                            .toList(),
                    registerTotals(pageReports)));
        }
        return models;
    }

    private RegisterPersonRow registerPersonRow(PayrollChangeComparison report, boolean institution) {
        List<PayrollChangeComponentComparison> components = report.components();
        ApprovalTotals totals = approvalTotals(components);
        return new RegisterPersonRow(
                blank(report.name()),
                blank(report.personCode()),
                SensitiveData.maskIdCard(report.idCard()),
                moneyOrDash(totals.beforeAmount()),
                moneyOrDash(totals.afterAmount()),
                money(totals.difference()),
                formatCompactPeriod(report.calculationPeriod()),
                blank(report.previousPositionName()),
                blank(report.currentPositionName()),
                registerLevelText(report, true, institution),
                registerLevelText(report, false, institution),
                moneyOrDash(amount(components, "ZWGZSE2", true)),
                moneyOrDash(amount(components, "ZWGZSE2", false)),
                moneyOrDash(amount(components, "JBGZSE2", true)),
                moneyOrDash(amount(components, "JBGZSE2", false)),
                moneyOrDash(amount(components, "JSDJGZ2", true)),
                moneyOrDash(amount(components, "JSDJGZ2", false)),
                moneyOrDash(amount(components, "JJJY2", true)),
                moneyOrDash(amount(components, "JJJY2", false)),
                moneyOrDash(amount(components, "BLFB2", true)),
                moneyOrDash(amount(components, "BLFB2", false)),
                moneyOrDash(amount(components, "JXJT", true)),
                moneyOrDash(amount(components, "JXJT", false)),
                moneyOrDash(amount(components, "TGBLBF", true)),
                moneyOrDash(amount(components, "TGBLBF", false)),
                moneyOrDash(amount(components, "SDBT", true)),
                moneyOrDash(amount(components, "SDBT", false)),
                moneyOrDash(amount(components, "DFBT2", true)),
                moneyOrDash(amount(components, "DFBT2", false)),
                moneyOrDash(amount(components, "GWJT2", true)),
                moneyOrDash(amount(components, "GWJT2", false)),
                moneyOrDash(amount(components, "PGBC", true)),
                moneyOrDash(amount(components, "PGBC", false)),
                moneyOrDash(amount(components, "QTBT", true)),
                moneyOrDash(amount(components, "QTBT", false)),
                moneyOrDash(amount(components, "NJBT", true)),
                moneyOrDash(amount(components, "NJBT", false)));
    }

    private RegisterTotalsRow registerTotals(List<PayrollChangeComparison> reports) {
        BigDecimal beforeTotal = BigDecimal.ZERO;
        BigDecimal afterTotal = BigDecimal.ZERO;
        BigDecimal difference = BigDecimal.ZERO;
        BigDecimal beforePositionSalary = BigDecimal.ZERO;
        BigDecimal afterPositionSalary = BigDecimal.ZERO;
        BigDecimal beforeGradeSalary = BigDecimal.ZERO;
        BigDecimal afterGradeSalary = BigDecimal.ZERO;
        BigDecimal beforeRetained = BigDecimal.ZERO;
        BigDecimal afterRetained = BigDecimal.ZERO;
        BigDecimal beforeRankAllowance = BigDecimal.ZERO;
        BigDecimal afterRankAllowance = BigDecimal.ZERO;
        BigDecimal beforeWorkAllowance = BigDecimal.ZERO;
        BigDecimal afterWorkAllowance = BigDecimal.ZERO;
        BigDecimal beforePerformance = BigDecimal.ZERO;
        BigDecimal afterPerformance = BigDecimal.ZERO;
        BigDecimal beforeRetainedReformSalary = BigDecimal.ZERO;
        BigDecimal afterRetainedReformSalary = BigDecimal.ZERO;
        BigDecimal beforeRuralTeacher = BigDecimal.ZERO;
        BigDecimal afterRuralTeacher = BigDecimal.ZERO;
        for (PayrollChangeComparison report : reports) {
            List<PayrollChangeComponentComparison> components = report.components();
            ApprovalTotals totals = approvalTotals(components);
            beforeTotal = beforeTotal.add(totals.beforeAmount());
            afterTotal = afterTotal.add(totals.afterAmount());
            difference = difference.add(totals.difference());
            beforePositionSalary = beforePositionSalary.add(amount(components, "ZWGZSE2", true));
            afterPositionSalary = afterPositionSalary.add(amount(components, "ZWGZSE2", false));
            beforeGradeSalary = beforeGradeSalary.add(amount(components, "JBGZSE2", true));
            afterGradeSalary = afterGradeSalary.add(amount(components, "JBGZSE2", false));
            beforeRetained = beforeRetained.add(amount(components, "BLFB2", true));
            afterRetained = afterRetained.add(amount(components, "BLFB2", false));
            beforeRankAllowance = beforeRankAllowance.add(amount(components, "JXJT", true));
            afterRankAllowance = afterRankAllowance.add(amount(components, "JXJT", false));
            beforeWorkAllowance = beforeWorkAllowance.add(amount(components, "SDBT", true));
            afterWorkAllowance = afterWorkAllowance.add(amount(components, "SDBT", false));
            beforePerformance = beforePerformance.add(amount(components, "DFBT2", true));
            afterPerformance = afterPerformance.add(amount(components, "DFBT2", false));
            beforeRetainedReformSalary = beforeRetainedReformSalary.add(amount(components, "PGBC", true));
            afterRetainedReformSalary = afterRetainedReformSalary.add(amount(components, "PGBC", false));
            beforeRuralTeacher = beforeRuralTeacher.add(amount(components, "NJBT", true));
            afterRuralTeacher = afterRuralTeacher.add(amount(components, "NJBT", false));
        }
        return new RegisterTotalsRow(
                reports.size(),
                money(beforeTotal),
                money(afterTotal),
                money(difference),
                money(beforePositionSalary),
                money(afterPositionSalary),
                money(beforeGradeSalary),
                money(afterGradeSalary),
                money(beforeRetained),
                money(afterRetained),
                money(beforeRankAllowance),
                money(afterRankAllowance),
                money(beforeWorkAllowance),
                money(afterWorkAllowance),
                money(beforePerformance),
                money(afterPerformance),
                money(beforeRetainedReformSalary),
                money(afterRetainedReformSalary),
                money(beforeRuralTeacher),
                money(afterRuralTeacher));
    }

    private List<ApprovalRow> approvalRows(PayrollChangeComparison report, boolean institution) {
        return institution ? institutionApprovalRows(report) : agencyApprovalRows(report);
    }

    private List<ApprovalRow> agencyApprovalRows(PayrollChangeComparison report) {
        List<PayrollChangeComponentComparison> components = report.components();
        List<ApprovalRow> rows = new ArrayList<>();
        rows.add(textApprovalRow(
                "执行工资职务层次",
                report.previousPositionName(),
                report.currentPositionName()));
        rows.add(textApprovalRow(
                "级别档次",
                gradeStepText(report.previousGradeLevel(), report.previousStepOrSalaryLevel()),
                gradeStepText(report.currentGradeLevel(), report.currentStepOrSalaryLevel())));
        rows.add(textApprovalRow("警衔、法检、监察等级", null, null));
        rows.add(amountApprovalRow("职务(岗位)工资", component(components, "ZWGZSE2")));
        rows.add(amountApprovalRow("级别工资", component(components, "JBGZSE2")));
        rows.add(amountApprovalRow("技术等级工资", component(components, "JSDJGZ2")));
        rows.add(amountApprovalRow("保留副补", component(components, "BLFB2")));
        rows.add(amountApprovalRow("保留奖金", null));
        rows.add(amountApprovalRow("岗位津贴", component(components, "GWJT2")));
        rows.add(amountApprovalRow("生活性补贴", component(components, "DFBT2")));
        rows.add(amountApprovalRow("工作性津贴", component(components, "SDBT")));
        rows.add(amountApprovalRow("警衔\\检察津贴", component(components, "JXJT")));
        rows.add(amountApprovalRow("特殊岗位津贴", component(components, "SIDBT")));
        rows.add(amountApprovalRow("工改保留津贴", component(components, "TGBLBF")));
        rows.add(amountApprovalRow("工改保留工资", component(components, "PGBC")));
        rows.add(amountApprovalRow("其它补贴", component(components, "QTBT")));
        return rows;
    }

    private List<ApprovalRow> institutionApprovalRows(PayrollChangeComparison report) {
        List<PayrollChangeComponentComparison> components = report.components();
        List<ApprovalRow> rows = new ArrayList<>();
        rows.add(textApprovalRow(
                "执行工资岗位等级",
                report.previousPositionName(),
                report.currentPositionName()));
        rows.add(textApprovalRow(
                "薪级",
                formatSalaryLevel(report.previousStepOrSalaryLevel()),
                formatSalaryLevel(report.currentStepOrSalaryLevel())));
        rows.add(amountApprovalRow("岗位工资", component(components, "ZWGZSE2")));
        rows.add(amountApprovalRow("薪级工资", component(components, "JBGZSE2")));
        rows.add(amountApprovalRow("教护提高部分", component(components, "JSFSZWTG2")));
        rows.add(amountApprovalRow("教护龄津贴", component(components, "JHLJT")));
        rows.add(amountApprovalRow("保留高补", component(components, "BLFB2")));
        rows.add(amountApprovalRow("保留奖金", component(components, "JJJY2")));
        rows.add(amountApprovalRow("工改保留津贴", component(components, "TGBLBF")));
        rows.add(amountApprovalRow("基础绩效", component(components, "DFBT2")));
        rows.add(amountApprovalRow("浮动工资", component(components, "FDGZ2")));
        rows.add(amountApprovalRow("监察津贴", component(components, "JXJT")));
        rows.add(amountApprovalRow("特殊岗位津贴", component(components, "SIDBT")));
        rows.add(amountApprovalRow("挂岗保留部分", null));
        rows.add(amountApprovalRow("农村学校教师补贴", component(components, "NJBT")));
        rows.add(amountApprovalRow("其它补贴", component(components, "QTBT")));
        return rows;
    }

    private ApprovalRow textApprovalRow(String label, String beforeValue, String afterValue) {
        String beforeText = blankDash(beforeValue, null);
        String afterText = blankDash(afterValue, null);
        boolean same = Objects.equals(beforeText, afterText);
        return new ApprovalRow(label, beforeText, afterText, same ? "——" : "", !same);
    }

    private ApprovalRow amountApprovalRow(String label, PayrollChangeComponentComparison component) {
        if (component == null) {
            return new ApprovalRow(label, "——", "——", "——", false);
        }
        BigDecimal difference = component.difference() == null ? BigDecimal.ZERO : component.difference();
        boolean zero = difference.compareTo(BigDecimal.ZERO) == 0;
        return new ApprovalRow(
                label,
                moneyOrDash(component.beforeAmount()),
                moneyOrDash(component.afterAmount()),
                zero ? "——" : money(difference),
                !zero);
    }

    private ApprovalTotals approvalTotals(List<PayrollChangeComponentComparison> components) {
        PayrollChangeComponentComparison total = component(components, "HJ2");
        if (total != null) {
            return new ApprovalTotals(
                    zeroIfNull(total.beforeAmount()),
                    zeroIfNull(total.afterAmount()),
                    zeroIfNull(total.difference()));
        }
        BigDecimal beforeAmount = BigDecimal.ZERO;
        BigDecimal afterAmount = BigDecimal.ZERO;
        BigDecimal difference = BigDecimal.ZERO;
        for (PayrollChangeComponentComparison component : components) {
            if ("HJ2".equalsIgnoreCase(safe(component.fieldName()))) {
                continue;
            }
            beforeAmount = beforeAmount.add(zeroIfNull(component.beforeAmount()));
            afterAmount = afterAmount.add(zeroIfNull(component.afterAmount()));
            difference = difference.add(zeroIfNull(component.difference()));
        }
        return new ApprovalTotals(beforeAmount, afterAmount, difference);
    }

    private boolean resolveInstitution(
            PayrollChangeComparison report,
            String selectedTitle,
            Boolean institutionOverride) {
        if (institutionOverride != null) {
            return institutionOverride;
        }
        String nature = safe(report.organizationNature());
        String title = safe(selectedTitle);
        String position = safe(report.currentPositionName()) + safe(report.previousPositionName());
        return nature.contains("事")
                || title.contains("事业")
                || title.contains("薪级")
                || position.contains("专业技术岗位")
                || position.contains("管理岗位")
                || position.contains("工勤")
                || position.contains("职员");
    }

    private String resolveApprovalTitle(PayrollChangeComparison report, String selectedTitle) {
        String title = selectedTitle == null ? "" : selectedTitle.trim();
        boolean institution = resolveInstitution(report, selectedTitle, null);
        if (institution) {
            return "河南省事业单位工作人员正常晋升薪级工资审批表";
        }
        if (title.isEmpty()) {
            title = "河南省机关工作人员" + safe(report.changeType()) + "工资变动审批表";
        }
        if (!title.contains("审批表")) {
            if (title.endsWith("工资变动")) {
                title = title.substring(0, title.length() - "工资变动".length()) + "工资变动审批表";
            } else {
                title += "审批表";
            }
        }
        return title;
    }

    private String previousChangeText(PayrollChangeComparison report) {
        if (report.previousPayrollHistoryId() == null || report.previousPayrollHistoryId().isBlank()) {
            return "无";
        }
        return (formatCompactPeriod(report.previousCalculationPeriod()) + " " + safe(report.previousChangeType())).trim();
    }

    private List<String> basisDetailLines(PayrollChangeComparison report) {
        List<String> lines = new ArrayList<>();
        String currentYear = report.calculationPeriod() != null && report.calculationPeriod().length() >= 4
                ? report.calculationPeriod().substring(0, 4)
                : "";
        String previousYear = report.previousCalculationPeriod() != null && report.previousCalculationPeriod().length() >= 4
                ? report.previousCalculationPeriod().substring(0, 4)
                : "";
        if (!currentYear.isEmpty()) {
            lines.add(currentYear + "年：" + safe(report.changeType()));
        }
        if (!previousYear.isEmpty()) {
            lines.add(previousYear + "年：" + safe(report.previousChangeType()));
        }
        return lines;
    }

    private String approvalBasisTitle(String changeType, boolean institution) {
        String type = safe(changeType);
        if (institution) {
            return "正常增加薪级工资";
        }
        if (type.contains("档")) {
            return "按年度考核结果晋升级别工资档次";
        }
        if (type.contains("级")) {
            return "按年度考核结果晋升级别工资";
        }
        return type.isEmpty() ? "工资变动" : type;
    }

    private RegisterColumnLabels registerColumnLabels(boolean institution) {
        if (institution) {
            return new RegisterColumnLabels("聘任岗位", "薪级", "岗位工资", "薪级工资");
        }
        return new RegisterColumnLabels("职务岗位\n(或技术等级)", "级别", "职务工资", "级别工资");
    }

    private String registerLevelText(PayrollChangeComparison report, boolean previous, boolean institution) {
        if (institution) {
            String step = previous ? report.previousStepOrSalaryLevel() : report.currentStepOrSalaryLevel();
            String grade = previous ? report.previousGradeLevel() : report.currentGradeLevel();
            return blankDash(step, grade);
        }
        return gradeStepText(
                previous ? report.previousGradeLevel() : report.currentGradeLevel(),
                previous ? report.previousStepOrSalaryLevel() : report.currentStepOrSalaryLevel());
    }

    private String formatSalaryLevel(String value) {
        String text = safe(value);
        if (text.isEmpty() || "-".equals(text)) {
            return text.isEmpty() ? "-" : text;
        }
        return text.endsWith("级") ? text : text + "级";
    }

    private String gradeStepText(String grade, String step) {
        String normalizedGrade = safe(grade);
        String normalizedStep = safe(step);
        if (!normalizedGrade.isEmpty() && !normalizedStep.isEmpty()) {
            return normalizedGrade + "-" + normalizedStep;
        }
        if (!normalizedGrade.isEmpty()) {
            return normalizedGrade;
        }
        return normalizedStep.isEmpty() ? "-" : normalizedStep;
    }

    private String displayApprovalYear(String value, String periodFallback) {
        String text = safe(value);
        if (text.isEmpty() && periodFallback != null && periodFallback.length() >= 4) {
            return periodFallback.substring(0, 4);
        }
        if (text.length() >= 4) {
            return text.substring(0, 4);
        }
        return text.isEmpty() ? "-" : text;
    }

    private String formatApprovalPeriod(String period) {
        if (period == null || period.length() < 6) {
            return "-";
        }
        return period.substring(0, 4) + "年" + period.substring(4, 6) + "月";
    }

    private String formatCompactPeriod(String period) {
        if (period == null || period.length() < 6) {
            return "";
        }
        return period.substring(0, 4) + "." + period.substring(4, 6);
    }

    private PayrollChangeComponentComparison component(
            List<PayrollChangeComponentComparison> components,
            String fieldName) {
        if (components == null) {
            return null;
        }
        return components.stream()
                .filter(component -> fieldName.equalsIgnoreCase(safe(component.fieldName())))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal amount(List<PayrollChangeComponentComparison> components, String fieldName, boolean before) {
        PayrollChangeComponentComparison component = component(components, fieldName);
        if (component == null) {
            return BigDecimal.ZERO;
        }
        return zeroIfNull(before ? component.beforeAmount() : component.afterAmount());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String money(BigDecimal value) {
        return MONEY.format(zeroIfNull(value));
    }

    private String moneyOrDash(BigDecimal value) {
        if (value == null) {
            return "——";
        }
        return money(value);
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankDash(String primary, String fallback) {
        String text = blank(primary);
        if (!text.isEmpty()) {
            return text;
        }
        text = blank(fallback);
        return text.isEmpty() ? "-" : text;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private <T> List<List<T>> chunk(List<T> items, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int index = 0; index < items.size(); index += size) {
            chunks.add(items.subList(index, Math.min(index + size, items.size())));
        }
        if (chunks.isEmpty()) {
            chunks.add(List.of());
        }
        return chunks;
    }
}
