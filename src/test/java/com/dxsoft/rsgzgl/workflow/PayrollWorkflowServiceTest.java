package com.dxsoft.rsgzgl.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.exchange.notification.ExchangeDeploymentProperties;
import com.dxsoft.rsgzgl.exchange.notification.ExchangeNotificationService;
import com.dxsoft.rsgzgl.personnel.PersonnelMaintenanceRecord;
import com.dxsoft.rsgzgl.personnel.PersonnelRepository;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PayrollWorkflowServiceTest {

    private PayrollWorkflowRepository repository;
    private PayrollImpactEvaluator impactEvaluator;
    private PersonnelRepository personnelRepository;
    private ExchangeNotificationService exchangeNotificationService;
    private PayrollWorkflowService service;

    @BeforeEach
    void setUp() {
        repository = mock(PayrollWorkflowRepository.class);
        impactEvaluator = mock(PayrollImpactEvaluator.class);
        personnelRepository = mock(PersonnelRepository.class);
        exchangeNotificationService = mock(ExchangeNotificationService.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.hasPermission("PAYROLL_READ")).thenReturn(true);
        when(accessControlService.currentUser()).thenReturn(new AppUserPrincipal(
                1L, "tester", "hash", "测试员", true, Set.of(), Set.of("PAYROLL_READ"),
                true, Set.of(), null, null, null));

        PayrollWorkflowProperties properties = new PayrollWorkflowProperties();
        properties.setEnabled(true);
        properties.setAutoQueueOnApprove(true);
        ExchangeDeploymentProperties deploymentProperties = new ExchangeDeploymentProperties();
        deploymentProperties.setDeploymentMode("SHARED");

        service = new PayrollWorkflowService(
                repository,
                impactEvaluator,
                personnelRepository,
                exchangeNotificationService,
                properties,
                deploymentProperties,
                accessControlService);
    }

    @Test
    void onPersonnelApprovedCreatesPendingWorkflowWhenImpactDetected() {
        PersonnelMaintenanceRecord person = samplePerson();
        when(personnelRepository.findMaintenanceByUid(1)).thenReturn(Optional.of(person));
        when(impactEvaluator.evaluate(1, PayrollWorkflowSourceType.EDUCATION, 9)).thenReturn(Optional.of(
                new PayrollImpactEvaluator.PayrollImpactResult(
                        PayrollWorkflowModule.EDUCATION_PROMOTION,
                        "学历变化",
                        PayrollWorkflowSourceType.EDUCATION,
                        9,
                        "张三 学历信息审批通过，待办理学历工资变动")));
        when(repository.existsPendingBySource("EDUCATION", 9)).thenReturn(false);
        when(repository.insert(any())).thenReturn(100L);

        service.onPersonnelApproved(1, PayrollWorkflowSourceType.EDUCATION, 9);

        verify(repository).insert(any());
        verify(exchangeNotificationService).onPayrollWorkflowPending(
                eq(100L), eq(1), eq("001"), eq("00001"), eq("张三"), eq("EDUCATION"), eq(9),
                eq("EDUCATION_PROMOTION"), any());
        verify(exchangeNotificationService).onPayrollWorkflowStarted(
                eq(100L), eq(1), eq("001"), eq("00001"), eq("张三"), eq("EDUCATION"), eq(9), any());
    }

    @Test
    void onPersonnelApprovedSkipsWhenWorkflowDisabled() {
        PayrollWorkflowProperties properties = new PayrollWorkflowProperties();
        properties.setEnabled(false);
        ExchangeDeploymentProperties deploymentProperties = new ExchangeDeploymentProperties();
        deploymentProperties.setDeploymentMode("SHARED");
        PayrollWorkflowService disabled = new PayrollWorkflowService(
                repository,
                impactEvaluator,
                personnelRepository,
                exchangeNotificationService,
                properties,
                deploymentProperties,
                mock(AccessControlService.class));

        disabled.onPersonnelApproved(1, PayrollWorkflowSourceType.MAIN, null);

        verify(repository, never()).insert(any());
    }

    @Test
    void onPayrollAppliedCompletesEarliestPendingWorkflow() {
        when(repository.findEarliestPending(1, PayrollWorkflowModule.EDUCATION_PROMOTION.name()))
                .thenReturn(Optional.of(new PayrollWorkflowRecord(
                        100L, "wf1", 1, "001", "00001", "张三", "EDUCATION", 9,
                        "EDUCATION_PROMOTION", "学历变化", PayrollWorkflowStatus.PAYROLL_PENDING,
                        null, null, null, "tester", null, "摘要")));
        when(repository.complete(eq(100L), eq("H123"), eq("tester"), any())).thenReturn(true);

        service.onPayrollApplied(1, "学历变化", "H123");

        verify(repository).complete(eq(100L), eq("H123"), eq("tester"), any());
        verify(exchangeNotificationService).onPayrollWorkflowDone(
                eq(100L), eq(1), eq("001"), eq("00001"), eq("张三"), eq("EDUCATION"), eq(9),
                eq("学历变化"), any());
    }

    private static PersonnelMaintenanceRecord samplePerson() {
        return new PersonnelMaintenanceRecord(
                1, "001", "测试单位", "", "", "00001", "张三",
                "", "", "", "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "",
                "草稿", "", "", false, null, null, null, null);
    }
}
