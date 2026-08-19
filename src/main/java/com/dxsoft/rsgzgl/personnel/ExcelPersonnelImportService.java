package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import org.springframework.context.annotation.Lazy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcelPersonnelImportService {

    private static final String[] TEMPLATE_HEADERS = {
            "人员编码", "姓名", "性别", "身份证号", "出生年月", "参加工作年月", "进入本单位年月", "转正年月",
            "人员类别", "单位属性", "岗位分类", "最高学历", "毕业学校", "毕业时间",
            "职务级别", "当前职务", "任职年月", "民族", "政治面貌", "档案号", "工资年限"
    };

    private static final String DEFAULT_PERSONNEL_CATEGORY = "专业技术人员";
    private static final String DEFAULT_ORGANIZATION_TYPE = "10";
    private static final String DEFAULT_POST_CATEGORY = "专业技术岗位";

    private final NamedParameterJdbcTemplate jdbc;
    private final PersonnelRepository personnelRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;
    private final PayrollService payrollService;
    private final DataFormatter dataFormatter = new DataFormatter();

    ExcelPersonnelImportService(
            NamedParameterJdbcTemplate jdbc,
            PersonnelRepository personnelRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            @Lazy PayrollService payrollService) {
        this.jdbc = jdbc;
        this.personnelRepository = personnelRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.payrollService = payrollService;
    }

    public byte[] templateWorkbook() {
        requireImportPermission();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("人员建库");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_HEADERS[i]);
                sheet.setColumnWidth(i, 4200);
            }
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("00001");
            sample.createCell(1).setCellValue("张三");
            sample.createCell(2).setCellValue("男");
            sample.createCell(3).setCellValue("411500199001011234");
            sample.createCell(4).setCellValue("1990.01");
            sample.createCell(5).setCellValue("2012.07");
            sample.createCell(6).setCellValue("2012.07");
            sample.createCell(7).setCellValue("2013.07");
            sample.createCell(8).setCellValue(DEFAULT_PERSONNEL_CATEGORY);
            sample.createCell(9).setCellValue(DEFAULT_ORGANIZATION_TYPE);
            sample.createCell(10).setCellValue(DEFAULT_POST_CATEGORY);
            sample.createCell(11).setCellValue("本科");
            sample.createCell(12).setCellValue("示例大学");
            sample.createCell(13).setCellValue("2012.06");
            sample.createCell(14).setCellValue("十级专业技术岗位");
            sample.createCell(15).setCellValue("十级专业技术岗位");
            sample.createCell(16).setCellValue("2020.01");
            sample.createCell(17).setCellValue("汉族");
            sample.createCell(18).setCellValue("中共党员");
            sample.createCell(20).setCellValue("0");
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("生成 Excel 模板失败。", e);
        }
    }

    public ExcelImportPreview preview(String organizationCode, InputStream inputStream) {
        requireImportPermission();
        String normalizedOrganizationCode = requireOrganizationCode(organizationCode);
        accessControlService.requireOrganization(normalizedOrganizationCode);
        String organizationName = findOrganizationName(normalizedOrganizationCode);
        ParseResult parsed = parseWorkbook(inputStream);
        return buildPreview(normalizedOrganizationCode, organizationName, parsed);
    }

    @Transactional
    public ExcelImportResult importPersonnel(String organizationCode, InputStream inputStream) {
        requireImportPermission();
        String normalizedOrganizationCode = requireOrganizationCode(organizationCode);
        accessControlService.requireOrganization(normalizedOrganizationCode);
        ParseResult parsed = parseWorkbook(inputStream);
        ExcelImportPreview preview = buildPreview(
                normalizedOrganizationCode, findOrganizationName(normalizedOrganizationCode), parsed);
        if (preview.validRows() == 0) {
            throw new IllegalArgumentException("没有可导入的有效行。");
        }
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>(parsed.errors());
        Set<String> seenCodes = new HashSet<>();
        for (ExcelPersonnelImportRow row : parsed.rows()) {
            if (row.name() == null || row.name().isBlank()) {
                continue;
            }
            if (row.personCode() == null || row.personCode().isBlank()) {
                errors.add("第 " + row.rowNumber() + " 行：人员编码不能为空。");
                skipped++;
                continue;
            }
            if (!seenCodes.add(row.personCode())) {
                skipped++;
                continue;
            }
            if (personExists(normalizedOrganizationCode, row.personCode())) {
                skipped++;
                continue;
            }
            try {
                importRow(normalizedOrganizationCode, row);
                imported++;
            } catch (RuntimeException ex) {
                errors.add("第 " + row.rowNumber() + " 行：" + ex.getMessage());
                skipped++;
            }
        }
        applyPostImportUpdates(normalizedOrganizationCode);
        operationLogService.record(
                "IMPORT_PERSONNEL_EXCEL",
                "dryjbxx",
                normalizedOrganizationCode,
                "Excel 导入人员 " + imported + " 人，单位 " + normalizedOrganizationCode);
        return new ExcelImportResult(
                normalizedOrganizationCode,
                imported,
                skipped,
                errors,
                "Excel 导入完成，成功 " + imported + " 人，跳过 " + skipped + " 行。");
    }

    private void importRow(String organizationCode, ExcelPersonnelImportRow row) {
        String positionLevel = emptyToBlank(row.positionLevel());
        String currentPosition = emptyToBlank(row.currentPosition());
        if (currentPosition.isBlank()) {
            currentPosition = positionLevel;
        }
        String educationName = resolveEducationName(row.highestEducation());
        String educationCode = resolveEducationCode(row.highestEducation());
        String workStartYearMonth = normalizeYearMonth(row.workStartYearMonth());
        String joinYearMonth = normalizeYearMonth(row.joinYearMonth());
        String joinType = "";
        if (!joinYearMonth.isBlank()
                && workStartYearMonth.length() == 6
                && joinYearMonth.length() == 6
                && (workStartYearMonth.equals(joinYearMonth) || workStartYearMonth.compareTo(joinYearMonth) < 0)) {
            joinType = "新招人员";
        }
        PersonnelMaintenanceRequest request = new PersonnelMaintenanceRequest(
                organizationCode,
                row.personCode(),
                row.name(),
                emptyToBlank(row.idCard()),
                emptyToBlank(row.gender()),
                normalizeYearMonth(row.birthYearMonth()),
                firstNonBlank(row.personnelCategory(), DEFAULT_PERSONNEL_CATEGORY),
                firstNonBlank(row.organizationType(), DEFAULT_ORGANIZATION_TYPE),
                firstNonBlank(row.postCategory(), DEFAULT_POST_CATEGORY),
                workStartYearMonth,
                normalizeYearMonth(row.regularizationYearMonth()),
                parseSalaryYears(row.salaryYears()),
                educationCode,
                educationName,
                positionLevel,
                "",
                currentPosition,
                normalizeYearMonth(row.positionStartYearMonth()),
                emptyToBlank(row.ethnicity()),
                emptyToBlank(row.politicalStatus()),
                emptyToBlank(row.archiveNumber()),
                joinYearMonth,
                joinType);
        int uid = personnelRepository.createPersonnel(request);
        if (uid <= 0) {
            throw new IllegalStateException("写入人员档案失败。");
        }
        PersonKey key = new PersonKey(organizationCode, row.personCode());
        if (!educationName.isBlank()) {
            personnelRepository.createEducation(key, new EducationMaintenanceRequest(
                    educationCode,
                    educationName,
                    emptyToBlank(row.school()),
                    "",
                    normalizeYearMonth(row.graduationDate()),
                    0,
                    "普通全日制",
                    ""));
        }
        if (!positionLevel.isBlank()) {
            personnelRepository.createPosition(key, new PositionMaintenanceRequest(
                    "",
                    currentPosition,
                    positionLevel,
                    "",
                    "",
                    currentPosition,
                    normalizeYearMonth(row.positionStartYearMonth()),
                    0,
                    "1",
                    "0"));
        }
        if (workStartYearMonth.equals(joinYearMonth) && "新招人员".equals(joinType)) {
            payrollService.ensureNoExperienceInternSalary(uid);
        }
        payrollService.ensureTransferInSalaryDetermination(uid);
        jdbc.update("""
                UPDATE dryjbxx
                SET xckhndzw = '2022'
                WHERE uid = :uid
                """, new MapSqlParameterSource("uid", uid));
    }

    private void applyPostImportUpdates(String organizationCode) {
        MapSqlParameterSource params = new MapSqlParameterSource("organizationCode", organizationCode);
        jdbc.update("""
                UPDATE dryjbxx
                SET csny = SUBSTRING(sfzh, 7, 4) || '.' || SUBSTRING(sfzh, 11, 2)
                WHERE dwbm = :organizationCode
                  AND (csny IS NULL OR TRIM(csny) = '')
                  AND sfzh IS NOT NULL AND LENGTH(TRIM(sfzh)) >= 14
                """, params);
        jdbc.update("""
                UPDATE dryjbxx
                SET jrny = CONCAT(LEFT(jrny, 5), '0', SUBSTRING(jrny, 6, 2))
                WHERE dwbm = :organizationCode
                  AND jrny IS NOT NULL AND TRIM(jrny) <> ''
                  AND LENGTH(TRIM(jrny)) < 7
                """, params);
        jdbc.update("""
                UPDATE dryjbxx
                SET zzny = CONCAT(LEFT(zzny, 5), '0', SUBSTRING(zzny, 6, 2))
                WHERE dwbm = :organizationCode
                  AND zzny IS NOT NULL AND TRIM(zzny) <> ''
                  AND LENGTH(TRIM(zzny)) < 7
                """, params);
        jdbc.update("""
                UPDATE dryjbxx
                SET srny = CONCAT(LEFT(srny, 5), '0', SUBSTRING(srny, 6, 2))
                WHERE dwbm = :organizationCode
                  AND srny IS NOT NULL AND TRIM(srny) <> ''
                  AND LENGTH(TRIM(srny)) < 7
                """, params);
        for (Map.Entry<String, String> entry : educationCodeMappings().entrySet()) {
            jdbc.update("""
                    UPDATE dryjbxx SET xlbm = :code
                    WHERE dwbm = :organizationCode AND zgxl = :educationName
                      AND (xlbm IS NULL OR TRIM(xlbm) = '')
                    """, new MapSqlParameterSource()
                    .addValue("organizationCode", organizationCode)
                    .addValue("code", entry.getValue())
                    .addValue("educationName", entry.getKey()));
        }
        jdbc.update("""
                UPDATE dryjbxx p
                INNER JOIN dmb d ON p.zwjb = d.mc AND LEFT(d.bm, 3) = '001'
                SET p.zjbm = SUBSTRING(d.bm, 4, 4)
                WHERE p.dwbm = :organizationCode
                  AND (p.zjbm IS NULL OR TRIM(p.zjbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dxl SET xlbm = '23' WHERE dwbm = :organizationCode AND xl = '本科' AND (xlbm IS NULL OR TRIM(xlbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dxl SET xlbm = '31' WHERE dwbm = :organizationCode AND (xl = '专科' OR xl = '大专') AND (xlbm IS NULL OR TRIM(xlbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dryzwbh p
                INNER JOIN dmb d ON p.zwjb = d.mc AND LEFT(d.bm, 3) = '001'
                SET p.zjbm = SUBSTRING(d.bm, 4, 4), p.xrzwbm = SUBSTRING(d.bm, 4, 4)
                WHERE p.dwbm = :organizationCode
                """, params);
        jdbc.update("""
                UPDATE dryzwbh SET srny = CONCAT(LEFT(srny, 5), '0', SUBSTRING(srny, 6, 2))
                WHERE dwbm = :organizationCode
                  AND srny IS NOT NULL AND TRIM(srny) <> ''
                  AND LENGTH(TRIM(srny)) < 7
                """, params);
        jdbc.update("""
                UPDATE dryzwbh SET zwbm = '1004', xzzw = '四级专业技术岗位'
                WHERE dwbm = :organizationCode AND SUBSTRING(zjbm, 3, 2) = '01'
                  AND (zwbm IS NULL OR TRIM(zwbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dryzwbh SET zwbm = '1007', xzzw = '七级专业技术岗位'
                WHERE dwbm = :organizationCode AND SUBSTRING(zjbm, 3, 2) = '02'
                  AND (zwbm IS NULL OR TRIM(zwbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dryzwbh SET zwbm = '1010', xzzw = '十级专业技术岗位'
                WHERE dwbm = :organizationCode AND SUBSTRING(zjbm, 3, 2) = '03'
                  AND (zwbm IS NULL OR TRIM(zwbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dryzwbh SET zwbm = '1012', xzzw = '十二级专业技术岗位'
                WHERE dwbm = :organizationCode AND SUBSTRING(zjbm, 3, 2) = '04'
                  AND (zwbm IS NULL OR TRIM(zwbm) = '')
                """, params);
        jdbc.update("""
                UPDATE dryzwbh SET zwbm = '1013', xzzw = '十三级专业技术岗位'
                WHERE dwbm = :organizationCode AND SUBSTRING(zjbm, 3, 2) = '05'
                  AND (zwbm IS NULL OR TRIM(zwbm) = '')
                """, params);
    }

    private ExcelImportPreview buildPreview(String organizationCode, String organizationName, ParseResult parsed) {
        List<ExcelImportPreviewRow> rows = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();
        int validRows = 0;
        int duplicateRows = 0;
        int errorRows = parsed.errors().size();
        for (ExcelPersonnelImportRow row : parsed.rows()) {
            if (row.name() == null || row.name().isBlank()) {
                continue;
            }
            String category = firstNonBlank(row.personnelCategory(), DEFAULT_PERSONNEL_CATEGORY);
            List<String> rowErrors = validateRow(row);
            if (!rowErrors.isEmpty()) {
                errorRows += rowErrors.size();
                rows.add(new ExcelImportPreviewRow(
                        row.rowNumber(), row.personCode(), row.name(), category,
                        resolveEducationName(row.highestEducation()), emptyToBlank(row.positionLevel()),
                        "错误", String.join("；", rowErrors)));
                continue;
            }
            if (!seenCodes.add(row.personCode())) {
                duplicateRows++;
                rows.add(new ExcelImportPreviewRow(
                        row.rowNumber(), row.personCode(), row.name(), category,
                        resolveEducationName(row.highestEducation()), emptyToBlank(row.positionLevel()),
                        "重复", "Excel 内人员编码重复，将跳过。"));
                continue;
            }
            if (personExists(organizationCode, row.personCode())) {
                duplicateRows++;
                rows.add(new ExcelImportPreviewRow(
                        row.rowNumber(), row.personCode(), row.name(), category,
                        resolveEducationName(row.highestEducation()), emptyToBlank(row.positionLevel()),
                        "跳过", "单位内已存在该人员编码，不会覆盖。"));
                continue;
            }
            validRows++;
            rows.add(new ExcelImportPreviewRow(
                    row.rowNumber(), row.personCode(), row.name(), category,
                    resolveEducationName(row.highestEducation()), emptyToBlank(row.positionLevel()),
                    "新增", "将导入人员档案、学历与任职记录。"));
        }
        return new ExcelImportPreview(
                organizationCode,
                organizationName,
                parsed.rows().size(),
                validRows,
                duplicateRows,
                errorRows,
                rows,
                parsed.errors(),
                "共解析 " + parsed.rows().size() + " 行，可导入 " + validRows + " 行（已存在编码将跳过）。");
    }

    private List<String> validateRow(ExcelPersonnelImportRow row) {
        List<String> errors = new ArrayList<>();
        if (row.personCode() == null || row.personCode().isBlank()) {
            errors.add("人员编码不能为空");
        }
        if (row.name() == null || row.name().isBlank()) {
            errors.add("姓名不能为空");
        }
        String education = emptyToBlank(row.highestEducation());
        if (!education.isBlank() && resolveEducationCode(education).isBlank() && !education.matches("\\d{2}")) {
            // 允许自由文本学历名；仅当看起来像未知代码时提示
            if (education.matches("\\d+")) {
                errors.add("学历编码无法识别：" + education);
            }
        }
        String salaryYears = emptyToBlank(row.salaryYears());
        if (!salaryYears.isBlank()) {
            try {
                Integer.parseInt(salaryYears.replaceAll("\\.0+$", ""));
            } catch (NumberFormatException ex) {
                errors.add("工资年限须为整数");
            }
        }
        return errors;
    }

    private ParseResult parseWorkbook(InputStream inputStream) {
        List<String> errors = new ArrayList<>();
        List<ExcelPersonnelImportRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                errors.add("Excel 文件中没有工作表。");
                return new ParseResult(rows, errors);
            }
            int lastRow = sheet.getLastRowNum();
            for (int rowIndex = 1; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                String name = cellText(row.getCell(1));
                if (name.isBlank()) {
                    break;
                }
                rows.add(new ExcelPersonnelImportRow(
                        rowIndex + 1,
                        normalizePersonCode(cellText(row.getCell(0))),
                        name,
                        cellText(row.getCell(2)),
                        cellText(row.getCell(3)),
                        cellText(row.getCell(4)),
                        cellText(row.getCell(5)),
                        cellText(row.getCell(6)),
                        cellText(row.getCell(7)),
                        cellText(row.getCell(8)),
                        cellText(row.getCell(9)),
                        cellText(row.getCell(10)),
                        cellText(row.getCell(11)),
                        cellText(row.getCell(12)),
                        cellText(row.getCell(13)),
                        cellText(row.getCell(14)),
                        cellText(row.getCell(15)),
                        cellText(row.getCell(16)),
                        cellText(row.getCell(17)),
                        cellText(row.getCell(18)),
                        cellText(row.getCell(19)),
                        cellText(row.getCell(20))));
            }
        } catch (IOException e) {
            errors.add("读取 Excel 失败：" + e.getMessage());
        }
        return new ParseResult(rows, errors);
    }

    private String cellText(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }

    private String normalizePersonCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String trimmed = raw.trim();
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return trimmed;
        }
        try {
            int value = Integer.parseInt(digits.length() > 5 ? digits.substring(digits.length() - 5) : digits);
            return String.format("%05d", value);
        } catch (NumberFormatException ex) {
            return trimmed;
        }
    }

    private String normalizeYearMonth(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String normalized = raw.trim().replace("-", ".").replace("/", ".");
        if (normalized.matches("\\d{6}")) {
            return normalized.substring(0, 4) + "." + normalized.substring(4, 6);
        }
        if (normalized.matches("\\d{4}\\.\\d{1}")) {
            return normalized.substring(0, 5) + "0" + normalized.substring(5);
        }
        return normalized;
    }

    private int parseSalaryYears(String raw) {
        String value = emptyToBlank(raw);
        if (value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.replaceAll("\\.0+$", ""));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String resolveEducationCode(String education) {
        String raw = emptyToBlank(education);
        if (raw.isBlank()) {
            return "";
        }
        if (raw.matches("\\d{2}")) {
            return raw;
        }
        return educationCodeMappings().getOrDefault(raw, "");
    }

    private String resolveEducationName(String education) {
        String raw = emptyToBlank(education);
        if (raw.isBlank()) {
            return "";
        }
        if (raw.matches("\\d{2}")) {
            for (Map.Entry<String, String> entry : educationCodeMappings().entrySet()) {
                if (entry.getValue().equals(raw)) {
                    return entry.getKey();
                }
            }
            return raw;
        }
        return raw;
    }

    private Map<String, String> educationCodeMappings() {
        Map<String, String> mappings = new LinkedHashMap<>();
        mappings.put("硕士研究生", "12");
        mappings.put("研究生", "12");
        mappings.put("本科", "23");
        mappings.put("专科", "31");
        mappings.put("大专", "31");
        mappings.put("中专", "41");
        mappings.put("技校", "51");
        mappings.put("职高", "61");
        mappings.put("高中", "62");
        return mappings;
    }

    private boolean personExists(String organizationCode, String personCode) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM dryjbxx WHERE dwbm = :organizationCode AND grbm = :personCode
                """, new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("personCode", personCode), Long.class);
        return count != null && count > 0;
    }

    private String findOrganizationName(String organizationCode) {
        List<String> names = jdbc.query("""
                SELECT dwmc FROM dwbm WHERE dwbm = :organizationCode
                """, new MapSqlParameterSource("organizationCode", organizationCode),
                (rs, rowNum) -> rs.getString("dwmc"));
        if (names.isEmpty()) {
            throw new NotFoundException("Organization not found: " + organizationCode);
        }
        return names.getFirst();
    }

    private String requireOrganizationCode(String organizationCode) {
        String normalized = emptyToNull(organizationCode);
        if (normalized == null) {
            throw new IllegalArgumentException("单位编码不能为空。");
        }
        return normalized;
    }

    private void requireImportPermission() {
        if (!accessControlService.hasPermission("SYSTEM_CONFIG")) {
            throw new IllegalStateException("当前用户没有 Excel 导入权限。");
        }
    }

    private static String firstNonBlank(String value, String fallback) {
        String trimmed = emptyToBlank(value);
        return trimmed.isBlank() ? fallback : trimmed;
    }

    private static String emptyToBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ParseResult(List<ExcelPersonnelImportRow> rows, List<String> errors) {
    }
}
