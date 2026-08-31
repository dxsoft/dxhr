package com.dxsoft.rsgzgl.workflow;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.exchange.notification.ExchangeDeploymentProperties;
import com.dxsoft.rsgzgl.exchange.notification.ExchangeNotificationService;
import com.dxsoft.rsgzgl.personnel.PersonnelMaintenanceRecord;
import com.dxsoft.rsgzgl.personnel.PersonnelRepository;
import com.dxsoft.rsgzgl.personnel.PersonnelSubrecordType;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollWorkflowService {

    private final PayrollWorkflowRepository repository;
    private final PayrollImpactEvaluator impactEvaluator;
    private final PersonnelRepository personnelRepository;
    private final ExchangeNotificationService exchangeNotificationService;
    private final PayrollWorkflowProperties workflowProperties;
    private final ExchangeDeploymentProperties deploymentProperties;
    private final AccessControlService accessControlService;

    public PayrollWorkflowService(
            PayrollWorkflowRepository repository,
            PayrollImpactEvaluator impactEvaluator,
            PersonnelRepository personnelRepository,
            ExchangeNotificationService exchangeNotificationService,
            PayrollWorkflowProperties workflowProperties,
            ExchangeDeploymentProperties deploymentProperties,
            AccessControlService accessControlService) {
        this.repository = repository;
        this.impactEvaluator = impactEvaluator;
        this.personnelRepository = personnelRepository;
        this.exchangeNotificationService = exchangeNotificationService;
        this.workflowProperties = workflowProperties;
        this.deploymentProperties = deploymentProperties;
        this.accessControlService = accessControlService;
    }

    public boolean active() {
        return workflowProperties.isEnabled() && deploymentProperties.sharedDatabase();
    }

    @Transactional
    public void onPersonnelApproved(int uid, PayrollWorkflowSourceType sourceType, Integer sourceId) {
        if (!active() || !workflowProperties.isAutoQueueOnApprove()) {
            return;
        }
        if (sourceId != null && repository.existsPendingBySource(sourceType.name(), sourceId)) {
            return;
        }
        Optional<PayrollImpactEvaluator.PayrollImpactResult> impact =
                impactEvaluator.evaluate(uid, sourceType, sourceId);
        Optional<PersonnelMaintenanceRecord> person = personnelRepository.findMaintenanceByUid(uid);
        if (person.isEmpty()) {
            return;
        }
        PersonnelMaintenanceRecord record = person.get();
        if (impact.isEmpty()) {
            repository.insert(new PayrollWorkflowRepository.PayrollWorkflowInsert(
                    shortWorkflowNo(),
                    uid,
                    record.organizationCode(),
                    record.personCode(),
                    record.name(),
                    sourceType.name(),
                    sourceId,
                    PayrollWorkflowModule.NEW_PERSONNEL_SALARY.name(),
                    null,
                    PayrollWorkflowStatus.NO_PAYROLL,
                    LocalDateTime.now(),
                    currentUsername(),
                    record.name() + " 审批通过，当前无需办理工资变动"));
            return;
        }
        PayrollImpactEvaluator.PayrollImpactResult result = impact.get();
        Integer resolvedSourceId = result.sourceId() != null ? result.sourceId() : sourceId;
        if (resolvedSourceId != null && repository.existsPendingBySource(result.sourceType().name(), resolvedSourceId)) {
            return;
        }
        long workflowId = repository.insert(new PayrollWorkflowRepository.PayrollWorkflowInsert(
                shortWorkflowNo(),
                uid,
                record.organizationCode(),
                record.personCode(),
                record.name(),
                result.sourceType().name(),
                resolvedSourceId,
                result.module().name(),
                result.expectedJslb(),
                PayrollWorkflowStatus.PAYROLL_PENDING,
                LocalDateTime.now(),
                currentUsername(),
                result.summary()));
        exchangeNotificationService.onPayrollWorkflowPending(
                workflowId,
                uid,
                record.organizationCode(),
                record.personCode(),
                record.name(),
                result.sourceType().name(),
                resolvedSourceId,
                result.module().name(),
                result.summary());
        exchangeNotificationService.onPayrollWorkflowStarted(
                workflowId,
                uid,
                record.organizationCode(),
                record.personCode(),
                record.name(),
                result.sourceType().name(),
                resolvedSourceId,
                result.summary());
    }

    public void onPersonnelApproved(int uid) {
        onPersonnelApproved(uid, PayrollWorkflowSourceType.MAIN, null);
    }

    public void onSubrecordApproved(int uid, PersonnelSubrecordType type, int sourceId) {
        onPersonnelApproved(uid, PayrollWorkflowSourceType.from(type), sourceId);
    }

    @Transactional
    public void onPayrollApplied(String organizationCode, String personCode, String changeType, String payrollHistoryId) {
        if (!active()) {
            return;
        }
        Optional<Integer> uid = personnelRepository.findUidByOrgPerson(organizationCode, personCode);
        uid.ifPresent(value -> onPayrollApplied(value, changeType, payrollHistoryId));
    }

    @Transactional
    public void onPayrollApplied(int uid, String changeType, String payrollHistoryId) {
        if (!active()) {
            return;
        }
        PayrollWorkflowModule module = PayrollWorkflowModule.fromChangeType(changeType);
        if (module == null) {
            return;
        }
        Optional<PayrollWorkflowRecord> pending = repository.findEarliestPending(uid, module.name());
        if (pending.isEmpty()) {
            return;
        }
        PayrollWorkflowRecord workflow = pending.get();
        boolean updated = repository.complete(
                workflow.id(),
                payrollHistoryId,
                currentUsername(),
                LocalDateTime.now());
        if (!updated) {
            return;
        }
        exchangeNotificationService.onPayrollWorkflowDone(
                workflow.id(),
                uid,
                workflow.organizationCode(),
                workflow.personCode(),
                workflow.personName(),
                workflow.sourceType(),
                workflow.sourceId(),
                changeType,
                workflow.personName() + " 的" + changeType + "已办理完成");
    }

    public PageResponse<PayrollWorkflowRecord> list(
            String status,
            String payrollModule,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        requireReadPermission();
        return repository.list(status, payrollModule, organizationCode, keyword, pageRequest);
    }

    public long countPending() {
        requireReadPermission();
        return repository.countPendingForScope(true);
    }

    public Optional<PayrollWorkflowRecord> findById(long id) {
        requireReadPermission();
        return repository.findById(id);
    }

    private void requireReadPermission() {
        if (!accessControlService.hasPermission("PAYROLL_READ")
                && !accessControlService.hasPermission("DATA_EXCHANGE_READ")) {
            throw new IllegalStateException("当前用户没有工资变动或数据交换查看权限。");
        }
    }

    private String currentUsername() {
        return accessControlService.currentUser().getUsername();
    }

    private static String shortWorkflowNo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
