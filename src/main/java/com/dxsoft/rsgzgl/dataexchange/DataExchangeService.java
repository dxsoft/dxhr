package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final OperationLogService operationLogService;

    DataExchangeService(
            DataExchangeRepository dataExchangeRepository,
            ObjectMapper objectMapper,
            OperationLogService operationLogService) {
        this.dataExchangeRepository = dataExchangeRepository;
        this.objectMapper = objectMapper;
        this.operationLogService = operationLogService;
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
        return buildPersonnelPackage(request, true);
    }

    PersonnelExchangePackage buildPersonnelPackage(
            DataExchangeController.PersonnelDispatchRequest request,
            boolean includeRelatedTables) {
        List<PersonnelExportRecord> records = resolveDispatchPersonnel(request);
        if (includeRelatedTables && records.size() > 800) {
            throw new IllegalArgumentException(
                    "下发人数过多（" + records.size() + "），请勾选人员分批生成下发包（建议每次不超过 800 人）。");
        }
        return new PersonnelExchangePackage(
                "PERSONNEL",
                LocalDateTime.now().toString(),
                request.organizationCodes() == null ? List.of() : request.organizationCodes(),
                request.includeDescendants(),
                records,
                includeRelatedTables ? dataExchangeRepository.exportRelatedTables(records) : List.of());
    }

    ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> dispatchPersonnelPackage(
            DataExchangeController.PersonnelDispatchRequest request) {
        PersonnelExchangePackage payload = buildPersonnelPackage(request, true);
        this.recordDataExchangeAction(
                "EXPORT_PERSONNEL_PACKAGE",
                String.valueOf(payload.personnel() == null ? 0 : payload.personnel().size()),
                "导出人员下发包 " + (payload.personnel() == null ? 0 : payload.personnel().size()) + " 人");
        org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody body = outputStream ->
                objectMapper.writeValue(outputStream, payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "rsgzgl_personnel_package.json");
        return ResponseEntity.ok().headers(headers).body(body);
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
        this.recordDataExchangeAction(
                "EXPORT_SUBMISSION_PACKAGE",
                String.valueOf(payload.personnel() == null ? 0 : payload.personnel().size()),
                "导出申报包 " + (payload.personnel() == null ? 0 : payload.personnel().size()) + " 人");
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

    DataExchangeController.ApprovalDispatchPreviewResponse previewApprovalPackage(
            DataExchangeController.PersonnelDispatchRequest request) {
        PayrollSubmissionPackage payload = buildApprovalPackage(request);
        List<DataExchangeController.ApprovalStatusCount> statusCounts =
                dataExchangeRepository.countCurrentPayrollStatuses(
                        request.organizationCodes(),
                        request.includeDescendants(),
                        request.keyword());
        String distribution = formatApprovalStatusCounts(statusCounts);
        String message;
        if (payload.personnel() == null || payload.personnel().isEmpty()) {
            message = "当前筛选条件下无可列表人员。"
                    + (distribution.isBlank() ? "" : " 本单位范围状态分布：" + distribution)
                    + "。可下发状态为：申报 / 已审 / 审批通过；已下发可筛选后「退回已审」。";
        } else {
            message = "审批下发预览 " + payload.personnel().size() + " 人"
                    + (distribution.isBlank() ? "" : "；范围分布：" + distribution)
                    + "。勾选后可只下发勾选人员。";
        }
        return new DataExchangeController.ApprovalDispatchPreviewResponse(payload, statusCounts, message);
    }

    ResponseEntity<byte[]> dispatchApprovalPackage(DataExchangeController.PersonnelDispatchRequest request) {
        List<String> statuses = normalizeApprovalStatuses(request.approvalStatuses(), true);
        if (statuses.size() == 1 && statuses.contains("已下发")) {
            throw new IllegalArgumentException("「已下发」仅可查看或退回，不能再次生成审批包。请先「退回已审」。");
        }
        DataExchangeController.PersonnelDispatchRequest exportRequest = new DataExchangeController.PersonnelDispatchRequest(
                request.organizationCodes(),
                request.includeDescendants(),
                request.keyword(),
                request.selectedPersonnel(),
                statuses);
        PayrollSubmissionPackage payload = buildApprovalPackage(exportRequest);
        if (payload.personnel() == null || payload.personnel().isEmpty()) {
            throw new IllegalArgumentException("没有可下发的审批数据（需为申报 / 已审 / 审批通过）");
        }
        dataExchangeRepository.markPayrollApprovalDispatched(payload.personnel());
        this.recordDataExchangeAction(
                "EXPORT_APPROVAL_PACKAGE",
                String.valueOf(payload.personnel().size()),
                "导出审批下发包 " + payload.personnel().size() + " 人");
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "rsgzgl_approval_package.json");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @Transactional
    public DataExchangeController.ApprovalRevertResponse revertDispatchedApproval(
            DataExchangeController.PersonnelDispatchRequest request) {
        List<PersonnelExportRecord> targets;
        if (request.selectedPersonnel() != null && !request.selectedPersonnel().isEmpty()) {
            targets = dataExchangeRepository.exportSelectedPersonnelByStatuses(
                    request.selectedPersonnel(), List.of("已下发"));
        } else {
            targets = dataExchangeRepository.exportApprovedPersonnelPackageByOrganizations(
                    request.organizationCodes(),
                    request.includeDescendants(),
                    request.keyword(),
                    List.of("已下发"));
        }
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("没有可退回的「已下发」记录，请先筛选或勾选已下发人员。");
        }
        int updated = dataExchangeRepository.revertPayrollApprovalDispatched(targets);
        this.recordDataExchangeAction(
                "REVERT_APPROVAL_DISPATCH",
                String.valueOf(updated),
                "退回已下发审批 " + updated + " 人");
        return new DataExchangeController.ApprovalRevertResponse(
                updated,
                "已将 " + updated + " 人从「已下发」退回为「已审」，可重新下发。");
    }

    private String formatApprovalStatusCounts(List<DataExchangeController.ApprovalStatusCount> statusCounts) {
        if (statusCounts == null || statusCounts.isEmpty()) {
            return "";
        }
        return statusCounts.stream()
                .filter(item -> item != null && item.count() > 0)
                .map(item -> (item.status() == null || item.status().isBlank() ? "(空)" : item.status())
                        + " " + item.count())
                .collect(java.util.stream.Collectors.joining("，"));
    }

    private List<String> normalizeApprovalStatuses(List<String> requested, boolean forExport) {
        if (requested == null || requested.isEmpty()) {
            return DataExchangeRepository.DISPATCHABLE_APPROVAL_STATUSES;
        }
        List<String> cleaned = requested.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (cleaned.isEmpty()) {
            return DataExchangeRepository.DISPATCHABLE_APPROVAL_STATUSES;
        }
        if (forExport) {
            return cleaned.stream()
                    .filter(DataExchangeRepository.DISPATCHABLE_APPROVAL_STATUSES::contains)
                    .toList();
        }
        return cleaned;
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
        this.recordDataExchangeAction(
                "APPLY_APPROVAL_RECEIVE",
                String.valueOf(count),
                "接收审批数据 " + count + " 人");
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
        DataExchangeController.SubmissionReviewSummary summary = buildSubmissionReviewSummary(previewRows);
        String message;
        if (summary.inconsistentRecords() == 0 && summary.newRecords() == 0) {
            message = "审核完成：上报人员信息与本地完全一致（" + summary.consistentRecords()
                    + " 人）。可按需勾选后接收工资变动，一般无需接收。";
        } else {
            message = "审核完成：完全一致 " + summary.consistentRecords()
                    + " 人，不一致 " + summary.inconsistentRecords()
                    + " 人，新增 " + summary.newRecords()
                    + " 人。请核对差异后勾选并「同意接收」。";
        }
        return new DataExchangeController.SubmissionReviewPreviewResponse(
                rows.size(),
                previewRows.stream().limit(200).toList(),
                summary,
                message);
    }

    @Transactional
    public DataExchangeController.SubmissionReviewApplyResponse applySubmissionReview(
            DataExchangeController.SubmissionReviewRequest request) {
        PayrollSubmissionPackage payload = parseSubmissionPackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("请先勾选要处理的人员（建议勾选「不一致」或「新增人员」）。");
        }
        boolean approve = !"REJECT".equalsIgnoreCase(request.decision());
        List<DataExchangeController.SubmissionReviewPreviewRow> previewRows = rows.stream()
                .map(row -> buildSubmissionReviewPreviewRow(row, payload))
                .toList();
        DataExchangeController.SubmissionReviewSummary summary = buildSubmissionReviewSummary(previewRows);
        if (Boolean.TRUE.equals(request.dryRun())) {
            return new DataExchangeController.SubmissionReviewApplyResponse(
                    0,
                    summary,
                    approve ? "试运行：同意接收未写入数据库" : "试运行：拒绝接收未写入数据库");
        }
        if (!approve) {
            return new DataExchangeController.SubmissionReviewApplyResponse(
                    0,
                    summary,
                    "已拒绝接收 " + rows.size() + " 条申报记录，未写入数据库");
        }
        int count = dataExchangeRepository.applyApprovedSubmission(rows, payload.payrollTables(), payload.relatedTables());
        this.recordDataExchangeAction(
                "APPLY_SUBMISSION_REVIEW",
                String.valueOf(count),
                "同意接收申报 " + count + " 人");
        return new DataExchangeController.SubmissionReviewApplyResponse(
                count,
                summary,
                "已同意接收 " + count + " 人，并更新工资变动及关联申报数据");
    }

    DataExchangeController.ReceivePreviewResponse previewReceive(DataExchangeController.ReceiveRequest request) {
        PersonnelExchangePackage payload = parsePackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        boolean append = "APPEND".equalsIgnoreCase(request.mode());
        List<DataExchangeController.ReceivePreviewRow> previewRows = buildPreviewRows(rows, payload.relatedTables(), append, request.targetOrganizationCode());
        DataExchangeController.ReceiveSummary summary = buildSummary(previewRows, payload.relatedTables());
        return new DataExchangeController.ReceivePreviewResponse(
                rows.size(),
                rows,
                previewRows,
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
        List<DataExchangeController.ReceivePreviewRow> previewRows = buildPreviewRows(
                rows, payload.relatedTables(), append, request.targetOrganizationCode());
        int existing = (int) previewRows.stream().filter(row -> "替换".equals(row.action())).count();
        DataExchangeController.ReceiveSummary summary = buildSummary(previewRows, payload.relatedTables());
        if (dryRun) {
            return new DataExchangeController.ReceiveApplyResponse(
                    0,
                    append ? 0 : rows.size() - existing,
                    append ? 0 : existing,
                    append ? rows.size() : 0,
                    mappings.stream().limit(200).toList(),
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
        this.recordDataExchangeAction(
                "APPLY_DATA_RECEIVE",
                String.valueOf(count),
                (append ? "追加接收 " : "整体接收 ") + count + " 人");
        return new DataExchangeController.ReceiveApplyResponse(
                count,
                append ? 0 : count - existing,
                append ? 0 : existing,
                append ? count : 0,
                mappings.stream().limit(200).toList(),
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
                previewRow.personExists() ? "替换工资记录" : "新增人员并写入审批结果",
                previewRow.auditStatus(),
                previewRow.mismatchSummary(),
                previewRow.diffs());
    }

    private List<PersonnelExportRecord> resolveApprovalPersonnel(DataExchangeController.PersonnelDispatchRequest request) {
        List<String> statuses = normalizeApprovalStatuses(request.approvalStatuses(), false);
        if (request.selectedPersonnel() != null && !request.selectedPersonnel().isEmpty()) {
            return filterByKeyword(
                    dataExchangeRepository.exportSelectedPersonnelByStatuses(request.selectedPersonnel(), statuses),
                    request.keyword());
        }
        return dataExchangeRepository.exportApprovedPersonnelPackageByOrganizations(
                request.organizationCodes(),
                request.includeDescendants(),
                request.keyword(),
                statuses);
    }

    private DataExchangeController.SubmissionReviewPreviewRow buildSubmissionReviewPreviewRow(
            PersonnelExportRecord row,
            PayrollSubmissionPackage payload) {
        Map<String, Object> packagePayroll = findPackagePayrollSummary(payload.payrollTables(), row.organizationCode(), row.personCode());
        int payrollCount = payrollCountForPerson(payload.payrollTables(), row.organizationCode(), row.personCode());
        boolean orgExists = dataExchangeRepository.organizationExists(row.organizationCode());
        boolean personExists = dataExchangeRepository.personExists(row.organizationCode(), row.personCode());

        List<DataExchangeController.SubmissionReviewDiff> diffs = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        String auditStatus;
        String action;
        if (!personExists) {
            auditStatus = "新增人员";
            action = "同意接收后新增人员并写入工资";
            categories.add("新增人员");
            diffs.add(new DataExchangeController.SubmissionReviewDiff("人员", "本地不存在", "申报新增"));
        } else {
            PersonnelExportRecord local = dataExchangeRepository.findPersonnelExportRecord(
                    row.organizationCode(), row.personCode());
            comparePersonnelBasics(local, row, diffs);
            if (!diffs.isEmpty()) {
                categories.add("基本信息");
            }
            int beforeRelated = diffs.size();
            compareRelatedPresence(payload.relatedTables(), "dryzwbh", row, List.of("xrzw", "zwjb", "zwbm", "xzzw", "srny"), "职务信息", diffs);
            if (diffs.size() > beforeRelated) {
                categories.add("职务信息");
            }
            beforeRelated = diffs.size();
            compareRelatedPresence(payload.relatedTables(), "dxl", row, List.of("xl", "bysj"), "学历信息", diffs);
            if (diffs.size() > beforeRelated) {
                categories.add("学历信息");
            }
            beforeRelated = diffs.size();
            compareRelatedPresence(payload.relatedTables(), "dndkh", row, List.of("khnd", "khjg"), "年度考核", diffs);
            if (diffs.size() > beforeRelated) {
                categories.add("年度考核");
            }
            if (diffs.isEmpty()) {
                auditStatus = "完全一致";
                action = "基本信息一致，可按需接收工资变动";
            } else {
                auditStatus = "不一致";
                action = "核对差异后同意接收或拒绝";
            }
        }

        String mismatchSummary = categories.isEmpty() ? "" : String.join(" ", categories);
        return new DataExchangeController.SubmissionReviewPreviewRow(
                row.organizationCode(),
                row.organizationName(),
                row.personCode(),
                row.name(),
                stringValue(packagePayroll.get("jslb")),
                stringValue(packagePayroll.get("period")),
                numericValue(packagePayroll.get("hj2")),
                stringValue(packagePayroll.get("bbz")),
                "申报",
                payrollCount,
                orgExists,
                personExists,
                action,
                auditStatus,
                mismatchSummary,
                diffs);
    }

    private void comparePersonnelBasics(
            PersonnelExportRecord local,
            PersonnelExportRecord submitted,
            List<DataExchangeController.SubmissionReviewDiff> diffs) {
        if (local == null) {
            diffs.add(new DataExchangeController.SubmissionReviewDiff("人员", "本地不存在", "申报存在"));
            return;
        }
        compareField("姓名", local.name(), submitted.name(), diffs);
        compareField("身份证", local.idCard(), submitted.idCard(), diffs);
        compareField("性别", local.gender(), submitted.gender(), diffs);
        compareField("出生年月", local.birthYearMonth(), submitted.birthYearMonth(), diffs);
        compareField("人员类别", local.personnelCategory(), submitted.personnelCategory(), diffs);
        compareField("单位属性", local.organizationType(), submitted.organizationType(), diffs);
        compareField("岗位分类", local.postCategory(), submitted.postCategory(), diffs);
        compareField("参加工作", local.workStart(), submitted.workStart(), diffs);
        compareField("转正年月", local.regularization(), submitted.regularization(), diffs);
        compareField("最高学历", local.highestEducation(), submitted.highestEducation(), diffs);
        compareField("职务级别", local.positionLevel(), submitted.positionLevel(), diffs);
        compareField("现任职务", local.currentPosition(), submitted.currentPosition(), diffs);
        compareField("民族", local.ethnicity(), submitted.ethnicity(), diffs);
        compareField("档案号", local.archiveNumber(), submitted.archiveNumber(), diffs);
    }

    private void compareField(
            String item,
            String localValue,
            String submittedValue,
            List<DataExchangeController.SubmissionReviewDiff> diffs) {
        String left = normalizeCompareValue(localValue);
        String right = normalizeCompareValue(submittedValue);
        if (!left.equals(right)) {
            diffs.add(new DataExchangeController.SubmissionReviewDiff(item, left, right));
        }
    }

    private void compareRelatedPresence(
            List<ExchangeTable> relatedTables,
            String tableName,
            PersonnelExportRecord person,
            List<String> keyFields,
            String categoryLabel,
            List<DataExchangeController.SubmissionReviewDiff> diffs) {
        List<Map<String, Object>> packageRows = rowsForPerson(relatedTables, tableName, person.organizationCode(), person.personCode());
        if (packageRows.isEmpty()) {
            return;
        }
        List<Map<String, Object>> localRows = dataExchangeRepository.findRelatedRowsForPerson(
                tableName, person.organizationCode(), person.personCode());
        for (Map<String, Object> packageRow : packageRows) {
            boolean found = localRows.stream().anyMatch(local -> relatedKeysMatch(local, packageRow, keyFields));
            if (!found) {
                String submitted = keyFields.stream()
                        .map(field -> field + "=" + normalizeCompareValue(mapText(packageRow, field)))
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");
                diffs.add(new DataExchangeController.SubmissionReviewDiff(categoryLabel, "本地无匹配", submitted));
            }
        }
    }

    private boolean relatedKeysMatch(Map<String, Object> local, Map<String, Object> submitted, List<String> keyFields) {
        for (String field : keyFields) {
            if (!normalizeCompareValue(mapText(local, field)).equals(normalizeCompareValue(mapText(submitted, field)))) {
                return false;
            }
        }
        return true;
    }

    private List<Map<String, Object>> rowsForPerson(
            List<ExchangeTable> tables,
            String tableName,
            String organizationCode,
            String personCode) {
        if (tables == null || tables.isEmpty()) {
            return List.of();
        }
        return tables.stream()
                .filter(table -> tableName.equalsIgnoreCase(table.tableName()))
                .flatMap(table -> table.rows() == null ? java.util.stream.Stream.<Map<String, Object>>empty() : table.rows().stream())
                .filter(row -> equalsText(mapText(row, "dwbm"), organizationCode) && equalsText(mapText(row, "grbm"), personCode))
                .toList();
    }

    private Map<String, Object> findPackagePayrollSummary(
            List<ExchangeTable> payrollTables,
            String organizationCode,
            String personCode) {
        if (payrollTables == null) {
            return Map.of();
        }
        return payrollTables.stream()
                .filter(table -> "hisbase".equalsIgnoreCase(table.tableName()))
                .flatMap(table -> table.rows() == null ? java.util.stream.Stream.<Map<String, Object>>empty() : table.rows().stream())
                .filter(row -> equalsText(mapText(row, "dwbm"), organizationCode) && equalsText(mapText(row, "grbm"), personCode))
                .filter(row -> {
                    String sid = mapText(row, "sid");
                    return sid.isBlank();
                })
                .findFirst()
                .map(row -> {
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("jslb", mapText(row, "jslb"));
                    summary.put("period", mapText(row, "jsnf") + mapText(row, "jsyf"));
                    summary.put("hj2", row.getOrDefault("hj2", row.get("HJ2")));
                    summary.put("bbz", mapText(row, "bbz"));
                    return summary;
                })
                .orElse(Map.of());
    }

    private String mapText(Map<String, Object> row, String field) {
        if (row == null || field == null) {
            return "";
        }
        Object value = row.get(field);
        if (value == null) {
            value = row.get(field.toUpperCase());
        }
        if (value == null) {
            value = row.get(field.toLowerCase());
        }
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String normalizeCompareValue(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "0".equals(text) || "0.0".equals(text)) {
            return text.equals("0") || text.equals("0.0") ? "0" : "";
        }
        return text;
    }

    private DataExchangeController.SubmissionReviewSummary buildSubmissionReviewSummary(
            List<DataExchangeController.SubmissionReviewPreviewRow> rows) {
        int replaceCount = (int) rows.stream().filter(DataExchangeController.SubmissionReviewPreviewRow::personExists).count();
        int createCount = rows.size() - replaceCount;
        int payrollRows = rows.stream().mapToInt(DataExchangeController.SubmissionReviewPreviewRow::payrollRecordCount).sum();
        int consistent = (int) rows.stream().filter(row -> "完全一致".equals(row.auditStatus())).count();
        int inconsistent = (int) rows.stream().filter(row -> "不一致".equals(row.auditStatus())).count();
        return new DataExchangeController.SubmissionReviewSummary(
                rows.size(), createCount, replaceCount, payrollRows, consistent, inconsistent);
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
        List<PersonnelExportRecord> records;
        if (request.selectedPersonnel() != null && !request.selectedPersonnel().isEmpty()) {
            records = filterByKeyword(
                    dataExchangeRepository.exportSelectedPersonnel(dedupePersonKeys(request.selectedPersonnel())),
                    request.keyword());
        } else {
            records = dataExchangeRepository.exportPersonnelPackageByOrganizations(
                    request.organizationCodes(),
                    request.includeDescendants(),
                    request.keyword());
        }
        return dedupePersonnel(records);
    }

    private List<DataExchangeController.PersonKey> dedupePersonKeys(List<DataExchangeController.PersonKey> keys) {
        Map<String, DataExchangeController.PersonKey> unique = new LinkedHashMap<>();
        for (DataExchangeController.PersonKey key : keys) {
            if (key == null) {
                continue;
            }
            String code = key.organizationCode() == null ? "" : key.organizationCode().trim();
            String person = key.personCode() == null ? "" : key.personCode().trim();
            if (code.isEmpty() || person.isEmpty()) {
                continue;
            }
            unique.putIfAbsent(code + "|" + person, new DataExchangeController.PersonKey(code, person));
        }
        return List.copyOf(unique.values());
    }

    private List<PersonnelExportRecord> dedupePersonnel(List<PersonnelExportRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        Map<String, PersonnelExportRecord> unique = new LinkedHashMap<>();
        for (PersonnelExportRecord record : records) {
            String code = record.organizationCode() == null ? "" : record.organizationCode().trim();
            String person = record.personCode() == null ? "" : record.personCode().trim();
            unique.putIfAbsent(code + "|" + person, record);
        }
        return List.copyOf(unique.values());
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
        Set<String> existingKeys = dataExchangeRepository.existingPersonKeys(rows);
        boolean targetExists = !append || dataExchangeRepository.organizationExists(targetOrganizationCode);
        List<DataExchangeController.CodeMapping> appendMappings = append
                ? dataExchangeRepository.plannedAppendMappings(rows, targetOrganizationCode)
                : List.of();
        Map<String, String> appendTargetCodes = new HashMap<>();
        for (DataExchangeController.CodeMapping mapping : appendMappings) {
            appendTargetCodes.put(
                    personKey(mapping.sourceOrganizationCode(), mapping.sourcePersonCode()),
                    mapping.targetPersonCode());
        }
        Map<String, List<DataExchangeController.TableCount>> relatedByPerson = indexRelatedCountsByPerson(relatedTables);
        List<DataExchangeController.TableCount> emptyRelated = emptyRelatedCounts(relatedTables);
        return rows.stream().map(row -> {
            String key = personKey(row.organizationCode(), row.personCode());
            boolean exists = existingKeys.contains(key);
            String action = append ? "重新编码追加" : exists ? "替换" : "新增";
            String targetCode = append
                    ? appendTargetCodes.getOrDefault(key, row.personCode())
                    : row.personCode();
            return new DataExchangeController.ReceivePreviewRow(
                    row.organizationCode(),
                    row.personCode(),
                    row.name(),
                    action,
                    targetExists,
                    append ? targetOrganizationCode : row.organizationCode(),
                    targetCode,
                    relatedByPerson.getOrDefault(key, emptyRelated));
        }).toList();
    }

    private Map<String, List<DataExchangeController.TableCount>> indexRelatedCountsByPerson(List<ExchangeTable> relatedTables) {
        Map<String, List<DataExchangeController.TableCount>> indexed = new HashMap<>();
        if (relatedTables == null || relatedTables.isEmpty()) {
            return indexed;
        }
        Map<String, Map<String, Integer>> raw = new HashMap<>();
        for (ExchangeTable table : relatedTables) {
            if (table.rows() == null) {
                continue;
            }
            for (Map<String, Object> row : table.rows()) {
                String orgCode = String.valueOf(row.getOrDefault("dwbm", row.getOrDefault("DWBM", "")));
                String personCode = String.valueOf(row.getOrDefault("grbm", row.getOrDefault("GRBM", "")));
                String key = personKey(orgCode, personCode);
                raw.computeIfAbsent(key, ignored -> new HashMap<>())
                        .merge(table.tableName(), 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Map<String, Integer>> entry : raw.entrySet()) {
            List<DataExchangeController.TableCount> counts = new ArrayList<>(relatedTables.size());
            for (ExchangeTable table : relatedTables) {
                counts.add(new DataExchangeController.TableCount(
                        table.tableName(),
                        entry.getValue().getOrDefault(table.tableName(), 0)));
            }
            indexed.put(entry.getKey(), counts);
        }
        return indexed;
    }

    private List<DataExchangeController.TableCount> emptyRelatedCounts(List<ExchangeTable> relatedTables) {
        if (relatedTables == null || relatedTables.isEmpty()) {
            return List.of();
        }
        return relatedTables.stream()
                .map(table -> new DataExchangeController.TableCount(table.tableName(), 0))
                .toList();
    }

    private String personKey(String organizationCode, String personCode) {
        return (organizationCode == null ? "" : organizationCode.trim())
                + "|"
                + (personCode == null ? "" : personCode.trim());
    }

    private void recordDataExchangeAction(String action, String targetId, String summary) {
        operationLogService.record(action, "data_exchange", targetId, summary);
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
