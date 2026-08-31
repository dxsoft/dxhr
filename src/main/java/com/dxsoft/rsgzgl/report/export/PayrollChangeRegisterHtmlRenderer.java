package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterColumnLabels;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterPageModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterPersonRow;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterTotalsRow;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class PayrollChangeRegisterHtmlRenderer {

    private final String stylesheet;

    PayrollChangeRegisterHtmlRenderer() {
        this.stylesheet = ReportHtmlSupport.loadClasspathText("/report/register-print.css");
    }

    String renderDocument(List<RegisterPageModel> pages) {
        StringBuilder body = new StringBuilder();
        for (RegisterPageModel page : pages) {
            body.append(renderPage(page));
        }
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                <meta charset="utf-8"/>
                <title>工资变动花名册</title>
                <style>%s</style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(stylesheet, body);
    }

    String renderPreviewBody(List<RegisterPageModel> pages, boolean unauthorized) {
        StringBuilder body = new StringBuilder();
        for (RegisterPageModel page : pages) {
            String html = renderPage(page);
            body.append(unauthorized ? PrintAuthWatermark.wrap(html) : html);
        }
        return body.toString();
    }

    private String renderPage(RegisterPageModel page) {
        return page.judicialForm() ? renderJudicialPage(page) : renderAgencyPage(page);
    }

    private String renderAgencyPage(RegisterPageModel page) {
        RegisterColumnLabels labels = page.labels();
        StringBuilder rows = new StringBuilder();
        for (RegisterPersonRow person : page.people()) {
            rows.append(renderAgencyPersonRows(person));
        }
        rows.append(renderAgencyTotals(page.totals()));
        return """
                <div class="register-sheet register-sheet-agency">
                    <div class="approval-sheet-header"><h3>%s</h3></div>
                    <div class="register-topline">
                        <span>单位名称：%s[单位编码：%s]</span>
                        <span>第 %d 页&#160;&#160;共 %d 页</span>
                    </div>
                    <table class="register-table">
                        <colgroup>
                            <col class="c-name"/>
                            <col class="c-id"/>
                            <col class="c-ba"/>
                            <col class="c-money"/>
                            <col class="c-position"/>
                            <col class="c-level"/>
                            <col class="c-money"/><col class="c-money"/>
                            <col class="c-money"/><col class="c-money"/><col class="c-money"/><col class="c-money"/>
                            <col class="c-money"/><col class="c-money"/><col class="c-money"/><col class="c-money"/>
                            <col class="c-money"/><col class="c-money"/>
                            <col class="c-diff"/>
                            <col class="c-period"/>
                        </colgroup>
                        <thead>
                        <tr>
                            <th rowspan="2">姓名</th>
                            <th rowspan="2">身份证号</th>
                            <th rowspan="2">变动前后</th>
                            <th colspan="15">工资情况</th>
                            <th rowspan="2">月增(+)<br/>减(-)</th>
                            <th rowspan="2">执行时间</th>
                        </tr>
                        <tr>
                            <th>月工资<br/>合计</th>
                            <th>%s</th>
                            <th>%s</th>
                            <th>%s</th>
                            <th>%s</th>
                            <th>技术<br/>等级<br/>工资</th>
                            <th>保留<br/>奖金</th>
                            <th>保留<br/>福补</th>
                            <th>警衔/<br/>监察<br/>津贴</th>
                            <th>工改<br/>保留<br/>津贴</th>
                            <th>工作性<br/>津贴</th>
                            <th>生活性<br/>补贴</th>
                            <th>岗位<br/>津贴</th>
                            <th>工改<br/>保留<br/>工资</th>
                            <th>其它<br/>补贴</th>
                        </tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>
                </div>
                """.formatted(
                esc(page.reportTitle()),
                esc(page.organizationName()),
                esc(page.organizationCode()),
                page.pageNumber(),
                page.pageCount(),
                ReportHtmlSupport.escapedMultilineBr(labels.position()),
                esc(labels.level()),
                esc(labels.positionSalary()),
                esc(labels.gradeSalary()),
                rows);
    }

    private String renderJudicialPage(RegisterPageModel page) {
        StringBuilder rows = new StringBuilder();
        for (RegisterPersonRow person : page.people()) {
            rows.append(renderJudicialPersonRows(person));
        }
        rows.append(renderJudicialTotals(page.totals()));
        return """
                <div class="register-sheet register-sheet-judicial">
                    <div class="approval-sheet-header"><h3>%s</h3></div>
                    <div class="register-topline">
                        <span>单位名称：%s[%s]</span>
                        <span>第 %d 页&#160;&#160;共 %d 页</span>
                    </div>
                    <table class="register-table register-table-judicial">
                        <colgroup>
                            <col class="cj-name"/>
                            <col class="cj-id"/>
                            <col class="cj-ba"/>
                            <col class="cj-money"/>
                            <col class="cj-position"/>
                            <col class="cj-level"/><col class="cj-step"/>
                            <col class="cj-money"/><col class="cj-money"/>
                            <col class="cj-money"/><col class="cj-money"/><col class="cj-money"/><col class="cj-money"/>
                            <col class="cj-money"/><col class="cj-money"/><col class="cj-money"/><col class="cj-money"/>
                            <col class="cj-money"/>
                            <col class="cj-diff"/>
                            <col class="cj-period"/>
                        </colgroup>
                        <thead>
                        <tr>
                            <th rowspan="3">姓名</th>
                            <th rowspan="3">身份证号</th>
                            <th rowspan="3"></th>
                            <th rowspan="3">月工资<br/>合计</th>
                            <th colspan="14">工资情况</th>
                            <th rowspan="3">月增(+)<br/>减(-)</th>
                            <th rowspan="3">执行时间</th>
                        </tr>
                        <tr>
                            <th colspan="5">基本工资</th>
                            <th rowspan="2">保留<br/>职务<br/>工资</th>
                            <th rowspan="2">技术<br/>等级<br/>工资</th>
                            <th rowspan="2">保留<br/>奖金</th>
                            <th rowspan="2">保留<br/>福补</th>
                            <th rowspan="2">工作性<br/>津贴</th>
                            <th rowspan="2">生活性<br/>补贴</th>
                            <th rowspan="2">特殊<br/>岗位<br/>津贴</th>
                            <th rowspan="2">警衔<br/>津贴</th>
                            <th rowspan="2">工改<br/>保留<br/>津贴</th>
                        </tr>
                        <tr>
                            <th>职务<br/>(或职务等级)</th>
                            <th>级别</th>
                            <th>档次</th>
                            <th>职务<br/>(职务等级)<br/>工资</th>
                            <th>级别工资</th>
                        </tr>
                        </thead>
                        <tbody>%s</tbody>
                    </table>
                </div>
                """.formatted(
                esc(page.reportTitle()),
                esc(page.organizationName()),
                esc(page.organizationCode()),
                page.pageNumber(),
                page.pageCount(),
                rows);
    }

    private String renderAgencyPersonRows(RegisterPersonRow person) {
        return """
                <tr>
                    <td rowspan="2">%s<br/>%s</td>
                    <td rowspan="2">%s</td>
                    <td>前</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td rowspan="2">%s</td>
                    <td rowspan="2">%s</td>
                </tr>
                <tr>
                    <td>后</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                esc(person.name()),
                esc(person.personCode()),
                esc(person.maskedIdCard()),
                esc(person.beforeTotal()),
                esc(person.beforePosition()),
                esc(person.beforeLevel()),
                esc(person.beforePositionSalary()),
                esc(person.beforeGradeSalary()),
                esc(person.beforeTechnicalSalary()),
                esc(person.beforeBonus()),
                esc(person.beforeRetained()),
                esc(person.beforeRankAllowance()),
                esc(person.beforeRetainedReformAllowance()),
                esc(person.beforeWorkAllowance()),
                esc(person.beforePerformance()),
                esc(person.beforePositionAllowance()),
                esc(person.beforeRetainedReformSalary()),
                esc(person.beforeOtherAllowance()),
                esc(person.difference()),
                esc(person.executePeriod()),
                esc(person.afterTotal()),
                esc(person.afterPosition()),
                esc(person.afterLevel()),
                esc(person.afterPositionSalary()),
                esc(person.afterGradeSalary()),
                esc(person.afterTechnicalSalary()),
                esc(person.afterBonus()),
                esc(person.afterRetained()),
                esc(person.afterRankAllowance()),
                esc(person.afterRetainedReformAllowance()),
                esc(person.afterWorkAllowance()),
                esc(person.afterPerformance()),
                esc(person.afterPositionAllowance()),
                esc(person.afterRetainedReformSalary()),
                esc(person.afterOtherAllowance()));
    }

    private String renderJudicialPersonRows(RegisterPersonRow person) {
        return """
                <tr>
                    <td rowspan="2">%s<br/>%s</td>
                    <td rowspan="2">%s</td>
                    <td>变动前</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td rowspan="2">%s</td>
                    <td rowspan="2">%s</td>
                </tr>
                <tr>
                    <td>变动后</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                esc(person.name()),
                esc(person.personCode()),
                esc(person.maskedIdCard()),
                esc(person.beforeTotal()),
                esc(person.beforePosition()),
                esc(person.beforeLevel()),
                esc(person.beforeStep()),
                esc(person.beforePositionSalary()),
                esc(person.beforeGradeSalary()),
                esc(person.beforeRetainedReformSalary()),
                esc(person.beforeTechnicalSalary()),
                esc(person.beforeBonus()),
                esc(person.beforeRetained()),
                esc(person.beforeWorkAllowance()),
                esc(person.beforePerformance()),
                esc(person.beforeSpecialPostAllowance()),
                esc(person.beforeRankAllowance()),
                esc(person.beforeRetainedReformAllowance()),
                esc(person.difference()),
                esc(person.executePeriod()),
                esc(person.afterTotal()),
                esc(person.afterPosition()),
                esc(person.afterLevel()),
                esc(person.afterStep()),
                esc(person.afterPositionSalary()),
                esc(person.afterGradeSalary()),
                esc(person.afterRetainedReformSalary()),
                esc(person.afterTechnicalSalary()),
                esc(person.afterBonus()),
                esc(person.afterRetained()),
                esc(person.afterWorkAllowance()),
                esc(person.afterPerformance()),
                esc(person.afterSpecialPostAllowance()),
                esc(person.afterRankAllowance()),
                esc(person.afterRetainedReformAllowance()));
    }

    private String renderAgencyTotals(RegisterTotalsRow totals) {
        return """
                <tr class="register-total-row">
                    <td rowspan="2">合计</td>
                    <td rowspan="2">人数<br/>%d</td>
                    <td>前</td>
                    <td>%s</td>
                    <td colspan="2"></td>
                    <td>%s</td>
                    <td>%s</td>
                    <td></td><td></td><td>%s</td><td>%s</td><td></td>
                    <td>%s</td><td>%s</td><td></td><td>%s</td><td></td>
                    <td rowspan="2">%s</td>
                    <td rowspan="2"></td>
                </tr>
                <tr class="register-total-row">
                    <td>后</td>
                    <td>%s</td>
                    <td colspan="2"></td>
                    <td>%s</td>
                    <td>%s</td>
                    <td></td><td></td><td>%s</td><td>%s</td><td></td>
                    <td>%s</td><td>%s</td><td></td><td>%s</td><td></td>
                </tr>
                """.formatted(
                totals.personCount(),
                esc(totals.beforeTotal()),
                esc(totals.beforePositionSalary()),
                esc(totals.beforeGradeSalary()),
                esc(totals.beforeRetained()),
                esc(totals.beforeRankAllowance()),
                esc(totals.beforeWorkAllowance()),
                esc(totals.beforePerformance()),
                esc(totals.beforeRetainedReformSalary()),
                esc(totals.difference()),
                esc(totals.afterTotal()),
                esc(totals.afterPositionSalary()),
                esc(totals.afterGradeSalary()),
                esc(totals.afterRetained()),
                esc(totals.afterRankAllowance()),
                esc(totals.afterWorkAllowance()),
                esc(totals.afterPerformance()),
                esc(totals.afterRetainedReformSalary()));
    }

    private String renderJudicialTotals(RegisterTotalsRow totals) {
        return """
                <tr class="register-total-row">
                    <td rowspan="2">合计</td>
                    <td rowspan="2">人数<br/>%d</td>
                    <td>变动前</td>
                    <td>%s</td>
                    <td colspan="3"></td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td rowspan="2">%s</td>
                    <td rowspan="2"></td>
                </tr>
                <tr class="register-total-row">
                    <td>变动后</td>
                    <td>%s</td>
                    <td colspan="3"></td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                totals.personCount(),
                esc(totals.beforeTotal()),
                esc(totals.beforePositionSalary()),
                esc(totals.beforeGradeSalary()),
                esc(totals.beforeRetainedReformSalary()),
                esc(totals.beforeTechnicalSalary()),
                esc(totals.beforeBonus()),
                esc(totals.beforeRetained()),
                esc(totals.beforeWorkAllowance()),
                esc(totals.beforePerformance()),
                esc(totals.beforeSpecialPostAllowance()),
                esc(totals.beforeRankAllowance()),
                esc(totals.beforeRetainedReformAllowance()),
                esc(totals.difference()),
                esc(totals.afterTotal()),
                esc(totals.afterPositionSalary()),
                esc(totals.afterGradeSalary()),
                esc(totals.afterRetainedReformSalary()),
                esc(totals.afterTechnicalSalary()),
                esc(totals.afterBonus()),
                esc(totals.afterRetained()),
                esc(totals.afterWorkAllowance()),
                esc(totals.afterPerformance()),
                esc(totals.afterSpecialPostAllowance()),
                esc(totals.afterRankAllowance()),
                esc(totals.afterRetainedReformAllowance()));
    }

    private String esc(String value) {
        return ReportHtmlSupport.escape(value);
    }
}
