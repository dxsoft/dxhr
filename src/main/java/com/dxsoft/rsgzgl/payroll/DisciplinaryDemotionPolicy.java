package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.personnel.AwardRecord;
import com.dxsoft.rsgzgl.personnel.PersonnelRepository;
import com.dxsoft.rsgzgl.personnel.PositionChangeReasons;
import com.dxsoft.rsgzgl.personnel.PositionRecord;
import org.springframework.stereotype.Component;

@Component
public class DisciplinaryDemotionPolicy {

    private final PayrollRepository payrollRepository;
    private final PersonnelRepository personnelRepository;

    public DisciplinaryDemotionPolicy(PayrollRepository payrollRepository, PersonnelRepository personnelRepository) {
        this.payrollRepository = payrollRepository;
        this.personnelRepository = personnelRepository;
    }

    public boolean isDisciplinaryDemotion(String organizationCode, String personCode, PositionChangeCandidate position) {
        if (position == null) {
            return false;
        }
        if (PositionChangeReasons.isDisciplinary(position.positionChangeReason())) {
            return true;
        }
        if (position.linkedAwardId() != null && position.linkedAwardId() > 0 && personnelRepository != null) {
            return personnelRepository.findAwardById(position.linkedAwardId())
                    .map(this::isDisciplinaryAward)
                    .orElse(false);
        }
        return payrollRepository.hasDemotionDisciplinaryRecord(
                organizationCode,
                personCode,
                normalizeYearMonth(position.startYearMonth()));
    }

    public boolean isDisciplinaryDemotion(String organizationCode, String personCode, PositionRecord position) {
        if (position == null) {
            return false;
        }
        if (PositionChangeReasons.isDisciplinary(position.positionChangeReason())) {
            return true;
        }
        if (position.linkedAwardId() != null && position.linkedAwardId() > 0 && personnelRepository != null) {
            return personnelRepository.findAwardById(position.linkedAwardId())
                    .map(this::isDisciplinaryAward)
                    .orElse(false);
        }
        return payrollRepository.hasDemotionDisciplinaryRecord(
                organizationCode,
                personCode,
                normalizeYearMonth(position.startYearMonth()));
    }

    public boolean isDisciplinaryAward(AwardRecord award) {
        if (award == null) {
            return false;
        }
        return matchesDisciplinaryText(award.hjmc())
                || matchesDisciplinaryText(award.jllx())
                || matchesDisciplinaryText(award.qtqk());
    }

    static boolean matchesDisciplinaryText(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return false;
        }
        return text.contains("降职")
                || text.contains("撤职")
                || text.contains("处分");
    }

    private static String normalizeYearMonth(String value) {
        return value == null ? "" : value.replace(".", "").replace("-", "");
    }
}
