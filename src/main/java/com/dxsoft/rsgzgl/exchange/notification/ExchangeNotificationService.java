package com.dxsoft.rsgzgl.exchange.notification;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.dataexchange.PersonnelExportRecord;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ExchangeNotificationService {

    static final String AUDIENCE_APPROVAL = "APPROVAL_CENTER";
    static final String AUDIENCE_ORGANIZATION = "ORGANIZATION";

    static final String DIRECTION_INTERNAL = "INTERNAL";
    static final String DIRECTION_OUTBOUND = "OUTBOUND";
    static final String DIRECTION_INBOUND = "INBOUND";

    private final ExchangeNotificationRepository repository;
    private final ExchangeDeploymentProperties deploymentProperties;
    private final AccessControlService accessControlService;

    ExchangeNotificationService(
            ExchangeNotificationRepository repository,
            ExchangeDeploymentProperties deploymentProperties,
            AccessControlService accessControlService) {
        this.repository = repository;
        this.deploymentProperties = deploymentProperties;
        this.accessControlService = accessControlService;
    }

    public PageResponse<ExchangeNotificationRecord> list(String status, PageRequest pageRequest) {
        requireReadPermission();
        return repository.list(status, pageRequest);
    }

    public long unreadCount() {
        requireReadPermission();
        return repository.countUnread();
    }

    public boolean markRead(long id) {
        requireReadPermission();
        return repository.markRead(id, currentUsername());
    }

    public int markAllRead() {
        requireReadPermission();
        return repository.markAllRead(currentUsername());
    }

    public String newBatchId() {
        return UUID.randomUUID().toString();
    }

    public void onSubmissionExported(List<PersonnelExportRecord> personnel, String batchId) {
        int count = personnel == null ? 0 : personnel.size();
        if (count == 0) {
            return;
        }
        Set<String> orgCodes = organizationCodes(personnel);
        String orgList = ExchangeNotificationRepository.joinOrganizationCodes(orgCodes);
        String primaryOrg = orgCodes.stream().findFirst().orElse(null);
        if (deploymentProperties.sharedDatabase()) {
            insert(
                    "SUBMISSION_PENDING",
                    DIRECTION_INTERNAL,
                    AUDIENCE_APPROVAL,
                    primaryOrg,
                    null,
                    primaryOrg,
                    orgList,
                    "SUBMISSION",
                    batchId,
                    count,
                    "收到申报包待审核：" + count + " 人",
                    "submission-review");
        } else {
            insert(
                    "SUBMISSION_PENDING",
                    DIRECTION_OUTBOUND,
                    AUDIENCE_ORGANIZATION,
                    primaryOrg,
                    null,
                    primaryOrg,
                    orgList,
                    "SUBMISSION",
                    batchId,
                    count,
                    "已生成申报包（" + count + " 人），待送达审批部门",
                    "submission-export");
        }
    }

    public void onSubmissionReceivedPreview(List<PersonnelExportRecord> personnel, String batchId) {
        int count = personnel == null ? 0 : personnel.size();
        if (count == 0) {
            return;
        }
        Set<String> orgCodes = organizationCodes(personnel);
        String orgList = ExchangeNotificationRepository.joinOrganizationCodes(orgCodes);
        String primaryOrg = orgCodes.stream().findFirst().orElse(null);
        insert(
                "SUBMISSION_RECEIVED",
                DIRECTION_INBOUND,
                AUDIENCE_APPROVAL,
                primaryOrg,
                null,
                primaryOrg,
                orgList,
                "SUBMISSION",
                batchId,
                count,
                "收到申报包，" + count + " 人待审核",
                "submission-review");
    }

    public void onSubmissionApproved(List<PersonnelExportRecord> personnel, String batchId) {
        int count = personnel == null ? 0 : personnel.size();
        if (count == 0) {
            return;
        }
        Set<String> orgCodes = organizationCodes(personnel);
        String orgList = ExchangeNotificationRepository.joinOrganizationCodes(orgCodes);
        String primaryOrg = orgCodes.stream().findFirst().orElse(null);
        if (deploymentProperties.sharedDatabase()) {
            insert(
                    "SUBMISSION_APPROVED",
                    DIRECTION_INTERNAL,
                    AUDIENCE_ORGANIZATION,
                    primaryOrg,
                    null,
                    primaryOrg,
                    orgList,
                    "SUBMISSION",
                    batchId,
                    count,
                    "申报已审核通过：" + count + " 人，待接收审批结果",
                    "approval-receive");
        } else {
            insert(
                    "SUBMISSION_APPROVED",
                    DIRECTION_OUTBOUND,
                    AUDIENCE_APPROVAL,
                    primaryOrg,
                    null,
                    primaryOrg,
                    orgList,
                    "SUBMISSION",
                    batchId,
                    count,
                    "已审核申报 " + count + " 人，待下发审批包",
                    "approval-dispatch");
        }
    }

    public void onApprovalDispatched(List<PersonnelExportRecord> personnel, String batchId) {
        int count = personnel == null ? 0 : personnel.size();
        if (count == 0) {
            return;
        }
        Set<String> orgCodes = organizationCodes(personnel);
        String orgList = ExchangeNotificationRepository.joinOrganizationCodes(orgCodes);
        String primaryOrg = orgCodes.stream().findFirst().orElse(null);
        if (deploymentProperties.sharedDatabase()) {
            insert(
                    "APPROVAL_DISPATCHED",
                    DIRECTION_INTERNAL,
                    AUDIENCE_ORGANIZATION,
                    primaryOrg,
                    null,
                    primaryOrg,
                    orgList,
                    "APPROVAL",
                    batchId,
                    count,
                    "审批包已下发：" + count + " 人待接收",
                    "approval-receive");
        } else {
            insert(
                    "APPROVAL_DISPATCHED",
                    DIRECTION_OUTBOUND,
                    AUDIENCE_APPROVAL,
                    primaryOrg,
                    null,
                    primaryOrg,
                    orgList,
                    "APPROVAL",
                    batchId,
                    count,
                    "已生成审批包（" + count + " 人），待送达下属单位",
                    "approval-dispatch");
        }
    }

    public void onApprovalReceivedPreview(List<PersonnelExportRecord> personnel, String batchId) {
        int count = personnel == null ? 0 : personnel.size();
        if (count == 0) {
            return;
        }
        Set<String> orgCodes = organizationCodes(personnel);
        String orgList = ExchangeNotificationRepository.joinOrganizationCodes(orgCodes);
        String primaryOrg = orgCodes.stream().findFirst().orElse(null);
        insert(
                "APPROVAL_RECEIVED",
                DIRECTION_INBOUND,
                AUDIENCE_ORGANIZATION,
                primaryOrg,
                null,
                primaryOrg,
                orgList,
                "APPROVAL",
                batchId,
                count,
                "收到审批包，" + count + " 人待接收",
                "approval-receive");
    }

    public void onApprovalApplied(List<PersonnelExportRecord> personnel, String batchId) {
        int count = personnel == null ? 0 : personnel.size();
        if (count == 0) {
            return;
        }
        Set<String> orgCodes = organizationCodes(personnel);
        String orgList = ExchangeNotificationRepository.joinOrganizationCodes(orgCodes);
        String primaryOrg = orgCodes.stream().findFirst().orElse(null);
        insert(
                "APPROVAL_APPLIED",
                DIRECTION_INTERNAL,
                AUDIENCE_ORGANIZATION,
                primaryOrg,
                null,
                primaryOrg,
                orgList,
                "APPROVAL",
                batchId,
                count,
                "已接收审批结果：" + count + " 人",
                "approval-receive");
    }

    public void onPersonnelSubmitted(String organizationCode, String personCode, String personName, String recordLabel) {
        String label = recordLabel == null || recordLabel.isBlank() ? "人员信息" : recordLabel;
        String namePart = personName == null || personName.isBlank() ? personCode : personName;
        insert(
                "PERSONNEL_SUBMITTED",
                deploymentProperties.sharedDatabase() ? DIRECTION_INTERNAL : DIRECTION_INTERNAL,
                AUDIENCE_APPROVAL,
                organizationCode,
                null,
                organizationCode,
                organizationCode,
                "PERSONNEL",
                null,
                1,
                label + "已提交审核：" + namePart + "（" + organizationCode + "-" + personCode + "）",
                "personnel-approval-tracking");
    }

    public void onPersonnelDecided(
            String organizationCode,
            String personCode,
            String personName,
            String recordLabel,
            boolean approved) {
        String label = recordLabel == null || recordLabel.isBlank() ? "人员信息" : recordLabel;
        String namePart = personName == null || personName.isBlank() ? personCode : personName;
        String type = approved ? "PERSONNEL_APPROVED" : "PERSONNEL_RETURNED";
        String action = approved ? "审批通过" : "已退回草稿";
        insert(
                type,
                DIRECTION_INTERNAL,
                AUDIENCE_ORGANIZATION,
                organizationCode,
                null,
                organizationCode,
                organizationCode,
                "PERSONNEL",
                null,
                1,
                label + action + "：" + namePart + "（" + organizationCode + "-" + personCode + "）",
                "personnel-approval-tracking");
    }

    public void onPayrollWorkflowPending(
            long workflowId,
            int uid,
            String organizationCode,
            String personCode,
            String personName,
            String sourceType,
            Integer sourceId,
            String payrollModule,
            String summary) {
        String namePart = personName == null || personName.isBlank() ? personCode : personName;
        insertWorkflow(
                "PAYROLL_WORKFLOW_PENDING",
                DIRECTION_INTERNAL,
                AUDIENCE_APPROVAL,
                organizationCode,
                organizationCode,
                "WORKFLOW",
                summary == null || summary.isBlank()
                        ? namePart + " 待办理工资变动（" + payrollModule + "）"
                        : summary,
                "payroll-workflow-center",
                workflowId,
                uid,
                sourceId,
                sourceType);
    }

    public void onPayrollWorkflowStarted(
            long workflowId,
            int uid,
            String organizationCode,
            String personCode,
            String personName,
            String sourceType,
            Integer sourceId,
            String summary) {
        String namePart = personName == null || personName.isBlank() ? personCode : personName;
        insertWorkflow(
                "PAYROLL_WORKFLOW_STARTED",
                DIRECTION_INTERNAL,
                AUDIENCE_ORGANIZATION,
                organizationCode,
                organizationCode,
                "WORKFLOW",
                summary == null || summary.isBlank()
                        ? namePart + " 的信息已审批通过，已进入工资变动办理"
                        : summary,
                "payroll-workflow-status",
                workflowId,
                uid,
                sourceId,
                sourceType);
    }

    public void onPayrollWorkflowDone(
            long workflowId,
            int uid,
            String organizationCode,
            String personCode,
            String personName,
            String sourceType,
            Integer sourceId,
            String changeType,
            String summary) {
        String namePart = personName == null || personName.isBlank() ? personCode : personName;
        String changePart = changeType == null || changeType.isBlank() ? "工资变动" : changeType;
        insertWorkflow(
                "PAYROLL_WORKFLOW_DONE",
                DIRECTION_INTERNAL,
                AUDIENCE_ORGANIZATION,
                organizationCode,
                organizationCode,
                "WORKFLOW",
                summary == null || summary.isBlank()
                        ? namePart + " 的" + changePart + "已办理完成"
                        : summary,
                "payroll-workflow-status",
                workflowId,
                uid,
                sourceId,
                sourceType);
    }

    private void insertWorkflow(
            String type,
            String direction,
            String audienceScope,
            String organizationCode,
            String organizationCodes,
            String packageType,
            String summary,
            String actionTab,
            Long workflowId,
            Integer personUid,
            Integer sourceId,
            String sourceType) {
        repository.insert(new ExchangeNotificationRepository.ExchangeNotificationInsert(
                type,
                direction,
                audienceScope,
                organizationCode,
                null,
                organizationCode,
                organizationCodes,
                packageType,
                null,
                1,
                summary,
                actionTab,
                workflowId,
                personUid,
                sourceId,
                sourceType));
    }

    private void insert(
            String type,
            String direction,
            String audienceScope,
            String sourceOrg,
            String targetOrg,
            String organizationCode,
            String organizationCodes,
            String packageType,
            String batchId,
            int personCount,
            String summary,
            String actionTab) {
        repository.insert(new ExchangeNotificationRepository.ExchangeNotificationInsert(
                type,
                direction,
                audienceScope,
                trimToNull(sourceOrg),
                trimToNull(targetOrg),
                trimToNull(organizationCode),
                trimToNull(organizationCodes),
                trimToNull(packageType),
                trimToNull(batchId),
                personCount,
                summary,
                trimToNull(actionTab)));
    }

    private static Set<String> organizationCodes(List<PersonnelExportRecord> personnel) {
        Set<String> codes = new LinkedHashSet<>();
        if (personnel == null) {
            return codes;
        }
        for (PersonnelExportRecord row : personnel) {
            if (row != null && row.organizationCode() != null && !row.organizationCode().isBlank()) {
                codes.add(row.organizationCode().trim());
            }
        }
        return codes;
    }

    private void requireReadPermission() {
        if (!accessControlService.hasPermission("DATA_EXCHANGE_READ")) {
            throw new IllegalStateException("当前用户没有数据交换权限。");
        }
    }

    private String currentUsername() {
        return accessControlService.currentUser().getUsername();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
