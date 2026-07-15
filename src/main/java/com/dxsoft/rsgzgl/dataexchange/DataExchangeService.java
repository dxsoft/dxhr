package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
class DataExchangeService {

    private final DataExchangeRepository dataExchangeRepository;
    private final ObjectMapper objectMapper;

    DataExchangeService(DataExchangeRepository dataExchangeRepository, ObjectMapper objectMapper) {
        this.dataExchangeRepository = dataExchangeRepository;
        this.objectMapper = objectMapper;
    }

    PageResponse<PersonnelExportRecord> exportPersonnel(
            String organizationCode, String keyword, PageRequest pageRequest) {
        return dataExchangeRepository.exportPersonnel(organizationCode, keyword, pageRequest);
    }

    PageResponse<AnnualReportRecord> exportAnnualReport(
            String organizationCode, String period, String keyword, PageRequest pageRequest) {
        return dataExchangeRepository.exportAnnualReport(organizationCode, period, keyword, pageRequest);
    }

    ResponseEntity<byte[]> downloadPersonnelCsv(String organizationCode, String keyword) {
        List<PersonnelExportRecord> records = dataExchangeRepository.exportAllPersonnelForDownload(organizationCode, keyword);

        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("单位编码", "单位名称", "人员编码", "姓名", "身份证", "性别", "出生年月",
                        "人员类别", "单位属性", "岗位分类", "参加工作年月", "转正年月", "工资年限",
                        "学历编码", "最高学历", "当前职务级别", "职级编码", "当前职务", "任职年月",
                        "民族", "政治面貌", "档案号", "当前岗位", "当前职务名称", "当前级别", "当前档次")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (PersonnelExportRecord r : records) {
                printer.printRecord(
                        r.organizationCode(), r.organizationName(), r.personCode(), r.name(),
                        maskIdCard(r.idCard()), r.gender(), r.birthYearMonth(),
                        r.personnelCategory(), r.organizationType(), r.postCategory(),
                        r.workStart(), r.regularization(), r.salaryYears(),
                        r.educationCode(), r.highestEducation(), r.positionLevel(),
                        r.rankCode(), r.currentPosition(), r.positionStart(),
                        r.ethnicity(), r.politicalStatus(), r.archiveNumber(),
                        r.currentJob(), r.currentGrade(), r.currentLevel(), r.currentTechGrade());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }

        String csvContent = writer.toString();
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "personnel_export.csv");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    PersonnelExchangePackage buildPersonnelPackage(DataExchangeController.PersonnelDispatchRequest request) {
        List<PersonnelExportRecord> records = resolveDispatchPersonnel(request);
        return new PersonnelExchangePackage(
                "PERSONNEL",
                LocalDateTime.now().toString(),
                request.organizationCodes() == null ? List.of() : request.organizationCodes(),
                request.includeDescendants(),
                records,
                dataExchangeRepository.exportRelatedTables(records));
    }

    ResponseEntity<byte[]> dispatchPersonnelPackage(DataExchangeController.PersonnelDispatchRequest request) {
        PersonnelExchangePackage payload = buildPersonnelPackage(request);
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "rsgzgl_personnel_package.json");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    PayrollSubmissionPackage buildSubmissionPackage(DataExchangeController.PersonnelDispatchRequest request) {
        List<PersonnelExportRecord> records = resolveDispatchPersonnel(request);
        return new PayrollSubmissionPackage(
                "SUBMISSION",
                LocalDateTime.now().toString(),
                request.organizationCodes() == null ? List.of() : request.organizationCodes(),
                request.includeDescendants(),
                records,
                dataExchangeRepository.exportPayrollTables(records),
                dataExchangeRepository.exportSubmissionRelatedTables(records));
    }

