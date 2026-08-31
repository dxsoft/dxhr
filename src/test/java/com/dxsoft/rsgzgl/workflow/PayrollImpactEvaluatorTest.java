package com.dxsoft.rsgzgl.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.payroll.PayrollRepository;
import com.dxsoft.rsgzgl.personnel.PersonnelMaintenanceRecord;
import com.dxsoft.rsgzgl.personnel.PersonnelRepository;
import com.dxsoft.rsgzgl.personnel.RankRecord;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PayrollImpactEvaluatorTest {

    private PayrollRepository payrollRepository;
    private PersonnelRepository personnelRepository;
    private PayrollImpactEvaluator evaluator;

    @BeforeEach
    void setUp() {
        payrollRepository = mock(PayrollRepository.class);
        personnelRepository = mock(PersonnelRepository.class);
        evaluator = new PayrollImpactEvaluator(payrollRepository, personnelRepository);
    }

    @Test
    void mainApprovalQueuesNewPersonnelSalaryWhenCandidateExists() {
        stubPerson(1);
        when(payrollRepository.findNewPersonnelSalaryCandidate(1)).thenReturn(Optional.of(mock(com.dxsoft.rsgzgl.payroll.NewPersonnelSalaryCandidate.class)));

        Optional<PayrollImpactEvaluator.PayrollImpactResult> result =
                evaluator.evaluate(1, PayrollWorkflowSourceType.MAIN, null);

        assertThat(result).isPresent();
        assertThat(result.get().module()).isEqualTo(PayrollWorkflowModule.NEW_PERSONNEL_SALARY);
    }

    @Test
    void educationApprovalQueuesWhenCandidateListContainsUid() {
        stubPerson(1);
        when(payrollRepository.findEducationPromotionCandidateUids(OrganizationScope.unrestricted(), "001", null))
                .thenReturn(List.of(1));

        Optional<PayrollImpactEvaluator.PayrollImpactResult> result =
                evaluator.evaluate(1, PayrollWorkflowSourceType.EDUCATION, 9);

        assertThat(result).isPresent();
        assertThat(result.get().module()).isEqualTo(PayrollWorkflowModule.EDUCATION_PROMOTION);
    }

    @Test
    void rankApprovalRoutesToPoliceModuleByDefault() {
        stubPerson(1);
        when(personnelRepository.findRankById(5)).thenReturn(Optional.of(new RankRecord(
                5, "001", "00001", "一级警督", "202401", "", "", 1, "jx",
                "审批通过", null, null, null, null, 0)));

        Optional<PayrollImpactEvaluator.PayrollImpactResult> result =
                evaluator.evaluate(1, PayrollWorkflowSourceType.RANK, 5);

        assertThat(result).isPresent();
        assertThat(result.get().module()).isEqualTo(PayrollWorkflowModule.POLICE_RANK_CHANGE_PROMOTION);
    }

    @Test
    void awardApprovalDoesNotQueue() {
        stubPerson(1);

        Optional<PayrollImpactEvaluator.PayrollImpactResult> result =
                evaluator.evaluate(1, PayrollWorkflowSourceType.AWARD, 3);

        assertThat(result).isEmpty();
    }

    private void stubPerson(int uid) {
        when(personnelRepository.findMaintenanceByUid(uid)).thenReturn(Optional.of(new PersonnelMaintenanceRecord(
                uid, "001", "测试单位", "", "", "00001", "张三",
                "", "", "", "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "",
                "审批通过", "", "", false, null, null, null, null)));
    }
}
