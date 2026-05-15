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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    ResponseEntity<byte[]> dispatchPersonnelPackage(DataExchangeController.PersonnelDispatchRequest request) {
        List<PersonnelExportRecord> records = request.selectedPersonnel() != null && !request.selectedPersonnel().isEmpty()
                ? dataExchangeRepository.exportSelectedPersonnel(request.selectedPersonnel())
                : dataExchangeRepository.exportPersonnelPackageByOrganizations(request.organizationCodes(), request.includeDescendants());
        PersonnelExchangePackage payload = new PersonnelExchangePackage(
                "PERSONNEL",
                LocalDateTime.now().toString(),
                request.organizationCodes() == null ? List.of() : request.organizationCodes(),
                request.includeDescendants(),
                records,
                dataExchangeRepository.exportRelatedTables(records));
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "rsgzgl_personnel_package.json");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
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

    DataExchangeController.ReceiveApplyResponse applyReceive(DataExchangeController.ReceiveRequest request) {
        PersonnelExchangePackage payload = parsePackage(request.packageJson());
        List<PersonnelExportRecord> rows = filterReceiveRows(payload.personnel(), request.selectedPersonnel());
        boolean append = "APPEND".equalsIgnoreCase(request.mode());
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
        int count = append
                ? dataExchangeRepository.appendReceivedPersonnel(rows, payload.relatedTables(), request.targetOrganizationCode())
                : dataExchangeRepository.replaceReceivedPersonnel(rows, payload.relatedTables());
        DataExchangeController.ReceiveSummary summary = buildSummary(buildPreviewRows(rows, payload.relatedTables(), append, request.targetOrganizationCode()), payload.relatedTables());
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