    ResponseEntity<byte[]> dispatchSubmissionPackage(DataExchangeController.PersonnelDispatchRequest request) {
        PayrollSubmissionPackage payload = buildSubmissionPackage(request);
        dataExchangeRepository.markPayrollSubmitted(payload.personnel());
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "rsgzgl_submission_package.json");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    PayrollSubmissionPackage buildApprovalPackage(DataExchangeController.PersonnelDispatchRequest request) {
        List<PersonnelExportRecord> records = resolveApprovalPersonnel(request);
        return new PayrollSubmissionPackage(
                "APPROVAL",
                LocalDateTime.now().toString(),
                request.organizationCodes() == null ? List.of() : request.organizationCodes(),
                request.includeDescendants(),
                records,
                dataExchangeRepository.exportPayrollTables(records),
                dataExchangeRepository.exportSubmissionRelatedTables(records));
    }

    ResponseEntity<byte[]> dispatchApprovalPackage(DataExchangeController.PersonnelDispatchRequest request) {
        PayrollSubmissionPackage payload = buildApprovalPackage(request);
        if (payload.personnel() == null || payload.personnel().isEmpty()) {
            throw new IllegalArgumentException("没有可下发的已审工资审批数据");
        }
        dataExchangeRepository.markPayrollApprovalDispatched(payload.personnel());
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "rsgzgl_approval_package.json");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    DataExchangeController.SubmissionReviewPreviewResponse previewApprovalReceive(
            DataExchangeController.ApprovalReceiveRequest request) {
        PayrollSubmissionPackage payload = parseApprovalPackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                .map(row -> buildApprovalReceivePreviewRow(row, payload))
                .toList();
        return new DataExchangeController.SubmissionReviewPreviewResponse(
                rows.size(),
                previewRows.stream().limit(100).toList(),
                buildSubmissionReviewSummary(previewRows),
                "预览成功：接收后将写入上级审批结果并替换本地工资变动记录");
    }

