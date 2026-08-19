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
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class PayrollChangeReportLayoutService {

    private static final DecimalFormat MONEY = new DecimalFormat("##0");
    private static final Set<String> GOVERNMENT_WORKER_POSITION_PREFIXES = Set.of("05", "06");

    private final String agencyGradeStepTitle;
    private final String agencyGradeLevelTitle;
    private final String institutionSalaryLevelTitle;
    private final String agencyInternTitle;
    private final String institutionInternTitle;
    private final String institutionRegularizationTitle;

    PayrollChangeReportLayoutService(
            @Value("${rsgzgl.report.approval-title.agency-grade-step:河南省机关工作人员正常档次晋升工资变动审批表}")
            String agencyGradeStepTitle,
            @Value("${rsgzgl.report.approval-title.agency-grade-level:河南省机关工作人员正常级别晋升工资变动审批表}")
            String agencyGradeLevelTitle,
            @Value("${rsgzgl.report.approval-title.institution-salary-level:河南省事业单位工作人员正常晋升薪级工资审批表}")
            String institutionSalaryLevelTitle,
            @Value("${rsgzgl.report.approval-title.agency-intern:河南省机关见习人员见习期工资审批表}")
            String agencyInternTitle,
            @Value("${rsgzgl.report.approval-title.institution-intern:河南省事业单位见习人员见习期工资审批表}")
            String institutionInternTitle,
            @Value("${rsgzgl.report.approval-title.institution-regularization:河南省事业单位转正人员确定工资审批表}")
            String institutionRegularizationTitle) {
        this.agencyGradeStepTitle = agencyGradeStepTitle;
        this.agencyGradeLevelTitle = agencyGradeLevelTitle;
        this.institutionSalaryLevelTitle = institutionSalaryLevelTitle;
        this.agencyInternTitle = agencyInternTitle;
        this.institutionInternTitle = institutionInternTitle;
        this.institutionRegularizationTitle = institutionRegularizationTitle;
    }

    record ApprovalRow(
            String label,
            String beforeText,
            String afterText,
            String differenceText,
            boolean highlight,
            String groupLabel) {
        ApprovalRow(String label, String beforeText, String afterText, String differenceText, boolean highlight) {
            this(label, beforeText, afterText, differenceText, highlight, null);
        }
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
            String degree,
            String school,
            String studyYears,
            String graduationDate,
            String educationCategory,
            String organizationName,
            String workStartDate,
            String workYears,
            String currentPositionName,
            String positionStartDate,
            String legalPositionName,
            String legalPositionStartDate,
            String probationFrom,
            String probationTo,
            String apprenticePeriod,
            String previousChangeText,
            String stepYear,
            String levelYear,
            String basisTitle,
            String executionPeriod,
            List<String> basisDetailLines,
            String executionYear,
            String executionMonth,
            boolean institution,
            boolean internForm,
            boolean regularizationForm,
            boolean judicialForm,
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
            String beforeStep,
            String afterStep,
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
            String afterRuralTeacher,
            String beforeSpecialPostAllowance,
            String afterSpecialPostAllowance) {
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
            String afterRuralTeacher,
            String beforeRetainedReformAllowance,
            String afterRetainedReformAllowance,
            String beforeSpecialPostAllowance,
            String afterSpecialPostAllowance,
            String beforeTechnicalSalary,
            String afterTechnicalSalary,
            String beforeBonus,
            String afterBonus) {
    }

    record RegisterPageModel(
            String reportTitle,
            String organizationName,
            String organizationCode,
            int pageNumber,
            int pageCount,
            boolean judicialForm,
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
            boolean regularizationForm = institution && isRegularizationChange(report.changeType(), selectedTitle);
            boolean internForm = !regularizationForm && isInternSalaryChange(report.changeType(), selectedTitle);
            boolean judicialForm = !internForm && !regularizationForm && this.usesJudicialApprovalForm(report);
            String reportTitle = resolveApprovalTitle(
                    report, selectedTitle, institution, internForm, regularizationForm, judicialForm);
            String period = safe(report.calculationPeriod());
            // 事业见习旧表：下次晋档/级别考核年度留空手填，不回填变动年
            String stepYear = (internForm && institution)
                    ? ""
                    : displayApprovalYear(report.nextStepAssessmentYear(),
                            internForm ? "" : period);
            String levelYear = (internForm && institution)
                    ? ""
                    : displayApprovalYear(report.nextLevelAssessmentYear(), "");
            sheets.add(new ApprovalSheetModel(
                    reportTitle,
                    blank(report.organizationCode()),
                    blank(report.personCode()),
                    safe(report.archiveNumber()),
                    blank(report.name()),
                    blank(report.gender()),
                    blank(report.birthDate()),
                    blank(report.education()),
                    blank(report.degree()),
                    blank(report.school()),
                    blank(report.studyYears()),
                    blank(report.graduationDate()),
                    blank(report.educationCategory()),
                    blankDash(report.organizationName(), report.organizationCode()),
                    blank(report.workStartDate()),
                    report.workYears() == null ? "-" : String.valueOf(report.workYears()),
                    blank(report.currentPositionName()),
                    blank(report.positionStartDate()),
                    "",
                    "",
                    blank(report.workStartDate()),
                    blank(report.probationEndDate()),
                    "",
                    previousChangeText(report),
                    stepYear,
                    levelYear,
                    approvalBasisTitle(report.changeType(), institution, internForm, regularizationForm),
                    formatApprovalPeriod(period),
                    basisDetailLines(report),
                    period.length() >= 4 ? period.substring(0, 4) : "-",
                    period.length() >= 6 ? period.substring(4, 6) : "-",
                    institution,
                    internForm,
                    regularizationForm,
                    judicialForm,
                    safe(report.performanceRatio()),
                    approvalRows(report, institution, internForm, regularizationForm, judicialForm),
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
        boolean judicialForm = reports.stream().anyMatch(this::isCourtOrProcuratorateOrganization);
        RegisterColumnLabels labels = registerColumnLabels(institution);
        String reportTitle = resolveRegisterTitle(selectedTitle, judicialForm);
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
                    judicialForm,
                    labels,
                    pageReports.stream()
                            .map(report -> registerPersonRow(report, institution, judicialForm))
                            .toList(),
                    registerTotals(pageReports, judicialForm)));
        }
        return models;
    }

    private RegisterPersonRow registerPersonRow(
            PayrollChangeComparison report, boolean institution, boolean judicialForm) {
        List<PayrollChangeComponentComparison> components = report.components();
        ApprovalTotals totals = approvalTotals(components);
        String beforeLevel = judicialForm
                ? blankDash(report.previousGradeLevel(), "")
                : registerLevelText(report, true, institution);
        String afterLevel = judicialForm
                ? blankDash(report.currentGradeLevel(), "")
                : registerLevelText(report, false, institution);
        String beforeStep = judicialForm ? blankDash(report.previousStepOrSalaryLevel(), "") : "";
        String afterStep = judicialForm ? blankDash(report.currentStepOrSalaryLevel(), "") : "";
        BigDecimal beforeReformRetention = judicialForm
                ? amount(components, "JZMCBT", true).add(amount(components, "NZGWSF", true))
                : amount(components, "TGBLBF", true);
        BigDecimal afterReformRetention = judicialForm
                ? amount(components, "JZMCBT", false).add(amount(components, "NZGWSF", false))
                : amount(components, "TGBLBF", false);
        return new RegisterPersonRow(
                blank(report.name()),
                blank(report.personCode()),
                SensitiveData.maskIdCard(report.idCard()),
                moneyOrDash(totals.beforeAmount()),
                moneyOrDash(totals.afterAmount()),
                moneyOrDash(totals.difference()),
                formatCompactPeriod(report.calculationPeriod()),
                blank(report.previousPositionName()),
                blank(report.currentPositionName()),
                beforeLevel,
                afterLevel,
                beforeStep,
                afterStep,
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
                moneyOrDash(beforeReformRetention),
                moneyOrDash(afterReformRetention),
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
                moneyOrDash(amount(components, "NJBT", false)),
                moneyOrDash(amount(components, "SIDBT", true)),
                moneyOrDash(amount(components, "SIDBT", false)));
    }

    private RegisterTotalsRow registerTotals(List<PayrollChangeComparison> reports, boolean judicialForm) {
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
        BigDecimal beforeRetainedReformAllowance = BigDecimal.ZERO;
        BigDecimal afterRetainedReformAllowance = BigDecimal.ZERO;
        BigDecimal beforeSpecialPostAllowance = BigDecimal.ZERO;
        BigDecimal afterSpecialPostAllowance = BigDecimal.ZERO;
        BigDecimal beforeTechnicalSalary = BigDecimal.ZERO;
        BigDecimal afterTechnicalSalary = BigDecimal.ZERO;
        BigDecimal beforeBonus = BigDecimal.ZERO;
        BigDecimal afterBonus = BigDecimal.ZERO;
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
            beforeTechnicalSalary = beforeTechnicalSalary.add(amount(components, "JSDJGZ2", true));
            afterTechnicalSalary = afterTechnicalSalary.add(amount(components, "JSDJGZ2", false));
            beforeBonus = beforeBonus.add(amount(components, "JJJY2", true));
            afterBonus = afterBonus.add(amount(components, "JJJY2", false));
            beforeSpecialPostAllowance = beforeSpecialPostAllowance.add(amount(components, "SIDBT", true));
            afterSpecialPostAllowance = afterSpecialPostAllowance.add(amount(components, "SIDBT", false));
            if (judicialForm) {
                beforeRetainedReformAllowance = beforeRetainedReformAllowance
                        .add(amount(components, "JZMCBT", true))
                        .add(amount(components, "NZGWSF", true));
                afterRetainedReformAllowance = afterRetainedReformAllowance
                        .add(amount(components, "JZMCBT", false))
                        .add(amount(components, "NZGWSF", false));
            } else {
                beforeRetainedReformAllowance = beforeRetainedReformAllowance.add(amount(components, "TGBLBF", true));
                afterRetainedReformAllowance = afterRetainedReformAllowance.add(amount(components, "TGBLBF", false));
            }
        }
        return new RegisterTotalsRow(
                reports.size(),
                moneyOrDash(beforeTotal),
                moneyOrDash(afterTotal),
                moneyOrDash(difference),
                moneyOrDash(beforePositionSalary),
                moneyOrDash(afterPositionSalary),
                moneyOrDash(beforeGradeSalary),
                moneyOrDash(afterGradeSalary),
                moneyOrDash(beforeRetained),
                moneyOrDash(afterRetained),
                moneyOrDash(beforeRankAllowance),
                moneyOrDash(afterRankAllowance),
                moneyOrDash(beforeWorkAllowance),
                moneyOrDash(afterWorkAllowance),
                moneyOrDash(beforePerformance),
                moneyOrDash(afterPerformance),
                moneyOrDash(beforeRetainedReformSalary),
                moneyOrDash(afterRetainedReformSalary),
                moneyOrDash(beforeRuralTeacher),
                moneyOrDash(afterRuralTeacher),
                moneyOrDash(beforeRetainedReformAllowance),
                moneyOrDash(afterRetainedReformAllowance),
                moneyOrDash(beforeSpecialPostAllowance),
                moneyOrDash(afterSpecialPostAllowance),
                moneyOrDash(beforeTechnicalSalary),
                moneyOrDash(afterTechnicalSalary),
                moneyOrDash(beforeBonus),
                moneyOrDash(afterBonus));
    }

    private List<ApprovalRow> approvalRows(
            PayrollChangeComparison report,
            boolean institution,
            boolean internForm,
            boolean regularizationForm,
            boolean judicialForm) {
        if (regularizationForm) {
            return institutionRegularizationApprovalRows(report);
        }
        if (internForm) {
            return institution ? institutionInternApprovalRows(report) : agencyInternApprovalRows(report);
        }
        if (judicialForm) {
            return judicialApprovalRows(report);
        }
        return institution ? institutionApprovalRows(report) : agencyApprovalRows(report);
    }

    private List<ApprovalRow> judicialApprovalRows(PayrollChangeComparison report) {
        List<PayrollChangeComponentComparison> components = report.components();
        String group = "基本工资";
        List<ApprovalRow> rows = new ArrayList<>();
        rows.add(groupedTextApprovalRow(
                "执行工资职务层次",
                report.previousPositionName(),
                report.currentPositionName(),
                group));
        rows.add(groupedTextApprovalRow(
                "级别",
                formatJudicialGrade(report.previousGradeLevel()),
                formatJudicialGrade(report.currentGradeLevel()),
                group));
        rows.add(groupedTextApprovalRow(
                "档次",
                formatJudicialStep(report.previousStepOrSalaryLevel()),
                formatJudicialStep(report.currentStepOrSalaryLevel()),
                group));
        rows.add(groupedAmountApprovalRow("职务/职务等级工资", component(components, "ZWGZSE2"), group));
        rows.add(groupedAmountApprovalRow("级别工资", component(components, "JBGZSE2"), group));
        rows.add(amountApprovalRow("保留职务工资", component(components, "PGBC")));
        rows.add(amountApprovalRow("技术等级工资", component(components, "JSDJGZ2")));
        rows.add(amountApprovalRow("保留福补", component(components, "BLFB2")));
        rows.add(amountApprovalRow("保留奖金", component(components, "JJJY2")));
        rows.add(amountApprovalRow("生活性补贴", component(components, "DFBT2")));
        rows.add(amountApprovalRow("工作性津贴", component(components, "SDBT")));
        rows.add(amountApprovalRow("特殊岗位津贴", component(components, "SIDBT")));
        rows.add(amountApprovalRow("警衔津贴", component(components, "JXJT")));
        rows.add(sumAmountApprovalRow("工改保留津贴", components, "JZMCBT", "NZGWSF"));
        return rows;
    }

    private List<ApprovalRow> agencyInternApprovalRows(PayrollChangeComparison report) {
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
        rows.add(internSalaryAmountRow("试用期工资(职务工资)", components));
        rows.add(amountApprovalRow("级别工资", component(components, "JBGZSE2")));
        rows.add(amountApprovalRow("技术等级工资", component(components, "JSDJGZ2")));
        rows.add(amountApprovalRow("保留副补", component(components, "BLFB2")));
        rows.add(amountApprovalRow("保留奖金", component(components, "JJJY2")));
        rows.add(amountApprovalRow("工改保留津贴", component(components, "TGBLBF")));
        rows.add(amountApprovalRow("生活性补贴", component(components, "DFBT2")));
        rows.add(amountApprovalRow("警衔津贴", component(components, "JXJT")));
        rows.add(amountApprovalRow("特殊岗位津贴", component(components, "SIDBT")));
        rows.add(amountApprovalRow("其它补贴", component(components, "QTBT")));
        rows.add(amountApprovalRow("特岗保留部分", component(components, "PGBC")));
        rows.add(amountApprovalRow("教护龄津贴", component(components, "JHLJT")));
        rows.add(amountApprovalRow("工作性津贴", component(components, "SDBT")));
        return rows;
    }

    private List<ApprovalRow> institutionRegularizationApprovalRows(PayrollChangeComparison report) {
        List<PayrollChangeComponentComparison> components = report.components();
        List<ApprovalRow> rows = new ArrayList<>();
        rows.add(textApprovalRow(
                "执行工资岗位等级",
                null,
                report.currentPositionName()));
        rows.add(textApprovalRow(
                "薪级",
                null,
                formatSalaryLevel(report.currentStepOrSalaryLevel())));
        rows.add(internAmountApprovalRow("岗位工资", component(components, "ZWGZSE2")));
        rows.add(internAmountApprovalRow("薪级工资", component(components, "JBGZSE2")));
        rows.add(internAmountApprovalRow("教护提高部分", component(components, "JSFSZWTG2")));
        rows.add(internAmountApprovalRow("教护龄津贴", component(components, "JHLJT")));
        rows.add(internAmountApprovalRow("保留副补", component(components, "BLFB2")));
        rows.add(internAmountApprovalRow("保留奖金", component(components, "JJJY2")));
        rows.add(internAmountApprovalRow("工改保留津贴", component(components, "TGBLBF")));
        rows.add(internAmountApprovalRow("基础绩效", component(components, "DFBT2")));
        rows.add(internAmountApprovalRow("浮动工资", component(components, "FDGZ2")));
        rows.add(internAmountApprovalRow("特殊岗位津贴", component(components, "SIDBT")));
        rows.add(internAmountApprovalRow("特岗保留部分", component(components, "PGBC")));
        rows.add(internAmountApprovalRow("农村学校教师补贴", component(components, "NJBT")));
        rows.add(internAmountApprovalRow("其它补贴", component(components, "QTBT")));
        return rows;
    }

    private List<ApprovalRow> institutionInternApprovalRows(PayrollChangeComparison report) {
        List<PayrollChangeComponentComparison> components = report.components();
        List<ApprovalRow> rows = new ArrayList<>();
        rows.add(textApprovalRow(
                "执行工资岗位等级",
                null,
                report.currentPositionName()));
        rows.add(internSalaryAmountRow("见习工资", components));
        rows.add(internAmountApprovalRow("薪级工资", component(components, "JBGZSE2")));
        rows.add(internAmountApprovalRow("教护提高部分", component(components, "JSFSZWTG2")));
        rows.add(internAmountApprovalRow("教护龄津贴", component(components, "JHLJT")));
        rows.add(internAmountApprovalRow("保留副补", component(components, "BLFB2")));
        rows.add(internAmountApprovalRow("保留奖金", component(components, "JJJY2")));
        rows.add(internAmountApprovalRow("工改保留津贴", component(components, "TGBLBF")));
        rows.add(internAmountApprovalRow("基础绩效", component(components, "DFBT2")));
        rows.add(internAmountApprovalRow("警衔津贴", component(components, "JXJT")));
        rows.add(internAmountApprovalRow("特殊岗位津贴", component(components, "SIDBT")));
        rows.add(internAmountApprovalRow("特岗保留部分", component(components, "PGBC")));
        rows.add(internAmountApprovalRow("农村学校教师补贴", component(components, "NJBT")));
        rows.add(internAmountApprovalRow("其它补贴", component(components, "QTBT")));
        return rows;
    }

    private ApprovalRow internSalaryAmountRow(String label, List<PayrollChangeComponentComparison> components) {
        PayrollChangeComponentComparison intern = component(components, "JXGZ");
        if (intern != null && (
                (intern.afterAmount() != null && intern.afterAmount().compareTo(BigDecimal.ZERO) > 0)
                        || (intern.beforeAmount() != null && intern.beforeAmount().compareTo(BigDecimal.ZERO) > 0))) {
            return internAmountApprovalRow(label, intern);
        }
        return internAmountApprovalRow(label, component(components, "ZWGZSE2"));
    }

    private ApprovalRow internAmountApprovalRow(String label, PayrollChangeComponentComparison component) {
        if (component == null) {
            return new ApprovalRow(label, "——", "——", "——", false);
        }
        return new ApprovalRow(label, "——", internMoneyOrDash(component.afterAmount()), "——", false);
    }

    private String internMoneyOrDash(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "——";
        }
        return new DecimalFormat("#0").format(value);
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
        return groupedTextApprovalRow(label, beforeValue, afterValue, null);
    }

    private ApprovalRow groupedTextApprovalRow(
            String label, String beforeValue, String afterValue, String groupLabel) {
        String beforeText = blankDash(beforeValue, null);
        String afterText = blankDash(afterValue, null);
        boolean same = Objects.equals(beforeText, afterText);
        return new ApprovalRow(label, beforeText, afterText, same ? "——" : "", !same, groupLabel);
    }

    private ApprovalRow amountApprovalRow(String label, PayrollChangeComponentComparison component) {
        return groupedAmountApprovalRow(label, component, null);
    }

    private ApprovalRow groupedAmountApprovalRow(
            String label, PayrollChangeComponentComparison component, String groupLabel) {
        if (component == null) {
            return new ApprovalRow(label, "——", "——", "——", false, groupLabel);
        }
        BigDecimal difference = component.difference() == null ? BigDecimal.ZERO : component.difference();
        boolean zero = difference.compareTo(BigDecimal.ZERO) == 0;
        return new ApprovalRow(
                label,
                moneyOrDash(component.beforeAmount()),
                moneyOrDash(component.afterAmount()),
                zero ? "——" : money(difference),
                !zero,
                groupLabel);
    }

    private ApprovalRow sumAmountApprovalRow(
            String label, List<PayrollChangeComponentComparison> components, String... fieldNames) {
        BigDecimal before = BigDecimal.ZERO;
        BigDecimal after = BigDecimal.ZERO;
        for (String fieldName : fieldNames) {
            before = before.add(amount(components, fieldName, true));
            after = after.add(amount(components, fieldName, false));
        }
        BigDecimal difference = after.subtract(before);
        boolean zero = difference.compareTo(BigDecimal.ZERO) == 0;
        return new ApprovalRow(
                label,
                moneyOrDash(before),
                moneyOrDash(after),
                zero ? "——" : money(difference),
                !zero,
                null);
    }

    private String formatJudicialGrade(String grade) {
        String text = safe(grade);
        if (text.isEmpty() || "-".equals(text)) {
            return text.isEmpty() ? "-" : text;
        }
        if (text.matches(".*[一二三四五六七八九十百].*")) {
            return text;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return text;
        }
        try {
            return toChineseNumber(Integer.parseInt(digits));
        } catch (NumberFormatException ignored) {
            return text;
        }
    }

    private String formatJudicialStep(String step) {
        String text = safe(step);
        if (text.isEmpty() || "-".equals(text)) {
            return text.isEmpty() ? "-" : text;
        }
        String core = text.endsWith("档") ? text.substring(0, text.length() - 1).trim() : text;
        return core.isEmpty() ? "-" : core + "档";
    }

    private String toChineseNumber(int value) {
        if (value <= 0) {
            return String.valueOf(value);
        }
        String[] digits = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        if (value < 10) {
            return digits[value];
        }
        if (value == 10) {
            return "十";
        }
        if (value < 20) {
            return "十" + digits[value % 10];
        }
        if (value < 100) {
            int tens = value / 10;
            int ones = value % 10;
            return digits[tens] + "十" + (ones == 0 ? "" : digits[ones]);
        }
        return String.valueOf(value);
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

    private String resolveApprovalTitle(
            PayrollChangeComparison report,
            String selectedTitle,
            boolean institution,
            boolean internForm,
            boolean regularizationForm,
            boolean judicialForm) {
        if (regularizationForm && institution) {
            return institutionRegularizationTitle;
        }
        if (internForm) {
            return institution ? institutionInternTitle : agencyInternTitle;
        }
        if (judicialForm) {
            return "河南省法官、检察官和司法辅助、司法行政人员工资变动审批表";
        }
        if (institution) {
            return institutionSalaryLevelTitle;
        }
        // 机关：级别 / 档次晋升分别使用标准标题；优先按变动类别判定，避免「正常+晋升」误判。
        if (isAgencyGradeLevelPromotion(report, selectedTitle)) {
            return agencyGradeLevelTitle;
        }
        if (isAgencyGradeStepPromotion(report, selectedTitle)) {
            return agencyGradeStepTitle;
        }
        String title = selectedTitle == null ? "" : selectedTitle.trim();
        if (title.isEmpty()) {
            title = "河南省机关工作人员" + safe(report.changeType()) + "工资变动审批表";
        } else if (!title.startsWith("河南省")) {
            if (title.startsWith("机关工作人员")) {
                title = "河南省" + title;
            } else if (title.startsWith("工作人员")) {
                title = "河南省机关" + title;
            } else if (!title.contains("机关工作人员")) {
                title = "河南省机关工作人员" + title;
            } else {
                title = "河南省" + title;
            }
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

    private boolean isAgencyGradeLevelPromotion(PayrollChangeComparison report, String selectedTitle) {
        String type = safe(report.changeType());
        String title = safe(selectedTitle);
        if (type.contains("档次") || type.contains("晋档")) {
            return false;
        }
        if (type.contains("级别") || type.contains("晋级") || type.contains("级别滚动")) {
            return true;
        }
        return (title.contains("级别") || title.contains("晋级"))
                && !title.contains("档次")
                && !title.contains("晋档")
                && !title.contains("薪级");
    }

    private boolean isAgencyGradeStepPromotion(PayrollChangeComparison report, String selectedTitle) {
        if (isAgencyGradeLevelPromotion(report, selectedTitle)) {
            return false;
        }
        String type = safe(report.changeType());
        String title = safe(selectedTitle);
        return type.contains("档次")
                || type.contains("晋档")
                || title.contains("档次")
                || title.contains("晋档")
                || ((type.contains("正常") || title.contains("正常"))
                        && (type.contains("晋升") || title.contains("晋升"))
                        && !type.contains("薪级")
                        && !title.contains("薪级")
                        && !type.contains("级别")
                        && !title.contains("级别"));
    }

    private boolean isRegularizationChange(String changeType, String selectedTitle) {
        String type = safe(changeType);
        String title = safe(selectedTitle);
        return type.contains("转正") || title.contains("转正");
    }

    private boolean isInternSalaryChange(String changeType, String selectedTitle) {
        String type = safe(changeType);
        String title = safe(selectedTitle);
        if (isRegularizationChange(type, title)) {
            return false;
        }
        return type.contains("见习") || title.contains("见习");
    }

    private String previousChangeText(PayrollChangeComparison report) {
        if (report.previousPayrollHistoryId() == null || report.previousPayrollHistoryId().isBlank()) {
            return "无";
        }
        return (formatCompactPeriod(report.previousCalculationPeriod()) + " " + safe(report.previousChangeType())).trim();
    }

    private List<String> basisDetailLines(PayrollChangeComparison report) {
        List<String> lines = new ArrayList<>();
        if (report.basisAssessments() != null) {
            for (var assessment : report.basisAssessments()) {
                String result = safe(assessment.result());
                lines.add(assessment.year() + "年度考核：" + (result.isEmpty() ? "-" : result));
            }
        }
        return lines;
    }

    private String approvalBasisTitle(
            String changeType, boolean institution, boolean internForm, boolean regularizationForm) {
        if (regularizationForm) {
            return "转正定级工资待遇";
        }
        if (internForm) {
            return "见习期工资待遇";
        }
        String type = safe(changeType);
        if (institution) {
            return "正常增加薪级工资";
        }
        if (type.contains("档")) {
            return "按年度考核结果晋升级别工资档次";
        }
        if (type.contains("级")) {
            return "按年度考核结果晋升级别";
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
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "——";
        }
        return money(value);
    }

    private String resolveRegisterTitle(String selectedTitle, boolean judicialForm) {
        if (judicialForm) {
            String source = selectedTitle == null ? "" : selectedTitle.trim();
            if (source.contains("档次")) {
                return "河南省法官、检察官和司法辅助、司法行政人员正常档次工资变动花名册";
            }
            return "河南省法官、检察官和司法辅助、司法行政人员正常级别工资变动花名册";
        }
        return withAgencyRegisterTitlePrefix(selectedTitle);
    }

    private String withAgencyRegisterTitlePrefix(String selectedTitle) {
        String title = selectedTitle == null || selectedTitle.isBlank()
                ? "工资变动花名册"
                : selectedTitle.trim();
        if (title.startsWith("河南省")) {
            return title;
        }
        return "河南省机关" + title;
    }

    private boolean usesJudicialApprovalForm(PayrollChangeComparison report) {
        if (!this.isCourtOrProcuratorateOrganization(report)) {
            return false;
        }
        return !this.isGovernmentWorkerPersonnel(report);
    }

    /** 法院、检察院内的机关工勤（岗位 05/06 等）走一般机关人员审批表，不走法检专用表。 */
    private boolean isGovernmentWorkerPersonnel(PayrollChangeComparison report) {
        if (this.isGovernmentWorkerPositionCode(report.currentPositionCode())
                || this.isGovernmentWorkerPositionCode(report.previousPositionCode())) {
            return true;
        }
        String position = safe(report.currentPositionName()) + safe(report.previousPositionName());
        return position.contains("工勤");
    }

    private boolean isGovernmentWorkerPositionCode(String positionCode) {
        String normalized = safe(positionCode).toUpperCase(Locale.ROOT);
        return normalized.length() >= 2
                && GOVERNMENT_WORKER_POSITION_PREFIXES.contains(normalized.substring(0, 2));
    }

    private boolean isCourtOrProcuratorateOrganization(PayrollChangeComparison report) {
        String xtlb = safe(report.organizationSystemCategory());
        if (xtlb.contains("法院") || xtlb.contains("检察")) {
            return true;
        }
        // xtlb 未维护时，回退单位名称，避免法院空 xtlb 漏判。
        String name = safe(report.organizationName());
        return name.contains("法院") || name.contains("检察");
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
