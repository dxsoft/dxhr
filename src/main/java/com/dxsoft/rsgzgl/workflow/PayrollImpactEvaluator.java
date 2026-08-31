package com.dxsoft.rsgzgl.workflow;

import com.dxsoft.rsgzgl.payroll.LevelPromotionCandidateRow;
import com.dxsoft.rsgzgl.payroll.PayrollRepository;
import com.dxsoft.rsgzgl.payroll.PositionChangePromotionCandidateRow;
import com.dxsoft.rsgzgl.personnel.PersonnelMaintenanceRecord;
import com.dxsoft.rsgzgl.personnel.PersonnelRepository;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PayrollImpactEvaluator {

    private final PayrollRepository payrollRepository;
    private final PersonnelRepository personnelRepository;

    public PayrollImpactEvaluator(PayrollRepository payrollRepository, PersonnelRepository personnelRepository) {
        this.payrollRepository = payrollRepository;
        this.personnelRepository = personnelRepository;
    }

    public Optional<PayrollImpactResult> evaluate(
            int uid,
            PayrollWorkflowSourceType sourceType,
            Integer sourceId) {
        Optional<PersonnelMaintenanceRecord> person = personnelRepository.findMaintenanceByUid(uid);
        if (person.isEmpty()) {
            return Optional.empty();
        }
        PersonnelMaintenanceRecord record = person.get();
        OrganizationScope scope = OrganizationScope.unrestricted();
        String orgCode = record.organizationCode();
        return switch (sourceType) {
            case MAIN -> evaluateMain(uid, record);
            case EDUCATION -> evaluateUidInList(
                    uid,
                    payrollRepository.findEducationPromotionCandidateUids(scope, orgCode, null),
                    PayrollWorkflowModule.EDUCATION_PROMOTION,
                    sourceType,
                    sourceId,
                    record.name() + " 学历信息审批通过，待办理学历工资变动");
            case POSITION -> evaluatePosition(uid, scope, orgCode, sourceType, sourceId, record.name());
            case ASSESSMENT -> evaluateAssessment(uid, scope, orgCode, sourceType, sourceId, record.name());
            case RANK -> evaluateRank(sourceId, record);
            case AWARD -> Optional.empty();
        };
    }

    private Optional<PayrollImpactResult> evaluateMain(int uid, PersonnelMaintenanceRecord record) {
        if (payrollRepository.findNewPersonnelSalaryCandidate(uid).isEmpty()) {
            return Optional.empty();
        }
        PayrollWorkflowModule module = PayrollWorkflowModule.NEW_PERSONNEL_SALARY;
        return Optional.of(new PayrollImpactResult(
                module,
                module.changeTypes().iterator().next(),
                PayrollWorkflowSourceType.MAIN,
                null,
                record.name() + " 人员信息审批通过，待办理新增人员定资"));
    }

    private Optional<PayrollImpactResult> evaluatePosition(
            int uid,
            OrganizationScope scope,
            String orgCode,
            PayrollWorkflowSourceType sourceType,
            Integer sourceId,
            String personName) {
        List<PositionChangePromotionCandidateRow> rows =
                payrollRepository.findPositionChangePromotionCandidateRows(scope, orgCode, null);
        boolean pending = rows.stream().anyMatch(row -> row.uid() == uid && row.priority() != 2);
        if (!pending) {
            return Optional.empty();
        }
        PayrollWorkflowModule module = PayrollWorkflowModule.POSITION_CHANGE_PROMOTION;
        return Optional.of(new PayrollImpactResult(
                module,
                "职务变化",
                sourceType,
                sourceId,
                personName + " 任职信息审批通过，待办理职务变动工资"));
    }

    private Optional<PayrollImpactResult> evaluateAssessment(
            int uid,
            OrganizationScope scope,
            String orgCode,
            PayrollWorkflowSourceType sourceType,
            Integer sourceId,
            String personName) {
        List<Integer> normalUids = payrollRepository.findNormalPromotionCandidateUids(
                scope, orgCode, null, java.time.Year.now().getValue());
        if (normalUids.contains(uid)) {
            PayrollWorkflowModule module = PayrollWorkflowModule.NORMAL_PROMOTION;
            return Optional.of(new PayrollImpactResult(
                    module,
                    "正常档次",
                    sourceType,
                    sourceId,
                    personName + " 考核结果审批通过，待办理档次/薪级晋升"));
        }
        int promotionYear = java.time.Year.now().getValue();
        List<LevelPromotionCandidateRow> levelRows =
                payrollRepository.findLevelPromotionCandidateRows(scope, orgCode, null, promotionYear);
        boolean levelPending = levelRows.stream().anyMatch(row -> row.uid() == uid && row.priority() != 2);
        if (levelPending) {
            PayrollWorkflowModule module = PayrollWorkflowModule.LEVEL_PROMOTION;
            return Optional.of(new PayrollImpactResult(
                    module,
                    "正常级别",
                    sourceType,
                    sourceId,
                    personName + " 考核结果审批通过，待办理级别晋升"));
        }
        return Optional.empty();
    }

    private Optional<PayrollImpactResult> evaluateRank(Integer sourceId, PersonnelMaintenanceRecord record) {
        if (sourceId == null) {
            return Optional.empty();
        }
        return personnelRepository.findRankById(sourceId)
                .flatMap(rank -> mapRankImpact(rank, record.name()));
    }

    private Optional<PayrollImpactResult> mapRankImpact(
            com.dxsoft.rsgzgl.personnel.RankRecord rank,
            String personName) {
        String category = rank.lb() == null ? "" : rank.lb().trim().toLowerCase();
        String jxText = rank.jx() == null ? "" : rank.jx();
        PayrollWorkflowModule module;
        String jslb;
        if ("jc".equals(category) || jxText.contains("检察")) {
            module = PayrollWorkflowModule.PROSECUTION_RANK_CHANGE_PROMOTION;
            jslb = "检察等级";
        } else if ("sp".equals(category) || jxText.contains("法官") || jxText.contains("审判")) {
            module = PayrollWorkflowModule.JUDICIAL_RANK_CHANGE_PROMOTION;
            jslb = "法官等级";
        } else if ("mt".equals(category) || jxText.contains("监察")) {
            module = PayrollWorkflowModule.SUPERVISION_RANK_CHANGE_PROMOTION;
            jslb = "监察等级";
        } else {
            module = PayrollWorkflowModule.POLICE_RANK_CHANGE_PROMOTION;
            jslb = "警衔变化";
        }
        return Optional.of(new PayrollImpactResult(
                module,
                jslb,
                PayrollWorkflowSourceType.RANK,
                rank.id(),
                personName + " 警衔/等级信息审批通过，待办理" + module.label()));
    }

    private Optional<PayrollImpactResult> evaluateUidInList(
            int uid,
            List<Integer> candidateUids,
            PayrollWorkflowModule module,
            PayrollWorkflowSourceType sourceType,
            Integer sourceId,
            String summary) {
        if (!candidateUids.contains(uid)) {
            return Optional.empty();
        }
        return Optional.of(new PayrollImpactResult(
                module,
                module.changeTypes().iterator().next(),
                sourceType,
                sourceId,
                summary));
    }

    public record PayrollImpactResult(
            PayrollWorkflowModule module,
            String expectedJslb,
            PayrollWorkflowSourceType sourceType,
            Integer sourceId,
            String summary) {
    }
}