    @Transactional
    public DataExchangeController.SubmissionReviewApplyResponse applyApprovalReceive(
            DataExchangeController.ApprovalReceiveRequest request) {
        PayrollSubmissionPackage payload = parseApprovalPackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("审批数据包中没有可处理的人员记录");
        }
        boolean dryRun = Boolean.TRUE.equals(request.dryRun());
        List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                .map(row -> buildApprovalReceivePreviewRow(row, payload))
                .toList();
        if (dryRun) {
            return new DataExchangeController.SubmissionReviewApplyResponse(
                    0,
                    buildSubmissionReviewSummary(previewRows),
                    "试运行通过：审批数据接收未写入数据库");
        }
        int count = dataExchangeRepository.applyApprovedSubmission(rows, payload.payrollTables(), payload.relatedTables());
        return new DataExchangeController.SubmissionReviewApplyResponse(
                count,
                buildSubmissionReviewSummary(previewRows),
                "已接收 " + count + " 条审批数据，并更新本地工资变动记录");
    }

    DataExchangeController.SubmissionReviewPreviewResponse previewSubmissionReview(
            DataExchangeController.SubmissionReviewRequest request) {
        PayrollSubmissionPackage payload = parseSubmissionPackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                .map(row -> buildSubmissionReviewPreviewRow(row, payload))
                .toList();
        return new DataExchangeController.SubmissionReviewPreviewResponse(
                rows.size(),
                previewRows.stream().limit(100).toList(),
                buildSubmissionReviewSummary(previewRows),
                "APPROVE".equalsIgnoreCase(request.decision())
                        ? "预览成功：审核通过后将替换对应人员的工资变动记录"
                        : "预览成功：退回申报不会写入数据库");
    }

    @Transactional
    public DataExchangeController.SubmissionReviewApplyResponse applySubmissionReview(
            DataExchangeController.SubmissionReviewRequest request) {
        PayrollSubmissionPackage payload = parseSubmissionPackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("申报包中没有可处理的人员记录");
        }
        boolean approve = !"REJECT".equalsIgnoreCase(request.decision());
        boolean dryRun = Boolean.TRUE.equals(request.dryRun());
        if (dryRun) {
            List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                    .map(row -> buildSubmissionReviewPreviewRow(row, payload))
                    .toList();
            return new DataExchangeController.SubmissionReviewApplyResponse(
                    0,
                    buildSubmissionReviewSummary(previewRows),
                    approve ? "试运行通过：审核通过操作未写入数据库" : "试运行通过：退回操作未写入数据库");
        }
        if (!approve) {
            List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                    .map(row -> buildSubmissionReviewPreviewRow(row, payload))
                    .toList();
            return new DataExchangeController.SubmissionReviewApplyResponse(
                    0,
                    buildSubmissionReviewSummary(previewRows),
                    "已退回 " + rows.size() + " 条申报记录，未写入数据库");
        }
        int count = dataExchangeRepository.applyApprovedSubmission(rows, payload.payrollTables(), payload.relatedTables());
        List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                .map(row -> buildSubmissionReviewPreviewRow(row, payload))
                .toList();
        return new DataExchangeController.SubmissionReviewApplyResponse(
                count,
                buildSubmissionReviewSummary(previewRows),
                "已审核通过 " + count + " 条申报记录，并更新工资变动数据");
    }

    DataExchangeController.ReceivePreviewResponse previewReceive(DataExchangeController.ReceiveRequest request) {
        PersonnelExchangePackage payload = parsePackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        boolean append = "APPEND".equalsIgnoreCase(request.mode());
        List<DataExchangeController.ReceivePreviewRow> previewRows = buildPreviewRows(rows, payload.relatedTables(), append, request.targetOrganizationCode());
        DataExchangeController.ReceiveSummary summary = buildSummary(previewRows, payload.relatedTables());
        return new DataExchangeController.ReceivePreviewResponse(
                rows.size(),
                rows.stream().limit(50).toList(),
                previewRows.stream().limit(100).toList(),
                summary,
                List.of(),
                append ? "预览成功：勾选人员将追加到目标单位并重新编码" : "预览成功：整体接收将替换同单位同个人编码数据");
    }

    @Transactional
    public DataExchangeController.ReceiveApplyResponse applyReceive(DataExchangeController.ReceiveRequest request) {
        PersonnelExchangePackage payload = parsePackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        boolean append = "APPEND".equalsIgnoreCase(request.mode());
        boolean dryRun = Boolean.TRUE.equals(request.dryRun());
        if (append && (request.targetOrganizationCode() == null || request.targetOrganizationCode().isBlank())) {
            throw new IllegalArgumentException("追加接收需要选择接收单位");
        }
        List<DataExchangeController.CodeMapping> mappings = append
                ? dataExchangeRepository.plannedAppendMappings(rows, request.targetOrganizationCode())
                : rows.stream()
                        .map(row -> new DataExchangeController.CodeMapping(
                                row.organizationCode(), row.personCode(), row.organizationCode(), row.personCode(), row.name()))
                        .toList();
        int existing = append ? 0 : (int) rows.stream().filter(row -> dataExchangeRepository.personExists(row.organizationCode(), row.personCode())).count();
        DataExchangeController.ReceiveSummary summary = buildSummary(buildPreviewRows(rows, payload.relatedTables(), append, request.targetOrganizationCode()), payload.relatedTables());
        if (dryRun) {
            return new DataExchangeController.ReceiveApplyResponse(
                    0,
                    append ? 0 : rows.size() - existing,
                    append ? 0 : existing,
                    append ? rows.size() : 0,
                    mappings,
                    summary,
                    append ? "试运行通过：勾选人员可追加接收并重新编码，未写入数据库" : "试运行通过：整体接收可替换/新增，未写入数据库");
        }
        int count;
        try {
            count = append
                    ? dataExchangeRepository.appendReceivedPersonnel(rows, payload.relatedTables(), request.targetOrganizationCode())
                    : dataExchangeRepository.replaceReceivedPersonnel(rows, payload.relatedTables());
        } catch (DataAccessException e) {
            throw new IllegalStateException("数据接收写入失败：" + e.getMostSpecificCause().getMessage(), e);
        }
        return new DataExchangeController.ReceiveApplyResponse(
                count,
                append ? 0 : count - existing,
                append ? 0 : existing,
                append ? count : 0,
                mappings,
                summary,
                append ? "已按追加方式接收并重新编码" : "已整体接收并替换相同单位编码和个人编码数据");
    }

    ResponseEntity<byte[]> downloadAnnualReportCsv(String organizationCode, String period, String keyword) {
        PageResponse<AnnualReportRecord> page = dataExchangeRepository.exportAnnualReport(
                organizationCode, period, keyword, PageRequest.of(0, 10000));
        List<AnnualReportRecord> records = page.content();

        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("单位编码", "单位名称", "人员编码", "姓名", "身份证", "性别", "出生年月",
                        "人员类别", "当前岗位", "当前职务", "当前级别", "当前档次",
                        "年月", "变动类别", "职务工资", "级别工资", "技术等级工资",
                        "绩效/生活补贴", "保留福补", "警衔津贴", "年补贴",
                        "教护龄津贴", "提高工资", "浮动工资", "奖金结余", "PGBC", "合计")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (AnnualReportRecord r : records) {
                printer.printRecord(
                        r.organizationCode(), r.organizationName(), r.personCode(), r.name(),
                        maskIdCard(r.idCard()), r.gender(), r.birthYearMonth(),
                        r.personnelCategory(), r.currentPosition(), r.currentJob(), r.currentGrade(), r.currentLevel(),
                        r.period(), r.changeType(), r.positionSalary(), r.gradeSalary(), r.techGradeSalary(),
                        r.performanceAllowance(), r.retainedAllowance(), r.rankAllowance(), r.yearAllowance(),
                        r.teachingAllowance(), r.improvedSalary(), r.floatingSalary(), r.bonusBalance(),
                        r.pgbc(), r.total());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }

        String csvContent = writer.toString();
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment",
                "annual_report" + (period != null ? "_" + period : "") + ".csv");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    ResponseEntity<byte[]> downloadAnnualReportExcel(String organizationCode, String period, String keyword) {
        PageResponse<AnnualReportRecord> page = dataExchangeRepository.exportAnnualReport(
                organizationCode, period, keyword, PageRequest.of(0, 50000));
        List<AnnualReportRecord> records = page.content();
        try (Workbook workbook = new XSSFWorkbook(); java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("工资年报数据");
            String[] headers = {
                    "序号", "单位编码", "单位名称", "人员编码", "姓名", "身份证号", "性别", "出生年月",
                    "人员类别", "当前岗位", "当前职务", "当前级别", "当前档次", "年月", "变动类别",
                    "职务/岗位工资", "级别/薪级工资", "技术等级工资", "绩效/生活补贴", "保留福补",
                    "警衔津贴", "年补贴", "教护龄津贴", "提高工资", "浮动工资", "奖金结余", "PGBC", "合计"
            };

            CellStyle titleStyle = titleStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle textStyle = textStyle(workbook);
            CellStyle moneyStyle = moneyStyle(workbook);

            Row title = sheet.createRow(0);
            title.setHeightInPoints(28);
            Cell titleCell = title.createCell(0);
            titleCell.setCellValue("工资年报数据");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            Row subtitle = sheet.createRow(1);
            subtitle.createCell(0).setCellValue("单位：" + emptyText(organizationCode, "全部")
                    + "    年月：" + emptyText(period, "全部")
                    + "    关键词：" + emptyText(keyword, "无"));
            subtitle.getCell(0).setCellStyle(textStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            Row header = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            sheet.createFreezePane(0, 3);

            for (int i = 0; i < records.size(); i++) {
                AnnualReportRecord record = records.get(i);
                Row row = sheet.createRow(i + 3);
                int c = 0;
                writeText(row, c++, i + 1, textStyle);
                writeText(row, c++, record.organizationCode(), textStyle);
                writeText(row, c++, record.organizationName(), textStyle);
                writeText(row, c++, record.personCode(), textStyle);
                writeText(row, c++, record.name(), textStyle);
                writeText(row, c++, maskIdCard(record.idCard()), textStyle);
                writeText(row, c++, record.gender(), textStyle);
                writeText(row, c++, record.birthYearMonth(), textStyle);
                writeText(row, c++, record.personnelCategory(), textStyle);
                writeText(row, c++, record.currentPosition(), textStyle);
                writeText(row, c++, record.currentJob(), textStyle);
                writeText(row, c++, record.currentGrade(), textStyle);
                writeText(row, c++, record.currentLevel(), textStyle);
                writeText(row, c++, record.period(), textStyle);
                writeText(row, c++, record.changeType(), textStyle);
                writeMoney(row, c++, record.positionSalary(), moneyStyle);
                writeMoney(row, c++, record.gradeSalary(), moneyStyle);
                writeMoney(row, c++, record.techGradeSalary(), moneyStyle);
                writeMoney(row, c++, record.performanceAllowance(), moneyStyle);
                writeMoney(row, c++, record.retainedAllowance(), moneyStyle);
                writeMoney(row, c++, record.rankAllowance(), moneyStyle);
                writeMoney(row, c++, record.yearAllowance(), moneyStyle);
                writeMoney(row, c++, record.teachingAllowance(), moneyStyle);
                writeMoney(row, c++, record.improvedSalary(), moneyStyle);
                writeMoney(row, c++, record.floatingSalary(), moneyStyle);
                writeMoney(row, c++, record.bonusBalance(), moneyStyle);
                writeMoney(row, c++, record.pgbc(), moneyStyle);
                writeMoney(row, c, record.total(), moneyStyle);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, Math.min(20, Math.max(8, headers[i].length() + 4)) * 256);
            }
            workbook.write(output);
            byte[] bytes = output.toByteArray();
            HttpHeaders headersOut = new HttpHeaders();
            headersOut.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headersOut.setContentDispositionFormData("attachment",
                    "annual_report" + (period != null && !period.isBlank() ? "_" + period : "") + ".xlsx");
            headersOut.setContentLength(bytes.length);
            return ResponseEntity.ok().headers(headersOut).body(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private CellStyle titleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle textStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle moneyStyle(Workbook workbook) {
        CellStyle style = borderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
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

    private void writeText(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : String.valueOf(value));
        cell.setCellStyle(style);
    }

    private void writeMoney(Row row, int column, java.math.BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? 0D : value.doubleValue());
        cell.setCellStyle(style);
    }

    private String emptyText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    private PersonnelExchangePackage parsePackage(String packageJson) {
        if (packageJson == null || packageJson.isBlank()) {
            throw new IllegalArgumentException("数据包内容不能为空");
        }
        return objectMapper.readValue(packageJson, PersonnelExchangePackage.class);
    }

    private PayrollSubmissionPackage parseSubmissionPackage(String packageJson) {
        if (packageJson == null || packageJson.isBlank()) {
            throw new IllegalArgumentException("数据包内容不能为空");
        }
        PayrollSubmissionPackage payload = objectMapper.readValue(packageJson, PayrollSubmissionPackage.class);
        if (payload.packageType() != null && !"SUBMISSION".equalsIgnoreCase(payload.packageType())) {
            throw new IllegalArgumentException("不是工资申报数据包");
        }
        return payload;
    }

    private PayrollSubmissionPackage parseApprovalPackage(String packageJson) {
        if (packageJson == null || packageJson.isBlank()) {
            throw new IllegalArgumentException("数据包内容不能为空");
        }
        PayrollSubmissionPackage payload = objectMapper.readValue(packageJson, PayrollSubmissionPackage.class);
        if (payload.packageType() != null && !"APPROVAL".equalsIgnoreCase(payload.packageType())) {
            throw new IllegalArgumentException("不是工资审批数据包");
        }
        return payload;
    }

    private DataExchangeController.SubmissionReviewPreviewRow buildApprovalReceivePreviewRow(
            PersonnelExportRecord row,
            PayrollSubmissionPackage payload) {
        DataExchangeController.SubmissionReviewPreviewRow previewRow = buildSubmissionReviewPreviewRow(row, payload);
        return new DataExchangeController.SubmissionReviewPreviewRow(
                previewRow.organizationCode(),
                previewRow.organizationName(),
                previewRow.personCode(),
                previewRow.name(),
                previewRow.changeType(),
                previewRow.calculationPeriod(),
                previewRow.totalAmount(),
                previewRow.approvalStatus(),
                previewRow.submissionStatus(),
                previewRow.payrollRecordCount(),
                previewRow.organizationExists(),
                previewRow.personExists(),
                previewRow.personExists() ? "替换工资记录" : "新增人员并写入审批结果");
    }

    private List<PersonnelExportRecord> resolveApprovalPersonnel(DataExchangeController.PersonnelDispatchRequest request) {
        if (request.selectedPersonnel() != null && !request.selectedPersonnel().isEmpty()) {
            return filterByKeyword(
                    dataExchangeRepository.exportSelectedApprovedPersonnel(request.selectedPersonnel()),
                    request.keyword());
        }
        return dataExchangeRepository.exportApprovedPersonnelPackageByOrganizations(
                request.organizationCodes(),
                request.includeDescendants(),
                request.keyword());
    }

    private DataExchangeController.SubmissionReviewPreviewRow buildSubmissionReviewPreviewRow(
            PersonnelExportRecord row,
            PayrollSubmissionPackage payload) {
        Map<String, Object> current = dataExchangeRepository.findCurrentPayrollSummary(row.organizationCode(), row.personCode());
        int payrollCount = payrollCountForPerson(payload.payrollTables(), row.organizationCode(), row.personCode());
        boolean orgExists = dataExchangeRepository.organizationExists(row.organizationCode());
        boolean personExists = dataExchangeRepository.personExists(row.organizationCode(), row.personCode());
        return new DataExchangeController.SubmissionReviewPreviewRow(
                row.organizationCode(),
                row.organizationName(),
                row.personCode(),
                row.name(),
                stringValue(current.get("jslb")),
                stringValue(current.get("period")),
                numericValue(current.get("hj2")),
                stringValue(current.get("jzgb")),
                stringValue(current.get("bbz")),
                payrollCount,
                orgExists,
                personExists,
                personExists ? "替换工资记录" : "新增人员并写入工资");
    }

    private DataExchangeController.SubmissionReviewSummary buildSubmissionReviewSummary(
            List<DataExchangeController.SubmissionReviewPreviewRow> rows) {
        int replaceCount = (int) rows.stream().filter(DataExchangeController.SubmissionReviewPreviewRow::personExists).count();
        int createCount = rows.size() - replaceCount;
        int payrollRows = rows.stream().mapToInt(DataExchangeController.SubmissionReviewPreviewRow::payrollRecordCount).sum();
        return new DataExchangeController.SubmissionReviewSummary(rows.size(), createCount, replaceCount, payrollRows);
    }

    private int payrollCountForPerson(
            List<ExchangeTable> payrollTables,
            String organizationCode,
            String personCode) {
        if (payrollTables == null) {
            return 0;
        }
        return payrollTables.stream()
                .filter(table -> "hisbase".equalsIgnoreCase(table.tableName()))
                .mapToInt(table -> table.rows() == null ? 0 : (int) table.rows().stream()
                        .filter(row -> equalsText(String.valueOf(row.getOrDefault("dwbm", row.get("DWBM"))), organizationCode)
                                && equalsText(String.valueOf(row.getOrDefault("grbm", row.get("GRBM"))), personCode))
                        .count())
                .sum();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Integer numericValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<PersonnelExportRecord> filterReceiveRows(
            List<PersonnelExportRecord> rows,
            List<DataExchangeController.PersonKey> selectedPersonnel) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        if (selectedPersonnel == null || selectedPersonnel.isEmpty()) {
            return rows;
        }
        return rows.stream()
                .filter(row -> selectedPersonnel.stream().anyMatch(key ->
                        equalsText(key.organizationCode(), row.organizationCode())
                                && equalsText(key.personCode(), row.personCode())))
                .toList();
    }

    private boolean equalsText(String left, String right) {
        return String.valueOf(left == null ? "" : left).trim().equals(String.valueOf(right == null ? "" : right).trim());
    }

    private List<PersonnelExportRecord> resolveDispatchPersonnel(DataExchangeController.PersonnelDispatchRequest request) {
        if (request.selectedPersonnel() != null && !request.selectedPersonnel().isEmpty()) {
            return filterByKeyword(dataExchangeRepository.exportSelectedPersonnel(request.selectedPersonnel()), request.keyword());
        }
        return dataExchangeRepository.exportPersonnelPackageByOrganizations(
                request.organizationCodes(),
                request.includeDescendants(),
                request.keyword());
    }

    private List<PersonnelExportRecord> filterByKeyword(List<PersonnelExportRecord> records, String keyword) {
        if (keyword == null || keyword.isBlank() || records == null || records.isEmpty()) {
            return records == null ? List.of() : records;
        }
        String trimmed = keyword.trim();
        return records.stream()
                .filter(record -> containsIgnoreCase(record.personCode(), trimmed)
                        || containsIgnoreCase(record.name(), trimmed)
                        || containsIgnoreCase(record.idCard(), trimmed))
                .toList();
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private List<DataExchangeController.ReceivePreviewRow> buildPreviewRows(
            List<PersonnelExportRecord> rows,
            List<ExchangeTable> relatedTables,
            boolean append,
            String targetOrganizationCode) {
        List<DataExchangeController.CodeMapping> appendMappings = append
                ? dataExchangeRepository.plannedAppendMappings(rows, targetOrganizationCode)
                : List.of();
        return rows.stream().map(row -> {
            boolean exists = dataExchangeRepository.personExists(row.organizationCode(), row.personCode());
            boolean targetExists = !append || dataExchangeRepository.organizationExists(targetOrganizationCode);
            String action = append ? "重新编码追加" : exists ? "替换" : "新增";
            String targetCode = appendMappings.stream()
                    .filter(mapping -> equalsText(mapping.sourceOrganizationCode(), row.organizationCode())
                            && equalsText(mapping.sourcePersonCode(), row.personCode()))
                    .map(DataExchangeController.CodeMapping::targetPersonCode)
                    .findFirst()
                    .orElse(row.personCode());
            return new DataExchangeController.ReceivePreviewRow(
                    row.organizationCode(),
                    row.personCode(),
                    row.name(),
                    action,
                    targetExists,
                    append ? targetOrganizationCode : row.organizationCode(),
                    targetCode,
                    relatedCountsForPerson(relatedTables, row.organizationCode(), row.personCode()));
        }).toList();
    }

    private DataExchangeController.ReceiveSummary buildSummary(
            List<DataExchangeController.ReceivePreviewRow> rows,
            List<ExchangeTable> relatedTables) {
        int append = (int) rows.stream().filter(row -> "重新编码追加".equals(row.action())).count();
        int replace = (int) rows.stream().filter(row -> "替换".equals(row.action())).count();
        int created = (int) rows.stream().filter(row -> "新增".equals(row.action())).count();
        List<DataExchangeController.TableCount> counts = relatedTables == null ? List.of() : relatedTables.stream()
                .map(table -> new DataExchangeController.TableCount(table.tableName(), table.rows() == null ? 0 : table.rows().size()))
                .toList();
        return new DataExchangeController.ReceiveSummary(rows.size(), created, replace, append, counts);
    }

    private List<DataExchangeController.TableCount> relatedCountsForPerson(List<ExchangeTable> relatedTables, String orgCode, String personCode) {
        if (relatedTables == null) {
            return List.of();
        }
        return relatedTables.stream()
                .map(table -> new DataExchangeController.TableCount(
                        table.tableName(),
                        table.rows() == null ? 0 : (int) table.rows().stream()
                                .filter(row -> equalsText(String.valueOf(row.getOrDefault("dwbm", row.get("DWBM"))), orgCode)
                                        && equalsText(String.valueOf(row.getOrDefault("grbm", row.get("GRBM"))), personCode))
                                .count()))
                .toList();
    }

    record PersonnelExchangePackage(
            String packageType,
            String generatedAt,
            List<String> organizationCodes,
            Boolean includeDescendants,
            List<PersonnelExportRecord> personnel,
            List<ExchangeTable> relatedTables) {
    }

    record ExchangeTable(
            String tableName,
            List<Map<String, Object>> rows) {
    }
}
