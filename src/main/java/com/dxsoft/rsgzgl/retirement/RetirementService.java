package com.dxsoft.rsgzgl.retirement;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.personnel.PersonnelChangeRequest;
import com.dxsoft.rsgzgl.personnel.PersonnelChangeResult;
import com.dxsoft.rsgzgl.personnel.PersonnelRepository;
import com.dxsoft.rsgzgl.payroll.PayrollRepository;
import com.dxsoft.rsgzgl.payroll.PayrollRounding;
import com.dxsoft.rsgzgl.payroll.PayrollRoundingPolicy;
import com.dxsoft.rsgzgl.report.export.ReportPdfService;
import com.dxsoft.rsgzgl.retirement.RetirementRepository.RetirementApprovalDetailRow;
import com.dxsoft.rsgzgl.retirement.RetirementRepository.RetirementSeedInsert;
import com.dxsoft.rsgzgl.retirement.RetirementRepository.RetirementSeedRow;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.statistics.RetirementMonthCalculator;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetirementService {

    private final RetirementRepository retirementRepository;
    private final PersonnelRepository personnelRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;
    private final RetirementApprovalHtmlRenderer approvalHtmlRenderer;
    private final ReportPdfService reportPdfService;
    private final RetirementCalculationEngine calculationEngine;
    private final PayrollRepository payrollRepository;

    RetirementService(
            RetirementRepository retirementRepository,
            PersonnelRepository personnelRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            RetirementApprovalHtmlRenderer approvalHtmlRenderer,
            ReportPdfService reportPdfService,
            RetirementCalculationEngine calculationEngine,
            PayrollRepository payrollRepository) {
        this.retirementRepository = retirementRepository;
        this.personnelRepository = personnelRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.approvalHtmlRenderer = approvalHtmlRenderer;
        this.reportPdfService = reportPdfService;
        this.calculationEngine = calculationEngine;
        this.payrollRepository = payrollRepository;
    }

    public List<RetirementApprovalStyleOption> approvalStyles() {
        return Arrays.stream(RetirementApprovalStyle.values())
                .map(style -> new RetirementApprovalStyleOption(
                        style.code(),
                        style.label(),
                        style.agencyTemplate(),
                        style.institutionTemplate(),
                        style == RetirementApprovalStyle.STYLE_2025))
                .toList();
    }

    public RetirementApprovalStyle resolveStyle(String styleCode) {
        return RetirementApprovalStyle.fromCode(styleCode);
    }

    public String resolveTemplateName(RetirementApprovalStyle style, String organizationNature) {
        if (style == RetirementApprovalStyle.STYLE_2025 || style == RetirementApprovalStyle.STYLE_2021) {
            return style.agencyTemplate();
        }
        boolean agency = organizationNature != null && organizationNature.contains("行政");
        return agency ? style.agencyTemplate() : style.institutionTemplate();
    }

    public PageResponse<RetirementProcessingCandidate> processingCandidates(
            String organizationCode,
            String keyword,
            String referencePeriod,
            boolean includeDescendants,
            PageRequest pageRequest) {
        String orgFilter = emptyToNull(organizationCode);
        if (orgFilter != null) {
            accessControlService.requireOrganization(orgFilter);
        }
        // 含下属时权限范围用全量可访问单位，再按编码前缀过滤；不含下属则精确锁定本单位。
        var scope = includeDescendants || orgFilter == null
                ? accessControlService.organizationScope(Optional.empty())
                : accessControlService.organizationScope(Optional.of(orgFilter));
        String reference = resolveReferencePeriod(referencePeriod);
        List<RetirementProcessingCandidate> rows = new ArrayList<>();
        for (RetirementSeedRow seed : retirementRepository.findActiveSeedCandidates(
                scope, orgFilter, keyword, includeDescendants)) {
            RetirementProcessingCandidate row = toCandidate(seed, reference);
            if (row != null) {
                rows.add(row);
            }
        }
        rows.sort((left, right) -> {
            int byMonth = RetirementMonthCalculator.compareYearMonth(
                    left.calculatedRetirementMonth(), right.calculatedRetirementMonth());
            if (byMonth != 0) {
                return byMonth;
            }
            int byOrg = nullToEmpty(left.organizationCode()).compareTo(nullToEmpty(right.organizationCode()));
            if (byOrg != 0) {
                return byOrg;
            }
            return nullToEmpty(left.personCode()).compareTo(nullToEmpty(right.personCode()));
        });
        return pageOf(rows, pageRequest);
    }

    public RetirementProcessingPreview processingPreview(
            int uid,
            String retirementDate,
            String retirementCategory,
            String retirementReason) {
        RetirementSeedRow seed = retirementRepository.findActiveSeedByUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("未找到可办理退休的在职人员，或缺少当前工资记录：" + uid));
        accessControlService.requireOrganization(seed.organizationCode());
        return buildPreview(seed, retirementDate, retirementCategory, retirementReason);
    }

    @Transactional
    public RetirementProcessingApplyResult applyProcessing(int uid, RetirementProcessingApplyRequest request) {
        requireRetirementWrite();
        return doApplyProcessing(uid, request, "离退域退休办理，离退人员编码 ");
    }

    /**
     * 在职人员变动「退休」入口：计发写入 {@code ryjbxxb(待办退休)} 并归档在职记录。
     * 权限由人员变动侧校验，此处不再要求 RETIREMENT_WRITE。
     */
    @Transactional
    public PersonnelChangeResult applyFromPersonnelChange(int uid, PersonnelChangeRequest request) {
        RetirementProcessingApplyRequest applyRequest = new RetirementProcessingApplyRequest(
                request == null ? null : request.effectivePeriod(),
                "退休",
                "到龄",
                request == null ? null : request.remark());
        RetirementProcessingApplyResult applied = doApplyProcessing(
                uid, applyRequest, "在职人员变动退休，离退人员编码 ");
        return new PersonnelChangeResult(
                applied.organizationCode(),
                applied.sourcePersonCode(),
                applied.name(),
                "退休",
                "已归档并写入离退休待办（" + applied.retireePersonCode()
                        + "），合计 " + applied.estimatedTotal() + "。请到离退域「离退休人员」办理/审批。");
    }

    private RetirementProcessingApplyResult doApplyProcessing(
            int uid,
            RetirementProcessingApplyRequest request,
            String remarkPrefix) {
        RetirementSeedRow seed = retirementRepository.findActiveSeedByUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("未找到可办理退休的在职人员，或缺少当前工资记录：" + uid));
        accessControlService.requireOrganization(seed.organizationCode());
        RetirementProcessingPreview preview = buildPreview(
                seed,
                request == null ? null : request.retirementDate(),
                request == null ? null : request.retirementCategory(),
                request == null ? null : request.retirementReason());
        if (!Boolean.TRUE.equals(preview.applyEligible())) {
            throw new IllegalArgumentException(preview.note() == null ? "当前人员不满足退休办理条件。" : preview.note());
        }
        int retireeId = retirementRepository.insertRetiree(toInsert(seed, preview));
        String remark = request == null || request.remark() == null || request.remark().isBlank()
                ? remarkPrefix + preview.retireePersonCode()
                : request.remark().trim();
        PersonnelChangeResult archive = personnelRepository.archivePersonnelChange(
                uid,
                new PersonnelChangeRequest("退休", preview.retirementDate(), remark));
        operationLogService.record(
                "APPLY_RETIREMENT_PROCESSING",
                "ryjbxxb",
                preview.retireePersonCode(),
                "退休建档 " + seed.organizationCode() + "-" + seed.personCode()
                        + " → 离退 " + preview.retireePersonCode() + " " + seed.name()
                        + "，退休时间 " + preview.retirementDate()
                        + "，状态 " + preview.approvalStatus());
        return new RetirementProcessingApplyResult(
                uid,
                archive.organizationCode(),
                archive.personCode(),
                preview.retireePersonCode(),
                archive.name(),
                preview.retirementDate(),
                retireeId,
                preview.estimatedTotal(),
                "退休办理完成：已写入离退休主档（待办退休）并归档在职记录。");
    }

    public PageResponse<RetirementRetireeRecord> retirees(
            String organizationCode,
            String keyword,
            boolean includeDescendants,
            boolean pendingOnly,
            PageRequest pageRequest) {
        String orgFilter = emptyToNull(organizationCode);
        if (orgFilter != null) {
            accessControlService.requireOrganization(orgFilter);
        }
        var scope = includeDescendants || orgFilter == null
                ? accessControlService.organizationScope(Optional.empty())
                : accessControlService.organizationScope(Optional.of(orgFilter));
        long total = retirementRepository.countRetirees(
                scope, orgFilter, keyword, true, includeDescendants, pendingOnly);
        if (total == 0) {
            return PageResponse.of(List.of(), pageRequest, 0);
        }
        List<RetirementRetireeRecord> rows = retirementRepository.findRetirees(
                scope,
                orgFilter,
                keyword,
                true,
                includeDescendants,
                pendingOnly,
                pageRequest.size(),
                pageRequest.offset());
        return PageResponse.of(rows, pageRequest, total);
    }

    public PageResponse<RetirementRetireeRecord> approvalReportCandidates(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        return retirees(organizationCode, keyword, false, false, pageRequest);
    }

    public RetirementRetireeDetail retireeDetail(int id) {
        var scope = accessControlService.organizationScope(Optional.empty());
        RetirementRepository.RetirementRetireeDetailRow row = retirementRepository.findRetireeDetailById(scope, id)
                .orElseThrow(() -> new IllegalArgumentException("未找到离退休人员：" + id));
        accessControlService.requireOrganization(row.organizationCode());
        return toDetail(row);
    }

    @Transactional
    public RetirementRetireeDetail updateRetiree(int id, RetirementRetireeUpdateRequest request) {
        requireRetirementWrite();
        var scope = accessControlService.organizationScope(Optional.empty());
        RetirementRepository.RetirementRetireeDetailRow existing = retirementRepository.findRetireeDetailById(scope, id)
                .orElseThrow(() -> new IllegalArgumentException("未找到离退休人员：" + id));
        accessControlService.requireOrganization(existing.organizationCode());
        if ("审批通过".equals(nullToEmpty(existing.approvalStatus()))) {
            throw new IllegalStateException("审批通过的记录不可修改。");
        }
        String name = required(request == null ? null : request.name(), "姓名");
        String category = required(request == null ? null : request.retirementCategory(), "离退休类别");
        String postCategory = required(request == null ? null : request.postCategory(), "岗位分类");
        String approvalOrganization = emptyToNull(request == null ? null : request.approvalOrganization()) == null
                ? existing.approvalOrganization()
                : request.approvalOrganization().trim();
        if (nullToEmpty(approvalOrganization).isBlank()) {
            throw new IllegalStateException("审批单位不能为空。");
        }
        String positionName = nullToEmpty(request.positionName());
        String positionCode = emptyToNull(request.positionCode()) == null
                ? nullToEmpty(existing.positionCode())
                : request.positionCode().trim();
        if (positionCode.isBlank() && !positionName.isBlank()) {
            positionCode = retirementRepository.findPositionCodeByName(positionName).orElse("");
        }
        if (positionCode.isBlank()) {
            throw new IllegalStateException("职务岗位编码缺失，请重新选择职务岗位后再保存。");
        }
        String gradeStep = nullToEmpty(request.gradeStep());
        String gradeLevel = nullToEmpty(request.gradeLevel());
        if (isWorkerPostCategory(postCategory) || !isCivilServantPostCategory(postCategory)) {
            gradeLevel = "";
        }
        String retirementDate = formatDisplayPeriod(request.retirementDate());
        int salaryYears = request.salaryYears() == null ? existing.salaryYears() : request.salaryYears();
        int teachingRaisePercentage = request.teachingRaisePercentage() == null
                ? existing.teachingRaisePercentage()
                : request.teachingRaisePercentage();
        int increaseRatio = request.increaseRatio() == null ? existing.increaseRatio() : request.increaseRatio();
        if (increaseRatio < 0 || increaseRatio % 5 != 0) {
            throw new IllegalArgumentException("提高比例须为 5 的倍数。");
        }
        RetirementSeedRow calcSeed = new RetirementSeedRow(
                existing.id(),
                existing.organizationCode(),
                existing.organizationName(),
                existing.personCode(),
                name,
                nullToEmpty(request.idCard()),
                nullToEmpty(request.gender()),
                formatDisplayPeriod(request.birthYearMonth()),
                "",
                "",
                postCategory,
                formatDisplayPeriod(request.workStartYearMonth()),
                request.interruptedYears() == null ? 0 : request.interruptedYears(),
                salaryYears,
                emptyToNull(request.education()) == null
                        ? nullToEmpty(existing.education())
                        : request.education().trim(),
                0,
                emptyToNull(request.nation()) == null
                        ? (emptyToNull(existing.nation()) == null ? "汉" : existing.nation())
                        : request.nation().trim(),
                approvalOrganization,
                nullToEmpty(existing.assessmentStartYear()),
                "",
                nullToEmpty(request.interruptedNote()),
                nullToEmpty(request.interruptedMonths()),
                teachingRaisePercentage,
                "",
                request.teachingYears() == null ? existing.teachingYears() : request.teachingYears(),
                positionCode,
                positionName,
                gradeStep,
                gradeLevel,
                "",
                "200607",
                "201401",
                existing.positionSalary(),
                existing.gradeSalary(),
                existing.technicalSalary(),
                0,
                existing.rankAllowance(),
                existing.retainedAllowance(),
                existing.localAllowance(),
                existing.postAllowance(),
                existing.floatingSalary(),
                existing.bonusBalance(),
                existing.livingAllowance(),
                existing.specialPostAllowance(),
                existing.positionAllowance(),
                existing.otherAllowance(),
                0,
                0,
                existing.totalAmount());
        RetirementWageCalculation wage = calculationEngine.calculate(calcSeed, retirementDate, category);
        // 职务/岗位、级别/薪级、技术等级工资自动计算，不可手工覆盖
        int positionSalary = wage.positionSalary() > 0 ? wage.positionSalary() : Math.max(existing.positionSalary(), 0);
        int gradeSalary = wage.gradeSalary() > 0 ? wage.gradeSalary() : Math.max(existing.gradeSalary(), 0);
        int technicalSalary = wage.technicalSalary() > 0 ? wage.technicalSalary() : Math.max(existing.technicalSalary(), 0);
        int autoTeachingRaise = PayrollRounding.zroundPercent(
                positionSalary + gradeSalary, Math.max(teachingRaisePercentage, 0), this.roundingPolicy());
        int teachingRaiseAmount = request.teachingRaise() == null ? autoTeachingRaise : Math.max(request.teachingRaise(), 0);
        int rankAllowance = request.rankAllowance() == null
                ? Math.max(existing.rankAllowance(), 0)
                : Math.max(request.rankAllowance(), 0);
        int bonusBalance = request.bonusBalance() == null
                ? Math.max(existing.bonusBalance(), 0)
                : Math.max(request.bonusBalance(), 0);
        int conversionRatio = retirementRepository.lookupConversionRatio(postCategory, salaryYears, category);
        int effectiveRatio = Math.min(100, conversionRatio + Math.max(increaseRatio, 0));
        int convertedWageBase = positionSalary + gradeSalary + technicalSalary + rankAllowance + teachingRaiseAmount;
        int convertedBase = PayrollRounding.zroundPercent(convertedWageBase, effectiveRatio, this.roundingPolicy());
        int basicRetirementFee = convertedBase + wage.teachingAllowance();
        int afterAllowanceTotal = wage.afterAllowanceTotal() - wage.afterBonusBalance() + bonusBalance;
        int totalAmount = basicRetirementFee + existing.cumulativeIncrease() + afterAllowanceTotal;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("gender", nullToEmpty(request.gender()))
                .addValue("idCard", nullToEmpty(request.idCard()))
                .addValue("nation", emptyToNull(request.nation()) == null
                        ? (emptyToNull(existing.nation()) == null ? "汉" : existing.nation())
                        : request.nation().trim())
                .addValue("birthYearMonth", formatDisplayPeriod(request.birthYearMonth()))
                .addValue("workStartYearMonth", formatDisplayPeriod(request.workStartYearMonth()))
                .addValue("interruptedYears", request.interruptedYears() == null ? 0 : request.interruptedYears())
                .addValue("salaryYears", salaryYears)
                .addValue("education", emptyToNull(request.education()) == null
                        ? nullToEmpty(existing.education())
                        : request.education().trim())
                .addValue("retirementCategory", category)
                .addValue("retirementDate", retirementDate)
                .addValue("retirementReason", nullToEmpty(request.retirementReason()))
                .addValue("postCategory", postCategory)
                .addValue("positionCode", positionCode)
                .addValue("positionName", positionName)
                .addValue("gradeLevel", gradeLevel)
                .addValue("gradeStep", gradeStep)
                .addValue("assessmentStartYear", existing.assessmentStartYear())
                .addValue("salaryStandardYearMonth", "200607")
                .addValue("allowanceStandardYearMonth", "201401")
                .addValue("approvalOrganization", approvalOrganization)
                .addValue("teachingRaisePercentage", teachingRaisePercentage)
                .addValue("teachingYears",
                        request.teachingYears() == null ? existing.teachingYears() : request.teachingYears())
                .addValue("increaseRatio", increaseRatio)
                .addValue("increaseReason", nullToEmpty(request.increaseReason()))
                .addValue("approvalDocumentNumber", nullToEmpty(request.approvalDocumentNumber()))
                .addValue("interruptedNote", nullToEmpty(request.interruptedNote()))
                .addValue("interruptedMonths", nullToEmpty(request.interruptedMonths()))
                .addValue("bankAccount", emptyToNull(request.bankAccount()) == null
                        ? nullToEmpty(existing.bankAccount())
                        : request.bankAccount().trim())
                .addValue("positionSalary", positionSalary)
                .addValue("gradeSalary", gradeSalary)
                .addValue("technicalSalary", technicalSalary)
                .addValue("teachingRaiseAmount", teachingRaiseAmount)
                .addValue("rankAllowance", rankAllowance)
                .addValue("basicRetirementFee", basicRetirementFee)
                .addValue("retainedAllowance", wage.afterRetainedAllowance())
                .addValue("localAllowance", wage.afterLocalAllowance())
                .addValue("postAllowance", wage.afterPostAllowance())
                .addValue("floatingSalary", wage.afterFloatingSalary())
                .addValue("bonusBalance", bonusBalance)
                .addValue("livingAllowance", wage.afterLivingAllowance())
                .addValue("specialPostAllowance", wage.afterSpecialPostAllowance())
                .addValue("positionAllowance", wage.afterPositionAllowance())
                .addValue("otherAllowance", wage.afterOtherAllowance())
                .addValue("totalAmount", totalAmount);
        retirementRepository.updateRetiree(id, parameters);
        operationLogService.record(
                "UPDATE_RETIREE",
                "ryjbxxb",
                existing.personCode(),
                "维护离退人员 " + existing.organizationCode() + "-" + existing.personCode() + " " + name);
        return retireeDetail(id);
    }

    @Transactional
    public RetirementRetireeDetail approveRetiree(int id) {
        requireRetirementWrite();
        var scope = accessControlService.organizationScope(Optional.empty());
        RetirementRepository.RetirementRetireeDetailRow existing = retirementRepository.findRetireeDetailById(scope, id)
                .orElseThrow(() -> new IllegalArgumentException("未找到离退休人员：" + id));
        accessControlService.requireOrganization(existing.organizationCode());
        String status = nullToEmpty(existing.approvalStatus());
        if ("审批通过".equals(status)) {
            return toDetail(existing);
        }
        if (!List.of("建库未核", "申报", "待办退休", "").contains(status)) {
            throw new IllegalStateException("当前审批状态不允许审批通过：" + status);
        }
        retirementRepository.updateRetireeApprovalStatus(id, "审批通过");
        operationLogService.record(
                "APPROVE_RETIREE",
                "ryjbxxb",
                existing.personCode(),
                "审批通过 " + existing.organizationCode() + "-" + existing.personCode() + " " + existing.name());
        return retireeDetail(id);
    }

    @Transactional
    public RetirementRetireeDetail cancelRetireeApproval(int id) {
        requireRetirementWrite();
        var scope = accessControlService.organizationScope(Optional.empty());
        RetirementRepository.RetirementRetireeDetailRow existing = retirementRepository.findRetireeDetailById(scope, id)
                .orElseThrow(() -> new IllegalArgumentException("未找到离退休人员：" + id));
        accessControlService.requireOrganization(existing.organizationCode());
        if (!"审批通过".equals(nullToEmpty(existing.approvalStatus()))) {
            throw new IllegalStateException("仅审批通过的记录可以取消审核。");
        }
        retirementRepository.updateRetireeApprovalStatus(id, "待办退休");
        operationLogService.record(
                "CANCEL_RETIREE_APPROVAL",
                "ryjbxxb",
                existing.personCode(),
                "取消审核 " + existing.organizationCode() + "-" + existing.personCode() + " " + existing.name());
        return retireeDetail(id);
    }

    private RetirementRetireeDetail toDetail(RetirementRepository.RetirementRetireeDetailRow row) {
        String status = nullToEmpty(row.approvalStatus());
        boolean approved = "审批通过".equals(status);
        boolean editable = !approved;
        boolean approvable = !approved && (
                status.isBlank()
                        || "建库未核".equals(status)
                        || "申报".equals(status)
                        || "待办退休".equals(status));
        int conversionRatio = retirementRepository.lookupConversionRatio(
                row.postCategory(), row.salaryYears(), row.retirementCategory());
        int effectiveRatio = Math.min(100, conversionRatio + Math.max(row.increaseRatio(), 0));
        int convertedWageBase = row.positionSalary() + row.gradeSalary() + row.technicalSalary()
                + row.teachingRaise() + row.rankAllowance();
        int convertedAmount = PayrollRounding.zroundPercent(convertedWageBase, effectiveRatio, this.roundingPolicy());
        int beforeAllowanceTotal = row.beforeRetainedAllowance() + row.beforeLocalAllowance()
                + row.beforePostAllowance() + row.beforeFloatingSalary() + row.beforeBonusBalance()
                + row.beforeLivingAllowance() + row.beforeSpecialPostAllowance()
                + row.beforePositionAllowance() + row.beforeOtherAllowance();
        int afterAllowanceTotal = row.retainedAllowance() + row.localAllowance() + row.postAllowance()
                + row.floatingSalary() + row.bonusBalance() + row.livingAllowance()
                + row.specialPostAllowance() + row.positionAllowance() + row.otherAllowance();
        Optional<int[]> levelRange = retirementRepository.findPositionLevelRange(row.positionCode());
        if (levelRange.isEmpty() && nullToEmpty(row.positionCode()).length() > 4) {
            String code = row.positionCode().trim();
            levelRange = retirementRepository.findPositionLevelRange(code.substring(code.length() - 4));
        }
        return new RetirementRetireeDetail(
                row.id(),
                row.organizationCode(),
                row.organizationName(),
                row.personCode(),
                row.name(),
                row.gender(),
                row.idCard(),
                row.nation(),
                RetirementMonthCalculator.formatYearMonth(row.birthYearMonth()),
                RetirementMonthCalculator.formatYearMonth(row.workStartYearMonth()),
                row.interruptedYears(),
                row.salaryYears(),
                row.education(),
                row.retirementCategory(),
                RetirementMonthCalculator.formatYearMonth(row.retirementDate()),
                row.retirementReason(),
                row.postCategory(),
                row.positionCode(),
                row.positionName(),
                row.gradeLevel(),
                row.gradeStep(),
                levelRange.map(range -> Math.min(range[0], range[1])).orElse(null),
                levelRange.map(range -> Math.max(range[0], range[1])).orElse(null),
                row.assessmentStartYear(),
                row.salaryStandardYearMonth(),
                row.allowanceStandardYearMonth(),
                row.approvalOrganization(),
                row.approvalStatus(),
                row.approvalDocumentNumber(),
                row.interruptedNote(),
                row.interruptedMonths(),
                row.increaseReason(),
                row.teachingRaisePercentage(),
                row.teachingYears(),
                conversionRatio,
                row.increaseRatio(),
                effectiveRatio,
                row.positionSalary(),
                row.gradeSalary(),
                row.technicalSalary(),
                row.teachingRaise(),
                row.rankAllowance(),
                convertedWageBase,
                convertedAmount,
                row.basicRetirementFee() > 0 ? row.basicRetirementFee() : convertedAmount,
                row.cumulativeIncrease(),
                row.beforePositionSalary(),
                row.beforeGradeSalary(),
                row.beforeTechnicalSalary(),
                row.beforeTeachingRaise(),
                row.beforeRankAllowance(),
                row.beforeRetainedAllowance(),
                row.beforeLocalAllowance(),
                row.beforePostAllowance(),
                row.beforeFloatingSalary(),
                row.beforeBonusBalance(),
                row.beforeLivingAllowance(),
                row.beforeSpecialPostAllowance(),
                row.beforePositionAllowance(),
                row.beforeOtherAllowance(),
                beforeAllowanceTotal,
                row.beforeTotal(),
                row.retainedAllowance(),
                row.localAllowance(),
                row.postAllowance(),
                row.floatingSalary(),
                row.bonusBalance(),
                row.livingAllowance(),
                row.specialPostAllowance(),
                row.positionAllowance(),
                row.otherAllowance(),
                afterAllowanceTotal,
                row.totalAmount(),
                row.bankAccount(),
                row.sourceOrganizationCode(),
                row.sourcePersonCode(),
                editable,
                approvable,
                approved);
    }

    private boolean isWorkerPostCategory(String postCategory) {
        String value = nullToEmpty(postCategory);
        return "机关技术工人".equals(value)
                || "机关普通工人".equals(value)
                || "事业技术工人".equals(value)
                || "事业普通工人".equals(value)
                || "技术工岗位".equals(value)
                || "普通工岗位".equals(value);
    }

    private boolean isCivilServantPostCategory(String postCategory) {
        String value = nullToEmpty(postCategory);
        if (value.isBlank()) {
            return true;
        }
        if (isWorkerPostCategory(value) || value.contains("事业")) {
            return false;
        }
        return true;
    }

    private int firstPositive(Integer preferred, int calculated, int fallback) {
        if (preferred != null && preferred > 0) {
            return preferred;
        }
        if (calculated > 0) {
            return calculated;
        }
        return Math.max(fallback, 0);
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "不能为空。");
        }
        return value.trim();
    }

    public RetirementPositionLevelRange positionLevelRange(String positionCode) {
        String code = nullToEmpty(positionCode);
        if (code.isBlank()) {
            return new RetirementPositionLevelRange("", false, null, null);
        }
        Optional<int[]> range = retirementRepository.findPositionLevelRange(code);
        if (range.isEmpty() && code.length() > 4) {
            // 字典全码可能带前缀，回退取后 4 位职务编码（如 0510190 → 0190）
            range = retirementRepository.findPositionLevelRange(code.substring(code.length() - 4));
        }
        if (range.isEmpty()) {
            return new RetirementPositionLevelRange(code, false, null, null);
        }
        int[] bounds = range.get();
        int min = Math.min(bounds[0], bounds[1]);
        int max = Math.max(bounds[0], bounds[1]);
        return new RetirementPositionLevelRange(code, true, min, max);
    }

    public List<RetirementRatioStandard> ratioStandards() {
        return retirementRepository.findRatioStandards();
    }

    public ResponseEntity<byte[]> exportApprovalReportPdf(RetirementApprovalExportRequest request) {
        List<RetirementApprovalSheet> sheets = buildApprovalSheets(request);
        byte[] pdf = reportPdfService.renderPdf(approvalHtmlRenderer.renderDocument(sheets));
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "retirement-approval-" + stamp + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    public String previewApprovalReportHtml(RetirementApprovalExportRequest request) {
        return approvalHtmlRenderer.renderPreviewBody(buildApprovalSheets(request));
    }

    private List<RetirementApprovalSheet> buildApprovalSheets(RetirementApprovalExportRequest request) {
        List<Integer> ids = normalizeIds(request == null ? null : request.retireeIds());
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("请勾选至少一名离退休人员。");
        }
        RetirementApprovalStyle style = resolveStyle(request == null ? null : request.style());
        String organizationNature = emptyToNull(request == null ? null : request.organizationNature()) == null
                ? "事业"
                : request.organizationNature().trim();
        String template = resolveTemplateName(style, organizationNature);
        var scope = accessControlService.organizationScope(Optional.empty());
        List<RetirementApprovalDetailRow> rows = retirementRepository.findApprovalDetailsByIds(scope, ids);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("未找到可打印的离退休人员记录。");
        }
        Set<Integer> found = new LinkedHashSet<>();
        List<RetirementApprovalSheet> sheets = new ArrayList<>();
        for (RetirementApprovalDetailRow row : rows) {
            accessControlService.requireOrganization(row.organizationCode());
            found.add(row.id());
            int ratio = retirementRepository.lookupConversionRatio(
                    row.postCategory(), row.salaryYears(), row.retirementCategory());
            sheets.add(new RetirementApprovalSheet(
                    row.id(),
                    template,
                    style.label(),
                    organizationNature,
                    row.organizationCode(),
                    row.organizationName(),
                    row.personCode(),
                    row.name(),
                    row.gender(),
                    row.idCard(),
                    row.nation(),
                    RetirementMonthCalculator.formatYearMonth(row.birthYearMonth()),
                    RetirementMonthCalculator.formatYearMonth(row.workStartYearMonth()),
                    row.salaryYears(),
                    row.education(),
                    row.retirementCategory(),
                    RetirementMonthCalculator.formatYearMonth(row.retirementDate()),
                    row.retirementReason(),
                    row.postCategory(),
                    row.positionCode(),
                    row.positionName(),
                    row.beforePositionName(),
                    row.gradeLevel(),
                    row.gradeStep(),
                    row.interruptedNote(),
                    row.interruptedMonths(),
                    ratio,
                    row.increaseRatio(),
                    row.beforePositionSalary(),
                    row.beforeGradeSalary(),
                    row.beforeTechnicalSalary(),
                    row.beforeTeachingRaise(),
                    row.beforeRankAllowance(),
                    row.beforeRetainedAllowance(),
                    row.beforeBonusBalance(),
                    row.beforeJobAllowance(),
                    row.beforeLocalAllowance(),
                    row.beforePostAllowance(),
                    row.beforeOther(),
                    row.beforeTotal(),
                    row.afterPositionSalary(),
                    row.afterGradeSalary(),
                    row.afterTechnicalSalary(),
                    row.afterTeachingRaise(),
                    row.afterRankAllowance(),
                    row.afterRetainedAllowance(),
                    row.afterLocalAllowance(),
                    row.afterPostAllowance(),
                    row.afterConvertedBase(),
                    row.afterOther(),
                    row.afterTotal(),
                    row.teachingAgeAllowance(),
                    row.livingAllowance(),
                    row.cumulativeIncrease(),
                    row.approvalStatus()));
        }
        if (found.size() != ids.size()) {
            List<Integer> missing = ids.stream().filter(id -> !found.contains(id)).toList();
            throw new IllegalArgumentException("部分人员不存在或无权访问：" + missing);
        }
        Map<Integer, RetirementApprovalSheet> byId = new HashMap<>();
        for (RetirementApprovalSheet sheet : sheets) {
            byId.put(sheet.id(), sheet);
        }
        List<RetirementApprovalSheet> ordered = new ArrayList<>();
        for (Integer id : ids) {
            RetirementApprovalSheet sheet = byId.get(id);
            if (sheet != null) {
                ordered.add(sheet);
            }
        }
        return ordered;
    }

    private List<Integer> normalizeIds(List<Integer> retireeIds) {
        if (retireeIds == null || retireeIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> unique = new LinkedHashSet<>();
        for (Integer id : retireeIds) {
            if (id != null && id > 0) {
                unique.add(id);
            }
        }
        return List.copyOf(unique);
    }

    private RetirementProcessingCandidate toCandidate(RetirementSeedRow seed, String referencePeriod) {
        RetirementMonthCalculator.CalculationResult calculation = RetirementMonthCalculator.calculate(
                seed.birthYearMonth(), seed.gender(), seed.positionCode());
        if (calculation.retirementYearMonth().isBlank()) {
            return null;
        }
        if (RetirementMonthCalculator.compareYearMonth(calculation.retirementYearMonth(), referencePeriod) > 0) {
            return null;
        }
        boolean seeded = retirementRepository.existsRetireeByInterfaceKey(seed.organizationCode(), seed.personCode());
        return new RetirementProcessingCandidate(
                seed.uid(),
                seed.organizationCode(),
                seed.organizationName(),
                seed.personCode(),
                seed.name(),
                seed.gender(),
                RetirementMonthCalculator.formatYearMonth(seed.birthYearMonth()),
                seed.postCategory(),
                seed.positionCode(),
                seed.positionName(),
                RetirementMonthCalculator.formatYearMonth(seed.workStartYearMonth()),
                seed.salaryYears(),
                calculation.retirementYearMonth(),
                calculation.category().label(),
                seed.currentTotal(),
                seeded,
                seeded ? "该人员已存在离退接口记录，不能重复办理。" : "可办理套改后退休建档。");
    }

    private RetirementProcessingPreview buildPreview(
            RetirementSeedRow seed,
            String retirementDate,
            String retirementCategory,
            String retirementReason) {
        boolean seeded = retirementRepository.existsRetireeByInterfaceKey(seed.organizationCode(), seed.personCode());
        RetirementMonthCalculator.CalculationResult calculation = RetirementMonthCalculator.calculate(
                seed.birthYearMonth(), seed.gender(), seed.positionCode());
        String defaultDate = calculation.retirementYearMonth().isBlank()
                ? resolveReferencePeriod(null)
                : calculation.retirementYearMonth();
        String date = formatDisplayPeriod(emptyToNull(retirementDate) == null ? defaultDate : retirementDate);
        if (RetirementMonthCalculator.compareYearMonth(date, "200607") <= 0) {
            return previewBlocked(seed, date, retirementCategory, retirementReason, seeded,
                    "仅支持 2006.07 之后的套改后退休办理。");
        }
        if (seeded) {
            return previewBlocked(seed, date, retirementCategory, retirementReason, true,
                    "该在职人员已办理离退建档（jkdwbm/jkgrbm 已存在）。");
        }
        String category = emptyToNull(retirementCategory) == null ? "退休" : retirementCategory.trim();
        String reason = emptyToNull(retirementReason) == null ? "到龄" : retirementReason.trim();
        RetirementWageCalculation wage = calculationEngine.calculate(seed, date, category);
        String retireeCode = retirementRepository.allocateRetireePersonCode(seed.organizationCode());
        return new RetirementProcessingPreview(
                seed.uid(),
                seed.organizationCode(),
                seed.organizationName(),
                seed.personCode(),
                retireeCode,
                seed.name(),
                seed.gender(),
                RetirementMonthCalculator.formatYearMonth(seed.birthYearMonth()),
                category,
                date,
                reason,
                seed.postCategory(),
                seed.positionCode(),
                seed.positionName(),
                seed.gradeLevel(),
                seed.gradeStep(),
                wage.salaryYears(),
                seed.raisePercentage(),
                wage.conversionRatio(),
                wage.increaseRatio(),
                wage.effectiveRatio(),
                wage.positionSalary(),
                wage.gradeSalary(),
                wage.technicalSalary(),
                wage.teachingRaise(),
                wage.rankAllowance(),
                wage.retainedSpecial(),
                wage.teachingAllowance(),
                wage.wageBase(),
                wage.convertedWageBase(),
                wage.basicRetirementFee(),
                wage.beforeRetainedAllowance(),
                wage.beforeLocalAllowance(),
                wage.beforePostAllowance(),
                wage.beforeFloatingSalary(),
                wage.beforeBonusBalance(),
                wage.beforeLivingAllowance(),
                wage.beforeSpecialPostAllowance(),
                wage.beforePositionAllowance(),
                wage.beforeOtherAllowance(),
                wage.beforeAllowanceTotal(),
                wage.beforeTotal(),
                wage.afterRetainedAllowance(),
                wage.afterLocalAllowance(),
                wage.afterPostAllowance(),
                wage.afterFloatingSalary(),
                wage.afterBonusBalance(),
                wage.afterLivingAllowance(),
                wage.afterSpecialPostAllowance(),
                wage.afterPositionAllowance(),
                wage.afterOtherAllowance(),
                wage.afterAllowanceTotal(),
                wage.afterTotal(),
                "待办退休",
                wage.note(),
                true,
                wage.wagesFromStandards(),
                wage.allowancesFromStandards());
    }

    private RetirementProcessingPreview previewBlocked(
            RetirementSeedRow seed,
            String date,
            String retirementCategory,
            String retirementReason,
            boolean seeded,
            String note) {
        return new RetirementProcessingPreview(
                seed.uid(),
                seed.organizationCode(),
                seed.organizationName(),
                seed.personCode(),
                null,
                seed.name(),
                seed.gender(),
                RetirementMonthCalculator.formatYearMonth(seed.birthYearMonth()),
                emptyToNull(retirementCategory) == null ? "退休" : retirementCategory.trim(),
                date,
                emptyToNull(retirementReason) == null ? "到龄" : retirementReason.trim(),
                seed.postCategory(),
                seed.positionCode(),
                seed.positionName(),
                seed.gradeLevel(),
                seed.gradeStep(),
                seed.salaryYears(),
                seed.raisePercentage(),
                0,
                0,
                0,
                seed.positionSalary(),
                seed.gradeSalary(),
                seed.technicalSalary(),
                seed.teachingRaise(),
                seed.rankAllowance(),
                seed.retainedSpecial(),
                seed.teachingAllowance(),
                0,
                0,
                0,
                seed.retainedAllowance(),
                seed.localAllowance(),
                seed.postAllowance(),
                seed.floatingSalary(),
                seed.bonusBalance(),
                seed.livingAllowance(),
                seed.specialPostAllowance(),
                seed.positionAllowance(),
                seed.otherAllowance(),
                0,
                seed.currentTotal(),
                seed.retainedAllowance(),
                seed.localAllowance(),
                seed.postAllowance(),
                seed.floatingSalary(),
                seed.bonusBalance(),
                seed.livingAllowance(),
                seed.specialPostAllowance(),
                seed.positionAllowance(),
                seed.otherAllowance(),
                0,
                seed.currentTotal(),
                "待办退休",
                note,
                false,
                false,
                false);
    }

    private RetirementSeedInsert toInsert(RetirementSeedRow seed, RetirementProcessingPreview preview) {
        String standardYm = digitsPeriod(emptyToNull(seed.salaryStandardYearMonth()) == null
                ? "200607"
                : seed.salaryStandardYearMonth());
        String allowanceYm = digitsPeriod(emptyToNull(seed.allowanceStandardYearMonth()) == null
                ? "201401"
                : seed.allowanceStandardYearMonth());
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("organizationCode", seed.organizationCode())
                .addValue("retireePersonCode", preview.retireePersonCode())
                .addValue("name", seed.name())
                .addValue("idCard", nullToEmpty(seed.idCard()))
                .addValue("gender", nullToEmpty(seed.gender()))
                .addValue("birthYearMonth", formatDisplayPeriod(seed.birthYearMonth()))
                .addValue("organizationType", nullToEmpty(seed.organizationType()))
                .addValue("personnelCategory", nullToEmpty(seed.personnelCategory()))
                .addValue("postCategory", nullToEmpty(seed.postCategory()))
                .addValue("workStartYearMonth", formatDisplayPeriod(seed.workStartYearMonth()))
                .addValue("interruptedYears", seed.interruptedYears())
                .addValue("salaryYears", preview.salaryYears() == null ? seed.salaryYears() : preview.salaryYears())
                .addValue("education", nullToEmpty(seed.education()))
                .addValue("educationYears", seed.educationYears())
                .addValue("retirementCategory", preview.retirementCategory())
                .addValue("retirementDate", preview.retirementDate())
                .addValue("stepAssessmentYear", nullToEmpty(seed.stepAssessmentYear()))
                .addValue("levelAssessmentYear", nullToEmpty(seed.levelAssessmentYear()))
                .addValue("interruptedNote", nullToEmpty(seed.interruptedNote()))
                .addValue("interruptedMonths", nullToEmpty(seed.interruptedMonths()))
                .addValue("approvalStatus", preview.approvalStatus())
                .addValue("raisePercentage", seed.raisePercentage())
                .addValue("nation", emptyToNull(seed.nation()) == null ? "汉" : seed.nation())
                .addValue("approvalOrganization", nullToEmpty(seed.approvalOrganization()))
                .addValue("retirementReason", preview.retirementReason())
                .addValue("standardYearMonth", standardYm)
                .addValue("allowanceStandard", allowanceYm)
                .addValue("sourceOrganizationCode", seed.organizationCode())
                .addValue("sourcePersonCode", seed.personCode())
                .addValue("retainedSpecial", intOrZero(preview.retainedSpecial()))
                .addValue("teachingYears", seed.teachingInterruptedYears())
                .addValue("teachingAllowance", intOrZero(preview.teachingAllowance()))
                .addValue("positionCode", nullToEmpty(seed.positionCode()))
                .addValue("positionName", nullToEmpty(seed.positionName()))
                .addValue("gradeStep", nullToEmpty(seed.gradeStep()))
                .addValue("positionSalary", intOrZero(preview.positionSalary()))
                .addValue("gradeLevel", nullToEmpty(seed.gradeLevel()))
                .addValue("gradeStepExtra", nullToEmpty(seed.gradeStepExtra()))
                .addValue("gradeSalary", intOrZero(preview.gradeSalary()))
                .addValue("technicalSalary", intOrZero(preview.technicalGradeSalary()))
                .addValue("teachingRaise", intOrZero(preview.teachingRaise()))
                .addValue("rankAllowance", intOrZero(preview.rankAllowance()))
                .addValue("beforeRetainedAllowance", intOrZero(preview.beforeRetainedAllowance()))
                .addValue("beforeLocalAllowance", intOrZero(preview.beforeLocalAllowance()))
                .addValue("beforePostAllowance", intOrZero(preview.beforePostAllowance()))
                .addValue("beforeFloatingSalary", intOrZero(preview.beforeFloatingSalary()))
                .addValue("beforeBonusBalance", intOrZero(preview.beforeBonusBalance()))
                .addValue("beforeLivingAllowance", intOrZero(preview.beforeLivingAllowance()))
                .addValue("beforeSpecialPostAllowance", intOrZero(preview.beforeSpecialPostAllowance()))
                .addValue("beforePositionAllowance", intOrZero(preview.beforePositionAllowance()))
                .addValue("beforeOtherAllowance", intOrZero(preview.beforeOtherAllowance()))
                .addValue("beforeTotal", intOrZero(preview.beforeTotal()))
                .addValue("retainedAllowance", intOrZero(preview.retainedAllowance()))
                .addValue("localAllowance", intOrZero(preview.localAllowance()))
                .addValue("postAllowance", intOrZero(preview.postAllowance()))
                .addValue("floatingSalary", intOrZero(preview.floatingSalary()))
                .addValue("bonusBalance", intOrZero(preview.bonusBalance()))
                .addValue("livingAllowance", intOrZero(preview.livingAllowance()))
                .addValue("specialPostAllowance", intOrZero(preview.specialPostAllowance()))
                .addValue("positionAllowance", intOrZero(preview.positionAllowance()))
                .addValue("otherAllowance", intOrZero(preview.otherAllowance()))
                .addValue("convertedBase", intOrZero(preview.basicRetirementFee()))
                .addValue("estimatedTotal", intOrZero(preview.estimatedTotal()));
        return new RetirementSeedInsert(parameters);
    }

    private int intOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void requireRetirementWrite() {
        if (!accessControlService.hasPermission("RETIREMENT_WRITE")
                && !accessControlService.hasPermission("PERSONNEL_WRITE")) {
            throw new IllegalStateException("当前账号无离退休办理权限。");
        }
    }

    private <T> PageResponse<T> pageOf(List<T> rows, PageRequest pageRequest) {
        int fromIndex = pageRequest.offset();
        if (fromIndex >= rows.size()) {
            return PageResponse.of(List.of(), pageRequest, rows.size());
        }
        int toIndex = Math.min(fromIndex + pageRequest.size(), rows.size());
        return PageResponse.of(rows.subList(fromIndex, toIndex), pageRequest, rows.size());
    }

    private String resolveReferencePeriod(String referencePeriod) {
        String normalized = RetirementMonthCalculator.normalizeYearMonth(referencePeriod);
        if (!normalized.isBlank()) {
            return normalized;
        }
        return YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private String formatDisplayPeriod(String yearMonth) {
        String normalized = RetirementMonthCalculator.normalizeYearMonth(yearMonth);
        return RetirementMonthCalculator.formatYearMonth(normalized);
    }

    private String digitsPeriod(String yearMonth) {
        String normalized = RetirementMonthCalculator.normalizeYearMonth(yearMonth);
        return normalized.length() >= 6 ? normalized.substring(0, 6) : normalized;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private PayrollRoundingPolicy roundingPolicy() {
        return payrollRepository.roundingPolicy();
    }
}
