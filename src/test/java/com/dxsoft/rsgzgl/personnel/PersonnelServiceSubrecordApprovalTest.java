package com.dxsoft.rsgzgl.personnel;



import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.exchange.notification.ExchangeNotificationService;
import com.dxsoft.rsgzgl.workflow.PayrollWorkflowService;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.retirement.RetirementService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
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

import org.springframework.security.access.AccessDeniedException;



@ExtendWith(MockitoExtension.class)

class PersonnelServiceSubrecordApprovalTest {



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
    }

    @Test

    void approveEducationUpdatesStatusAndReloadsList() {

        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));

        when(personnelRepository.findEducationById(9)).thenReturn(Optional.of(sampleEducation("申报")));

        when(personnelRepository.findEducation(new PersonKey("02108", "00001"))).thenReturn(List.of(sampleEducation("审批通过")));



        List<EducationRecord> rows = personnelService.approveEducation(1, 9);



        verify(personnelSubrecordEditPolicy).validateApprove("申报");

        verify(personnelRepository).updateSubrecordApprovalApprove(
                eq(PersonnelSubrecordType.EDUCATION), eq(9), eq("tester"), any(LocalDateTime.class));

        assertEquals("审批通过", rows.getFirst().approvalStatus());

    }



    @Test

    void cancelEducationReturnsToDraft() {

        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));

        when(personnelRepository.findEducationById(9)).thenReturn(Optional.of(sampleEducation("审批通过")));

        when(personnelRepository.findEducation(new PersonKey("02108", "00001"))).thenReturn(List.of(sampleEducation("草稿")));



        List<EducationRecord> rows = personnelService.cancelEducationApproval(1, 9, null);



        verify(personnelRepository).updateSubrecordApprovalDraft(PersonnelSubrecordType.EDUCATION, 9);

        assertEquals("草稿", rows.getFirst().approvalStatus());

    }



    @Test

    void submitEducationMovesDraftToSubmitted() {

        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));

        when(personnelRepository.findEducationById(9)).thenReturn(Optional.of(sampleEducation("草稿")));

        when(personnelRepository.findEducation(new PersonKey("02108", "00001"))).thenReturn(List.of(sampleEducation("申报")));



        List<EducationRecord> rows = personnelService.submitEducation(1, 9);



        verify(personnelSubrecordEditPolicy).validateSubmit("草稿");

        verify(personnelRepository).updateSubrecordApprovalSubmit(
                eq(PersonnelSubrecordType.EDUCATION), eq(9), eq("tester"), any(LocalDateTime.class));

        assertEquals("申报", rows.getFirst().approvalStatus());

    }



    @Test

    void updateEducationBlockedWhenApproved() {

        when(personnelRepository.findEducationKeyById(9)).thenReturn(Optional.of(new PersonKey("02108", "00001")));

        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));

        when(personnelRepository.findEducationById(9)).thenReturn(Optional.of(sampleEducation("审批通过")));

        doThrow(new AccessDeniedException("locked"))

                .when(personnelSubrecordEditPolicy).assertEditable("审批通过");



        assertThrows(AccessDeniedException.class, () ->

                personnelService.updateEducation(1, 9, new EducationMaintenanceRequest(

                        "01", "本科", "测试大学", "2000-09", "2004-07", 4, "普通全日制", "")));

    }



    @Test
    void submitAwardMovesDraftToSubmitted() {
        when(personnelRepository.findKeyByUid(1)).thenReturn(Optional.of(new PersonKey("02108", "00001")));
        when(personnelRepository.findAwardById(12)).thenReturn(Optional.of(sampleAward("草稿")));
        when(personnelRepository.findAwards(new PersonKey("02108", "00001"))).thenReturn(List.of(sampleAward("申报")));

        List<AwardRecord> rows = personnelService.submitAward(1, 12);

        verify(personnelSubrecordEditPolicy).validateSubmit("草稿");
        verify(personnelRepository).updateSubrecordApprovalSubmit(
                eq(PersonnelSubrecordType.AWARD), eq(12), eq("tester"), any(LocalDateTime.class));
        assertEquals("申报", rows.getFirst().approvalStatus());
    }

    private static EducationRecord sampleEducation(String approvalStatus) {

        return new EducationRecord(

                9,

                "02108",

                "00001",

                "01",

                "本科",

                "测试大学",

                "2000-09",

                "2004-07",

                4,

                "普通全日制",

                "",

                approvalStatus,

                false,
                null,
                null,
                null,
                null,
                0);

    }

    private static AwardRecord sampleAward(String approvalStatus) {
        return new AwardRecord(
                12,
                "02108",
                "00001",
                "先进个人",
                "某单位",
                "奖励",
                "2020.01",
                "202001",
                "",
                1,
                2,
                approvalStatus,
                null,
                null,
                null,
                null,
                0);
    }

}


