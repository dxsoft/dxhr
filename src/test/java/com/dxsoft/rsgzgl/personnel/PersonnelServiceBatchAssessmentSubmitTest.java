package com.dxsoft.rsgzgl.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.exchange.notification.ExchangeNotificationService;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.retirement.RetirementService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;
import com.dxsoft.rsgzgl.workflow.PayrollWorkflowService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonnelServiceBatchAssessmentSubmitTest {

    @Mock
    private PersonnelRepository personnelRepository;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private RetirementService retirementService;

    @Mock
    private PayrollService payrollService;

    @Mock
    private PersonnelFieldEditPolicy personnelFieldEditPolicy;

    @Mock
    private PersonnelSubrecordEditPolicy personnelSubrecordEditPolicy;

    @Mock
    private SubrecordAttachmentService subrecordAttachmentService;

    @Mock
    private ExchangeNotificationService exchangeNotificationService;

    @Mock
    private PayrollWorkflowService payrollWorkflowService;

    @InjectMocks
    private PersonnelService personnelService;

    @BeforeEach
    void stubCurrentUser() {
        lenient().when(accessControlService.currentUser()).thenReturn(new AppUserPrincipal(
                1L,
                "tester",
                "",
                "测试员",
                true,
                List.of(),
                Set.of(),
                true,
                Set.of(),
                "001",
                null,
                null));
        lenient().when(accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)).thenReturn(true);
    }

    @Test
    void submitBatchAssessmentsSubmitsDraftRecords() {
        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));
        when(personnelRepository.findAssessmentById(9)).thenReturn(Optional.of(sampleAssessment("草稿")));
        when(personnelRepository.findAssessments(new PersonKey("02108", "00001"))).thenReturn(List.of(sampleAssessment("申报")));

        BatchAssessmentSubmitResult result = personnelService.submitBatchAssessments(new BatchAssessmentSubmitRequest(
                "02108",
                "2024",
                false,
                List.of(new BatchAssessmentSubmitItem(1, 9, "02108", "00001"))));

        assertEquals(1, result.submitted());
        assertEquals(0, result.skipped());
        verify(personnelSubrecordEditPolicy).validateSubmit("草稿");
        verify(personnelRepository).updateSubrecordApprovalSubmit(
                eq(PersonnelSubrecordType.ASSESSMENT), eq(9), eq("tester"), any(LocalDateTime.class));
    }

    @Test
    void submitBatchAssessmentsSkipsMissingAssessmentId() {
        BatchAssessmentSubmitResult result = personnelService.submitBatchAssessments(new BatchAssessmentSubmitRequest(
                "02108",
                "2024",
                false,
                List.of(new BatchAssessmentSubmitItem(1, null, "02108", "00001"))));

        assertEquals(0, result.submitted());
        assertEquals(1, result.skipped());
        assertEquals("考核记录尚未保存，请先保存后再提交申报。", result.failures().getFirst().message());
    }

    @Test
    void submitBatchAssessmentsSkipsNonDraftRecords() {
        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));
        when(personnelRepository.findAssessmentById(9)).thenReturn(Optional.of(sampleAssessment("申报")));
        doThrow(new IllegalArgumentException("仅草稿状态的子记录可以提交申报。"))
                .when(personnelSubrecordEditPolicy).validateSubmit("申报");
        when(personnelRepository.findPersonnelSummary(new PersonKey("02108", "00001")))
                .thenReturn(Optional.of(new PersonnelSummary(
                        1,
                        "02108",
                        "测试单位",
                        "00001",
                        "张三",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "草稿",
                        false,
                        false,
                        "")));

        BatchAssessmentSubmitResult result = personnelService.submitBatchAssessments(new BatchAssessmentSubmitRequest(
                "02108",
                "2024",
                false,
                List.of(new BatchAssessmentSubmitItem(1, 9, "02108", "00001"))));

        assertEquals(0, result.submitted());
        assertEquals(1, result.skipped());
        assertEquals("仅草稿状态的子记录可以提交申报。", result.failures().getFirst().message());
    }

    private static AssessmentRecord sampleAssessment(String approvalStatus) {
        return new AssessmentRecord(
                9,
                "02108",
                "00001",
                "2024",
                "称职",
                approvalStatus,
                false,
                null,
                null,
                null,
                null,
                0);
    }
}
