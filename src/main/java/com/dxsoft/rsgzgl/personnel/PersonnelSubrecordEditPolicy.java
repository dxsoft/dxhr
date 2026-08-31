package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PersonnelSubrecordEditPolicy {

    static final String APPROVAL_PASSED = PersonnelApprovalStatuses.APPROVED;
    static final String APPROVAL_SUBMITTED = PersonnelApprovalStatuses.SUBMITTED;

    private final AccessControlService accessControlService;

    PersonnelSubrecordEditPolicy(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void assertEditable(String approvalStatus) {
        requireWrite();
        if (!PersonnelApprovalStatuses.isDraft(approvalStatus)) {
            if (PersonnelApprovalStatuses.isApproved(approvalStatus)) {
                throw new AccessDeniedException("该子记录已审核通过，不能修改或删除。需具有审核权限的用户取消审核后方可修改。");
            }
            throw new AccessDeniedException("该子记录已提交申报，待审核期间不能修改或删除。需由审核员退回草稿后方可修改。");
        }
    }

    public void validateSubmit(String approvalStatus) {
        requireWrite();
        if (!PersonnelApprovalStatuses.isDraft(approvalStatus)) {
            throw new IllegalArgumentException("仅草稿状态的子记录可以提交申报。");
        }
    }

    public void validateApprove(String approvalStatus) {
        requireApprovalWrite();
        if (PersonnelApprovalStatuses.isApproved(approvalStatus)) {
            throw new IllegalArgumentException("该子记录已审核通过。");
        }
        if (!PersonnelApprovalStatuses.isSubmitted(approvalStatus)) {
            throw new IllegalArgumentException("仅申报状态的子记录可以审核通过。");
        }
    }

    public void validateReturnToDraft(String approvalStatus) {
        requireApprovalWrite();
        if (!PersonnelApprovalStatuses.isSubmitted(approvalStatus)) {
            throw new IllegalArgumentException("仅申报状态的子记录可以退回草稿。");
        }
    }

    public void validateCancel(String approvalStatus) {
        requireApprovalWrite();
        if (!PersonnelApprovalStatuses.isApproved(approvalStatus)) {
            throw new IllegalArgumentException("仅审核通过的子记录可以取消审核。");
        }
    }

    public boolean canEdit(String approvalStatus) {
        return accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)
                && PersonnelApprovalStatuses.isDraft(approvalStatus);
    }

    public boolean canSubmit(String approvalStatus) {
        return accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)
                && PersonnelApprovalStatuses.isDraft(approvalStatus);
    }

    public boolean canApprove(String approvalStatus) {
        return accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)
                && PersonnelApprovalStatuses.isSubmitted(approvalStatus);
    }

    public boolean canReturnToDraft(String approvalStatus) {
        return accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)
                && PersonnelApprovalStatuses.isSubmitted(approvalStatus);
    }

    public boolean canCancelApproval(String approvalStatus) {
        return accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)
                && PersonnelApprovalStatuses.isApproved(approvalStatus);
    }

    static String defaultApprovalStatus() {
        return PersonnelApprovalStatuses.defaultStatus();
    }

    private void requireWrite() {
        if (!accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)) {
            throw new AccessDeniedException(PersonnelFeaturePermissions.LEGACY_WRITE + " permission required");
        }
    }

    private void requireApprovalWrite() {
        if (!accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)) {
            throw new AccessDeniedException(PersonnelFeaturePermissions.APPROVAL_WRITE + " permission required");
        }
    }
}
