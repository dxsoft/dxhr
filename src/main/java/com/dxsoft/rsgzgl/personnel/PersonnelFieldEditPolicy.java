package com.dxsoft.rsgzgl.personnel;



import com.dxsoft.rsgzgl.organization.UnitPayrollClassification;

import com.dxsoft.rsgzgl.security.AccessControlService;

import com.dxsoft.rsgzgl.security.AppUserPrincipal;

import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import org.springframework.stereotype.Service;



@Service

public class PersonnelFieldEditPolicy {



    private static final String SALARY_ESTABLISHED = "已定工资";



    private final AccessControlService accessControlService;

    private final FieldMetadataRepository fieldMetadataRepository;



    PersonnelFieldEditPolicy(AccessControlService accessControlService, FieldMetadataRepository fieldMetadataRepository) {

        this.accessControlService = accessControlService;

        this.fieldMetadataRepository = fieldMetadataRepository;

    }



    public PersonnelFieldPolicyView evaluate(PersonnelMaintenanceRecord record) {

        AppUserPrincipal user = accessControlService.currentUser();

        boolean hasBasicWrite = hasBasicWritePermission();

        boolean hasKeyFieldWrite = accessControlService.hasPermission(PersonnelFeaturePermissions.KEY_FIELD_WRITE);

        boolean hasApprovalWrite = hasApprovalWritePermission();

        boolean unitScoped = !user.allOrganizations();

        boolean libraryComplete = record.organizationLibraryComplete();

        String approvalStatus = PersonnelApprovalStatuses.normalize(record.approvalStatus());

        String salaryStatus = normalize(record.salaryStatus());

        String unitProperty = resolveFieldMetadataUnitCategory(record);



        String blockReason = null;

        boolean editableBasic = hasBasicWrite && PersonnelApprovalStatuses.isDraft(approvalStatus);

        boolean deletable = hasBasicWrite && PersonnelApprovalStatuses.isDraft(approvalStatus);

        if (PersonnelApprovalStatuses.isApproved(approvalStatus)) {

            blockReason = "人员信息已审核通过，基本信息不可修改；草稿状态的任职、学历等子记录仍可在对应页签中编辑。如需修改基本信息，需具有审核权限的用户取消审核。";

        } else if (PersonnelApprovalStatuses.isSubmitted(approvalStatus)) {

            blockReason = "已提交申报，待审核期间不能修改。如需修改请由审核员退回草稿。";

        }

        final boolean canEditBasic = editableBasic;

        final boolean canDelete = deletable;



        List<FieldMetadataRecord> metadata = fieldMetadataRepository.findRyjbxxFields(unitProperty);

        List<PersonnelFieldPolicyEntry> fields = new ArrayList<>();

        for (FieldMetadataRecord field : metadata) {

            PersonnelBasicFieldRegistry.bindingForColumn(field.fieldName()).ifPresent(binding -> {

                boolean visible = true;

                boolean editable = canEditBasic;

                String reason = null;

                if (field.readOnly()) {

                    editable = false;

                    reason = "字段只读";

                } else if (field.salaryField() && !fieldEditableForSalaryField(

                        hasKeyFieldWrite, approvalStatus, salaryStatus, libraryComplete)) {

                    editable = false;

                    reason = hasKeyFieldWrite

                            ? "当前状态不允许修改工资字段"

                            : "需要关键工资字段维护权限";

                }

                if ("grbm".equalsIgnoreCase(field.fieldName())

                        && libraryComplete

                        && unitScoped

                        && canEditBasic) {

                    editable = false;

                    reason = "本单位建库已结束，不能修改个人编码。";

                }

                fields.add(new PersonnelFieldPolicyEntry(

                        field.fieldName(),

                        binding.requestProperty(),

                        binding.elementId(),

                        visible,

                        editable,

                        reason,

                        field.category(),

                        field.salaryField()));

            });

        }

        boolean canSave = canEditBasic && fields.stream().anyMatch(PersonnelFieldPolicyEntry::editable);

        boolean canSubmit = hasBasicWrite && PersonnelApprovalStatuses.isDraft(approvalStatus);

        boolean canApprove = hasApprovalWrite && PersonnelApprovalStatuses.isSubmitted(approvalStatus);

        boolean canReturnToDraft = hasApprovalWrite && PersonnelApprovalStatuses.isSubmitted(approvalStatus);

        boolean canCancelApproval = hasApprovalWrite && PersonnelApprovalStatuses.isApproved(approvalStatus);

        return new PersonnelFieldPolicyView(

                canEditBasic,

                canDelete,

                canSave,

                blockReason,

                approvalStatus,

                hasApprovalWrite,

                canSubmit,

                canApprove,

                canReturnToDraft,

                canCancelApproval,

                List.copyOf(fields));

    }



