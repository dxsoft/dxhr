package com.dxsoft.rsgzgl.statistics;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final AccessControlService accessControlService;

    StatisticsService(StatisticsRepository statisticsRepository, AccessControlService accessControlService) {
        this.statisticsRepository = statisticsRepository;
        this.accessControlService = accessControlService;
    }

    public PersonnelSummaryStatistics personnelSummary(String organizationCode) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return statisticsRepository.personnelSummary(scope, emptyToNull(organizationCode));
    }

    public List<PayrollChangeSummaryStatistics> payrollChangeSummary(
            String organizationCode,
            String year,
            String month,
            List<String> changeTypes) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return statisticsRepository.payrollChangeSummary(
                scope, emptyToNull(organizationCode), year, month, changeTypes);
    }

    public List<String> payrollChangeTypes(String organizationCode, String year, String month) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return statisticsRepository.payrollChangeTypes(scope, emptyToNull(organizationCode), year, month);
    }

    public PageResponse<RetirementDuePersonnel> retirementDuePersonnel(
            String organizationCode,
            String keyword,
            String referencePeriod,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        String reference = resolveReferencePeriod(referencePeriod);
        String maleBirthUpper = RetirementMonthCalculator.maleBirthUpperBound(reference);
        String femaleBirthUpper = RetirementMonthCalculator.femaleBirthUpperBound(reference);
        List<RetirementDuePersonnel> rows = new ArrayList<>();
        for (RetirementDueCandidate candidate : statisticsRepository.findRetirementDueCandidates(
                scope, emptyToNull(organizationCode), keyword, maleBirthUpper, femaleBirthUpper)) {
            RetirementDuePersonnel row = toRetirementDuePersonnel(candidate, reference);
            if (row != null) {
                rows.add(row);
            }
        }
        rows.sort((left, right) -> {
            int byOrganization = nullToEmpty(left.organizationCode()).compareTo(nullToEmpty(right.organizationCode()));
            if (byOrganization != 0) {
                return byOrganization;
            }
            int byPerson = nullToEmpty(left.personCode()).compareTo(nullToEmpty(right.personCode()));
            if (byPerson != 0) {
                return byPerson;
            }
            return RetirementMonthCalculator.compareYearMonth(
                    left.calculatedRetirementMonth(),
                    right.calculatedRetirementMonth());
        });
        int fromIndex = pageRequest.offset();
        if (fromIndex >= rows.size()) {
            return PageResponse.of(List.of(), pageRequest, rows.size());
        }
        int toIndex = Math.min(fromIndex + pageRequest.size(), rows.size());
        return PageResponse.of(rows.subList(fromIndex, toIndex), pageRequest, rows.size());
    }

    private RetirementDuePersonnel toRetirementDuePersonnel(RetirementDueCandidate candidate, String referencePeriod) {
        RetirementMonthCalculator.CalculationResult calculation = RetirementMonthCalculator.calculate(
                candidate.birthYearMonth(),
                candidate.gender(),
                candidate.positionCode());
        if (calculation.retirementYearMonth().isBlank()) {
            return null;
        }
        if (RetirementMonthCalculator.compareYearMonth(calculation.retirementYearMonth(), referencePeriod) > 0) {
            return null;
        }
        return new RetirementDuePersonnel(
                candidate.uid(),
                candidate.organizationCode(),
                candidate.organizationName(),
                candidate.personCode(),
                candidate.name(),
                candidate.gender(),
                RetirementMonthCalculator.formatYearMonth(candidate.birthYearMonth()),
                candidate.positionCode(),
                candidate.positionName(),
                calculation.category().label(),
                calculation.delayMonths(),
                calculation.retirementYearMonth(),
                RetirementMonthCalculator.storedRetirementYearMonth(candidate.storedRetirementValue()),
                RetirementMonthCalculator.formatYearMonth(referencePeriod));
    }

    private String resolveReferencePeriod(String referencePeriod) {
        String normalized = RetirementMonthCalculator.normalizeYearMonth(referencePeriod);
        if (!normalized.isBlank()) {
            return normalized;
        }
        YearMonth current = YearMonth.now();
        return current.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
