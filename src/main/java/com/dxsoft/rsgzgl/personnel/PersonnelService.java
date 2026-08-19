package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.retirement.RetirementService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.time.LocalDate;
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

    public PersonnelService(
            PersonnelRepository personnelRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            @Lazy RetirementService retirementService,
            @Lazy PayrollService payrollService) {
        this.personnelRepository = personnelRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.retirementService = retirementService;
        this.payrollService = payrollService;
    }

    public PageResponse<PersonnelSummary> list(String organizationCode, String keyword,
            String sort, String direction, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        List<PersonnelSummary> rows = personnelRepository.findAll(
                scope, emptyToNull(organizationCode), keyword, sort, direction, pageRequest);
        long total = personnelRepository.countAll(scope, emptyToNull(organizationCode), keyword);
        return PageResponse.of(rows, pageRequest, total);
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
        requireWritePermission();
        return record;
    }

    public PersonnelMaintenanceRecord create(PersonnelMaintenanceRequest request) {
        requireWritePermission();
        accessControlService.requireOrganization(requiredOrganizationCode(request));
        int uid = personnelRepository.createPersonnel(request);
        payrollService.ensureNoExperienceInternSalary(uid);
        payrollService.ensureTransferInSalaryDetermination(uid);
        PersonnelMaintenanceRecord created = maintenance(uid);
        operationLogService.record(
                "CREATE_PERSONNEL",
                "ryjbxx",
                String.valueOf(uid),
                "新增人员 " + created.organizationCode() + "-" + created.personCode() + " " + created.name());
        return created;
    }

    public PersonnelMaintenanceRecord update(int uid, PersonnelMaintenanceRequest request) {
        requireWritePermission();
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        accessControlService.requireOrganization(requiredOrganizationCode(request));
        personnelRepository.updatePersonnel(uid, request);
        payrollService.ensureNoExperienceInternSalary(uid);
        payrollService.ensureTransferInSalaryDetermination(uid);
        PersonnelMaintenanceRecord updated = maintenance(uid);
        operationLogService.record(
                "UPDATE_PERSONNEL",
                "ryjbxx",
                String.valueOf(uid),
                "更新人员 " + updated.organizationCode() + "-" + updated.personCode() + " " + updated.name());
        return updated;
    }

    public void delete(int uid) {
        requireWritePermission();
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
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
        requireWritePermission();
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
        requireWritePermission();
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
        return defaultAssessmentResultText(personnel.personnelCategory(), personnel.organizationType());
    }

    static String defaultAssessmentResultText(String personnelCategory, String organizationType) {
        return isCivilServantPersonnel(personnelCategory, organizationType) ? "称职" : "合格";
    }

    static void validateAssessmentResult(String personnelCategory, String organizationType, String result) {
        Set<String> allowed = isCivilServantPersonnel(personnelCategory, organizationType)
                ? ADMINISTRATIVE_ASSESSMENT_RESULTS
                : INSTITUTION_ASSESSMENT_RESULTS;
        if (!allowed.contains(result)) {
            throw new IllegalArgumentException("考核结果无效：" + result);
        }
    }

    /** 年度考核：仅公务员使用「称职」等行政考核结果，其余人员使用「合格」等事业考核结果。 */
    static boolean isCivilServantPersonnel(String personnelCategory, String organizationType) {
        return personnelCategory != null && personnelCategory.contains("公务员");
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
        requireWritePermission();
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
        requireWritePermission();
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
        requireWritePermission();
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
        requireWritePermission();
        personnelRepository.deleteAssessment(id);
        operationLogService.record(
                "DELETE_ASSESSMENT",
                "ndkh",
                String.valueOf(id),
                "删除考核 " + key.organizationCode() + "-" + key.personCode());
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

    private PersonKey getPersonKey(int uid) {
        PersonKey key = personnelRepository.findKeyByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(key.organizationCode());
        return key;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireWritePermission() {
        if (!accessControlService.hasPermission("PERSONNEL_WRITE")) {
            throw new AccessDeniedException("PERSONNEL_WRITE permission required");
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
