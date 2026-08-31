package com.dxsoft.rsgzgl.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.dictionary.DictionaryService;
import com.dxsoft.rsgzgl.exchange.notification.ExchangeNotificationService;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.retirement.RetirementService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;
import com.dxsoft.rsgzgl.security.SecurityAuditService;
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
class PersonnelServiceBatchApprovalTest {

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
    private SecurityAuditService securityAuditService;

    @Mock
    private DictionaryService dictionaryService;

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
        lenient().when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(true);
    }

    @Test
    void batchApproveTrackingRecords_submitsAssessment() {
        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));
        when(personnelRepository.findAssessmentById(9)).thenReturn(Optional.of(sampleAssessment("申报")));
        when(personnelRepository.findAssessments(new PersonKey("02108", "00001"))).thenReturn(List.of(sampleAssessment("审批通过")));
        when(personnelRepository.findMaintenanceByUid(1)).thenReturn(Optional.of(new PersonnelMaintenanceRecord(
                1, "02108", "测试单位", "", "", "00001", "张三",
                "", "", "", "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "",
                "申报", "", "", false, null, null, null, null)));

        BatchApprovalResult result = personnelService.batchApproveTrackingRecords(new BatchApprovalRequest(
                List.of(new BatchApprovalItem(1, "assessment", 9))));

        assertEquals(1, result.approved());
        assertEquals(0, result.skipped());
        verify(personnelSubrecordEditPolicy).validateApprove("申报");
        verify(personnelRepository).updateSubrecordApprovalApprove(
                eq(PersonnelSubrecordType.ASSESSMENT), eq(9), eq("tester"), any(LocalDateTime.class));
    }

    @Test
    void batchApproveTrackingRecords_skipsNonSubmittedAssessment() {
        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));
        when(personnelRepository.findAssessmentById(9)).thenReturn(Optional.of(sampleAssessment("审批通过")));
        doThrow(new IllegalArgumentException("仅申报状态的子记录可以审核通过。"))
                .when(personnelSubrecordEditPolicy).validateApprove("审批通过");
        when(personnelRepository.findPersonnelSummary(new PersonKey("02108", "00001")))
                .thenReturn(Optional.of(samplePerson("00001", "张三")));

        BatchApprovalResult result = personnelService.batchApproveTrackingRecords(new BatchApprovalRequest(
                List.of(new BatchApprovalItem(1, "assessment", 9))));

        assertEquals(0, result.approved());
        assertEquals(1, result.skipped());
        assertEquals("仅申报状态的子记录可以审核通过。", result.failures().getFirst().message());
    }

    private static AssessmentRecord sampleAssessment(String approvalStatus) {
        return new AssessmentRecord(
                9,
                "02108",
                "00001",
                "2025",
                "优秀",
                approvalStatus,
                false,
                null,
                null,
                null,
                null,
                0);
    }

    private static PersonnelSummary samplePerson(String personCode, String name) {
        return new PersonnelSummary(
                1,
                "02108",
                "测试单位",
                personCode,
                name,
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
                "");
    }
}
