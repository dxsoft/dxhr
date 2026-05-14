package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
class DataExchangeService {

    private final DataExchangeRepository dataExchangeRepository;

    DataExchangeService(DataExchangeRepository dataExchangeRepository) {
        this.dataExchangeRepository = dataExchangeRepository;
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
}
