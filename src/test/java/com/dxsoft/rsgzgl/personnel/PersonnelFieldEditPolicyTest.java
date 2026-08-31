package com.dxsoft.rsgzgl.personnel;



import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.when;



import com.dxsoft.rsgzgl.security.AccessControlService;

import com.dxsoft.rsgzgl.security.AppUserPrincipal;

import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;

import java.util.List;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;



@ExtendWith(MockitoExtension.class)

class PersonnelFieldEditPolicyTest {



    @Mock

    private AccessControlService accessControlService;



    @Mock

    private FieldMetadataRepository fieldMetadataRepository;



    @InjectMocks

    private PersonnelFieldEditPolicy policy;



    @AfterEach

    void clearContext() {

        SecurityContextHolder.clearContext();

    }



    @Test

    void administrativeUnitLoadsSharedAndAdministrativeFieldsOnly() {

        authenticate(Set.of(PersonnelFeaturePermissions.BASIC_WRITE, PersonnelFeaturePermissions.KEY_FIELD_WRITE), true);

        when(accessControlService.currentUser()).thenReturn(currentUser(true));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(false);

        when(fieldMetadataRepository.findRyjbxxFields("行政")).thenReturn(List.of(

                field("xm", "00", false),

                field("zwjb", "01", false),

                field("jbgzse2", "10", true)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("行政", "初始建库", "", false));



        assertEquals(2, view.fields().size());

        assertTrue(view.fields().stream().anyMatch(entry -> "xm".equals(entry.fieldName())));

        assertTrue(view.fields().stream().anyMatch(entry -> "zwjb".equals(entry.fieldName())));

        assertFalse(view.fields().stream().anyMatch(entry -> "jbgzse2".equals(entry.fieldName())));

        assertEquals("草稿", view.approvalStatus());

        assertTrue(view.canSubmit());

    }



    @Test

    void approvedRecordBlocksAllUsersFromEditing() {

        authenticate(Set.of(PersonnelFeaturePermissions.BASIC_WRITE), true);

        when(accessControlService.currentUser()).thenReturn(currentUser(true));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(false);

        when(fieldMetadataRepository.findRyjbxxFields("事业")).thenReturn(List.of(field("xm", "00", false)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("事业", "审批通过", "已定工资", true));



        assertFalse(view.canEditBasic());

        assertFalse(view.canDelete());

        assertEquals("人员信息已审核通过，关键信息不可修改。需具有审核权限的用户取消审核后方可修改。", view.blockReason());

        assertFalse(view.fields().getFirst().editable());

        assertEquals("审批通过", view.approvalStatus());

        assertFalse(view.canCancelApproval());

    }



    @Test

    void approvalWriteAllowsCancelApprovalWhenApproved() {

        authenticate(Set.of(PersonnelFeaturePermissions.APPROVAL_WRITE), true);

        when(accessControlService.currentUser()).thenReturn(currentUser(true));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(true);

        when(fieldMetadataRepository.findRyjbxxFields("事业")).thenReturn(List.of(field("xm", "00", false)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("事业", "审批通过", "已定工资", true));



        assertTrue(view.canChangeApproval());

        assertTrue(view.canCancelApproval());

        assertFalse(view.canEditBasic());

    }



    @Test

    void submittedRecordBlocksBasicWrite() {

        authenticate(Set.of(PersonnelFeaturePermissions.BASIC_WRITE), false);

        when(accessControlService.currentUser()).thenReturn(currentUser(false));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(false);

        when(fieldMetadataRepository.findRyjbxxFields("事业")).thenReturn(List.of(field("xm", "00", false)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("事业", "申报", "已定工资", true));



        assertFalse(view.canEditBasic());

        assertEquals("申报", view.approvalStatus());

        assertFalse(view.canCancelApproval());

        assertFalse(view.canSubmit());

        assertTrue(view.blockReason().contains("已提交申报"));

    }



    @Test

    void submittedRecordAllowsApproverActions() {

        authenticate(Set.of(PersonnelFeaturePermissions.APPROVAL_WRITE), true);

        when(accessControlService.currentUser()).thenReturn(currentUser(true));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(true);

        when(fieldMetadataRepository.findRyjbxxFields("事业")).thenReturn(List.of(field("xm", "00", false)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("事业", "申报", "已定工资", true));



        assertTrue(view.canApprove());

        assertTrue(view.canReturnToDraft());

        assertFalse(view.canSubmit());

    }



    @Test

    void salaryFieldRequiresKeyFieldWriteWhenEstablished() {

        authenticate(Set.of(PersonnelFeaturePermissions.BASIC_WRITE), true);

        when(accessControlService.currentUser()).thenReturn(currentUser(true));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(false);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(false);

        when(fieldMetadataRepository.findRyjbxxFields("事业")).thenReturn(List.of(

                field("xm", "00", false),

                field("gznx", "00", true)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("事业", "草稿", "已定工资", true));



        PersonnelFieldPolicyEntry salaryField = view.fields().stream()

                .filter(entry -> "gznx".equals(entry.fieldName()))

                .findFirst()

                .orElseThrow();

        assertFalse(salaryField.editable());

        assertEquals("需要关键工资字段维护权限", salaryField.readOnlyReason());

    }



    @Test

    void libraryCompleteLocksPersonCodeForUnitScopedUser() {

        authenticate(Set.of(PersonnelFeaturePermissions.BASIC_WRITE, PersonnelFeaturePermissions.KEY_FIELD_WRITE), false);

        when(accessControlService.currentUser()).thenReturn(currentUser(false));

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE)).thenReturn(true);

        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(false);

        when(fieldMetadataRepository.findRyjbxxFields("行政")).thenReturn(List.of(field("grbm", "00", false)));



        PersonnelFieldPolicyView view = policy.evaluate(sampleRecord("行政", "草稿", "", true));



        PersonnelFieldPolicyEntry personCode = view.fields().getFirst();

        assertFalse(personCode.editable());

        assertEquals("本单位建库已结束，不能修改个人编码。", personCode.readOnlyReason());

    }



    private static FieldMetadataRecord field(String name, String category, boolean salaryField) {

        return new FieldMetadataRecord(name, name, category, salaryField, false, true, 1);

    }



    private static PersonnelMaintenanceRecord sampleRecord(

            String unitProperty, String approvalStatus, String salaryStatus, boolean libraryComplete) {

        return new PersonnelMaintenanceRecord(

                1,

                "02108",

                "二高",

                unitProperty,

                "",

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

                0,

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

                "",

                approvalStatus,

                salaryStatus,

                "",

                libraryComplete,
                null,
                null,
                null,
                null);

    }



    private static void authenticate(Set<String> permissions, boolean allOrganizations) {

        AppUserPrincipal user = currentUser(allOrganizations);

        UsernamePasswordAuthenticationToken authentication =

                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

    }



    private static AppUserPrincipal currentUser(boolean allOrganizations) {

        return new AppUserPrincipal(

                1L,

                "02108",

                "hash",

                "二高",

                true,

                Set.of(),

                Set.of(PersonnelFeaturePermissions.BASIC_WRITE),

                allOrganizations,

                Set.of("02108"),

                "02108",

                null,

                null);

    }

}