    public void validateApprovalSubmit(PersonnelMaintenanceRecord existing) {

        if (!hasBasicWritePermission()) {

            throw new org.springframework.security.access.AccessDeniedException(

                    PersonnelFeaturePermissions.BASIC_WRITE + " permission required");

        }

        if (!PersonnelApprovalStatuses.isDraft(existing.approvalStatus())) {

            throw new IllegalArgumentException("仅草稿状态的人员可以提交申报。");

        }

    }



    public void validateApprovalApprove(PersonnelMaintenanceRecord existing) {

        if (!hasApprovalWritePermission()) {

            throw new org.springframework.security.access.AccessDeniedException(

                    PersonnelFeaturePermissions.APPROVAL_WRITE + " permission required");

        }

        if (!PersonnelApprovalStatuses.isSubmitted(existing.approvalStatus())) {

            throw new IllegalArgumentException("仅申报状态的人员可以审核通过。");

        }

    }



    public void validateApprovalReturnToDraft(PersonnelMaintenanceRecord existing) {

        if (!hasApprovalWritePermission()) {

            throw new org.springframework.security.access.AccessDeniedException(

                    PersonnelFeaturePermissions.APPROVAL_WRITE + " permission required");

        }

        if (!PersonnelApprovalStatuses.isSubmitted(existing.approvalStatus())) {

            throw new IllegalArgumentException("仅申报状态的人员可以退回草稿。");

        }

    }



    public void validateApprovalCancel(PersonnelMaintenanceRecord existing) {

        if (!hasApprovalWritePermission()) {

            throw new org.springframework.security.access.AccessDeniedException(

                    PersonnelFeaturePermissions.APPROVAL_WRITE + " permission required");

        }

        if (!PersonnelApprovalStatuses.isApproved(existing.approvalStatus())) {

            throw new IllegalArgumentException("仅审批通过的人员可以取消审核。");

        }

    }



    public void validateUpdate(PersonnelMaintenanceRecord existing, PersonnelMaintenanceRequest request) {

        if (!hasBasicWritePermission()) {

            throw new org.springframework.security.access.AccessDeniedException(

                    PersonnelFeaturePermissions.BASIC_WRITE + " permission required");

        }

        PersonnelFieldPolicyView policy = evaluate(existing);

        if (!policy.canEditBasic()) {

            throw new org.springframework.security.access.AccessDeniedException(

                    policy.blockReason() == null ? "当前账号不能修改该人员基本信息。" : policy.blockReason());

        }

        Map<String, PersonnelFieldPolicyEntry> byProperty = indexByRequestProperty(policy.fields());

        for (Map.Entry<String, String> change : changedRequestProperties(existing, request).entrySet()) {

            PersonnelFieldPolicyEntry entry = byProperty.get(change.getKey());

            if (entry == null) {

                continue;

            }

            if (!entry.editable()) {

                throw new IllegalArgumentException("字段不允许修改：" + (entry.readOnlyReason() == null ? change.getKey() : entry.readOnlyReason()));

            }

        }

    }



    private Map<String, PersonnelFieldPolicyEntry> indexByRequestProperty(List<PersonnelFieldPolicyEntry> fields) {

        Map<String, PersonnelFieldPolicyEntry> indexed = new LinkedHashMap<>();

        for (PersonnelFieldPolicyEntry field : fields) {

            indexed.put(field.requestProperty(), field);

        }

        return indexed;

    }



