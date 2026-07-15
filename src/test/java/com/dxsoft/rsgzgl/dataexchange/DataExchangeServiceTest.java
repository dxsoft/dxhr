package com.dxsoft.rsgzgl.dataexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class DataExchangeServiceTest {

    private DataExchangeRepository repository;
    private DataExchangeService service;

    @BeforeEach
    void setUp() {
        repository = mock(DataExchangeRepository.class);
        service = new DataExchangeService(repository, new ObjectMapper());
    }

    @Test
    void buildPersonnelPackageFiltersSelectedPersonnelByKeyword() {
        when(repository.exportSelectedPersonnel(any())).thenReturn(List.of(
                sample("001", "00001", "张三", "110101199001011234"),
                sample("001", "00002", "李四", "110101199002021234")));
        when(repository.exportRelatedTables(any())).thenReturn(List.of());

        DataExchangeService.PersonnelExchangePackage payload = service.buildPersonnelPackage(
                new DataExchangeController.PersonnelDispatchRequest(
                        List.of("001"),
                        true,
                        "张三",
                        List.of(new DataExchangeController.PersonKey("001", "00001"))));

        assertThat(payload.personnel()).hasSize(1);
        assertThat(payload.personnel().getFirst().name()).isEqualTo("张三");
    }

    @Test
    void previewReceiveMarksExistingPersonAsReplace() {
        when(repository.personExists("001", "00001")).thenReturn(true);
        when(repository.exportRelatedTables(any())).thenReturn(List.of());

        String packageJson = """
                {
                  "packageType": "PERSONNEL",
                  "personnel": [
                    {
                      "organizationCode": "001",
                      "organizationName": "测试单位",
                      "personCode": "00001",
                      "name": "张三"
                    }
                  ],
                  "relatedTables": []
                }
                """;

        DataExchangeController.ReceivePreviewResponse response = service.previewReceive(
                new DataExchangeController.ReceiveRequest(packageJson, "REPLACE", null, List.of(), null));

        assertThat(response.previewRows()).hasSize(1);
        assertThat(response.previewRows().getFirst().action()).isEqualTo("替换");
        assertThat(response.summary().replaceRecords()).isEqualTo(1);
    }

    @Test
    void applyReceiveAppendRequiresTargetOrganization() {
        String packageJson = """
                {
                  "packageType": "PERSONNEL",
                  "personnel": [{"organizationCode":"001","personCode":"00001","name":"张三"}],
                  "relatedTables": []
                }
                """;

        assertThatThrownBy(() -> service.applyReceive(
                new DataExchangeController.ReceiveRequest(packageJson, "APPEND", "", List.of(), false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("追加接收需要选择接收单位");
    }

    @Test
    void applyReceiveDryRunDoesNotWriteDatabase() {
        when(repository.personExists("001", "00001")).thenReturn(false);
        when(repository.plannedAppendMappings(any(), eq("002"))).thenReturn(List.of(
                new DataExchangeController.CodeMapping("001", "00001", "002", "00003", "张三")));
        when(repository.organizationExists("002")).thenReturn(true);

        String packageJson = """
                {
                  "packageType": "PERSONNEL",
                  "personnel": [{"organizationCode":"001","personCode":"00001","name":"张三"}],
                  "relatedTables": []
                }
                """;

        DataExchangeController.ReceiveApplyResponse response = service.applyReceive(
                new DataExchangeController.ReceiveRequest(packageJson, "APPEND", "002", List.of(), true));

        assertThat(response.receivedRecords()).isZero();
        assertThat(response.appendedRecords()).isEqualTo(1);
        assertThat(response.message()).contains("试运行");
    }

    @Test
    void buildSubmissionPackageIncludesPayrollTables() {
        when(repository.exportPersonnelPackageByOrganizations(any(), anyBoolean(), any())).thenReturn(List.of(
                sample("001", "00001", "张三", "110101199001011234")));
        when(repository.exportPayrollTables(any())).thenReturn(List.of(
                new DataExchangeService.ExchangeTable("hisbase", List.of(Map.of("dwbm", "001", "grbm", "00001", "jslb", "晋级")))));
        when(repository.exportSubmissionRelatedTables(any())).thenReturn(List.of());

        PayrollSubmissionPackage payload = service.buildSubmissionPackage(
                new DataExchangeController.PersonnelDispatchRequest(
                        List.of("001"),
                        true,
                        null,
                        List.of()));

        assertThat(payload.packageType()).isEqualTo("SUBMISSION");
        assertThat(payload.personnel()).hasSize(1);
        assertThat(payload.payrollTables()).hasSize(1);
        assertThat(payload.payrollTables().getFirst().tableName()).isEqualTo("hisbase");
    }

    @Test
    void previewSubmissionReviewMarksExistingPersonAsReplace() {
        when(repository.personExists("001", "00001")).thenReturn(true);
        when(repository.organizationExists("001")).thenReturn(true);
        when(repository.findCurrentPayrollSummary("001", "00001")).thenReturn(Map.of(
                "jslb", "晋级",
                "period", "202601",
                "hj2", 5000,
                "jzgb", "否",
                "bbz", "申报"));

        String packageJson = """
                {
                  "packageType": "SUBMISSION",
                  "personnel": [
                    {
                      "organizationCode": "001",
                      "organizationName": "测试单位",
                      "personCode": "00001",
                      "name": "张三"
                    }
                  ],
                  "payrollTables": [
                    {
                      "tableName": "hisbase",
                      "rows": [{"dwbm":"001","grbm":"00001","jslb":"晋级"}]
                    }
                  ],
                  "relatedTables": []
                }
                """;

        DataExchangeController.SubmissionReviewPreviewResponse response = service.previewSubmissionReview(
                new DataExchangeController.SubmissionReviewRequest(packageJson, null, List.of(), null));

        assertThat(response.previewRows()).hasSize(1);
        assertThat(response.previewRows().getFirst().action()).isEqualTo("替换工资记录");
        assertThat(response.summary().replaceRecords()).isEqualTo(1);
    }

    @Test
    void applySubmissionReviewDryRunDoesNotWriteDatabase() {
        when(repository.personExists("001", "00001")).thenReturn(true);
        when(repository.organizationExists("001")).thenReturn(true);
        when(repository.findCurrentPayrollSummary("001", "00001")).thenReturn(Map.of(
                "jslb", "晋级",
                "period", "202601",
                "hj2", 5000,
                "jzgb", "否",
                "bbz", "申报"));

        String packageJson = """
                {
                  "packageType": "SUBMISSION",
                  "personnel": [{"organizationCode":"001","personCode":"00001","name":"张三"}],
                  "payrollTables": [{"tableName":"hisbase","rows":[{"dwbm":"001","grbm":"00001"}]}],
                  "relatedTables": []
                }
                """;

        DataExchangeController.SubmissionReviewApplyResponse response = service.applySubmissionReview(
                new DataExchangeController.SubmissionReviewRequest(packageJson, "APPROVE", List.of(), true));

        assertThat(response.processedRecords()).isZero();
        assertThat(response.message()).contains("试运行");
    }

    @Test
    void buildApprovalPackageOnlyIncludesApprovedPersonnel() {
        when(repository.exportApprovedPersonnelPackageByOrganizations(any(), anyBoolean(), any())).thenReturn(List.of(
                sample("001", "00001", "张三", "110101199001011234")));
        when(repository.exportPayrollTables(any())).thenReturn(List.of());
        when(repository.exportSubmissionRelatedTables(any())).thenReturn(List.of());

        PayrollSubmissionPackage payload = service.buildApprovalPackage(
                new DataExchangeController.PersonnelDispatchRequest(
                        List.of("001"),
                        true,
                        null,
                        List.of()));

        assertThat(payload.packageType()).isEqualTo("APPROVAL");
        assertThat(payload.personnel()).hasSize(1);
    }

    @Test
    void previewApprovalReceiveShowsReplaceActionForExistingPerson() {
        when(repository.personExists("001", "00001")).thenReturn(true);
        when(repository.organizationExists("001")).thenReturn(true);
        when(repository.findCurrentPayrollSummary("001", "00001")).thenReturn(Map.of(
                "jslb", "晋级",
                "period", "202601",
                "hj2", 5000,
                "jzgb", "是",
                "bbz", "已审"));

        String packageJson = """
                {
                  "packageType": "APPROVAL",
                  "personnel": [{"organizationCode":"001","personCode":"00001","name":"张三"}],
                  "payrollTables": [{"tableName":"hisbase","rows":[{"dwbm":"001","grbm":"00001","jzgb":"是","bbz":"已审"}]}],
                  "relatedTables": []
                }
                """;

        DataExchangeController.SubmissionReviewPreviewResponse response = service.previewApprovalReceive(
                new DataExchangeController.ApprovalReceiveRequest(packageJson, List.of(), null));

        assertThat(response.previewRows()).hasSize(1);
        assertThat(response.previewRows().getFirst().action()).isEqualTo("替换工资记录");
    }

    private static PersonnelExportRecord sample(String org, String person, String name, String idCard) {
        return new PersonnelExportRecord(
                org, "单位", person, name, idCard,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}
