package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalRow;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterPageModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterPersonRow;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterTotalsRow;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
class PayrollChangeExcelExportService {

    private static final int REGISTER_SXSSF_ROW_WINDOW = 200;

    byte[] exportApprovals(List<ApprovalSheetModel> sheets) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle titleStyle = titleStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle textStyle = textStyle(workbook);
            CellStyle centerStyle = centerStyle(workbook);
            for (int index = 0; index < sheets.size(); index++) {
                ApprovalSheetModel sheet = sheets.get(index);
                String sheetName = safeSheetName(sheet.name() + "-" + sheet.personCode(), index + 1);
                Sheet page = workbook.createSheet(sheetName);
                setApprovalColumnWidths(page);
                int rowIndex = 0;

                Row topLine = page.createRow(rowIndex++);
                writeCell(topLine, 0, "单位编码：" + sheet.organizationCode(), textStyle);
                page.addMergedRegion(new CellRangeAddress(topLine.getRowNum(), topLine.getRowNum(), 0, 2));
                writeCell(topLine, 3, "个人编码：" + sheet.personCode(), textStyle);
                page.addMergedRegion(new CellRangeAddress(topLine.getRowNum(), topLine.getRowNum(), 3, 4));
                writeCell(topLine, 5, "档案号：" + sheet.archiveNumber(), textStyle);
                page.addMergedRegion(new CellRangeAddress(topLine.getRowNum(), topLine.getRowNum(), 5, 7));

                Row titleRow = page.createRow(rowIndex++);
                titleRow.setHeightInPoints(24);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(sheet.reportTitle());
                titleCell.setCellStyle(titleStyle);
                page.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 7));

                Row meta1 = page.createRow(rowIndex++);
                writeCell(meta1, 0, "姓名", headerStyle);
                writeCell(meta1, 1, sheet.name(), centerStyle);
                writeCell(meta1, 2, "性别", headerStyle);
                writeCell(meta1, 3, sheet.gender(), centerStyle);
                writeCell(meta1, 4, "出生日期", headerStyle);
                writeCell(meta1, 5, sheet.birthDate(), centerStyle);
                writeCell(meta1, 6, "学历", headerStyle);
                writeCell(meta1, 7, sheet.education(), centerStyle);

                Row meta2 = page.createRow(rowIndex++);
                writeCell(meta2, 0, "工作单位", headerStyle);
                writeCell(meta2, 1, sheet.organizationName(), centerStyle);
                page.addMergedRegion(new CellRangeAddress(meta2.getRowNum(), meta2.getRowNum(), 1, 3));
                writeCell(meta2, 4, "参加工作时间", headerStyle);
                writeCell(meta2, 5, sheet.workStartDate(), centerStyle);
                writeCell(meta2, 6, "工作年限", headerStyle);
                writeCell(meta2, 7, sheet.workYears(), centerStyle);

                Row meta3 = page.createRow(rowIndex++);
                writeCell(meta3, 0, "现任职务", headerStyle);
                writeCell(meta3, 1, sheet.currentPositionName(), centerStyle);
                page.addMergedRegion(new CellRangeAddress(meta3.getRowNum(), meta3.getRowNum(), 1, 3));
                writeCell(meta3, 4, "任职时间", headerStyle);
                writeCell(meta3, 5, sheet.positionStartDate(), centerStyle);
                writeCell(meta3, 6, "前次变动", headerStyle);
                writeCell(meta3, 7, sheet.previousChangeText(), centerStyle);

                Row tableHeader = page.createRow(rowIndex++);
                writeCell(tableHeader, 0, "项目", headerStyle);
                writeCell(tableHeader, 1, "变动前", headerStyle);
                writeCell(tableHeader, 2, "变动后", headerStyle);
                writeCell(tableHeader, 3, "增资额", headerStyle);
                writeCell(tableHeader, 4, "工资变动原因及依据", headerStyle);
                page.addMergedRegion(new CellRangeAddress(tableHeader.getRowNum(), tableHeader.getRowNum(), 4, 7));

                int bodyStart = rowIndex;
                for (ApprovalRow row : sheet.rows()) {
                    Row dataRow = page.createRow(rowIndex++);
                    writeCell(dataRow, 0, row.label(), textStyle);
                    writeCell(dataRow, 1, row.beforeText(), centerStyle);
                    writeCell(dataRow, 2, row.afterText(), centerStyle);
                    writeCell(dataRow, 3, row.differenceText(), centerStyle);
                }
                int bodyEnd = rowIndex - 1;

                Row totalRow = page.createRow(rowIndex++);
                writeCell(totalRow, 0, "月工资合计", headerStyle);
                writeCell(totalRow, 1, ReportFormatSupport.formatMoney(sheet.totals().beforeAmount()), centerStyle);
                writeCell(totalRow, 2, ReportFormatSupport.formatMoney(sheet.totals().afterAmount()), centerStyle);
                writeCell(totalRow, 3, ReportFormatSupport.formatMoney(sheet.totals().difference()), centerStyle);

                if (sheet.institution()) {
                    Row ratioRow = page.createRow(rowIndex++);
                    writeCell(ratioRow, 0, "基础性绩效工资与奖励性绩效工资比例", headerStyle);
                    page.addMergedRegion(new CellRangeAddress(ratioRow.getRowNum(), ratioRow.getRowNum(), 0, 1));
                    String ratio = sheet.performanceRatio() == null || sheet.performanceRatio().isBlank()
                            ? "7:3"
                            : sheet.performanceRatio();
                    writeCell(ratioRow, 2, ratio, centerStyle);
                    page.addMergedRegion(new CellRangeAddress(ratioRow.getRowNum(), ratioRow.getRowNum(), 2, 3));
                    bodyEnd = ratioRow.getRowNum();
                }

                if (bodyEnd >= bodyStart) {
                    page.addMergedRegion(new CellRangeAddress(bodyStart, bodyEnd, 4, 7));
                    Row firstBody = page.getRow(bodyStart);
                    if (firstBody == null) {
                        firstBody = page.createRow(bodyStart);
                    }
                    Cell basisCell = firstBody.getCell(4);
                    if (basisCell == null) {
                        basisCell = firstBody.createCell(4);
                    }
                    basisCell.setCellValue(buildBasisText(sheet));
                    basisCell.setCellStyle(textStyle);
                }

                Row signRow = page.createRow(rowIndex++);
                writeCell(signRow, 0, "单位意见", headerStyle);
                writeCell(signRow, 1, "同意", centerStyle);
                writeCell(signRow, 2, "主管部门意见", headerStyle);
                writeCell(signRow, 3, "同意单位意见", centerStyle);
                writeCell(signRow, 4, "批准机关意见", headerStyle);
                writeCell(signRow, 5, "同意主管部门意见；从 " + sheet.executionYear() + " 年 " + sheet.executionMonth() + " 月执行", textStyle);
                page.addMergedRegion(new CellRangeAddress(signRow.getRowNum(), signRow.getRowNum(), 5, 7));
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("生成审批表 Excel 失败", exception);
        }
    }

    private void setApprovalColumnWidths(Sheet page) {
        int[] widths = {9, 18, 9, 18, 9, 18, 9, 10};
        for (int column = 0; column < widths.length; column++) {
            page.setColumnWidth(column, widths[column] * 256);
        }
    }

    private String buildBasisText(ApprovalSheetModel sheet) {
        StringBuilder basis = new StringBuilder();
        if (sheet.institution()) {
            basis.append("下次薪级晋升起始考核年度：").append(sheet.stepYear()).append('\n');
        } else {
            basis.append("下次档次晋升起始考核年度：").append(sheet.stepYear()).append('\n');
            basis.append("下次级别晋升起始考核年度：").append(sheet.levelYear()).append('\n');
        }
        basis.append("工资变动原因及依据：").append(sheet.basisTitle()).append('\n');
        basis.append("执行时间：").append(sheet.executionPeriod()).append('\n');
        for (String line : sheet.basisDetailLines()) {
            basis.append(line).append('\n');
        }
        return basis.toString().trim();
    }

    byte[] exportRegister(List<RegisterPageModel> pages) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(REGISTER_SXSSF_ROW_WINDOW);
        workbook.setCompressTempFiles(true);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle titleStyle = titleStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle textStyle = textStyle(workbook);
            for (RegisterPageModel pageModel : pages) {
                Sheet sheet = workbook.createSheet("第" + pageModel.pageNumber() + "页");
                int rowIndex = 0;
                Row title = sheet.createRow(rowIndex++);
                title.createCell(0).setCellValue(pageModel.reportTitle());
                title.getCell(0).setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 20));
                Row subtitle = sheet.createRow(rowIndex++);
                subtitle.createCell(0).setCellValue(
                        "单位名称：" + pageModel.organizationName() + "[单位编码：" + pageModel.organizationCode() + "]"
                                + "    第 " + pageModel.pageNumber() + " 页 共 " + pageModel.pageCount() + " 页");
                subtitle.getCell(0).setCellStyle(textStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 20));
                String[] headers = {
                        "姓名", "人员编码", "身份证号", "变动前后", "月工资合计",
                        pageModel.labels().position(), pageModel.labels().level(),
                        pageModel.labels().positionSalary(), pageModel.labels().gradeSalary(),
                        "技术等级工资", "保留奖金", "保留福补", "警衔津贴", "工改保留津贴",
                        "工作性津贴", "生活性补贴", "岗位津贴", "工改保留工资", "其它补贴", "农村学校教师补贴",
                        "月增减", "执行时间"
                };
                Row headerRow = sheet.createRow(rowIndex++);
                for (int column = 0; column < headers.length; column++) {
                    writeCell(headerRow, column, headers[column], headerStyle);
                }
                for (RegisterPersonRow person : pageModel.people()) {
                    rowIndex = writeRegisterPerson(sheet, rowIndex, person, textStyle);
                }
                rowIndex = writeRegisterTotals(sheet, rowIndex, pageModel.totals(), headerStyle, textStyle);
                for (int column = 0; column < headers.length; column++) {
                    sheet.setColumnWidth(column, 12 * 256);
                }
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("生成花名册 Excel 失败", exception);
        } finally {
            workbook.dispose();
        }
    }

    private int writeRegisterPerson(Sheet sheet, int rowIndex, RegisterPersonRow person, CellStyle textStyle) {
        Row before = sheet.createRow(rowIndex++);
        int column = 0;
        writeCell(before, column++, person.name(), textStyle);
        writeCell(before, column++, person.personCode(), textStyle);
        writeCell(before, column++, person.maskedIdCard(), textStyle);
        writeCell(before, column++, "前", textStyle);
        writeCell(before, column++, person.beforeTotal(), textStyle);
        writeCell(before, column++, person.beforePosition(), textStyle);
        writeCell(before, column++, person.beforeLevel(), textStyle);
        writeCell(before, column++, person.beforePositionSalary(), textStyle);
        writeCell(before, column++, person.beforeGradeSalary(), textStyle);
        writeCell(before, column++, person.beforeTechnicalSalary(), textStyle);
        writeCell(before, column++, person.beforeBonus(), textStyle);
        writeCell(before, column++, person.beforeRetained(), textStyle);
        writeCell(before, column++, person.beforeRankAllowance(), textStyle);
        writeCell(before, column++, person.beforeRetainedReformAllowance(), textStyle);
        writeCell(before, column++, person.beforeWorkAllowance(), textStyle);
        writeCell(before, column++, person.beforePerformance(), textStyle);
        writeCell(before, column++, person.beforePositionAllowance(), textStyle);
        writeCell(before, column++, person.beforeRetainedReformSalary(), textStyle);
        writeCell(before, column++, person.beforeOtherAllowance(), textStyle);
        writeCell(before, column++, person.beforeRuralTeacher(), textStyle);
        writeCell(before, column++, "", textStyle);
        writeCell(before, column, "", textStyle);

        Row after = sheet.createRow(rowIndex++);
        column = 0;
        writeCell(after, column++, "", textStyle);
        writeCell(after, column++, "", textStyle);
        writeCell(after, column++, "", textStyle);
        writeCell(after, column++, "后", textStyle);
        writeCell(after, column++, person.afterTotal(), textStyle);
        writeCell(after, column++, person.afterPosition(), textStyle);
        writeCell(after, column++, person.afterLevel(), textStyle);
        writeCell(after, column++, person.afterPositionSalary(), textStyle);
        writeCell(after, column++, person.afterGradeSalary(), textStyle);
        writeCell(after, column++, person.afterTechnicalSalary(), textStyle);
        writeCell(after, column++, person.afterBonus(), textStyle);
        writeCell(after, column++, person.afterRetained(), textStyle);
        writeCell(after, column++, person.afterRankAllowance(), textStyle);
        writeCell(after, column++, person.afterRetainedReformAllowance(), textStyle);
        writeCell(after, column++, person.afterWorkAllowance(), textStyle);
        writeCell(after, column++, person.afterPerformance(), textStyle);
        writeCell(after, column++, person.afterPositionAllowance(), textStyle);
        writeCell(after, column++, person.afterRetainedReformSalary(), textStyle);
        writeCell(after, column++, person.afterOtherAllowance(), textStyle);
        writeCell(after, column++, person.afterRuralTeacher(), textStyle);
        writeCell(after, column++, person.difference(), textStyle);
        writeCell(after, column, person.executePeriod(), textStyle);
        return rowIndex;
    }

    private int writeRegisterTotals(
            Sheet sheet,
            int rowIndex,
            RegisterTotalsRow totals,
            CellStyle headerStyle,
            CellStyle textStyle) {
        Row before = sheet.createRow(rowIndex++);
        writeCell(before, 0, "合计", headerStyle);
        writeCell(before, 1, String.valueOf(totals.personCount()), textStyle);
        writeCell(before, 3, "前", headerStyle);
        writeCell(before, 4, totals.beforeTotal(), textStyle);
        writeCell(before, 7, totals.beforePositionSalary(), textStyle);
        writeCell(before, 8, totals.beforeGradeSalary(), textStyle);
        writeCell(before, 11, totals.beforeRetained(), textStyle);
        writeCell(before, 12, totals.beforeRankAllowance(), textStyle);
        writeCell(before, 14, totals.beforeWorkAllowance(), textStyle);
        writeCell(before, 15, totals.beforePerformance(), textStyle);
        writeCell(before, 17, totals.beforeRetainedReformSalary(), textStyle);
        writeCell(before, 19, totals.beforeRuralTeacher(), textStyle);
        Row after = sheet.createRow(rowIndex);
        writeCell(after, 3, "后", headerStyle);
        writeCell(after, 4, totals.afterTotal(), textStyle);
        writeCell(after, 7, totals.afterPositionSalary(), textStyle);
        writeCell(after, 8, totals.afterGradeSalary(), textStyle);
        writeCell(after, 11, totals.afterRetained(), textStyle);
        writeCell(after, 12, totals.afterRankAllowance(), textStyle);
        writeCell(after, 14, totals.afterWorkAllowance(), textStyle);
        writeCell(after, 15, totals.afterPerformance(), textStyle);
        writeCell(after, 17, totals.afterRetainedReformSalary(), textStyle);
        writeCell(after, 19, totals.afterRuralTeacher(), textStyle);
        writeCell(after, 20, totals.difference(), textStyle);
        return rowIndex + 1;
    }

    private void writeCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private CellStyle titleStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle textStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        return style;
    }

    private CellStyle centerStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle borderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private String safeSheetName(String base, int index) {
        String name = base == null || base.isBlank() ? "审批表" + index : base;
        name = name.replaceAll("[\\\\/?*\\[\\]:]", "_");
        if (name.length() > 31) {
            name = name.substring(0, 28) + index;
        }
        return name;
    }
}