    private Map<String, String> changedRequestProperties(PersonnelMaintenanceRecord existing, PersonnelMaintenanceRequest request) {

        Map<String, String> changed = new LinkedHashMap<>();

        compare(changed, "organizationCode", existing.organizationCode(), request.organizationCode());

        compare(changed, "personCode", existing.personCode(), request.personCode());

        compare(changed, "name", existing.name(), request.name());

        compare(changed, "idCard", existing.idCard(), request.idCard());

        compare(changed, "gender", existing.gender(), request.gender());

        compare(changed, "birthYearMonth", existing.birthYearMonth(), request.birthYearMonth());

        compare(changed, "personnelCategory", existing.personnelCategory(), request.personnelCategory());

        compare(changed, "organizationType", existing.organizationType(), request.organizationType());

        compare(changed, "postCategory", existing.postCategory(), request.postCategory());

        compare(changed, "workStartYearMonth", existing.workStartYearMonth(), request.workStartYearMonth());

        compare(changed, "regularizationYearMonth", existing.regularizationYearMonth(), request.regularizationYearMonth());

        compare(changed, "joinYearMonth", existing.joinYearMonth(), request.joinYearMonth());

        compare(changed, "joinType", existing.joinType(), request.joinType());

        compareInteger(changed, "salaryYears", existing.salaryYears(), request.salaryYears());

        compare(changed, "educationCode", existing.educationCode(), request.educationCode());

        compare(changed, "highestEducation", existing.highestEducation(), request.highestEducation());

        compare(changed, "currentPositionLevel", existing.currentPositionLevel(), request.currentPositionLevel());

        compare(changed, "currentRankCode", existing.currentRankCode(), request.currentRankCode());

        compare(changed, "currentPosition", existing.currentPosition(), request.currentPosition());

        compare(changed, "currentPositionStartYearMonth", existing.currentPositionStartYearMonth(), request.currentPositionStartYearMonth());

        compare(changed, "ethnicity", existing.ethnicity(), request.ethnicity());

        compare(changed, "politicalStatus", existing.politicalStatus(), request.politicalStatus());

        compare(changed, "archiveNumber", existing.archiveNumber(), request.archiveNumber());

        return changed;

    }



    private void compare(Map<String, String> changed, String property, String before, String after) {

        if (!Objects.equals(normalize(before), normalize(after))) {

            changed.put(property, after);

        }

    }



    private void compareInteger(Map<String, String> changed, String property, Integer before, Integer after) {

        if (!Objects.equals(before, after)) {

            changed.put(property, after == null ? null : String.valueOf(after));

        }

    }



    private boolean fieldEditableForSalaryField(

            boolean hasKeyFieldWrite,

            String approvalStatus,

            String salaryStatus,

            boolean libraryComplete) {

        if (hasKeyFieldWrite) {

            return true;

        }

        if (PersonnelApprovalStatuses.SUBMITTED.equals(approvalStatus)) {

            return true;

        }

        if (!SALARY_ESTABLISHED.equals(salaryStatus)) {

            return true;

        }

        return !libraryComplete;

    }



    private boolean hasBasicWritePermission() {

        return accessControlService.hasPermission(PersonnelFeaturePermissions.BASIC_WRITE);

    }



    private boolean hasApprovalWritePermission() {

        return accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE);

    }



    private String resolveFieldMetadataUnitCategory(PersonnelMaintenanceRecord record) {
        return UnitPayrollClassification.effectiveUnitCategory(
                record.organizationCategory(),
                record.organizationPayrollCategory());
    }

    private String resolveUnitProperty(PersonnelMaintenanceRecord record) {

        if (record.organizationProperty() != null && !record.organizationProperty().isBlank()) {

            return record.organizationProperty();

        }

        return record.organizationType();

    }



    private String normalize(String value) {

        return value == null || value.isBlank() ? null : value.trim();

    }

}


