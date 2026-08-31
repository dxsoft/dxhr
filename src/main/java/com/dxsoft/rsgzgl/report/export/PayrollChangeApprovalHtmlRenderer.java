package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalRow;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalTotals;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class PayrollChangeApprovalHtmlRenderer {

    private final String stylesheet;

    PayrollChangeApprovalHtmlRenderer() {
        this.stylesheet = ReportHtmlSupport.loadClasspathText("/report/approval-print.css");
    }

    String renderDocument(List<ApprovalSheetModel> sheets) {
        StringBuilder body = new StringBuilder();
        for (ApprovalSheetModel sheet : sheets) {
            body.append(renderSheet(sheet));
        }
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                <meta charset="utf-8"/>
                <title>工资变动审批表</title>
                <style>%s</style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(stylesheet, body);
    }

    String renderPreviewBody(List<ApprovalSheetModel> sheets, boolean unauthorized) {
        StringBuilder body = new StringBuilder();
        for (ApprovalSheetModel sheet : sheets) {
            String html = renderSheet(sheet);
            body.append(unauthorized ? PrintAuthWatermark.wrap(html) : html);
        }
        return body.toString();
    }

    private String renderSheet(ApprovalSheetModel sheet) {
        if (sheet.regularizationForm() && sheet.institution()) {
            return renderInstitutionRegularizationSheet(sheet);
        }
        if (sheet.internForm() && sheet.institution()) {
            return renderInstitutionInternSheet(sheet);
        }
        if (sheet.internForm()) {
            return renderAgencyInternSheet(sheet);
        }
        if (sheet.judicialForm()) {
            return renderJudicialSheet(sheet);
        }
        return renderStandardSheet(sheet);
    }

    private String renderInstitutionRegularizationSheet(ApprovalSheetModel sheet) {
        String[][] left = {
                {"执行工资岗位等级", internRowValue(sheet, "执行工资岗位等级")},
                {"薪级", internRowValue(sheet, "薪级")},
                {"岗位工资", internRowValue(sheet, "岗位工资")},
                {"薪级工资", internRowValue(sheet, "薪级工资")},
                {"教护提高部分", internRowValue(sheet, "教护提高部分")},
                {"教护龄津贴", internRowValue(sheet, "教护龄津贴")},
                {"保留副补", internRowValue(sheet, "保留副补")},
                {"保留奖金", internRowValue(sheet, "保留奖金")},
                {"工改保留津贴", internRowValue(sheet, "工改保留津贴")}
        };
        String[][] right = {
                {"基础绩效", internRowValue(sheet, "基础绩效")},
                {"浮动工资", internRowValue(sheet, "浮动工资")},
                {"特殊岗位津贴", internRowValue(sheet, "特殊岗位津贴")},
                {"特岗保留部分", internRowValue(sheet, "特岗保留部分")},
                {"农村学校教师补贴", internRowValue(sheet, "农村学校教师补贴")},
                {"其它补贴", internRowValue(sheet, "其它补贴")},
                {"", ""},
                {"", ""},
                {"月工资合计", internTotal(sheet.totals().afterAmount())}
        };
        StringBuilder wageRows = new StringBuilder();
        for (int i = 0; i < left.length; i++) {
            if (right[i][0].isEmpty()) {
                wageRows.append("""
                        <tr>
                            <th>%s</th><td>%s</td>
                            <th></th><td></td>
                        </tr>
                        """.formatted(esc(left[i][0]), esc(left[i][1])));
            } else {
                wageRows.append("""
                        <tr>
                            <th>%s</th><td>%s</td>
                            <th>%s</th><td>%s</td>
                        </tr>
                        """.formatted(
                        esc(left[i][0]), esc(left[i][1]),
                        esc(right[i][0]), esc(right[i][1])));
            }
        }

        String highestEducation = firstNonBlank(sheet.education(), sheet.degree());
        String educationCategory = firstNonBlank(sheet.educationCategory(), "");
        String probation = formatRange(sheet.probationFrom(), sheet.probationTo());
        String apprentice = blankDash(sheet.apprenticePeriod());
        return """
                <div class="approval-sheet approval-sheet-intern-institution approval-sheet-regularization">
                    <table class="approval-topline"><tbody><tr>
                        <td>单位编码：%s</td>
                        <td>个人编码：%s</td>
                        <td>档案号：%s</td>
                    </tr></tbody></table>
                    <div class="approval-sheet-header"><h3>%s</h3></div>
                    <table class="intern-institution-table">
                        <colgroup>
                            <col style="width:10%%"/><col style="width:11%%"/>
                            <col style="width:8%%"/><col style="width:8%%"/>
                            <col style="width:12%%"/><col style="width:10%%"/>
                            <col style="width:13%%"/><col style="width:9.3%%"/>
                            <col style="width:9.4%%"/><col style="width:9.3%%"/>
                        </colgroup>
                        <tbody>
                        <tr class="reg-meta-row">
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>出生日期</th><td>%s</td>
                            <th>最高学历</th><td colspan="3">%s</td>
                        </tr>
                        <tr class="reg-meta-row">
                            <th>毕业学校</th><td colspan="2">%s</td>
                            <th>学制</th><td>%s</td>
                            <th>学历类别</th><td colspan="2">%s</td>
                            <th>毕业时间</th><td>%s</td>
                        </tr>
                        <tr class="reg-meta-row">
                            <th>参加工作<br>时间</th><td>%s</td>
                            <th>现工作<br>单位</th><td colspan="3">%s</td>
                            <th>现任职务</th><td colspan="3">%s</td>
                        </tr>
                        <tr class="reg-meta-row">
                            <th>见习期</th><td colspan="3">%s</td>
                            <th>熟练期、<br>学徒期</th><td colspan="2">%s</td>
                            <th>下次晋档起始<br>考核年度</th><td colspan="2">%s</td>
                        </tr>
                        <tr>
                            <th class="intern-section-title" colspan="10">转正定级工资待遇</th>
                        </tr>
                        <tr class="intern-wage-embed-row">
                            <td colspan="10" class="intern-wage-embed-cell">
                                <table class="intern-wage-pair-table">
                                    <colgroup>
                                        <col style="width:28%%"/><col style="width:22%%"/>
                                        <col style="width:28%%"/><col style="width:22%%"/>
                                    </colgroup>
                                    <tbody>%s</tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <th colspan="6">基础性绩效工资：奖励性绩效工资</th>
                            <td colspan="4">%s</td>
                        </tr>
                        <tr class="intern-sign-row">
                            <td colspan="10" class="approval-signature-cell">
                                <table class="approval-signature-table">
                                    <colgroup>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col-wide"/>
                                    </colgroup>
                                    <tbody><tr>
                                        <th class="approval-signature-label">单位意见</th>
                                        <td class="approval-signature-box"><strong>同 意</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">主管部门意见</th>
                                        <td class="approval-signature-box"><strong>同意单位意见</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">批准机关意见</th>
                                        <td class="approval-signature-box"><strong>同意主管部门意见</strong><p>从 %s 年 %s 月执行</p><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                    </tr></tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(blank(sheet.archiveNumber())),
                esc(sheet.reportTitle()),
                esc(blank(sheet.name())),
                esc(blank(sheet.gender())),
                esc(blank(sheet.birthDate())),
                esc(blank(highestEducation)),
                esc(blank(sheet.school())),
                esc(blank(sheet.studyYears())),
                esc(blank(educationCategory)),
                esc(blank(sheet.graduationDate())),
                esc(blank(sheet.workStartDate())),
                esc(blank(sheet.organizationName())),
                esc(blank(sheet.currentPositionName())),
                esc(probation),
                esc(apprentice),
                esc(blank(sheet.stepYear())),
                wageRows,
                esc(ratioOrDefault(sheet)),
                esc(sheet.executionYear()),
                esc(sheet.executionMonth()));
    }

    private String renderInstitutionInternSheet(ApprovalSheetModel sheet) {
        // 旧表待遇双列：左 8 项，右 6 项 + 空行 + 月工资合计
        String[][] left = {
                {"执行工资岗位等级", internRowValue(sheet, "执行工资岗位等级")},
                {"见习工资", internRowValue(sheet, "见习工资")},
                {"薪级工资", internRowValue(sheet, "薪级工资")},
                {"教护提高部分", internRowValue(sheet, "教护提高部分")},
                {"教护龄津贴", internRowValue(sheet, "教护龄津贴")},
                {"保留副补", internRowValue(sheet, "保留副补")},
                {"保留奖金", internRowValue(sheet, "保留奖金")},
                {"工改保留津贴", internRowValue(sheet, "工改保留津贴")}
        };
        String[][] right = {
                {"基础绩效", internRowValue(sheet, "基础绩效")},
                {"警衔津贴", internRowValue(sheet, "警衔津贴")},
                {"特殊岗位津贴", internRowValue(sheet, "特殊岗位津贴")},
                {"特岗保留部分", internRowValue(sheet, "特岗保留部分")},
                {"农村学校教师补贴", internRowValue(sheet, "农村学校教师补贴")},
                {"其它补贴", internRowValue(sheet, "其它补贴")},
                {"", ""},
                {"月工资合计", internTotal(sheet.totals().afterAmount())}
        };
        StringBuilder wageRows = new StringBuilder();
        for (int i = 0; i < left.length; i++) {
            if (right[i][0].isEmpty()) {
                wageRows.append("""
                        <tr>
                            <th>%s</th><td>%s</td>
                            <td colspan="2"></td>
                        </tr>
                        """.formatted(esc(left[i][0]), esc(left[i][1])));
            } else {
                wageRows.append("""
                        <tr>
                            <th>%s</th><td>%s</td>
                            <th>%s</th><td>%s</td>
                        </tr>
                        """.formatted(
                        esc(left[i][0]), esc(left[i][1]),
                        esc(right[i][0]), esc(right[i][1])));
            }
        }

        String education = firstNonBlank(sheet.education(), sheet.degree());
        String degreeCell = firstNonBlank(sheet.educationCategory(), sheet.degree());
        String probation = formatRange(sheet.probationFrom(), sheet.probationTo());
        String apprentice = formatRange("", "");
        return """
                <div class="approval-sheet approval-sheet-intern-institution">
                    <table class="approval-topline"><tbody><tr>
                        <td>单位编码：%s</td>
                        <td>个人编码：%s</td>
                        <td></td>
                    </tr></tbody></table>
                    <div class="approval-sheet-header"><h3>%s</h3></div>
                    <table class="intern-institution-table">
                        <colgroup>
                            <col style="width:11%%"/><col style="width:12%%"/>
                            <col style="width:9%%"/><col style="width:8%%"/>
                            <col style="width:9%%"/><col style="width:12%%"/>
                            <col style="width:9.75%%"/><col style="width:9.75%%"/>
                            <col style="width:9.75%%"/><col style="width:9.75%%"/>
                        </colgroup>
                        <tbody>
                        <tr>
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>出生日期</th><td>%s</td>
                            <th>学历</th><td colspan="3">%s</td>
                        </tr>
                        <tr>
                            <th>毕业学校</th><td>%s</td>
                            <th>学制</th><td>%s</td>
                            <th>学位</th><td>%s</td>
                            <th>毕业时间</th><td>%s</td>
                            <th>参加工作时间</th><td>%s</td>
                        </tr>
                        <tr>
                            <th>现工作单位</th><td colspan="4">%s</td>
                            <th>现任职务</th><td colspan="4">%s</td>
                        </tr>
                        <tr>
                            <th>见习期</th><td colspan="4">%s</td>
                            <th>熟练期、学徒期</th><td colspan="4">%s</td>
                        </tr>
                        <tr>
                            <th>下次晋档起始考核年度</th><td colspan="4">%s</td>
                            <th>下次级别晋升起始考核年度</th><td colspan="4">%s</td>
                        </tr>
                        <tr>
                            <th class="intern-section-title" colspan="10">见习期工资待遇</th>
                        </tr>
                        <tr class="intern-wage-embed-row">
                            <td colspan="10" class="intern-wage-embed-cell">
                                <table class="intern-wage-pair-table">
                                    <colgroup>
                                        <col style="width:28%%"/><col style="width:22%%"/>
                                        <col style="width:28%%"/><col style="width:22%%"/>
                                    </colgroup>
                                    <tbody>%s</tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <th colspan="6">基础性绩效工资：奖励性绩效工资</th>
                            <td colspan="4">%s</td>
                        </tr>
                        <tr class="intern-sign-row">
                            <td colspan="10" class="approval-signature-cell">
                                <table class="approval-signature-table">
                                    <colgroup>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col-wide"/>
                                    </colgroup>
                                    <tbody><tr>
                                        <th class="approval-signature-label">单位意见</th>
                                        <td class="approval-signature-box"><strong>同 意</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">主管部门意见</th>
                                        <td class="approval-signature-box"><strong>同意单位意见</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">批准机关意见</th>
                                        <td class="approval-signature-box"><strong>同意主管部门意见</strong><p>从 %s 年 %s 月执行</p><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                    </tr></tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(sheet.reportTitle()),
                esc(blank(sheet.name())),
                esc(blank(sheet.gender())),
                esc(blank(sheet.birthDate())),
                esc(blank(education)),
                esc(blank(sheet.school())),
                esc(blank(sheet.studyYears())),
                esc(blank(degreeCell)),
                esc(blank(sheet.graduationDate())),
                esc(blank(sheet.workStartDate())),
                esc(blank(sheet.organizationName())),
                esc(blank(sheet.currentPositionName())),
                esc(probation),
                esc(apprentice),
                esc(emptyYear(sheet.stepYear())),
                esc(emptyYear(sheet.levelYear())),
                wageRows,
                esc(ratioOrDefault(sheet)),
                esc(sheet.executionYear()),
                esc(sheet.executionMonth()));
    }

    private String internRowValue(ApprovalSheetModel sheet, String label) {
        for (ApprovalRow row : sheet.rows()) {
            if (label.equals(row.label())) {
                return internValue(row);
            }
        }
        return "——";
    }

    private String ratioOrDefault(ApprovalSheetModel sheet) {
        return sheet.performanceRatio() == null || sheet.performanceRatio().isBlank()
                ? "7:3"
                : sheet.performanceRatio();
    }

    private String renderAgencyInternSheet(ApprovalSheetModel sheet) {
        StringBuilder rows = new StringBuilder();
        for (ApprovalRow row : sheet.rows()) {
            String rowClass = row.highlight() ? "highlight-row" : "";
            rows.append("""
                    <tr class="%s"><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>
                    """.formatted(
                    rowClass,
                    esc(row.label()),
                    esc(row.beforeText()),
                    esc(row.afterText()),
                    esc(row.differenceText())));
        }
        ApprovalTotals totals = sheet.totals();
        String probationText = formatProbation(sheet.probationFrom(), sheet.probationTo());
        return """
                <div class="approval-sheet">
                    <table class="approval-topline"><tbody><tr>
                        <td>单位编码：<strong>%s</strong></td>
                        <td>个人编码：<strong>%s</strong></td>
                        <td></td>
                    </tr></tbody></table>
                    <div class="approval-sheet-header"><h3>%s</h3></div>
                    <table class="approval-frame-table">
                        <colgroup>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-edu-label"/>
                            <col class="approval-col-edu-value"/>
                        </colgroup>
                        <tbody>
                        <tr class="approval-meta-row">
                            <th>姓名</th><td>%s</td>
                            <th>学历</th><td>%s</td>
                            <th>出生日期</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>学历类别</th><td>%s</td>
                            <th>毕业学校</th><td colspan="3">%s</td>
                            <th>学制</th><td>%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>毕业时间</th><td>%s</td>
                            <th>参加工作时间</th><td>%s</td>
                            <th>现工作单位</th><td colspan="3">%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>现任职务</th><td colspan="3">%s</td>
                            <th>见习期</th><td>%s</td>
                            <th>熟练期、学徒期</th><td>%s</td>
                        </tr>
                        <tr class="approval-body-row">
                            <td colspan="6" class="approval-body-main approval-frame-cell">
                                <table class="approval-component-table">
                                    <colgroup>
                                        <col class="approval-col-item"/>
                                        <col class="approval-col-amount"/>
                                        <col class="approval-col-amount"/>
                                        <col class="approval-col-diff"/>
                                    </colgroup>
                                    <thead><tr><th class="approval-item-header">项目</th><th>变动前</th><th>变动后</th><th>增资额</th></tr></thead>
                                    <tbody>%s</tbody>
                                    <tfoot>
                                        <tr><th>月工资合计</th><th>%s</th><th>%s</th><th>%s</th></tr>
                                    </tfoot>
                                </table>
                            </td>
                            <td colspan="2" class="approval-body-side approval-frame-cell">
                                <div class="approval-basis-panel">
                                    <table><tbody>
                                        <tr><th>下次晋档<br/>起始考核年度</th><td>%s</td></tr>
                                        <tr><th>下次级别晋升<br/>起始考核年度</th><td>%s</td></tr>
                                    </tbody></table>
                                    <div class="approval-basis-text">
                                        <p>工资变动原因及依据</p>
                                        <strong>%s</strong>
                                        <p>执行时间：</p>
                                        <strong>%s</strong>
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="8" class="approval-frame-cell approval-signature-cell">
                                <table class="approval-signature-table">
                                    <colgroup>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col-wide"/>
                                    </colgroup>
                                    <tbody><tr>
                                        <th class="approval-signature-label">单位意见</th>
                                        <td class="approval-signature-box"><strong>同 意</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">主管部门意见</th>
                                        <td class="approval-signature-box"><strong>同意单位意见</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">批准机关意见</th>
                                        <td class="approval-signature-box"><strong>同意主管部门意见</strong><p>从 %s 年 %s 月执行</p><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                    </tr></tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(sheet.reportTitle()),
                esc(sheet.name()),
                esc(blankDash(sheet.education())),
                esc(sheet.birthDate()),
                esc(sheet.gender()),
                esc(blankDash(sheet.educationCategory())),
                esc(blankDash(sheet.school())),
                esc(blankDash(sheet.studyYears())),
                esc(blankDash(sheet.graduationDate())),
                esc(blankDash(sheet.workStartDate())),
                esc(sheet.organizationName()),
                esc(blankDash(sheet.currentPositionName())),
                esc(probationText),
                esc(blankDash(sheet.apprenticePeriod())),
                rows,
                esc(ReportFormatSupport.formatMoney(totals.beforeAmount())),
                esc(ReportFormatSupport.formatMoney(totals.afterAmount())),
                esc(ReportFormatSupport.formatMoney(totals.difference())),
                esc(sheet.stepYear()),
                esc(sheet.levelYear()),
                esc(sheet.basisTitle()),
                esc(sheet.executionPeriod()),
                esc(sheet.executionYear()),
                esc(sheet.executionMonth()));
    }

    private String renderJudicialSheet(ApprovalSheetModel sheet) {
        StringBuilder rows = new StringBuilder();
        List<ApprovalRow> approvalRows = sheet.rows();
        for (int index = 0; index < approvalRows.size(); index++) {
            ApprovalRow row = approvalRows.get(index);
            String rowClass = row.highlight() ? "highlight-row" : "";
            String groupLabel = row.groupLabel();
            if (groupLabel != null && !groupLabel.isBlank()) {
                int span = 1;
                for (int look = index + 1; look < approvalRows.size(); look++) {
                    if (!groupLabel.equals(approvalRows.get(look).groupLabel())) {
                        break;
                    }
                    span++;
                }
                boolean groupStart = index == 0
                        || !groupLabel.equals(approvalRows.get(index - 1).groupLabel());
                if (groupStart) {
                    rows.append("""
                            <tr class="%s">
                                <td class="approval-group-label" rowspan="%d">%s</td>
                                <td class="approval-subitem">%s</td>
                                <td>%s</td>
                                <td>%s</td>
                                <td>%s</td>
                            </tr>
                            """.formatted(
                            rowClass,
                            span,
                            esc(groupLabel),
                            esc(row.label()),
                            esc(row.beforeText()),
                            esc(row.afterText()),
                            esc(row.differenceText())));
                } else {
                    rows.append("""
                            <tr class="%s">
                                <td class="approval-subitem">%s</td>
                                <td>%s</td>
                                <td>%s</td>
                                <td>%s</td>
                            </tr>
                            """.formatted(
                            rowClass,
                            esc(row.label()),
                            esc(row.beforeText()),
                            esc(row.afterText()),
                            esc(row.differenceText())));
                }
            } else {
                rows.append("""
                        <tr class="%s">
                            <td colspan="2">%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                        </tr>
                        """.formatted(
                        rowClass,
                        esc(row.label()),
                        esc(row.beforeText()),
                        esc(row.afterText()),
                        esc(row.differenceText())));
            }
        }
        ApprovalTotals totals = sheet.totals();
        StringBuilder basisLines = new StringBuilder();
        for (String line : sheet.basisDetailLines()) {
            basisLines.append("<p>").append(esc(line)).append("</p>");
        }
        return """
                <div class="approval-sheet approval-sheet-judicial">
                    <table class="approval-topline approval-topline-judicial"><tbody><tr>
                        <td>个人编码：<strong>%s</strong></td>
                        <td></td>
                        <td>档案号：<strong>%s</strong></td>
                    </tr></tbody></table>
                    <div class="approval-sheet-header approval-sheet-header-judicial">
                        <h3>%s</h3>
                    </div>
                    <table class="approval-frame-table approval-frame-table-judicial">
                        <colgroup>
                            <col class="approval-col-label approval-col-name-label"/>
                            <col class="approval-col-value approval-col-name-value"/>
                            <col class="approval-col-label approval-col-gender-label"/>
                            <col class="approval-col-value approval-col-gender-value"/>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-edu-label"/>
                            <col class="approval-col-edu-value"/>
                        </colgroup>
                        <tbody>
                        <tr class="approval-meta-row">
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>出生日期</th><td>%s</td>
                            <th>学历</th><td class="approval-edu-value">%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>工作单位</th><td colspan="3">%s</td>
                            <th>参加工作时间</th><td>%s</td>
                            <th>工作年限</th><td>%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>现任职务层次</th><td colspan="5">%s</td>
                            <th>任职时间</th><td>%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>现任法律职务</th><td colspan="5">%s</td>
                            <th>任法律职务时间</th><td>%s</td>
                        </tr>
                        <tr class="approval-body-row">
                            <td colspan="6" class="approval-body-main approval-frame-cell">
                                <table class="approval-component-table approval-component-table-judicial">
                                    <colgroup>
                                        <col class="approval-col-group"/>
                                        <col class="approval-col-item"/>
                                        <col class="approval-col-amount"/>
                                        <col class="approval-col-amount"/>
                                        <col class="approval-col-diff"/>
                                    </colgroup>
                                    <thead>
                                        <tr>
                                            <th class="approval-item-header" colspan="2" rowspan="2">项目</th>
                                            <th colspan="2">变动情况</th>
                                            <th rowspan="2">增资额</th>
                                        </tr>
                                        <tr>
                                            <th>套改前</th>
                                            <th>套改后</th>
                                        </tr>
                                    </thead>
                                    <tbody>%s</tbody>
                                    <tfoot>
                                        <tr>
                                            <th colspan="2">月工资合计</th>
                                            <th>%s</th>
                                            <th>%s</th>
                                            <th>%s</th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </td>
                            <td colspan="2" class="approval-body-side approval-frame-cell">
                                <div class="approval-basis-panel">
                                    <table><tbody>
                                        <tr><th>下一次晋档<br/>起始年度</th><td>%s</td></tr>
                                        <tr><th>下一次晋级<br/>起始年度</th><td>%s</td></tr>
                                    </tbody></table>
                                    <div class="approval-basis-text">
                                        <p>工资变动原因及依据</p>
                                        <strong>%s</strong>
                                        <p>执行时间：</p>
                                        <strong>%s</strong>
                                        %s
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="8" class="approval-frame-cell approval-signature-cell">
                                <table class="approval-signature-table">
                                    <colgroup>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col-wide"/>
                                    </colgroup>
                                    <tbody><tr>
                                        <th class="approval-signature-label">单位意见</th>
                                        <td class="approval-signature-box"><strong>同 意</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">主管部门意见</th>
                                        <td class="approval-signature-box"><strong>同意单位意见</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">批准机关意见</th>
                                        <td class="approval-signature-box"><strong>同意主管部门意见</strong><p>从 %s 年 %s 月执行</p><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                    </tr></tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                esc(sheet.personCode()),
                esc(sheet.archiveNumber()),
                esc(sheet.reportTitle()),
                esc(sheet.name()),
                esc(sheet.gender()),
                esc(sheet.birthDate()),
                esc(sheet.education()),
                esc(sheet.organizationName()),
                esc(sheet.workStartDate()),
                esc(sheet.workYears()),
                esc(sheet.currentPositionName()),
                esc(sheet.positionStartDate()),
                esc(emptyOrText(sheet.legalPositionName())),
                esc(emptyOrText(sheet.legalPositionStartDate())),
                rows,
                esc(ReportFormatSupport.formatMoney(totals.beforeAmount())),
                esc(ReportFormatSupport.formatMoney(totals.afterAmount())),
                esc(ReportFormatSupport.formatMoney(totals.difference())),
                esc(sheet.stepYear()),
                esc(sheet.levelYear()),
                esc(sheet.basisTitle()),
                esc(sheet.executionPeriod()),
                basisLines,
                esc(sheet.executionYear()),
                esc(sheet.executionMonth()));
    }

    private String renderStandardSheet(ApprovalSheetModel sheet) {
        StringBuilder rows = new StringBuilder();
        for (ApprovalRow row : sheet.rows()) {
            String rowClass = row.highlight() ? "highlight-row" : "";
            rows.append("""
                    <tr class="%s"><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>
                    """.formatted(
                    rowClass,
                    esc(row.label()),
                    esc(row.beforeText()),
                    esc(row.afterText()),
                    esc(row.differenceText())));
        }
        ApprovalTotals totals = sheet.totals();
        String ratioRow = sheet.institution()
                ? """
                <tr><th colspan="2">基础性绩效工资与奖励性绩效工资比例</th><th colspan="2">%s</th></tr>
                """.formatted(esc(
                        sheet.performanceRatio() == null || sheet.performanceRatio().isBlank()
                                ? "7:3"
                                : sheet.performanceRatio()))
                : "";
        StringBuilder basisLines = new StringBuilder();
        for (String line : sheet.basisDetailLines()) {
            basisLines.append("<p>").append(esc(line)).append("</p>");
        }
        String yearRows = sheet.institution()
                ? """
                <tr><th>下次薪级晋升<br/>起始考核年度</th><td>%s</td></tr>
                """.formatted(esc(sheet.stepYear()))
                : """
                <tr><th>下次档次晋升<br/>起始考核年度</th><td>%s</td></tr>
                <tr><th>下次级别晋升<br/>起始考核年度</th><td>%s</td></tr>
                """.formatted(esc(sheet.stepYear()), esc(sheet.levelYear()));
        return """
                <div class="approval-sheet">
                    <table class="approval-topline"><tbody><tr>
                        <td>单位编码：<strong>%s</strong></td>
                        <td>个人编码：<strong>%s</strong></td>
                        <td>档案号：<strong>%s</strong></td>
                    </tr></tbody></table>
                    <div class="approval-sheet-header"><h3>%s</h3></div>
                    <table class="approval-frame-table">
                        <colgroup>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-label"/>
                            <col class="approval-col-value"/>
                            <col class="approval-col-edu-label"/>
                            <col class="approval-col-edu-value"/>
                        </colgroup>
                        <tbody>
                        <tr class="approval-meta-row">
                            <th>姓名</th><td>%s</td>
                            <th>性别</th><td>%s</td>
                            <th>出生日期</th><td>%s</td>
                            <th>学历</th><td class="approval-edu-value">%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>工作单位</th><td colspan="3">%s</td>
                            <th>参加工作时间</th><td>%s</td>
                            <th>工作年限</th><td>%s</td>
                        </tr>
                        <tr class="approval-meta-row">
                            <th>现任职务</th><td colspan="5">%s</td>
                            <th>任职时间</th><td>%s</td>
                        </tr>
                        <tr class="approval-body-row">
                            <td colspan="6" class="approval-body-main approval-frame-cell">
                                <table class="approval-component-table">
                                    <colgroup>
                                        <col class="approval-col-item"/>
                                        <col class="approval-col-amount"/>
                                        <col class="approval-col-amount"/>
                                        <col class="approval-col-diff"/>
                                    </colgroup>
                                    <thead><tr><th class="approval-item-header">项目</th><th>变动前</th><th>变动后</th><th>增资额</th></tr></thead>
                                    <tbody>%s</tbody>
                                    <tfoot>
                                        <tr><th>月工资合计</th><th>%s</th><th>%s</th><th>%s</th></tr>
                                        %s
                                    </tfoot>
                                </table>
                            </td>
                            <td colspan="2" class="approval-body-side approval-frame-cell">
                                <div class="approval-basis-panel">
                                    <table><tbody>%s</tbody></table>
                                    <div class="approval-basis-text">
                                        <p>工资变动原因及依据</p>
                                        <strong>%s</strong>
                                        <p>执行时间：</p>
                                        <strong>%s</strong>
                                        %s
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="8" class="approval-frame-cell approval-signature-cell">
                                <table class="approval-signature-table">
                                    <colgroup>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col"/>
                                        <col class="approval-sig-label-col"/>
                                        <col class="approval-sig-box-col-wide"/>
                                    </colgroup>
                                    <tbody><tr>
                                        <th class="approval-signature-label">单位意见</th>
                                        <td class="approval-signature-box"><strong>同 意</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">主管部门意见</th>
                                        <td class="approval-signature-box"><strong>同意单位意见</strong><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                        <th class="approval-signature-label">批准机关意见</th>
                                        <td class="approval-signature-box"><strong>同意主管部门意见</strong><p>从 %s 年 %s 月执行</p><span>年&#160;&#160;&#160;&#160;月&#160;&#160;&#160;&#160;日</span></td>
                                    </tr></tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                """.formatted(
                esc(sheet.organizationCode()),
                esc(sheet.personCode()),
                esc(sheet.archiveNumber()),
                esc(sheet.reportTitle()),
                esc(sheet.name()),
                esc(sheet.gender()),
                esc(sheet.birthDate()),
                esc(sheet.education()),
                esc(sheet.organizationName()),
                esc(sheet.workStartDate()),
                esc(sheet.workYears()),
                esc(sheet.currentPositionName()),
                esc(sheet.positionStartDate()),
                rows,
                esc(ReportFormatSupport.formatMoney(totals.beforeAmount())),
                esc(ReportFormatSupport.formatMoney(totals.afterAmount())),
                esc(ReportFormatSupport.formatMoney(totals.difference())),
                ratioRow,
                yearRows,
                esc(sheet.basisTitle()),
                esc(sheet.executionPeriod()),
                basisLines,
                esc(sheet.executionYear()),
                esc(sheet.executionMonth()));
    }

    private String formatProbation(String from, String to) {
        return formatRange(from, to);
    }

    private String formatRange(String from, String to) {
        String start = from == null ? "" : from.trim();
        String end = to == null ? "" : to.trim();
        if ("-".equals(start) || "——".equals(start)) {
            start = "";
        }
        if ("-".equals(end) || "——".equals(end)) {
            end = "";
        }
        if (start.isEmpty() && end.isEmpty()) {
            return "至";
        }
        if (end.isEmpty()) {
            return start + " 至";
        }
        if (start.isEmpty()) {
            return "至 " + end;
        }
        return start + " 至 " + end;
    }

    private String internValue(ApprovalRow row) {
        String value = row.afterText();
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "0".equals(value.trim())
                || "——".equals(value.trim())) {
            return "——";
        }
        // 去掉千分位，与旧表一致
        String normalized = value.trim().replace(",", "");
        if ("0".equals(normalized)) {
            return "——";
        }
        return normalized;
    }

    private String internTotal(java.math.BigDecimal value) {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "——";
        }
        return new java.text.DecimalFormat("#0").format(value);
    }

    private String emptyYear(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "——".equals(value.trim())) {
            return "";
        }
        return value.trim();
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String primary, String secondary) {
        if (primary != null && !primary.isBlank() && !"-".equals(primary.trim())) {
            return primary.trim();
        }
        if (secondary != null && !secondary.isBlank() && !"-".equals(secondary.trim())) {
            return secondary.trim();
        }
        return "";
    }

    private String emptyOrText(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "——".equals(value.trim())) {
            return "";
        }
        return value.trim();
    }

    private String blankDash(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return "——";
        }
        return value.trim();
    }

    private String esc(String value) {
        return ReportHtmlSupport.escape(value);
    }
}
