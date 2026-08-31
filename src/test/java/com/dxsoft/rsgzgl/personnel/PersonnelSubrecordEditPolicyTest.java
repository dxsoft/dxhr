package com.dxsoft.rsgzgl.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class PersonnelSubrecordEditPolicyTest {

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private PersonnelSubrecordEditPolicy policy;

    @Test
    void approvedSubrecordIsNotEditable() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)).thenReturn(true);

        AccessDeniedException error = assertThrows(AccessDeniedException.class,
                () -> policy.assertEditable("审批通过"));

        assertTrue(error.getMessage().contains("已审核通过"));
    }

    @Test
    void submittedSubrecordIsNotEditable() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)).thenReturn(true);

        AccessDeniedException error = assertThrows(AccessDeniedException.class,
                () -> policy.assertEditable("申报"));

        assertTrue(error.getMessage().contains("已提交申报"));
    }

    @Test
    void draftSubrecordIsEditableWithWritePermission() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)).thenReturn(true);

        policy.assertEditable("草稿");
        policy.assertEditable("初始建库");

        assertTrue(policy.canEdit("草稿"));
        assertTrue(policy.canSubmit("初始建库"));
    }

    @Test
    void approveRequiresSubmittedStatus() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(true);

        policy.validateApprove("申报");
        assertTrue(policy.canApprove("申报"));
        assertFalse(policy.canApprove("草稿"));

        assertThrows(IllegalArgumentException.class, () -> policy.validateApprove("草稿"));
        assertThrows(IllegalArgumentException.class, () -> policy.validateApprove("审批通过"));
    }

    @Test
    void cancelRequiresApprovalWriteAndApprovedStatus() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(true);

        policy.validateCancel("审批通过");
        assertTrue(policy.canCancelApproval("审批通过"));
        assertFalse(policy.canApprove("审批通过"));

        assertThrows(IllegalArgumentException.class, () -> policy.validateCancel("申报"));
    }

    @Test
    void returnToDraftRequiresSubmittedStatus() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(true);

        policy.validateReturnToDraft("申报");
        assertTrue(policy.canReturnToDraft("申报"));

        assertThrows(IllegalArgumentException.class, () -> policy.validateReturnToDraft("草稿"));
    }

    @Test
    void missingApprovalWriteBlocksApproveAndCancel() {
        when(accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> policy.validateApprove("申报"));
        assertThrows(AccessDeniedException.class, () -> policy.validateCancel("审批通过"));
    }

    @Test
    void defaultApprovalStatusIsDraft() {
        assertEquals("草稿", PersonnelSubrecordEditPolicy.defaultApprovalStatus());
        assertEquals("草稿", PersonnelApprovalStatuses.normalize("初始建库"));
    }
}
