package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.dictionary.DictionaryService;
import com.dxsoft.rsgzgl.exchange.notification.ExchangeNotificationService;
import com.dxsoft.rsgzgl.workflow.PayrollWorkflowService;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.retirement.RetirementService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;
import com.dxsoft.rsgzgl.security.AuditActorMoment;
import com.dxsoft.rsgzgl.security.AuditTargetKey;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import com.dxsoft.rsgzgl.security.SecurityAuditService;
import com.dxsoft.rsgzgl.statistics.RetirementMonthCalculator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonnelService {

    private static final Set<String> ADMINISTRATIVE_ASSESSMENT_RESULTS = Set.of(
            "优秀",
            "称职",
            "基本称职",
            "不称职",
            "暂缓确定",
            "未定等次(试用期)",
            "未定等次(处分期)",
            "未定等次(其它)",
            "未参加考核",
            "未考核(中断年限)");

    private static final Set<String> INSTITUTION_ASSESSMENT_RESULTS = Set.of(
            "优秀",
            "合格",
            "基本合格",
            "不合格",
            "暂缓确定",
            "未定等次(见习期)",
            "未定等次(处分期)",
            "未定等次(其它)",
            "未参加考核",
            "未考核(中断年限)");

    private final PersonnelRepository personnelRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;
    private final RetirementService retirementService;
    private final PayrollService payrollService;
    private final PersonnelFieldEditPolicy personnelFieldEditPolicy;
    private final PersonnelSubrecordEditPolicy personnelSubrecordEditPolicy;
    private final SecurityAuditService securityAuditService;
    private final SubrecordAttachmentService subrecordAttachmentService;
    private final DictionaryService dictionaryService;
    private final ExchangeNotificationService exchangeNotificationService;
    private final PayrollWorkflowService payrollWorkflowService;

    public PersonnelService(
            PersonnelRepository personnelRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            SecurityAuditService securityAuditService,
            @Lazy RetirementService retirementService,
            @Lazy PayrollService payrollService,
            PersonnelFieldEditPolicy personnelFieldEditPolicy,
            PersonnelSubrecordEditPolicy personnelSubrecordEditPolicy,
            SubrecordAttachmentService subrecordAttachmentService,
            DictionaryService dictionaryService,
            ExchangeNotificationService exchangeNotificationService,
            PayrollWorkflowService payrollWorkflowService) {
        this.personnelRepository = personnelRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.securityAuditService = securityAuditService;
        this.retirementService = retirementService;
        this.payrollService = payrollService;
        this.personnelFieldEditPolicy = personnelFieldEditPolicy;
        this.personnelSubrecordEditPolicy = personnelSubrecordEditPolicy;
        this.subrecordAttachmentService = subrecordAttachmentService;
        this.dictionaryService = dictionaryService;
        this.exchangeNotificationService = exchangeNotificationService;
        this.payrollWorkflowService = payrollWorkflowService;
    }

    public PageResponse<PersonnelSummary> list(String organizationCode, String keyword,
            String sort, String direction, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        List<PersonnelSummary> rows = personnelRepository.findAll(
                scope, emptyToNull(organizationCode), keyword, sort, direction, pageRequest);
        String referencePeriod = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        List<PersonnelSummary> enriched = rows.stream()
                .map(row -> enrichRetirementDue(row, referencePeriod))
                .toList();
        long total = personnelRepository.countAll(scope, emptyToNull(organizationCode), keyword);
        return PageResponse.of(enriched, pageRequest, total);
    }

    private PersonnelSummary enrichRetirementDue(PersonnelSummary person, String referencePeriod) {
        String positionCode = firstNonBlank(person.payrollPositionCode(), person.currentPositionCode());
        RetirementMonthCalculator.CalculationResult calculation = RetirementMonthCalculator.calculate(
                person.birthYearMonth(), person.gender(), positionCode);
        boolean due = RetirementMonthCalculator.isRetirementDue(
                person.birthYearMonth(), person.gender(), positionCode, referencePeriod);
        boolean withinOneMonth = !due && RetirementMonthCalculator.isRetirementWithinOneMonth(
                person.birthYearMonth(), person.gender(), positionCode, referencePeriod);
        boolean highlighted = due || withinOneMonth;
        return new PersonnelSummary(
                person.uid(),
                person.organizationCode(),
                person.organizationName(),
                person.personCode(),
                person.name(),
                person.idCard(),
                person.gender(),
                person.birthYearMonth(),
                person.personnelCategory(),
                person.organizationType(),
                person.postCategory(),
                person.currentPosition(),
                person.currentPositionCode(),
                person.payrollPositionCode(),
                person.appointmentPosition(),
                person.approvalStatus(),
                due,
                withinOneMonth,
                highlighted ? calculation.retirementYearMonth() : null);
    }

    private static String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left.trim();
        }
        if (right != null && !right.isBlank()) {
            return right.trim();
        }
        return "";
    }

    public PageResponse<PersonnelComprehensiveQueryRecord> comprehensiveQueries(
            String organizationCode,
            String keyword,
            String gender,
            String personnelCategory,
            String organizationType,
            String postCategory,
            String educationCode,
            String birthYearMonthFrom,
            String birthYearMonthTo,
            String workStartYearMonthFrom,
            String workStartYearMonthTo,
            String regularizationYearMonthFrom,
            String regularizationYearMonthTo,
            String positionCode,
            String positionCodePrefix,
            String gradeLevelFrom,
            String gradeLevelTo,
            PageRequest pageRequest) {
        // Use the caller's full org permission set; requested unit is applied as a filter
        // (including descendants via LIKE) inside the repository SQL.
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        PersonnelComprehensiveQueryCriteria criteria = new PersonnelComprehensiveQueryCriteria(
                emptyToNull(organizationCode),
                keyword,
                gender,
                personnelCategory,
                organizationType,
                postCategory,
                educationCode,
                birthYearMonthFrom,
                birthYearMonthTo,
                workStartYearMonthFrom,
                workStartYearMonthTo,
                regularizationYearMonthFrom,
                regularizationYearMonthTo,
                positionCode,
                positionCodePrefix,
                gradeLevelFrom,
                gradeLevelTo);
        List<PersonnelComprehensiveQueryRecord> rows = personnelRepository.findComprehensiveQueries(scope, criteria, pageRequest);
        long total = personnelRepository.countComprehensiveQueries(scope, criteria);
        return PageResponse.of(rows, pageRequest, total);
    }

    public PersonnelComprehensiveQueryOptions comprehensiveQueryOptions() {
        return personnelRepository.findComprehensiveQueryOptions();
    }

    public PersonnelDetail get(int uid) {
        PersonnelDetail detail = personnelRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(detail.organizationCode());
        return detail;
    }

    public PersonnelInformationCollectionReport informationCollectionReport(int uid) {
        PersonnelInformationCollectionReport report = personnelRepository.findInformationCollectionReport(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(report.organizationCode());
        return report;
    }

    public PersonnelMaintenanceRecord maintenance(int uid) {
        PersonnelMaintenanceRecord record = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(record.organizationCode());
        requireBasicReadPermission();
        return record;
    }

    public PersonnelFieldPolicyView fieldPolicy(int uid) {
        PersonnelMaintenanceRecord record = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(record.organizationCode());
        requireBasicReadPermission();
        return personnelFieldEditPolicy.evaluate(record);
    }

    public PersonnelMaintenanceRecord create(PersonnelMaintenanceRequest request) {
        requireWritePermission();
        accessControlService.requireOrganization(requiredOrganizationCode(request));
        int uid = personnelRepository.createPersonnel(request);
        payrollService.ensureNoExperienceInternSalary(uid);
        payrollService.ensureTransferInSalaryDetermination(uid);
        PersonnelMaintenanceRecord created = loadMaintenanceRecord(uid);
        operationLogService.record(
                "CREATE_PERSONNEL",
                "ryjbxx",
                String.valueOf(uid),
                "新增人员 " + created.organizationCode() + "-" + created.personCode() + " " + created.name());
        return created;
    }

    public PersonnelMaintenanceRecord update(int uid, PersonnelMaintenanceRequest request) {
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        accessControlService.requireOrganization(requiredOrganizationCode(request));
        personnelFieldEditPolicy.validateUpdate(existing, request);
        personnelRepository.updatePersonnel(uid, request);
        payrollService.ensureNoExperienceInternSalary(uid);
        payrollService.ensureTransferInSalaryDetermination(uid);
        PersonnelMaintenanceRecord updated = loadMaintenanceRecord(uid);
        operationLogService.record(
                "UPDATE_PERSONNEL",
                "ryjbxx",
                String.valueOf(uid),
                "更新人员 " + updated.organizationCode() + "-" + updated.personCode() + " " + updated.name());
        return updated;
    }

    public PersonnelMaintenanceRecord cancelApproval(int uid, PersonnelApprovalCancelRequest request) {
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        personnelFieldEditPolicy.validateApprovalCancel(existing);
        personnelRepository.updateMainApprovalDraft(uid);
        String summary = "bbz: 审批通过 → 草稿";
        if (request != null && request.reason() != null && !request.reason().isBlank()) {
            summary += "；原因：" + request.reason().trim();
        }
        operationLogService.record(
                "PERSONNEL_APPROVAL_CANCEL",
                "personnel",
                String.valueOf(uid),
                summary);
        return loadMaintenanceRecord(uid);
    }

    public PersonnelMaintenanceRecord submitApproval(int uid) {
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        personnelFieldEditPolicy.validateApprovalSubmit(existing);
        personnelRepository.updateMainApprovalSubmit(uid, currentActorUsername(), LocalDateTime.now());
        operationLogService.record(
                "PERSONNEL_APPROVAL_SUBMIT",
                "personnel",
                String.valueOf(uid),
                "bbz: 草稿 → 申报");
        exchangeNotificationService.onPersonnelSubmitted(
                existing.organizationCode(),
                existing.personCode(),
                existing.name(),
                "人员基本信息");
        return loadMaintenanceRecord(uid);
    }

    public PersonnelMaintenanceRecord approvePersonnel(int uid) {
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        personnelFieldEditPolicy.validateApprovalApprove(existing);
        personnelRepository.updateMainApprovalApprove(uid, currentActorUsername(), LocalDateTime.now());
        operationLogService.record(
                "PERSONNEL_APPROVE",
                "personnel",
                String.valueOf(uid),
                "bbz: 申报 → 审批通过");
        exchangeNotificationService.onPersonnelDecided(
                existing.organizationCode(),
                existing.personCode(),
                existing.name(),
                "人员基本信息",
                true);
        payrollWorkflowService.onPersonnelApproved(uid);
        return loadMaintenanceRecord(uid);
    }

    public PersonnelMaintenanceRecord returnPersonnelToDraft(int uid) {
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        personnelFieldEditPolicy.validateApprovalReturnToDraft(existing);
        personnelRepository.updateMainApprovalDraft(uid);
        operationLogService.record(
                "PERSONNEL_APPROVAL_RETURN_DRAFT",
                "personnel",
                String.valueOf(uid),
                "bbz: 申报 → 草稿");
        exchangeNotificationService.onPersonnelDecided(
                existing.organizationCode(),
                existing.personCode(),
                existing.name(),
                "人员基本信息",
                false);
        return loadMaintenanceRecord(uid);
    }

    public void delete(int uid) {
        requireWritePermission();
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        PersonnelFieldPolicyView policy = personnelFieldEditPolicy.evaluate(existing);
        if (!policy.canDelete()) {
            throw new AccessDeniedException(
                    policy.blockReason() == null ? "当前账号不能删除该人员。" : policy.blockReason());
        }
        personnelRepository.deletePersonnel(uid);
        operationLogService.record(
                "DELETE_PERSONNEL",
                "ryjbxx",
                String.valueOf(uid),
                "删除人员 " + existing.organizationCode() + "-" + existing.personCode() + " " + existing.name());
    }

    @Transactional
    public PersonnelChangeResult changePersonnel(int uid, PersonnelChangeRequest request) {
        requireWritePermission();
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        String changeType = emptyToNull(request.changeType());
        if (changeType == null || !List.of("退休", "调动", "调出", "辞职", "辞退", "开除", "死亡").contains(changeType)) {
            throw new IllegalArgumentException("人员变动类别必须为退休、调动、调出、辞职、辞退、开除或死亡。");
        }
        if ("调动".equals(changeType)) {
            String targetOrganizationCode = emptyToNull(request.targetOrganizationCode());
            if (targetOrganizationCode == null) {
                throw new IllegalArgumentException("系统内调动必须选择调往单位。");
            }
            if (targetOrganizationCode.equals(existing.organizationCode())) {
                throw new IllegalArgumentException("调往单位不能与原单位相同。");
            }
            accessControlService.requireOrganization(targetOrganizationCode);
            PersonnelChangeResult result = personnelRepository.transferPersonnelWithinSystem(uid, request);
            operationLogService.record(
                    "TRANSFER_PERSONNEL",
                    "ryjbxx",
                    result.personCode(),
                    "系统内调动 " + existing.organizationCode() + "-" + existing.personCode()
                            + " → " + result.organizationCode() + "-" + result.personCode()
                            + " " + result.name());
            return result;
        }
        if ("退休".equals(changeType)) {
            PersonnelChangeResult result = retirementService.applyFromPersonnelChange(uid, request);
            operationLogService.record(
                    "APPLY_PERSONNEL_CHANGE",
                    "dryjbxxb",
                    result.personCode(),
                    "人员变动退休 " + result.organizationCode() + "-" + result.personCode()
                            + " " + result.name() + " → 离退待办");
            return result;
        }
        PersonnelChangeResult result = personnelRepository.movePersonnelToChanged(uid, request);
        operationLogService.record(
                "APPLY_PERSONNEL_CHANGE",
                "dryjbxxb",
                result.personCode(),
                "人员变动 " + result.organizationCode() + "-" + result.personCode()
                        + " " + result.name() + " → " + result.changeType());
        return result;
    }

    @Transactional
    public PersonnelChangeResult restoreChangedPersonnel(ChangedPersonnelRestoreRequest request) {
        requireWritePermission();
        String organizationCode = emptyToNull(request.organizationCode());
        String personCode = emptyToNull(request.personCode());
        if (organizationCode == null || personCode == null) {
            throw new IllegalArgumentException("单位编码和人员编码不能为空。");
        }
        accessControlService.requireOrganization(organizationCode);
        PersonnelChangeResult result = personnelRepository.restoreChangedPersonnel(organizationCode, personCode);
        operationLogService.record(
                "RESTORE_CHANGED_PERSONNEL",
                "ryjbxx",
                personCode,
                "恢复在册 " + result.organizationCode() + "-" + result.personCode() + " " + result.name());
        return result;
    }

    public List<PositionRecord> positions(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findPositions(personKey);
    }

    public PageResponse<PersonnelPositionHistoryRecord> positionHistories(
            String organizationCode,
            String keyword,
            String positionCode,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        String org = emptyToNull(organizationCode);
        String pos = emptyToNull(positionCode);
        return PageResponse.of(
                personnelRepository.findPositionHistories(scope, org, keyword, pos, pageRequest),
                pageRequest,
                personnelRepository.countPositionHistories(scope, org, keyword, pos));
    }

    public List<EducationRecord> education(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findEducation(personKey);
    }

    public List<EducationRecord> createEducation(int uid, EducationMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        int id = personnelRepository.createEducation(key, request);
        operationLogService.record(
                "CREATE_EDUCATION",
                "ryjbxx",
                String.valueOf(id),
                "新增学历 " + key.organizationCode() + "-" + key.personCode());
        return personnelRepository.findEducation(key);
    }

    public List<EducationRecord> updateEducation(int uid, int id, EducationMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findEducationKeyById(id), "Education record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        EducationRecord existing = personnelRepository.findEducationById(id)
                .orElseThrow(() -> new NotFoundException("Education record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        personnelRepository.updateEducation(id, request);
        operationLogService.record(
                "UPDATE_EDUCATION",
                "ryjbxx",
                String.valueOf(id),
                "更新学历 " + personKey.organizationCode() + "-" + personKey.personCode());
        return personnelRepository.findEducation(personKey);
    }

    public void deleteEducation(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findEducationKeyById(id), "Education record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        EducationRecord existing = personnelRepository.findEducationById(id)
                .orElseThrow(() -> new NotFoundException("Education record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        subrecordAttachmentService.deleteAllForEducation(id);
        personnelRepository.deleteEducation(id);
        operationLogService.record(
                "DELETE_EDUCATION",
                "ryjbxx",
                String.valueOf(id),
                "删除学历 " + key.organizationCode() + "-" + key.personCode());
    }

    public PageResponse<PersonnelEducationHistoryRecord> educationHistories(
            String organizationCode,
            String keyword,
            String educationCode,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        String org = emptyToNull(organizationCode);
        String edu = emptyToNull(educationCode);
        return PageResponse.of(
                personnelRepository.findEducationHistories(scope, org, keyword, edu, pageRequest),
                pageRequest,
                personnelRepository.countEducationHistories(scope, org, keyword, edu));
    }

    public List<AssessmentRecord> assessments(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findAssessments(personKey);
    }

    public MissingAssessmentPreview missingAssessments(int uid, String targetPeriod) {
        PersonKey personKey = getPersonKey(uid);
        PersonnelMaintenanceRecord personnel = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel not found: " + uid));
        int targetYear = targetYear(targetPeriod);
        int startYear = personnelRepository.currentAssessmentStartYear(personKey);
        return new MissingAssessmentPreview(
                personnelRepository.findMissingAssessmentYears(personKey, startYear, targetYear),
                defaultAssessmentResult(personnel),
                startYear > 0 ? String.valueOf(startYear) : "",
                String.valueOf(targetYear));
    }

    public Map<String, Object> relatedRecords(int uid) {
        PersonKey personKey = getPersonKey(uid);
        Map<String, Object> result = new java.util.LinkedHashMap<>(
                personnelRepository.findPersonnelRelatedRecords(personKey));
        String idCard = personnelRepository.findMaintenanceByUid(uid)
                .map(PersonnelMaintenanceRecord::idCard)
                .orElse("");
        result.put("transfers", personnelRepository.findTransferHistories(uid, idCard, personKey));
        return result;
    }

    private int targetYear(String targetPeriod) {
        if (targetPeriod != null && targetPeriod.length() >= 4) {
            try {
                return Integer.parseInt(targetPeriod.substring(0, 4));
            } catch (NumberFormatException ignored) {
                // use current year below
            }
        }
        return LocalDate.now().getYear();
    }

    static String defaultAssessmentResult(PersonnelMaintenanceRecord personnel) {
        return defaultAssessmentResultText(
                personnel.personnelCategory(),
                personnel.organizationType(),
                personnel.organizationCategory(),
                personnel.organizationPayrollCategory());
    }

    static String defaultAssessmentResultText(
            String personnelCategory,
            String organizationType,
            String organizationCategory,
            String organizationPayrollCategory) {
        return isCivilServantPersonnel(
                personnelCategory, organizationType, organizationCategory, organizationPayrollCategory) ? "称职" : "合格";
    }

    static String defaultAssessmentResultText(String personnelCategory, String organizationType) {
        return defaultAssessmentResultText(personnelCategory, organizationType, null, null);
    }

    static void validateAssessmentResult(String personnelCategory, String organizationType, String result) {
        validateAssessmentResult(personnelCategory, organizationType, result, null, null);
    }

    static void validateAssessmentResult(
            String personnelCategory,
            String organizationType,
            String result,
            String organizationCategory,
            String organizationPayrollCategory) {
        Set<String> allowed = isCivilServantPersonnel(
                personnelCategory, organizationType, organizationCategory, organizationPayrollCategory)
                ? ADMINISTRATIVE_ASSESSMENT_RESULTS
                : INSTITUTION_ASSESSMENT_RESULTS;
        if (!allowed.contains(result)) {
            throw new IllegalArgumentException("考核结果无效：" + result);
        }
    }

    /** 公务员及参照/依照公务员管理单位人员使用行政考核结果，其余使用事业考核结果。 */
    static boolean isCivilServantPersonnel(
            String personnelCategory,
            String organizationType,
            String organizationCategory,
            String organizationPayrollCategory) {
        if (personnelCategory != null && personnelCategory.contains("公务员")) {
            return true;
        }
        return com.dxsoft.rsgzgl.organization.UnitPayrollClassification.isCivilServiceManagedPayroll(
                organizationPayrollCategory);
    }

    static boolean isCivilServantPersonnel(String personnelCategory, String organizationType) {
        return isCivilServantPersonnel(personnelCategory, organizationType, null, null);
    }

    static boolean isInstitutionPersonnel(String personnelCategory, String organizationType) {
        return !isCivilServantPersonnel(personnelCategory, organizationType);
    }

    private String defaultAssessmentResultForSummary(PersonnelSummary person) {
        return defaultAssessmentResultText(person.personnelCategory(), person.organizationType());
    }

    private boolean personMatchesBatchScope(String personOrganizationCode, String organizationCode, boolean includeDescendants) {
        if (organizationCode == null || organizationCode.isBlank()) {
            return true;
        }
        if (includeDescendants) {
            return personOrganizationCode.startsWith(organizationCode);
        }
        return personOrganizationCode.equals(organizationCode);
    }

    private String requiredAssessmentYear(String year) {
        String normalized = emptyToNull(year);
        if (normalized == null || !normalized.matches("\\d{4}")) {
            throw new IllegalArgumentException("考核年度必须为四位年份。");
        }
        return normalized;
    }

    public List<PositionRecord> createPosition(int uid, PositionMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        int id = personnelRepository.createPosition(key, request);
        enforceSingleCurrentPosition(key, id, request);
        operationLogService.record(
                "CREATE_POSITION",
                "ryjbxx",
                String.valueOf(id),
                "新增任职 " + key.organizationCode() + "-" + key.personCode());
        return personnelRepository.findPositions(key);
    }

    public List<PositionRecord> updatePosition(int uid, int id, PositionMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findPositionKeyById(id), "Position record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        PositionRecord existing = personnelRepository.findPositionById(id)
                .orElseThrow(() -> new NotFoundException("Position record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        personnelRepository.updatePosition(id, request);
        enforceSingleCurrentPosition(personKey, id, request);
        operationLogService.record(
                "UPDATE_POSITION",
                "ryjbxx",
                String.valueOf(id),
                "更新任职 " + personKey.organizationCode() + "-" + personKey.personCode());
        return personnelRepository.findPositions(personKey);
    }

    static boolean isCurrentPositionFlag(String activeFlag) {
        return activeFlag != null && "1".equals(activeFlag.trim());
    }

    private void enforceSingleCurrentPosition(PersonKey key, int id, PositionMaintenanceRequest request) {
        if (isCurrentPositionFlag(request.activeFlag())) {
            personnelRepository.clearOtherActivePositions(key, id);
        }
    }

    public void deletePosition(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findPositionKeyById(id), "Position record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        PositionRecord existing = personnelRepository.findPositionById(id)
                .orElseThrow(() -> new NotFoundException("Position record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        subrecordAttachmentService.deleteAllForPosition(id);
        personnelRepository.deletePosition(id);
        operationLogService.record(
                "DELETE_POSITION",
                "ryjbxx",
                String.valueOf(id),
                "删除任职 " + key.organizationCode() + "-" + key.personCode());
    }

    public List<AssessmentRecord> createAssessment(int uid, AssessmentMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        personnelRepository.createAssessment(key, request);
        operationLogService.record(
                "CREATE_ASSESSMENT",
                "ndkh",
                key.personCode(),
                "新增考核 " + key.organizationCode() + "-" + key.personCode() + " " + request.year());
        return personnelRepository.findAssessments(key);
    }

    public List<AssessmentRecord> updateAssessment(int uid, int id, AssessmentMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findAssessmentKeyById(id), "Assessment record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        AssessmentRecord existing = personnelRepository.findAssessmentById(id)
                .orElseThrow(() -> new NotFoundException("Assessment record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        personnelRepository.updateAssessment(id, request);
        operationLogService.record(
                "UPDATE_ASSESSMENT",
                "ndkh",
                String.valueOf(id),
                "更新考核 " + personKey.organizationCode() + "-" + personKey.personCode() + " " + request.year());
        return personnelRepository.findAssessments(personKey);
    }

    public void deleteAssessment(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findAssessmentKeyById(id), "Assessment record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        AssessmentRecord existing = personnelRepository.findAssessmentById(id)
                .orElseThrow(() -> new NotFoundException("Assessment record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        subrecordAttachmentService.deleteAllForAssessment(id);
        personnelRepository.deleteAssessment(id);
        operationLogService.record(
                "DELETE_ASSESSMENT",
                "ndkh",
                String.valueOf(id),
                "删除考核 " + key.organizationCode() + "-" + key.personCode());
    }

    public List<AwardRecord> createAward(int uid, AwardMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        personnelRepository.createAward(key, request);
        operationLogService.record(
                "CREATE_AWARD",
                "hjxx",
                key.personCode(),
                "新增获奖 " + key.organizationCode() + "-" + key.personCode() + " " + request.hjmc());
        return personnelRepository.findAwards(key);
    }

    public List<AwardRecord> updateAward(int uid, int id, AwardMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findAwardKeyById(id), "Award record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        AwardRecord existing = personnelRepository.findAwardById(id)
                .orElseThrow(() -> new NotFoundException("Award record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        personnelRepository.updateAward(id, request);
        operationLogService.record(
                "UPDATE_AWARD",
                "hjxx",
                String.valueOf(id),
                "更新获奖 " + personKey.organizationCode() + "-" + personKey.personCode() + " " + request.hjmc());
        return personnelRepository.findAwards(personKey);
    }

    public void deleteAward(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findAwardKeyById(id), "Award record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        AwardRecord existing = personnelRepository.findAwardById(id)
                .orElseThrow(() -> new NotFoundException("Award record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        subrecordAttachmentService.deleteAllForAward(id);
        personnelRepository.deleteAward(id);
        operationLogService.record(
                "DELETE_AWARD",
                "hjxx",
                String.valueOf(id),
                "删除获奖 " + key.organizationCode() + "-" + key.personCode());
    }

    public List<RankRecord> createRank(int uid, RankMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        RankMaintenanceRequest normalized = normalizeRankRequest(request);
        personnelRepository.createRank(key, normalized);
        operationLogService.record(
                "CREATE_RANK",
                "jx",
                key.personCode(),
                "新增警衔 " + key.organizationCode() + "-" + key.personCode() + " " + normalized.jx());
        return personnelRepository.findRanks(key);
    }

    public List<RankRecord> updateRank(int uid, int id, RankMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findRankKeyById(id), "Rank record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        RankRecord existing = personnelRepository.findRankById(id)
                .orElseThrow(() -> new NotFoundException("Rank record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        RankMaintenanceRequest normalized = normalizeRankRequest(request);
        personnelRepository.updateRank(id, normalized);
        operationLogService.record(
                "UPDATE_RANK",
                "jx",
                String.valueOf(id),
                "更新警衔 " + personKey.organizationCode() + "-" + personKey.personCode() + " " + normalized.jx());
        return personnelRepository.findRanks(personKey);
    }

    private RankMaintenanceRequest normalizeRankRequest(RankMaintenanceRequest request) {
        String lb = dictionaryService.resolveRankCategory(request.jx(), request.lb());
        if (lb == null || lb.isBlank()) {
            lb = request.lb() == null ? "" : request.lb().trim();
        }
        return new RankMaintenanceRequest(
                request.jx(),
                request.sysj(),
                request.syyy(),
                request.rmwh(),
                request.xrjxbz(),
                lb);
    }

    public void deleteRank(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findRankKeyById(id), "Rank record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        RankRecord existing = personnelRepository.findRankById(id)
                .orElseThrow(() -> new NotFoundException("Rank record not found: " + id));
        personnelSubrecordEditPolicy.assertEditable(existing.approvalStatus());
        subrecordAttachmentService.deleteAllForRank(id);
        personnelRepository.deleteRank(id);
        operationLogService.record(
                "DELETE_RANK",
                "jx",
                String.valueOf(id),
                "删除警衔 " + key.organizationCode() + "-" + key.personCode());
    }

    public List<EducationRecord> approveEducation(int uid, int id) {
        return approveSubrecord(uid, id, PersonnelSubrecordType.EDUCATION);
    }

    public List<EducationRecord> submitEducation(int uid, int id) {
        return submitSubrecord(uid, id, PersonnelSubrecordType.EDUCATION);
    }

    public List<EducationRecord> returnEducationToDraft(int uid, int id) {
        return returnSubrecordToDraft(uid, id, PersonnelSubrecordType.EDUCATION);
    }

    public List<EducationRecord> cancelEducationApproval(int uid, int id, PersonnelApprovalCancelRequest request) {
        return cancelSubrecordApproval(uid, id, PersonnelSubrecordType.EDUCATION, request);
    }

    public List<PositionRecord> approvePosition(int uid, int id) {
        return approveSubrecord(uid, id, PersonnelSubrecordType.POSITION);
    }

    public List<PositionRecord> submitPosition(int uid, int id) {
        return submitSubrecord(uid, id, PersonnelSubrecordType.POSITION);
    }

    public List<PositionRecord> returnPositionToDraft(int uid, int id) {
        return returnSubrecordToDraft(uid, id, PersonnelSubrecordType.POSITION);
    }

    public List<PositionRecord> cancelPositionApproval(int uid, int id, PersonnelApprovalCancelRequest request) {
        return cancelSubrecordApproval(uid, id, PersonnelSubrecordType.POSITION, request);
    }

    public List<AssessmentRecord> approveAssessment(int uid, int id) {
        return approveSubrecord(uid, id, PersonnelSubrecordType.ASSESSMENT);
    }

    public List<AssessmentRecord> submitAssessment(int uid, int id) {
        return submitSubrecord(uid, id, PersonnelSubrecordType.ASSESSMENT);
    }

    public List<AssessmentRecord> returnAssessmentToDraft(int uid, int id) {
        return returnSubrecordToDraft(uid, id, PersonnelSubrecordType.ASSESSMENT);
    }

    public List<AssessmentRecord> cancelAssessmentApproval(int uid, int id, PersonnelApprovalCancelRequest request) {
        return cancelSubrecordApproval(uid, id, PersonnelSubrecordType.ASSESSMENT, request);
    }

    public List<AwardRecord> approveAward(int uid, int id) {
        return approveSubrecord(uid, id, PersonnelSubrecordType.AWARD);
    }

    public List<AwardRecord> submitAward(int uid, int id) {
        return submitSubrecord(uid, id, PersonnelSubrecordType.AWARD);
    }

    public List<AwardRecord> returnAwardToDraft(int uid, int id) {
        return returnSubrecordToDraft(uid, id, PersonnelSubrecordType.AWARD);
    }

    public List<AwardRecord> cancelAwardApproval(int uid, int id, PersonnelApprovalCancelRequest request) {
        return cancelSubrecordApproval(uid, id, PersonnelSubrecordType.AWARD, request);
    }

    public List<RankRecord> approveRank(int uid, int id) {
        return approveSubrecord(uid, id, PersonnelSubrecordType.RANK);
    }

    public List<RankRecord> submitRank(int uid, int id) {
        return submitSubrecord(uid, id, PersonnelSubrecordType.RANK);
    }

    public List<RankRecord> returnRankToDraft(int uid, int id) {
        return returnSubrecordToDraft(uid, id, PersonnelSubrecordType.RANK);
    }

    public List<RankRecord> cancelRankApproval(int uid, int id, PersonnelApprovalCancelRequest request) {
        return cancelSubrecordApproval(uid, id, PersonnelSubrecordType.RANK, request);
    }

    @SuppressWarnings("unchecked")
    private <T> T submitSubrecord(int uid, int id, PersonnelSubrecordType type) {
        PersonKey personKey = getPersonKey(uid);
        String approvalStatus = loadSubrecordApprovalStatus(type, id, personKey);
        personnelSubrecordEditPolicy.validateSubmit(approvalStatus);
        personnelRepository.updateSubrecordApprovalSubmit(type, id, currentActorUsername(), LocalDateTime.now());
        operationLogService.record(
                "PERSONNEL_SUBRECORD_SUBMIT",
                type.tableName(),
                String.valueOf(id),
                type.tableName() + "#" + id + " bbz: 草稿 → 申报");
        notifySubrecordSubmitted(uid, type);
        return (T) reloadSubrecords(type, personKey);
    }

    @SuppressWarnings("unchecked")
    private <T> T returnSubrecordToDraft(int uid, int id, PersonnelSubrecordType type) {
        PersonKey personKey = getPersonKey(uid);
        String approvalStatus = loadSubrecordApprovalStatus(type, id, personKey);
        personnelSubrecordEditPolicy.validateReturnToDraft(approvalStatus);
        personnelRepository.updateSubrecordApprovalDraft(type, id);
        operationLogService.record(
                "PERSONNEL_SUBRECORD_RETURN_DRAFT",
                type.tableName(),
                String.valueOf(id),
                type.tableName() + "#" + id + " bbz: 申报 → 草稿");
        notifySubrecordDecision(uid, type, false);
        return (T) reloadSubrecords(type, personKey);
    }

    @SuppressWarnings("unchecked")
    private <T> T approveSubrecord(int uid, int id, PersonnelSubrecordType type) {
        PersonKey personKey = getPersonKey(uid);
        String approvalStatus = loadSubrecordApprovalStatus(type, id, personKey);
        personnelSubrecordEditPolicy.validateApprove(approvalStatus);
        personnelRepository.updateSubrecordApprovalApprove(type, id, currentActorUsername(), LocalDateTime.now());
        operationLogService.record(
                "PERSONNEL_SUBRECORD_APPROVE",
                type.tableName(),
                String.valueOf(id),
                type.tableName() + "#" + id + " bbz: "
                        + PersonnelApprovalStatuses.normalize(approvalStatus) + " → 审批通过");
        notifySubrecordDecision(uid, type, true);
        payrollWorkflowService.onSubrecordApproved(uid, type, id);
        return (T) reloadSubrecords(type, personKey);
    }

    @SuppressWarnings("unchecked")
    private <T> T cancelSubrecordApproval(int uid, int id, PersonnelSubrecordType type, PersonnelApprovalCancelRequest request) {
        PersonKey personKey = getPersonKey(uid);
        String approvalStatus = loadSubrecordApprovalStatus(type, id, personKey);
        personnelSubrecordEditPolicy.validateCancel(approvalStatus);
        personnelRepository.updateSubrecordApprovalDraft(type, id);
        String summary = type.tableName() + "#" + id + " bbz: 审批通过 → 草稿";
        if (request != null && request.reason() != null && !request.reason().isBlank()) {
            summary += "；原因：" + request.reason().trim();
        }
        operationLogService.record(
                "PERSONNEL_SUBRECORD_APPROVAL_CANCEL",
                type.tableName(),
                String.valueOf(id),
                summary);
        return (T) reloadSubrecords(type, personKey);
    }

    private String loadSubrecordApprovalStatus(PersonnelSubrecordType type, int id, PersonKey personKey) {
        return switch (type) {
            case EDUCATION -> {
                EducationRecord record = personnelRepository.findEducationById(id)
                        .orElseThrow(() -> new NotFoundException("Education record not found: " + id));
                requireSamePerson(new PersonKey(record.organizationCode(), record.personCode()), personKey);
                accessControlService.requireOrganization(record.organizationCode());
                yield record.approvalStatus();
            }
            case POSITION -> {
                PositionRecord record = personnelRepository.findPositionById(id)
                        .orElseThrow(() -> new NotFoundException("Position record not found: " + id));
                requireSamePerson(new PersonKey(record.organizationCode(), record.personCode()), personKey);
                accessControlService.requireOrganization(record.organizationCode());
                yield record.approvalStatus();
            }
            case ASSESSMENT -> {
                AssessmentRecord record = personnelRepository.findAssessmentById(id)
                        .orElseThrow(() -> new NotFoundException("Assessment record not found: " + id));
                requireSamePerson(new PersonKey(record.organizationCode(), record.personCode()), personKey);
                accessControlService.requireOrganization(record.organizationCode());
                yield record.approvalStatus();
            }
            case AWARD -> {
                AwardRecord record = personnelRepository.findAwardById(id)
                        .orElseThrow(() -> new NotFoundException("Award record not found: " + id));
                requireSamePerson(new PersonKey(record.organizationCode(), record.personCode()), personKey);
                accessControlService.requireOrganization(record.organizationCode());
                yield record.approvalStatus();
            }
            case RANK -> {
                RankRecord record = personnelRepository.findRankById(id)
                        .orElseThrow(() -> new NotFoundException("Rank record not found: " + id));
                requireSamePerson(new PersonKey(record.organizationCode(), record.personCode()), personKey);
                accessControlService.requireOrganization(record.organizationCode());
                yield record.approvalStatus();
            }
        };
    }

    private Object reloadSubrecords(PersonnelSubrecordType type, PersonKey personKey) {
        return switch (type) {
            case EDUCATION -> personnelRepository.findEducation(personKey);
            case POSITION -> personnelRepository.findPositions(personKey);
            case ASSESSMENT -> personnelRepository.findAssessments(personKey);
            case AWARD -> personnelRepository.findAwards(personKey);
            case RANK -> personnelRepository.findRanks(personKey);
        };
    }

    public PageResponse<AnnualAssessmentRecord> annualAssessments(
            String organizationCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findAnnualAssessments(scope, emptyToNull(organizationCode), year, keyword, pageRequest),
                pageRequest,
                personnelRepository.countAnnualAssessments(scope, emptyToNull(organizationCode), year, keyword));
    }

    public PageResponse<AnnualAssessmentSummaryRecord> annualAssessmentSummary(
            String organizationCode,
            String year,
            String result,
            boolean includeDescendants,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findAnnualAssessmentSummary(
                        scope, emptyToNull(organizationCode), year, result, includeDescendants, pageRequest),
                pageRequest,
                personnelRepository.countAnnualAssessmentSummary(
                        scope, emptyToNull(organizationCode), year, result, includeDescendants));
    }

    public BatchAssessmentPreview batchAssessmentPreview(
            String organizationCode,
            String year,
            String keyword,
            boolean includeDescendants) {
        String normalizedOrganizationCode = emptyToNull(organizationCode);
        String normalizedYear = requiredAssessmentYear(year);
        if (normalizedOrganizationCode != null) {
            accessControlService.requireOrganization(normalizedOrganizationCode);
        }
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        List<BatchAssessmentEntryRow> rows = personnelRepository.findBatchAssessmentEntries(
                scope,
                normalizedOrganizationCode,
                normalizedYear,
                keyword,
                includeDescendants);
        int enteredCount = (int) rows.stream()
                .filter(row -> row.result() != null && !row.result().isBlank())
                .count();
        String organizationName = rows.stream()
                .map(BatchAssessmentEntryRow::organizationName)
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElse("");
        return new BatchAssessmentPreview(
                normalizedOrganizationCode == null ? "" : normalizedOrganizationCode,
                organizationName,
                normalizedYear,
                rows.size(),
                enteredCount,
                rows.size() - enteredCount,
                rows);
    }

    @Transactional
    public BatchAssessmentSaveResult saveBatchAssessments(BatchAssessmentSaveRequest request) {
        requireWritePermission();
        String normalizedOrganizationCode = emptyToNull(request.organizationCode());
        String normalizedYear = requiredAssessmentYear(request.year());
        if (normalizedOrganizationCode != null) {
            accessControlService.requireOrganization(normalizedOrganizationCode);
        }
        boolean includeDescendants = Boolean.TRUE.equals(request.includeDescendants());
        List<BatchAssessmentRecordItem> records = request.records() == null ? List.of() : request.records();
        if (records.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一条需要保存的考核记录。");
        }
        String fallbackResult = emptyToNull(request.defaultResult());
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        List<BatchAssessmentSaveFailure> failures = new ArrayList<>();
        for (BatchAssessmentRecordItem item : records) {
            String personCode = emptyToNull(item.personCode());
            String personOrganizationCode = emptyToNull(item.organizationCode());
            if (personCode == null) {
                skipped++;
                failures.add(new BatchAssessmentSaveFailure("", "", "人员编码不能为空。"));
                continue;
            }
            if (personOrganizationCode == null) {
                skipped++;
                failures.add(new BatchAssessmentSaveFailure(personCode, "", "单位编码不能为空。"));
                continue;
            }
            if (!personMatchesBatchScope(personOrganizationCode, normalizedOrganizationCode, includeDescendants)) {
                skipped++;
                failures.add(new BatchAssessmentSaveFailure(personCode, "", "人员单位不在选定单位范围内。"));
                continue;
            }
            accessControlService.requireOrganization(personOrganizationCode);
            Optional<PersonnelSummary> optionalPerson = personnelRepository.findPersonnelSummary(
                    new PersonKey(personOrganizationCode, personCode));
            if (optionalPerson.isEmpty()) {
                skipped++;
                failures.add(new BatchAssessmentSaveFailure(personCode, "", "未找到对应在册人员。"));
                continue;
            }
            PersonnelSummary person = optionalPerson.get();
            String result = emptyToNull(item.result());
            if (result == null) {
                result = fallbackResult;
            }
            if (result == null) {
                result = defaultAssessmentResultForSummary(person);
            }
            try {
                validateAssessmentResult(person.personnelCategory(), person.organizationType(), result);
            } catch (IllegalArgumentException error) {
                skipped++;
                failures.add(new BatchAssessmentSaveFailure(personCode, person.name(), error.getMessage()));
                continue;
            }
            PersonKey key = new PersonKey(person.organizationCode(), person.personCode());
            boolean created = personnelRepository.upsertAssessment(key, new AssessmentMaintenanceRequest(normalizedYear, result));
            if (created) {
                inserted++;
            } else {
                updated++;
            }
        }
        BatchAssessmentSaveResult result = new BatchAssessmentSaveResult(inserted, updated, skipped, failures);
        operationLogService.record(
                "SAVE_BATCH_ASSESSMENTS",
                "ndkh",
                normalizedOrganizationCode == null ? "*" : normalizedOrganizationCode,
                "批量考核录入 " + (normalizedOrganizationCode == null ? "全部权限单位" : normalizedOrganizationCode) + " " + normalizedYear
                        + " 年，新增 " + inserted + "、更新 " + updated + "、跳过 " + skipped);
        return result;
    }

    @Transactional
    public BatchAssessmentSubmitResult submitBatchAssessments(BatchAssessmentSubmitRequest request) {
        requireWritePermission();
        String normalizedOrganizationCode = emptyToNull(request.organizationCode());
        String normalizedYear = requiredAssessmentYear(request.year());
        if (normalizedOrganizationCode != null) {
            accessControlService.requireOrganization(normalizedOrganizationCode);
        }
        boolean includeDescendants = Boolean.TRUE.equals(request.includeDescendants());
        List<BatchAssessmentSubmitItem> records = request.records() == null ? List.of() : request.records();
        if (records.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一条需要提交申报的考核记录。");
        }
        int submitted = 0;
        int skipped = 0;
        List<BatchAssessmentSubmitFailure> failures = new ArrayList<>();
        for (BatchAssessmentSubmitItem item : records) {
            String personCode = emptyToNull(item.personCode());
            String personOrganizationCode = emptyToNull(item.organizationCode());
            if (item.uid() == null) {
                skipped++;
                failures.add(new BatchAssessmentSubmitFailure(
                        personCode == null ? "" : personCode,
                        "",
                        "人员标识不能为空。"));
                continue;
            }
            if (item.assessmentId() == null) {
                skipped++;
                failures.add(new BatchAssessmentSubmitFailure(
                        personCode == null ? "" : personCode,
                        "",
                        "考核记录尚未保存，请先保存后再提交申报。"));
                continue;
            }
            if (personCode == null) {
                skipped++;
                failures.add(new BatchAssessmentSubmitFailure("", "", "人员编码不能为空。"));
                continue;
            }
            if (personOrganizationCode == null) {
                skipped++;
                failures.add(new BatchAssessmentSubmitFailure(personCode, "", "单位编码不能为空。"));
                continue;
            }
            if (!personMatchesBatchScope(personOrganizationCode, normalizedOrganizationCode, includeDescendants)) {
                skipped++;
                failures.add(new BatchAssessmentSubmitFailure(personCode, "", "人员单位不在选定单位范围内。"));
                continue;
            }
            try {
                accessControlService.requireOrganization(personOrganizationCode);
                submitAssessment(item.uid(), item.assessmentId());
                submitted++;
            } catch (RuntimeException error) {
                skipped++;
                String name = personnelRepository.findPersonnelSummary(new PersonKey(personOrganizationCode, personCode))
                        .map(PersonnelSummary::name)
                        .orElse("");
                failures.add(new BatchAssessmentSubmitFailure(personCode, name, error.getMessage()));
            }
        }
        BatchAssessmentSubmitResult result = new BatchAssessmentSubmitResult(submitted, skipped, failures);
        operationLogService.record(
                "SUBMIT_BATCH_ASSESSMENTS",
                "ndkh",
                normalizedOrganizationCode == null ? "*" : normalizedOrganizationCode,
                "批量提交考核申报 " + (normalizedOrganizationCode == null ? "全部权限单位" : normalizedOrganizationCode)
                        + " " + normalizedYear + " 年，提交 " + submitted + " 条、跳过 " + skipped);
        return result;
    }

    public PageResponse<ChangedPersonnelRecord> changedPersonnel(
            String organizationCode,
            String period,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findChangedPersonnel(scope, emptyToNull(organizationCode), period, keyword, pageRequest),
                pageRequest,
                personnelRepository.countChangedPersonnel(scope, emptyToNull(organizationCode), period, keyword));
    }

    public ChangedPersonnelDetail changedPersonnelDetail(int uid) {
        PersonnelMaintenanceRecord basic = personnelRepository.findChangedMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Changed personnel record not found: " + uid));
        accessControlService.requireOrganization(basic.organizationCode());
        PersonKey key = personnelRepository.findChangedKeyByUid(uid)
                .orElseThrow(() -> new NotFoundException("Changed personnel record not found: " + uid));
        Map<String, Object> related = new java.util.LinkedHashMap<>(
                personnelRepository.findChangedPersonnelRelatedRecords(key));
        related.put("transfers", personnelRepository.findTransferHistories(uid, basic.idCard(), key));
        return new ChangedPersonnelDetail(
                basic,
                personnelRepository.findChangedEducation(key),
                personnelRepository.findChangedPositions(key),
                personnelRepository.findChangedAssessments(key),
                personnelRepository.findChangedPayrollHistories(key),
                related);
    }

    public PageResponse<PersonnelApprovalTrackingRecord> approvalTracking(
            String organizationCode,
            String keyword,
            String status,
            boolean submittedByMe,
            Integer approvedWithinDays,
            String recordType,
            String assessmentYear,
            PageRequest pageRequest) {
        requireApprovalTrackingReadPermission();
        String approvalStatus = normalizeApprovalTrackingStatus(status);
        String normalizedRecordType = normalizeApprovalTrackingRecordType(recordType);
        String normalizedAssessmentYear = normalizeAssessmentTrackingYear(assessmentYear);
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        String actor = accessControlService.currentUser().getUsername();
        Integer approvedWindowDays = resolveApprovedWithinDays(approvalStatus, approvedWithinDays);
        List<PersonnelApprovalTrackingRecord> rows = personnelRepository.findApprovalTracking(
                scope,
                emptyToNull(organizationCode),
                keyword,
                approvalStatus,
                submittedByMe,
                actor,
                approvedWindowDays,
                normalizedRecordType,
                normalizedAssessmentYear,
                pageRequest);
        List<PersonnelApprovalTrackingRecord> enriched = enrichApprovalTrackingRecords(rows);
        long total = personnelRepository.countApprovalTracking(
                scope,
                emptyToNull(organizationCode),
                keyword,
                approvalStatus,
                submittedByMe,
                actor,
                approvedWindowDays,
                normalizedRecordType,
                normalizedAssessmentYear);
        return PageResponse.of(enriched, pageRequest, total);
    }

    public AssessmentApprovalStats assessmentApprovalStats(
            String organizationCode,
            String year,
            boolean includeDescendants) {
        requireApprovalTrackingReadPermission();
        String normalizedYear = requiredAssessmentYear(year);
        String normalizedOrganizationCode = emptyToNull(organizationCode);
        if (normalizedOrganizationCode != null) {
            accessControlService.requireOrganization(normalizedOrganizationCode);
        }
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return personnelRepository.findAssessmentApprovalStats(
                scope,
                normalizedOrganizationCode,
                normalizedYear,
                includeDescendants).orElseThrow(() -> new NotFoundException("无法查询考核统计。"));
    }

    @Transactional
    public BatchApprovalResult batchApproveTrackingRecords(BatchApprovalRequest request) {
        requireApprovalWritePermission();
        List<BatchApprovalItem> records = request.records() == null ? List.of() : request.records();
        if (records.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一条需要审核通过的记录。");
        }
        int approved = 0;
        int skipped = 0;
        List<BatchApprovalFailure> failures = new ArrayList<>();
        for (BatchApprovalItem item : records) {
            if (item.uid() == null || item.recordId() == null) {
                skipped++;
                failures.add(new BatchApprovalFailure("", "", item.recordType(), item.recordId(), "记录标识不完整。"));
                continue;
            }
            String recordType = emptyToNull(item.recordType());
            if (recordType == null) {
                skipped++;
                failures.add(new BatchApprovalFailure("", "", "", item.recordId(), "记录类型不能为空。"));
                continue;
            }
            try {
                approveTrackingRecord(item.uid(), recordType, item.recordId());
                approved++;
            } catch (RuntimeException error) {
                skipped++;
                String personCode = "";
                String personName = "";
                Optional<PersonnelSummary> person = personnelRepository.findKeyByUid(item.uid())
                        .flatMap(key -> personnelRepository.findPersonnelSummary(key));
                if (person.isPresent()) {
                    personCode = person.get().personCode();
                    personName = person.get().name();
                }
                failures.add(new BatchApprovalFailure(
                        personCode,
                        personName,
                        recordType,
                        item.recordId(),
                        error.getMessage()));
            }
        }
        BatchApprovalResult result = new BatchApprovalResult(approved, skipped, failures);
        operationLogService.record(
                "BATCH_APPROVE_TRACKING",
                "personnel",
                String.valueOf(approved),
                "批量审核通过 " + approved + " 条，跳过 " + skipped + " 条");
        return result;
    }

    private void approveTrackingRecord(int uid, String recordType, int recordId) {
        switch (recordType) {
            case "main" -> approvePersonnel(uid);
            case "education" -> approveEducation(uid, recordId);
            case "position" -> approvePosition(uid, recordId);
            case "assessment" -> approveAssessment(uid, recordId);
            case "award" -> approveAward(uid, recordId);
            case "rank" -> approveRank(uid, recordId);
            default -> throw new IllegalArgumentException("不支持的记录类型：" + recordType);
        }
    }

    private void requireApprovalWritePermission() {
        if (!accessControlService.hasPermission(PersonnelFeaturePermissions.APPROVAL_WRITE)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    PersonnelFeaturePermissions.APPROVAL_WRITE + " permission required");
        }
    }

    private String normalizeApprovalTrackingRecordType(String recordType) {
        String normalized = emptyToNull(recordType);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "main", "education", "position", "assessment", "award", "rank" -> normalized;
            default -> throw new IllegalArgumentException("记录类型仅支持：main、education、position、assessment、award、rank");
        };
    }

    private String normalizeAssessmentTrackingYear(String assessmentYear) {
        String normalized = emptyToNull(assessmentYear);
        if (normalized == null) {
            return null;
        }
        if (!normalized.matches("\\d{4}")) {
            throw new IllegalArgumentException("考核年度必须为四位年份。");
        }
        return normalized;
    }

    private Integer resolveApprovedWithinDays(String approvalStatus, Integer approvedWithinDays) {
        if (!PersonnelApprovalStatuses.APPROVED.equals(approvalStatus)) {
            return null;
        }
        if (approvedWithinDays == null || approvedWithinDays <= 0) {
            return null;
        }
        return approvedWithinDays;
    }

    private List<PersonnelApprovalTrackingRecord> enrichApprovalTrackingRecords(
            List<PersonnelApprovalTrackingRecord> rows) {
        if (rows.isEmpty()) {
            return rows;
        }
        List<AuditTargetKey> needSubmit = rows.stream()
                .filter(row -> row.submittedBy() == null)
                .map(row -> new AuditTargetKey(row.auditTargetType(), row.auditTargetId()))
                .toList();
        List<AuditTargetKey> needApprove = rows.stream()
                .filter(row -> row.approvedBy() == null)
                .map(row -> new AuditTargetKey(row.auditTargetType(), row.auditTargetId()))
                .toList();
        Map<AuditTargetKey, AuditActorMoment> submitted = securityAuditService.findLatestByTargetsAndActions(
                needSubmit,
                List.of("PERSONNEL_APPROVAL_SUBMIT", "PERSONNEL_SUBRECORD_SUBMIT"));
        Map<AuditTargetKey, AuditActorMoment> approved = securityAuditService.findLatestByTargetsAndActions(
                needApprove,
                List.of("PERSONNEL_APPROVE", "PERSONNEL_SUBRECORD_APPROVE"));
        return rows.stream()
                .map(row -> enrichApprovalTrackingRecord(row, submitted, approved))
                .toList();
    }

    private PersonnelApprovalTrackingRecord enrichApprovalTrackingRecord(
            PersonnelApprovalTrackingRecord row,
            Map<AuditTargetKey, AuditActorMoment> submitted,
            Map<AuditTargetKey, AuditActorMoment> approved) {
        AuditTargetKey key = new AuditTargetKey(row.auditTargetType(), row.auditTargetId());
        AuditActorMoment submitMoment = row.submittedBy() == null
                ? submitted.get(key)
                : new AuditActorMoment(row.submittedBy(), row.submittedAt());
        AuditActorMoment approveMoment = row.approvedBy() == null
                ? approved.get(key)
                : new AuditActorMoment(row.approvedBy(), row.approvedAt());
        return new PersonnelApprovalTrackingRecord(
                row.uid(),
                row.recordType(),
                row.recordId(),
                row.organizationCode(),
                row.organizationName(),
                row.personCode(),
                row.personName(),
                row.summary(),
                row.positionName(),
                row.effectiveYearMonth(),
                row.attachmentCount(),
                row.approvalStatus(),
                row.auditTargetType(),
                row.auditTargetId(),
                submitMoment == null ? null : submitMoment.actorUsername(),
                submitMoment == null ? null : submitMoment.createdAt(),
                approveMoment == null ? null : approveMoment.actorUsername(),
                approveMoment == null ? null : approveMoment.createdAt());
    }

    private String normalizeApprovalTrackingStatus(String status) {
        String normalized = emptyToNull(status);
        if (normalized == null) {
            return PersonnelApprovalStatuses.SUBMITTED;
        }
        if (PersonnelApprovalStatuses.SUBMITTED.equals(normalized)
                || PersonnelApprovalStatuses.APPROVED.equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("审核状态仅支持：申报、审批通过");
    }

    private void requireApprovalTrackingReadPermission() {
        if (!accessControlService.hasAnyPermission(PersonnelFeaturePermissions.approvalTrackingReadAuthorities())) {
            throw new AccessDeniedException("当前用户没有人员审核跟踪查询权限。");
        }
    }

    private PersonKey getPersonKey(int uid) {
        PersonKey key = personnelRepository.findKeyByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(key.organizationCode());
        return key;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String currentActorUsername() {
        return accessControlService.currentUser().getUsername();
    }

    private void notifySubrecordSubmitted(int uid, PersonnelSubrecordType type) {
        personnelRepository.findMaintenanceByUid(uid).ifPresent(existing -> exchangeNotificationService.onPersonnelSubmitted(
                existing.organizationCode(),
                existing.personCode(),
                existing.name(),
                subrecordLabel(type)));
    }

    private void notifySubrecordDecision(int uid, PersonnelSubrecordType type, boolean approved) {
        personnelRepository.findMaintenanceByUid(uid).ifPresent(existing -> exchangeNotificationService.onPersonnelDecided(
                existing.organizationCode(),
                existing.personCode(),
                existing.name(),
                subrecordLabel(type),
                approved));
    }

    private static String subrecordLabel(PersonnelSubrecordType type) {
        return switch (type) {
            case EDUCATION -> "学历子记录";
            case POSITION -> "任职子记录";
            case ASSESSMENT -> "考核子记录";
            case AWARD -> "获奖子记录";
            case RANK -> "警衔子记录";
        };
    }

    private PersonnelMaintenanceRecord loadMaintenanceRecord(int uid) {
        return personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
    }

    private void requireWritePermission() {
        if (!accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)) {
            throw new AccessDeniedException(PersonnelFeaturePermissions.LEGACY_WRITE + " permission required");
        }
    }

    private void requireBasicReadPermission() {
        if (!accessControlService.hasAnyPermission(
                PersonnelFeaturePermissions.BASIC_READ,
                PersonnelFeaturePermissions.LEGACY_READ)) {
            throw new AccessDeniedException(PersonnelFeaturePermissions.BASIC_READ + " permission required");
        }
    }

    private PersonKey requireSubrecordOrganization(Optional<PersonKey> optionalKey, String message) {
        PersonKey key = optionalKey.orElseThrow(() -> new NotFoundException(message));
        accessControlService.requireOrganization(key.organizationCode());
        return key;
    }

    private void requireSamePerson(PersonKey recordKey, PersonKey personKey) {
        if (!recordKey.organizationCode().equals(personKey.organizationCode()) || !recordKey.personCode().equals(personKey.personCode())) {
            throw new AccessDeniedException("Record does not belong to the selected personnel");
        }
    }

    private String requiredOrganizationCode(PersonnelMaintenanceRequest request) {
        return requireOrganizationCode(request.organizationCode());
    }

    private String requireOrganizationCode(String organizationCode) {
        String normalized = emptyToNull(organizationCode);
        if (normalized == null) {
            throw new IllegalArgumentException("单位编码不能为空");
        }
        return normalized;
    }
}
