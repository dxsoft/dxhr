package com.dxsoft.rsgzgl.payroll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
class PayrollProjectionAuditExportService {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final PayrollService payrollService;

    PayrollProjectionAuditExportService(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    ResponseEntity<byte[]> downloadCsvZip(String organizationCode, boolean mismatchesOnly) {
        return downloadCsvZip(organizationCode, null, mismatchesOnly);
    }

    ResponseEntity<byte[]> downloadCsvZip(String organizationCode, String keyword, boolean mismatchesOnly) {
        PayrollProjectionAuditExportData data = payrollService.buildProjectionAuditExport(organizationCode, keyword, mismatchesOnly);
        byte[] bytes = buildCsvZip(data, mismatchesOnly);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", fileName("projection_audit", "zip"));
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    ResponseEntity<byte[]> downloadExcel(String organizationCode, boolean mismatchesOnly) {
        return downloadExcel(organizationCode, null, mismatchesOnly);
    }

    ResponseEntity<byte[]> downloadExcel(String organizationCode, String keyword, boolean mismatchesOnly) {
        PayrollProjectionAuditExportData data = payrollService.buildProjectionAuditExport(organizationCode, keyword, mismatchesOnly);
        byte[] bytes = buildExcel(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", fileName("projection_audit", "xlsx"));
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private byte[] buildCsvZip(PayrollProjectionAuditExportData data, boolean mismatchesOnly) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(output)) {
            writeZipEntry(zip, "人员汇总.csv", buildPersonSummaryCsv(data.personSummaries()));
            writeZipEntry(zip, "历次调资明细.csv", buildHistoryDetailCsv(data.historyDetails()));
            writeZipEntry(zip, "历次调资差异明细.csv", buildHistoryDetailCsv(mismatchRows(data.historyDetails())));
            writeZipEntry(zip, "导出说明.txt", buildReadme(data, mismatchesOnly).getBytes(StandardCharsets.UTF_8));
            zip.finish();
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate projection audit CSV export", e);
        }
    }

    private void writeZipEntry(ZipOutputStream zip, String entryName, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content);
        zip.closeEntry();
    }

    private byte[] buildPersonSummaryCsv(List<PayrollProjectionPersonAudit> summaries) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(
                        "UID", "单位编码", "人员编码", "姓名", "当前年月", "可推算", "说明",
                        "调资合计", "推算合计", "当前差额", "当前一致",
                        "调资条数", "差异条数")
                .build();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (PayrollProjectionPersonAudit item : summaries) {
                printer.printRecord(
                        item.uid(),
                        item.organizationCode(),
                        item.personCode(),
                        item.name(),
                        item.latestPeriod(),
                        yesNo(item.latestProjectionEligible()),
                        item.latestNote(),
                        item.storedTotal(),
                        decimal(item.projectedTotal()),
                        decimal(item.latestTotalDifference()),
                        yesNo(item.latestMatched()),
                        item.historyRecordCount(),
                        item.historyMismatchCount());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate person summary CSV", e);
        }
        return withUtf8Bom(writer.toString());
    }

