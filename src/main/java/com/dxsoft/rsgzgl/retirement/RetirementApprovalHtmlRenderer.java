package com.dxsoft.rsgzgl.retirement;

import com.dxsoft.rsgzgl.report.export.ReportHtmlSupport;
import com.dxsoft.rsgzgl.statistics.RetirementMonthCalculator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class RetirementApprovalHtmlRenderer {

    private final String stylesheet;

    RetirementApprovalHtmlRenderer() {
        this.stylesheet = ReportHtmlSupport.loadClasspathText("/report/retirement-approval-print.css");
    }

    String renderDocument(List<RetirementApprovalSheet> sheets) {
        StringBuilder body = new StringBuilder();
        for (RetirementApprovalSheet sheet : sheets) {
            body.append(renderSheet(sheet));
        }
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                <meta charset="utf-8"/>
                <title>退休审批表</title>
                <style>%s</style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(stylesheet, body);
    }

    String renderPreviewBody(List<RetirementApprovalSheet> sheets) {
        StringBuilder body = new StringBuilder();
        for (RetirementApprovalSheet sheet : sheets) {
            body.append(renderSheet(sheet));
        }
        return body.toString();
    }

    private String renderSheet(RetirementApprovalSheet sheet) {
        String template = blank(sheet.templateName());
        int year = styleYear(template);
        if (year >= 2025) {
            return renderSheet2025(sheet, template);
        }
        if (year >= 2021) {
            return renderSheet2021(sheet, template);
        }
        boolean agency = isAgency(template, sheet.organizationNature());
        if (agency) {
            return renderSheet2006Agency(sheet, template);
        }
        return renderSheetLegacy(sheet, template, year);
    }

    private String renderSheet2006Agency(RetirementApprovalSheet sheet, String template) {
        boolean worker = isWorkerCategory(sheet.postCategory());
        String title = worker ? "机关工人退休（职）审核表" : "国家公务员退休审核表";
        String gradeLabel = worker ? "技术等级工资" : "级别工资";
        int gradeOrTechAmount = worker
                ? (sheet.beforeTechnicalSalary() > 0 ? sheet.beforeTechnicalSalary() : sheet.beforeGradeSalary())
                : (sheet.beforeGradeSalary() > 0 ? sheet.beforeGradeSalary() : sheet.beforeTechnicalSalary());
        int positionAmount = sheet.beforePositionSalary();
        int wageBase = positionAmount + gradeOrTechAmount;
        int ratio = Math.max(sheet.conversionRatio(), 0);
        int converted = Math.round(wageBase * ratio / 100.0f);
        int retained = sheet.beforeRetainedAllowance();
        int bonus = sheet.beforeBonusBalance();
        int living = sheet.afterLocalAllowance() > 0 ? sheet.afterLocalAllowance() : sheet.livingAllowance();
        int jobAllowance = sheet.beforeJobAllowance();
        int rankAllowance = sheet.beforeRankAllowance();
        int otherSubsidy = sheet.beforePostAllowance() + sheet.beforeOther();
        int advance = sheet.cumulativeIncrease();
        int monthlyTotal = positionAmount + gradeOrTechAmount + retained + bonus + jobAllowance + rankAllowance + otherSubsidy;
        int monthlyPay = converted + retained + bonus + living + otherSubsidy + advance;
        String benefitFrom = nextMonthChinese(sheet.retirementDate());
        String retirementPosition = displayOptionalText(blank(sheet.positionName()));
        String gradeStepText = gradeText(sheet);
        if (!gradeStepText.isBlank()) {
            retirementPosition = retirementPosition.isBlank()
                    ? gradeStepText
                    : retirementPosition + "　" + gradeStepText;
        }
        String before2014 = displayOptionalText(blank(sheet.beforePositionName()));
        if (before2014.isBlank()) {
            before2014 = displayOptionalText(blank(sheet.positionName()));
        }
        String before2014Grade = gradeText(sheet);
        if (!before2014Grade.isBlank()) {
            before2014 = before2014.isBlank() ? before2014Grade : before2014 + "　" + before2014Grade;
        }
        String policy = worker
                ? "根据国家和河南省机关工人退休（职）有关规定："
                : "根据《公务员法》、豫政[2006]74号、豫人[2006]85号、豫人社办[2017]89号等规定：";
        String formula = "[职务工资+" + gradeLabel + "] × " + ratio + "% = " + converted;

        return """
                <div class="ret-sheet ret-y2006 ret-xz ret-universal">
                    <div class="ret-header">
                        <h3>%s</h3>
                    </div>
                    <table class="ret-frame ret-frame-2006">
                        <colgroup>
                            <col style="width:12.5%%"/><col style="width:12.5%%"/>
                            <col style="width:12.5%%"/><col style="width:12.5%%"/>
                            <col style="width:12.5%%"/><col style="width:12.5%%"/>
                            <col style="width:12.5%%"/><col style="width:12.5%%"/>
                        </colgroup>
                        <tbody>
                        <tr>
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>出生年月</th><td colspan="3">%s</td>
                        </tr>
                        <tr>
                            <th>工作单位</th><td colspan="2">%s</td>
                            <th colspan="2">退休时职务职级（技术等级）</th><td colspan="3">%s</td>
                        </tr>
                        <tr>
                            <th>参加工作时间</th><td colspan="2">%s</td>
                            <th colspan="2">工作年限</th><td colspan="3">%s年</td>
                        </tr>
                        <tr>
                            <th colspan="3">2014年9月30日前职务职级（技术等级）</th>
                            <td colspan="5">%s</td>
                        </tr>
                        <tr>
                            <th colspan="8" class="ret-section">2014年9月本人基本工资标准及津补贴金额（元）</th>
                        </tr>
                        <tr class="ret-wage-head">
                            <td>职务工资</td>
                            <td>%s</td>
                            <td>保留福补</td>
                            <td>保留奖金</td>
                            <td>职务津贴补贴</td>
                            <td>警衔津贴</td>
                            <td>其它补贴</td>
                            <td>月工资合计</td>
                        </tr>
                        <tr class="ret-wage-value">
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <th class="ret-side-label">退休（职）临时待遇计算办法及金额</th>
                            <td colspan="7" class="ret-calc-body">
                                <div>%s</div>
                                <div class="ret-calc-formula">%s</div>
                                <div>保留福补 %s 元；保留奖金 %s 元；生活补贴 %s 元；其他补贴 %s 元。</div>
                                <div>“预增发”退休费 %s 元；每月发放金额 %s 元。</div>
                                <div class="ret-calc-exec">退休（职）临时待遇执行时间：从 %s 起执行。</div>
                            </td>
                        </tr>
                        <tr class="ret-opinion-row">
                            <td colspan="3" class="ret-opinion-cell">
                                <div class="ret-opinion-title">所在单位申报意见</div>
                                <div class="ret-opinion-body ret-opinion-center">同意</div>
                                <div class="ret-sign">年　　月　　日</div>
                            </td>
                            <td colspan="2" class="ret-opinion-cell">
                                <div class="ret-opinion-title">主管部门审批意见</div>
                                <div class="ret-opinion-body ret-opinion-center">同意单位意见</div>
                                <div class="ret-sign">年　　月　　日</div>
                            </td>
                            <td colspan="3" class="ret-opinion-cell">
                                <div class="ret-opinion-title">人社部门备案意见</div>
                                <div class="ret-opinion-body">%s</div>
                                <div class="ret-sign">年　　月　　日</div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="ret-meta-line">单位编码：%s　个人编码：%s　表样：%s</div>
                </div>
                """.formatted(
                esc(title),
                esc(sheet.name()),
                esc(sheet.gender()),
                esc(sheet.birthYearMonth()),
                esc(blank(sheet.organizationName())),
                esc(retirementPosition),
                esc(sheet.workStartYearMonth()),
                sheet.salaryYears(),
                esc(before2014),
                esc(gradeLabel),
                moneyOrDash(positionAmount),
                moneyOrDash(gradeOrTechAmount),
                moneyOrDash(retained),
                moneyOrDash(bonus),
                moneyOrDash(jobAllowance),
                moneyOrDash(rankAllowance),
                moneyOrDash(otherSubsidy),
                moneyOrDash(monthlyTotal),
                esc(policy),
                esc(formula),
                moneyOrDash(retained),
                moneyOrDash(bonus),
                moneyOrDash(living),
                moneyOrDash(otherSubsidy),
                moneyOrDash(advance),
                moneyOrDash(monthlyPay),
                esc(benefitFrom),
                esc(displayOptionalText(sheet.approvalStatus())),
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(template));
    }

    private boolean isWorkerCategory(String postCategory) {
        String value = blank(postCategory);
        return value.contains("工人")
                || value.contains("工勤")
                || "技术工岗位".equals(value)
                || "普通工岗位".equals(value);
    }

    private String renderSheet2021(RetirementApprovalSheet sheet, String template) {
        String beforePosition = blank(sheet.beforePositionName());
        if (beforePosition.isBlank()) {
            beforePosition = blank(sheet.positionName());
        }
        int gradeOrTech = sheet.beforeGradeSalary() > 0 ? sheet.beforeGradeSalary() : sheet.beforeTechnicalSalary();
        int livingSubsidy = sheet.afterLocalAllowance() > 0 ? sheet.afterLocalAllowance() : sheet.livingAllowance();
        String benefitFrom = nextMonthChinese(sheet.retirementDate());
        return """
                <div class="ret-sheet ret-y2021 ret-universal">
                    <div class="ret-header">
                        <h3>机关事业单位职工退休（职）审核表</h3>
                    </div>
                    <div class="ret-unit-line">单位名称：%s</div>
                    <table class="ret-frame ret-frame-2021">
                        <colgroup>
                            <col style="width:14%%"/><col style="width:11%%"/>
                            <col style="width:14%%"/><col style="width:11%%"/>
                            <col style="width:16%%"/><col style="width:12%%"/>
                            <col style="width:12%%"/><col style="width:10%%"/>
                        </colgroup>
                        <tbody>
                        <tr>
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th colspan="2">退休时岗位等级</th><td colspan="2">%s</td>
                        </tr>
                        <tr>
                            <th>出生年月</th><td>%s</td>
                            <th>参加工作时间</th><td>%s</td>
                            <th>退休时间</th><td>%s</td>
                            <th>退休类别</th><td>%s</td>
                        </tr>
                        <tr>
                            <th>2014年9月人员类别(身份)</th><td colspan="1">%s</td>
                            <th>2014年9月职务职级(技术职称)</th><td colspan="1">%s</td>
                            <th>特殊工种折算月数</th><td></td>
                            <th>国人部发〔2006〕60号文件计发比例</th><td>%s%%</td>
                        </tr>
                        <tr>
                            <th>中断工作年限情况</th><td colspan="3">%s</td>
                            <th colspan="2">中断年限扣减月数</th><td colspan="2">%s</td>
                        </tr>
                        <tr>
                            <th rowspan="2" class="ret-side-label">2014年9月基本工资标准（元）</th>
                            <td colspan="2" class="ret-kv"><div class="k">职务/岗位工资</div><div class="v">%s</div></td>
                            <td colspan="3" class="ret-kv"><div class="k">级别/薪级/技术等级工资</div><div class="v">%s</div></td>
                            <td colspan="2" class="ret-kv"><div class="k">人民警察警衔津贴</div><div class="v">%s</div></td>
                        </tr>
                        <tr>
                            <td colspan="2" class="ret-kv"><div class="k">海关津贴</div><div class="v">%s</div></td>
                            <td colspan="3" class="ret-kv"><div class="k">基本工资提高部分</div><div class="v">%s</div></td>
                            <td colspan="2" class="ret-kv"><div class="k">特殊教育补贴</div><div class="v">%s</div></td>
                        </tr>
                        <tr>
                            <th rowspan="2" class="ret-side-label">2014年9月相应的退休补贴标准（元）</th>
                            <td colspan="4" class="ret-kv"><div class="k">退休人员生活补贴</div><div class="v">%s</div></td>
                            <td colspan="3" class="ret-kv"><div class="k">教(护)龄津贴</div><div class="v">%s</div></td>
                        </tr>
                        <tr>
                            <td colspan="4" class="ret-kv"><div class="k">特级教师津贴</div><div class="v">%s</div></td>
                            <td colspan="3" class="ret-kv"><div class="k">1993年工改保留物价福利补贴</div><div class="v">%s</div></td>
                        </tr>
                        <tr>
                            <th class="ret-side-label">按国办发〔2015〕3号文件规定相应增加的退休费标准（元）</th>
                            <td colspan="7" class="ret-kv-center"><div class="v">%s</div></td>
                        </tr>
                        <tr class="ret-opinion-row">
                            <td colspan="8" class="ret-opinion-wrap">
                                <table class="ret-opinion-3col">
                                    <colgroup>
                                        <col style="width:33.34%%"/>
                                        <col style="width:33.33%%"/>
                                        <col style="width:33.33%%"/>
                                    </colgroup>
                                    <tr>
                                        <td class="ret-opinion-cell">
                                            <div class="ret-opinion-title">所在单位<br/>申报意见</div>
                                            <div class="ret-opinion-body">从 %s 起执行退休待遇。</div>
                                            <div class="ret-sign">年　　月　　日</div>
                                        </td>
                                        <td class="ret-opinion-cell">
                                            <div class="ret-opinion-title">主管部门<br/>审批意见</div>
                                            <div class="ret-opinion-body ret-opinion-center">同意单位意见</div>
                                            <div class="ret-sign">年　　月　　日</div>
                                        </td>
                                        <td class="ret-opinion-cell">
                                            <div class="ret-opinion-title">人社部门<br/>备案意见</div>
                                            <div class="ret-opinion-body">%s</div>
                                            <div class="ret-sign">年　　月　　日</div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="ret-meta-line">单位编码：%s　个人编码：%s　表样：%s</div>
                </div>
                """.formatted(
                esc(blank(sheet.organizationName())),
                esc(sheet.name()),
                esc(sheet.gender()),
                esc(blank(sheet.positionName())),
                esc(sheet.birthYearMonth()),
                esc(sheet.workStartYearMonth()),
                esc(sheet.retirementDate()),
                esc(displayRetirementCategory(sheet.retirementCategory())),
                esc(blank(sheet.postCategory())),
                esc(beforePosition),
                sheet.conversionRatio(),
                esc(displayOptionalText(sheet.interruptedNote())),
                esc(displayOptionalText(sheet.interruptedMonths())),
                moneyOrDash(sheet.beforePositionSalary()),
                moneyOrDash(gradeOrTech),
                moneyOrDash(sheet.beforeRankAllowance()),
                "——",
                moneyOrDash(sheet.beforeTeachingRaise()),
                "——",
                moneyOrDash(livingSubsidy),
                moneyOrDash(sheet.teachingAgeAllowance()),
                "——",
                moneyOrDash(sheet.afterRetainedAllowance()),
                moneyOrDash(sheet.cumulativeIncrease()),
                esc(benefitFrom),
                esc(displayOptionalText(sheet.approvalStatus())),
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(template));
    }

    private String renderSheet2025(RetirementApprovalSheet sheet, String template) {
        ReasonChecklist reasons = reasonChecklist2025(sheet.retirementReason());
        String interruptedNote = blank(sheet.interruptedNote());
        if (interruptedNote.isBlank()) {
            interruptedNote = "无";
        }
        String interruptedMonths = blank(sheet.interruptedMonths());
        if (interruptedMonths.isBlank()) {
            interruptedMonths = "0";
        }
        String benefitFrom = nextMonthChinese(sheet.retirementDate());
        return """
                <div class="ret-sheet ret-y2025 ret-universal">
                    <div class="ret-header">
                        <h3>机关事业单位职工退休（职）审核表</h3>
                    </div>
                    <div class="ret-unit-line">单位名称：%s</div>
                    <table class="ret-frame ret-frame-2025">
                        <colgroup>
                            <col style="width:16%%"/><col style="width:17%%"/>
                            <col style="width:16%%"/><col style="width:17%%"/>
                            <col style="width:16%%"/><col style="width:18%%"/>
                        </colgroup>
                        <tbody>
                        <tr class="ret-row-info">
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>出生年月</th><td>%s</td>
                        </tr>
                        <tr class="ret-row-info">
                            <th>参加工作时间</th><td>%s</td>
                            <th>身份证号</th><td colspan="3">%s</td>
                        </tr>
                        <tr class="ret-row-info">
                            <th>退休时人员类别</th><td colspan="2">%s</td>
                            <th>退休时岗位等级及薪级</th><td colspan="2">%s</td>
                        </tr>
                        <tr class="ret-row-info">
                            <th>中断工作年限情况</th><td colspan="2">%s</td>
                            <th>中断年限扣减月数</th><td colspan="2">%s</td>
                        </tr>
                        <tr class="ret-row-reasons">
                            <th>退休类别</th>
                            <td colspan="5" class="ret-reasons ret-reasons-2025">%s</td>
                        </tr>
                        <tr class="ret-row-promise">
                            <th>退休时间</th><td>%s</td>
                            <th>本人承诺</th>
                            <td colspan="3" class="ret-promise">
                                <div>本人申请按以上第 <strong>%s</strong> 项办理退休</div>
                                <div class="ret-sign-inline">本人签字：____________</div>
                            </td>
                        </tr>
                        <tr class="ret-row-opinion">
                            <th class="ret-opinion-label">所在单位<br/>申报意见</th>
                            <td colspan="5" class="ret-opinion">
                                <div>经审核个人档案确认以上信息准确无误，同意申报。</div>
                                <div class="ret-sign">负责人：　　　　　　年　　月　　日</div>
                            </td>
                        </tr>
                        <tr class="ret-row-opinion">
                            <th class="ret-opinion-label">主管部门<br/>审批意见</th>
                            <td colspan="5" class="ret-opinion">
                                <div>经审核个人档案确认以上信息准确无误，符合退休条件，同意该同志退休。</div>
                                <div class="ret-sign">负责人：　　　　　　年　　月　　日</div>
                            </td>
                        </tr>
                        <tr class="ret-row-opinion">
                            <th class="ret-opinion-label">人社部门<br/>备案意见</th>
                            <td colspan="5" class="ret-opinion">
                                <div>同意备案，并从 %s 起计发待遇。</div>
                                <div class="ret-sign">负责人：　　　　　　年　　月　　日</div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                esc(blank(sheet.organizationName())),
                esc(sheet.name()),
                esc(sheet.gender()),
                esc(sheet.birthYearMonth()),
                esc(sheet.workStartYearMonth()),
                esc(sheet.idCard()),
                esc(blank(sheet.postCategory())),
                esc(positionGradeText(sheet)),
                esc(interruptedNote),
                esc(interruptedMonths),
                reasons.html(),
                esc(sheet.retirementDate()),
                esc(reasons.checkedIndexText()),
                esc(benefitFrom));
    }

    private String renderSheetLegacy(RetirementApprovalSheet sheet, String template, int year) {
        boolean agency = isAgency(template, sheet.organizationNature());
        String yearClass = "ret-y" + year;
        String natureClass = agency ? "ret-xz" : "ret-sy";
        String title = resolveTitle(year, agency, sheet.postCategory());
        String positionLabel = agency ? "退休时职务职级" : "退休时岗位";
        WageLabels wages = wageLabels(agency);
        String reasonMarks = reasonChecklistLegacy(sheet.retirementReason(), year);
        String policy = year == 2006 && agency
                ? "<div class=\"ret-policy\">根据《公务员法》、豫政[2006]74号、豫人[2006]85号、豫人社办[2017]89号等规定填报。</div>"
                : "";
        String treatmentLabel = year == 2006 ? "退休（职）临时待遇执行时间" : "退休费执行时间";
        String approvalLine = "同意备案，自 " + esc(sheet.retirementDate()) + " 起享受退休待遇。";

        return """
                <div class="ret-sheet %s %s">
                    <table class="ret-topline"><tbody><tr>
                        <td>单位编码：%s</td>
                        <td>个人编码：%s</td>
                        <td>表样：%s</td>
                    </tr></tbody></table>
                    <div class="ret-header">
                        <h3>%s</h3>
                        <div class="ret-sub">%s　%s</div>
                        %s
                    </div>
                    <table class="ret-frame">
                        <colgroup>
                            <col style="width:12%%"/><col style="width:13%%"/>
                            <col style="width:12%%"/><col style="width:13%%"/>
                            <col style="width:12%%"/><col style="width:13%%"/>
                            <col style="width:12%%"/><col style="width:13%%"/>
                        </colgroup>
                        <tbody>
                        <tr>
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>民族</th><td>%s</td>
                            <th>出生年月</th><td>%s</td>
                        </tr>
                        <tr>
                            <th>参加工作</th><td>%s</td>
                            <th>工龄</th><td>%s</td>
                            <th>学历</th><td>%s</td>
                            <th>岗位分类</th><td>%s</td>
                        </tr>
                        <tr>
                            <th>退休时间</th><td>%s</td>
                            <th>退休类别</th><td>%s</td>
                            <th>%s</th><td colspan="3">%s</td>
                        </tr>
                        <tr>
                            <th>级别/档次</th><td>%s</td>
                            <th>折算比例</th><td>%s%%</td>
                            <th>提高比例</th><td>%s%%</td>
                            <th>审批状态</th><td>%s</td>
                        </tr>
                        <tr>
                            <th colspan="8" class="ret-section">退休前工资额及补贴金额（元）</th>
                        </tr>
                        <tr>
                            <th>%s</th><td class="num">%s</td>
                            <th>%s</th><td class="num">%s</td>
                            <th>%s</th><td class="num">%s</td>
                            <th>%s</th><td class="num">%s</td>
                        </tr>
                        <tr>
                            <th>%s</th><td class="num">%s</td>
                            <th>保留福补</th><td class="num">%s</td>
                            <th>地方补贴</th><td class="num">%s</td>
                            <th>岗位津贴</th><td class="num">%s</td>
                        </tr>
                        <tr>
                            <th>其它</th><td class="num">%s</td>
                            <th>合计</th><td class="num" colspan="5"><strong>%s</strong></td>
                        </tr>
                        <tr>
                            <th colspan="8" class="ret-section">退休费及补贴发放金额（元）</th>
                        </tr>
                        <tr>
                            <th>%s</th><td class="num">%s</td>
                            <th>%s</th><td class="num">%s</td>
                            <th>%s</th><td class="num">%s</td>
                            <th>%s</th><td class="num">%s</td>
                        </tr>
                        <tr>
                            <th>%s</th><td class="num">%s</td>
                            <th>保留福补</th><td class="num">%s</td>
                            <th>地方补贴</th><td class="num">%s</td>
                            <th>岗位津贴</th><td class="num">%s</td>
                        </tr>
                        <tr>
                            <th>折算基数</th><td class="num">%s</td>
                            <th>其它</th><td class="num">%s</td>
                            <th>合计</th><td class="num" colspan="3"><strong>%s</strong></td>
                        </tr>
                        <tr>
                            <th>退休事由</th>
                            <td colspan="7" class="ret-reasons">%s</td>
                        </tr>
                        <tr>
                            <th>所在单位意见</th>
                            <td colspan="7" class="ret-opinion">
                                <div>经审核个人档案确认以上信息准确无误，同意申报主管单位审批。</div>
                                <div class="ret-sign">负责人：　　　　　年　　月　　日</div>
                            </td>
                        </tr>
                        <tr>
                            <th>主管单位意见</th>
                            <td colspan="7" class="ret-opinion">
                                <div>经复核个人档案确认以上信息准确无误，符合退休条件，同意批准该同志退休。</div>
                                <div class="ret-sign">负责人：　　　　　年　　月　　日</div>
                            </td>
                        </tr>
                        <tr>
                            <th>审批意见</th>
                            <td colspan="7" class="ret-opinion">
                                <div>%s</div>
                                <div class="ret-treatment">%s：%s</div>
                                <div class="ret-sign">审批机关：　　　　　年　　月　　日</div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                yearClass,
                natureClass,
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(template),
                esc(title),
                esc(blank(sheet.organizationName())),
                esc(blank(sheet.organizationNature()) + " / " + blank(sheet.styleLabel())),
                policy,
                esc(sheet.name()),
                esc(sheet.gender()),
                esc(sheet.nation()),
                esc(sheet.birthYearMonth()),
                esc(sheet.workStartYearMonth()),
                sheet.salaryYears(),
                esc(sheet.education()),
                esc(sheet.postCategory()),
                esc(sheet.retirementDate()),
                esc(sheet.retirementCategory()),
                esc(positionLabel),
                esc(positionText(sheet)),
                esc(gradeText(sheet)),
                sheet.conversionRatio(),
                sheet.increaseRatio(),
                esc(sheet.approvalStatus()),
                esc(wages.position()),
                money(sheet.beforePositionSalary()),
                esc(wages.grade()),
                money(sheet.beforeGradeSalary()),
                esc(wages.technical()),
                money(sheet.beforeTechnicalSalary()),
                esc(wages.teaching()),
                money(sheet.beforeTeachingRaise()),
                esc(wages.rank()),
                money(sheet.beforeRankAllowance()),
                money(sheet.beforeRetainedAllowance()),
                money(sheet.beforeLocalAllowance()),
                money(sheet.beforePostAllowance()),
                money(sheet.beforeOther()),
                money(sheet.beforeTotal()),
                esc(wages.position()),
                money(sheet.afterPositionSalary()),
                esc(wages.grade()),
                money(sheet.afterGradeSalary()),
                esc(wages.technical()),
                money(sheet.afterTechnicalSalary()),
                esc(wages.teaching()),
                money(sheet.afterTeachingRaise()),
                esc(wages.rank()),
                money(sheet.afterRankAllowance()),
                money(sheet.afterRetainedAllowance()),
                money(sheet.afterLocalAllowance()),
                money(sheet.afterPostAllowance()),
                money(sheet.afterConvertedBase()),
                money(sheet.afterOther()),
                money(sheet.afterTotal()),
                reasonMarks,
                approvalLine,
                esc(treatmentLabel),
                esc(sheet.retirementDate()));
    }

    private String resolveTitle(int year, boolean agency, String postCategory) {
        if (year >= 2021) {
            return "机关事业单位职工退休（职）审核表";
        }
        if (!agency) {
            return "事业单位职工退休（职）审批表";
        }
        String gwfl = blank(postCategory);
        if (gwfl.contains("工人")) {
            return "机关工人退休（职）审核表";
        }
        return "国家公务员退休审核表";
    }

    private WageLabels wageLabels(boolean agency) {
        if (agency) {
            return new WageLabels("职务工资", "级别工资", "技术等级", "教护提高", "警衔津贴");
        }
        return new WageLabels("岗位工资", "薪级工资", "技术等级", "教护提高", "特殊岗位");
    }

    private ReasonChecklist reasonChecklist2025(String retirementReason) {
        List<String> options = List.of(
                "到龄退休",
                "弹性提前退休",
                "弹性延迟期满退休",
                "终止弹性延迟退休",
                "特殊工种提前退休",
                "因病提前退休（职）",
                "县处级女干部和具有高级职称的女性专业技术人员自愿退休");
        String reason = blank(retirementReason);
        if (reason.isBlank()) {
            reason = "到龄";
        }
        String mapped = mapLegacyReason(reason);
        int checkedIndex = 0;
        StringBuilder html = new StringBuilder();
        html.append("<ol class=\"ret-reason-list ret-reason-list-2025\">");
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            boolean checked = matchesReason(mapped, option) || matchesReason(reason, option);
            if (checked && checkedIndex == 0) {
                checkedIndex = i + 1;
            }
            html.append("<li>")
                    .append(checked ? "（ √ ）" : "（　）")
                    .append(i + 1)
                    .append(".")
                    .append(esc(option))
                    .append("</li>");
        }
        html.append("</ol>");
        if (checkedIndex == 0) {
            checkedIndex = 1;
        }
        return new ReasonChecklist(html.toString(), String.valueOf(checkedIndex));
    }

    private String reasonChecklistLegacy(String retirementReason, int year) {
        List<String> options;
        if (year >= 2021) {
            options = List.of("到龄退休", "正常退休", "政策性退休", "特殊工种提前退休", "因病提前退休（职）", "其它");
        } else {
            options = List.of("到龄退休", "特殊工种提前退休", "因病提前退休（职）", "其它");
        }
        String reason = blank(retirementReason);
        if (reason.isBlank()) {
            reason = "到龄";
        }
        String mapped = mapLegacyReason(reason);
        StringBuilder html = new StringBuilder();
        html.append("<div>本人申请按以上第______项办理退休</div>");
        html.append("<ol class=\"ret-reason-list\">");
        for (String option : options) {
            boolean checked = matchesReason(mapped, option) || matchesReason(reason, option);
            html.append("<li>")
                    .append(checked ? "[√] " : "[ ] ")
                    .append(esc(option))
                    .append("</li>");
        }
        html.append("</ol>");
        return html.toString();
    }

    private String mapLegacyReason(String reason) {
        if ("到龄".equals(reason) || "退休".equals(reason)) {
            return "到龄退休";
        }
        if ("提前退休".equals(reason) || "批准延期".equals(reason)) {
            return "政策性退休";
        }
        return reason;
    }

    private boolean matchesReason(String reason, String option) {
        if (reason.contains(option) || option.contains(reason)) {
            return true;
        }
        String compactOption = option.replace("（职）", "");
        if (reason.contains(compactOption)) {
            return true;
        }
        return option.startsWith("到龄") && (reason.contains("到龄") || "退休".equals(reason) || "正常退休".equals(reason));
    }

    private int styleYear(String template) {
        if (template.endsWith("25") || "txspb25".equals(template)) {
            return 2025;
        }
        if (template.endsWith("21") || "txspb21".equals(template)) {
            return 2021;
        }
        return 2006;
    }

    private String displayRetirementCategory(String category) {
        String value = blank(category);
        if (value.isBlank() || "退休".equals(value)) {
            return "正常退休";
        }
        return value;
    }

    private String displayOptionalText(String value) {
        String text = blank(value);
        if (text.isBlank() || ".NULL.".equalsIgnoreCase(text) || "null".equalsIgnoreCase(text)) {
            return "";
        }
        return text;
    }

    private String moneyOrDash(int value) {
        return value == 0 ? "——" : String.valueOf(value);
    }

    private boolean isAgency(String template, String organizationNature) {
        if (template.contains("xz")) {
            return true;
        }
        if (template.contains("sy")) {
            return false;
        }
        return organizationNature != null && organizationNature.contains("行政");
    }

    private String positionText(RetirementApprovalSheet sheet) {
        String code = blank(sheet.positionCode());
        String name = blank(sheet.positionName());
        if (code.isBlank()) {
            return name;
        }
        if (name.isBlank() || name.equals(code)) {
            return code;
        }
        return code + " " + name;
    }

    private String gradeText(RetirementApprovalSheet sheet) {
        String level = blank(sheet.gradeLevel());
        String step = blank(sheet.gradeStep());
        if (level.isBlank()) {
            return step;
        }
        if (step.isBlank()) {
            return level;
        }
        return level + " / " + step;
    }

    private String positionGradeText(RetirementApprovalSheet sheet) {
        String name = blank(sheet.positionName());
        if (name.isBlank()) {
            name = blank(sheet.positionCode());
        }
        String level = blank(sheet.gradeLevel());
        String step = blank(sheet.gradeStep());
        StringBuilder text = new StringBuilder(name);
        if (!level.isBlank()) {
            if (!text.isEmpty()) {
                text.append(' ');
            }
            text.append("级别").append(level);
        }
        if (!step.isBlank()) {
            if (!text.isEmpty()) {
                text.append(' ');
            }
            text.append("薪级").append(step).append("级");
        }
        return text.toString();
    }

    private String nextMonthChinese(String retirementDate) {
        String digits = RetirementMonthCalculator.normalizeYearMonth(retirementDate);
        if (digits.length() < 6) {
            return "　　年　　月";
        }
        int year = Integer.parseInt(digits.substring(0, 4));
        int month = Integer.parseInt(digits.substring(4, 6));
        month += 1;
        if (month > 12) {
            month = 1;
            year += 1;
        }
        return year + "年" + month + "月";
    }

    private String money(int value) {
        return value == 0 ? "" : String.valueOf(value);
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private String esc(String value) {
        return ReportHtmlSupport.escape(value);
    }

    private record WageLabels(String position, String grade, String technical, String teaching, String rank) {
    }

    private record ReasonChecklist(String html, String checkedIndexText) {
    }
}