    private byte[] buildHistoryDetailCsv(List<PayrollProjectionAuditDetailRow> details) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(
                        "UID", "单位编码", "人员编码", "姓名", "调资ID", "年月", "变动类别",
                        "可推算", "说明", "一致", "调资合计", "推算合计", "差额", "结构差异", "金额差异")
                .build();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (PayrollProjectionAuditDetailRow item : details) {
                printer.printRecord(
                        item.uid(),
                        item.organizationCode(),
                        item.personCode(),
                        item.name(),
                        item.historyId(),
                        item.calculationPeriod(),
                        item.changeType(),
                        yesNo(item.projectionEligible()),
                        item.note(),
                        yesNo(item.matched()),
                        item.storedTotal(),
                        decimal(item.projectedTotal()),
                        decimal(item.totalDifference()),
                        item.structureMismatches(),
                        item.componentDifferences());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate history detail CSV", e);
        }
        return withUtf8Bom(writer.toString());
    }

    private List<PayrollProjectionAuditDetailRow> mismatchRows(List<PayrollProjectionAuditDetailRow> details) {
        return details.stream()
                .filter(row -> !Boolean.TRUE.equals(row.matched()))
                .toList();
    }

    private String buildReadme(PayrollProjectionAuditExportData data, boolean mismatchesOnly) {
        return """
                工资推算对账全库导出
                导出时间：%s
                明细范围：%s
                有调资人员：%d
                本次比较：%d
                当前工资差异人数：%d
                历次调资差异人数：%d
                历次调资比较条数：%d
                历次调资差异条数：%d
                """.formatted(
                LocalDateTime.now(),
                mismatchesOnly ? "仅差异记录" : "全部调资记录（另附差异明细）",
                data.totalPersonnelWithHistory(),
                data.comparedPersonnel(),
                data.latestDifferenceCount(),
                data.historyMismatchPersonCount(),
                data.totalHistoryRecordsCompared(),
                data.totalHistoryRecordMismatches());
    }

    private byte[] buildExcel(PayrollProjectionAuditExportData data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writePersonSummarySheet(workbook, data);
            writeHistoryDetailSheet(workbook, "历次调资明细", data.historyDetails());
            writeHistoryDetailSheet(workbook, "历次调资差异明细", mismatchRows(data.historyDetails()));
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate projection audit Excel export", e);
        }
    }

    private void writePersonSummarySheet(Workbook workbook, PayrollProjectionAuditExportData data) {
        var sheet = workbook.createSheet("人员汇总");
        String[] headers = {
                "UID", "单位编码", "人员编码", "姓名", "当前年月", "可推算", "说明",
                "调资合计", "推算合计", "当前差额", "当前一致", "调资条数", "差异条数"
        };
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle textStyle = textStyle(workbook);
        Row title = sheet.createRow(0);
        title.createCell(0).setCellValue("工资推算对账 — 人员汇总（全库 "
                + data.comparedPersonnel() + " 人）");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        Row meta = sheet.createRow(1);
        meta.createCell(0).setCellValue("导出时间：" + LocalDateTime.now()
                + "    有调资人员：" + data.totalPersonnelWithHistory()
                + "    当前差异：" + data.latestDifferenceCount()
                + "    历次差异条数：" + data.totalHistoryRecordMismatches());
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));
        Row header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 3);
        int rowIndex = 3;
        for (PayrollProjectionPersonAudit item : data.personSummaries()) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;
            writeText(row, column++, item.uid(), textStyle);
            writeText(row, column++, item.organizationCode(), textStyle);
            writeText(row, column++, item.personCode(), textStyle);
            writeText(row, column++, item.name(), textStyle);
            writeText(row, column++, item.latestPeriod(), textStyle);
            writeText(row, column++, yesNo(item.latestProjectionEligible()), textStyle);
            writeText(row, column++, item.latestNote(), textStyle);
            writeNumber(row, column++, item.storedTotal(), textStyle);
            writeDecimal(row, column++, item.projectedTotal(), textStyle);
            writeDecimal(row, column++, item.latestTotalDifference(), textStyle);
            writeText(row, column++, yesNo(item.latestMatched()), textStyle);
            writeNumber(row, column++, item.historyRecordCount(), textStyle);
            writeNumber(row, column, item.historyMismatchCount(), textStyle);
        }
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeHistoryDetailSheet(Workbook workbook, String sheetName, List<PayrollProjectionAuditDetailRow> details) {
        var sheet = workbook.createSheet(sheetName);
        String[] headers = {
                "UID", "单位编码", "人员编码", "姓名", "调资ID", "年月", "变动类别",
                "可推算", "说明", "一致", "调资合计", "推算合计", "差额", "结构差异", "金额差异"
        };
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle textStyle = textStyle(workbook);
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
        int rowIndex = 1;
        for (PayrollProjectionAuditDetailRow item : details) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;
            writeText(row, column++, item.uid(), textStyle);
            writeText(row, column++, item.organizationCode(), textStyle);
            writeText(row, column++, item.personCode(), textStyle);
            writeText(row, column++, item.name(), textStyle);
            writeText(row, column++, item.historyId(), textStyle);
            writeText(row, column++, item.calculationPeriod(), textStyle);
            writeText(row, column++, item.changeType(), textStyle);
            writeText(row, column++, yesNo(item.projectionEligible()), textStyle);
            writeText(row, column++, item.note(), textStyle);
            writeText(row, column++, yesNo(item.matched()), textStyle);
            writeNumber(row, column++, item.storedTotal(), textStyle);
            writeDecimal(row, column++, item.projectedTotal(), textStyle);
            writeDecimal(row, column++, item.totalDifference(), textStyle);
            writeText(row, column++, item.structureMismatches(), textStyle);
            writeText(row, column, item.componentDifferences(), textStyle);
        }
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle textStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void writeText(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value.toString());
        cell.setCellStyle(style);
    }

    private void writeNumber(Row row, int column, Integer value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private void writeDecimal(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        cell.setCellStyle(style);
    }

    private String yesNo(Boolean value) {
        return Boolean.TRUE.equals(value) ? "是" : "否";
    }

    private String decimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private byte[] withUtf8Bom(String content) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[body.length + 3];
        bytes[0] = (byte) 0xEF;
        bytes[1] = (byte) 0xBB;
        bytes[2] = (byte) 0xBF;
        System.arraycopy(body, 0, bytes, 3, body.length);
        return bytes;
    }

    private String fileName(String prefix, String extension) {
        return prefix + "_" + FILE_STAMP.format(LocalDateTime.now()) + "." + extension;
    }
}
