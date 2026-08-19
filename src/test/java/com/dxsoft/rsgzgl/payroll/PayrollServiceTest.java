package com.dxsoft.rsgzgl.payroll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class PayrollServiceTest {

    @Test
    void wageProjectionReplaysPromotionsBeforeLaterPositionChanges() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot positionChange = history("2017", "03", "职务变化", "0180", "副县处级领导职务", "18", "6", "2014", "2016");

        when(repository.findLatestHistory(10268)).thenReturn(Optional.of(positionChange));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(positionChange));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2001.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0190", "正科级领导职务", "2005.04", 0),
                        new WageReformPosition("01A0", "副科级领导职务", "1998.03", 4)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            int reformYears = invocation.getArgument(2);
            if ("0190".equals(positionCode) && reformYears == 20) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
            }
            if ("01A0".equals(positionCode) && appointmentYears == 5 && reformYears == 20) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "18", "7"));
        });
        when(repository.findPositionAtOrBefore("001", "00040", "202501"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2017.02")));
        when(repository.findPositionAtPeriod("001", "00040", "201702"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2017.02")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2017.02")));
        when(repository.findPositionLevelRange("0180"))
                .thenReturn(Optional.of(new PositionLevelRange("0180", 18, 12)));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 20 && step == 1) {
                return 950;
            }
            if (level == 20 && step == 2) {
                return 1030;
            }
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });

        WageProjectionPreview preview = service.wageProjection(10268, "202501");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("套改年限 20 年") && line.contains("21-4"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2008 年") && line.contains("晋升档次"));
        int stepPromotionIndex = indexOfLineContaining(preview.explanationLines(), "2008 年");
        int positionChangeIndex = indexOfLineContaining(preview.explanationLines(), "201703 职务变化");
        assertThat(stepPromotionIndex).isGreaterThanOrEqualTo(0);
        assertThat(positionChangeIndex).isGreaterThanOrEqualTo(0);
        assertThat(stepPromotionIndex).isLessThan(positionChangeIndex);
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201703 职务变化")
                && line.contains("按职务晋升政策")
                && line.contains("采用任职记录 2017.02 职务"));
    }

    @Test
    void wageProjectionAppliesReformLevelRollingIn2008After2007PositionPromotionUid134Style() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = history(
                "2006", "07", "套改", "01A0", "副科级领导职务", "23", "3", "2006", "2006", "1998.03");
        PayrollHistorySnapshot levelRolling = history(
                "2008", "01", "级别滚动", "0191", "正科级非领导职务", "21", "1", "2008", "2006", "2007.07");
        PayrollHistorySnapshot latest = history(
                "2014", "01", "正常级别", "0191", "正科级非领导职务", "20", "2", "2008", "2006", "2007.07");

        when(repository.findLatestHistory(134)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest, levelRolling, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A0", "副科级领导职务", "1998.03")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A0", "副科级领导职务", "1998.03", 4)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(16);
        when(repository.findPositionAtOrBefore(eq("001"), eq("00040"), anyString())).thenAnswer(invocation -> {
            String period = invocation.getArgument(2);
            if ("200607".compareTo(period) >= 0 || "200612".compareTo(period) >= 0) {
                return Optional.of(new PositionChangeCandidate("01A0", "副科级领导职务", "1998.03"));
            }
            return Optional.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2007.07"));
        });
        when(repository.findPositionChangesBetween(eq("001"), eq("00040"), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenAnswer(invocation -> {
                    String end = invocation.getArgument(3);
                    if (end.compareTo("200608") >= 0 && end.compareTo("201001") < 0) {
                        return List.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2007.07"));
                    }
                    return List.of();
                });
        when(repository.findPositionLevelRange("0191")).thenReturn(Optional.of(new PositionLevelRange("0191", 22, 16)));
        when(repository.findPositionLevelRange("01A0")).thenReturn(Optional.of(new PositionLevelRange("01A0", 24, 17)));
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            int reformYears = invocation.getArgument(2);
            if ("01A0".equals(positionCode) && reformYears == 16) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "23", "3"));
            }
            if ("01A0".equals(positionCode) && reformYears == 17) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "23", "3"));
            }
            if ("01A0".equals(positionCode) && reformYears == 18) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.empty();
        });
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(repository.findFirstWageReformStandardForPosition(anyString())).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int startYear = invocation.getArgument(2);
            int endYear = invocation.getArgument(3);
            if (startYear == endYear) {
                return 1;
            }
            return Math.max(0, endYear - startYear + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of(2006, 2007, 2008, 2009));

        WageProjectionPreview preview = service.wageProjection(134, "200901");

        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2007 年") && line.contains("套改后级别滚动"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("200708") && line.contains("职务变化"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2008 年")
                && line.contains("套改后级别滚动")
                && line.contains("晋升职务只晋升一个级别")
                && line.contains("级别考核起算年（xckhndjb）更新为 2008"));
        assertThat(preview.levelStepDisplay()).isEqualTo("21-1");
    }

    @Test
    void wageProjectionAppliesReformLevelRollingWhenPromotedOneLayerSinceReform() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2010", "01", "正常级别", "0191", "正科级非领导职务", "21", "4", "2006", "2006", "2007.07");

        when(repository.findLatestHistory(134)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A0", "副科级领导职务", "1998.03")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A0", "副科级领导职务", "1998.03", 4)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(16);
        when(repository.findPositionAtOrBefore(eq("001"), eq("00040"), anyString())).thenAnswer(invocation -> {
            String period = invocation.getArgument(2);
            if ("200607".compareTo(period) >= 0 || "200612".compareTo(period) >= 0) {
                return Optional.of(new PositionChangeCandidate("01A0", "副科级领导职务", "1998.03"));
            }
            return Optional.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2007.07"));
        });
        when(repository.findPositionChangesBetween(eq("001"), eq("00040"), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenAnswer(invocation -> {
                    String end = invocation.getArgument(3);
                    if (end.compareTo("200608") >= 0) {
                        return List.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2007.07"));
                    }
                    return List.of();
                });
        when(repository.findPositionLevelRange("0191")).thenReturn(Optional.of(new PositionLevelRange("0191", 22, 16)));
        when(repository.findPositionLevelRange("01A0")).thenReturn(Optional.of(new PositionLevelRange("01A0", 24, 17)));
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int reformYears = invocation.getArgument(2);
            if ("01A0".equals(positionCode) && (reformYears == 16 || reformYears == 17)) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "23", "3"));
            }
            if ("01A0".equals(positionCode) && reformYears == 18) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.empty();
        });
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(repository.findFirstWageReformStandardForPosition(anyString())).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int startYear = invocation.getArgument(2);
            int endYear = invocation.getArgument(3);
            if (startYear == endYear) {
                return 1;
            }
            return Math.max(0, endYear - startYear + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of(2006, 2007, 2008, 2009));

        WageProjectionPreview preview = service.wageProjection(134, "201001");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2008 年")
                && line.contains("套改后级别滚动")
                && line.contains("晋升职务只晋升一个级别")
                && line.contains("级别考核起算年（xckhndjb）更新为 2008"));
    }

    @Test
    void levelPromotionPreviewIncludesReformLevelRollingWhenDueOnly() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot current = history(
                "2008", "01", "正常级别", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2008)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(134, 2)));
        stubLevelPromotionListContext(repository, Map.of(134, current), Map.of());
        when(repository.findLatestHistory(134)).thenReturn(Optional.of(current));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(current));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2001.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0190", "正科级领导职务", "2005.04", 0),
                        new WageReformPosition("01A0", "副科级领导职务", "1998.03", 4)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            int reformYears = invocation.getArgument(2);
            if ("0190".equals(positionCode) && reformYears == 20) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
            }
            if ("01A0".equals(positionCode) && appointmentYears == 5 && reformYears == 20) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "18", "7"));
        });
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            int reformYears = invocation.getArgument(2);
            if ("0190".equals(positionCode) && reformYears == 20) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
            }
            if ("01A0".equals(positionCode) && appointmentYears == 5 && reformYears == 20) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "18", "7"));
        });
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 20 && step == 1) {
                return 950;
            }
            if (level == 20 && step == 2) {
                return 1030;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            if (start == end && start == 2007) {
                return 1;
            }
            if (start == end) {
                return 0;
            }
            return Math.max(0, end - start + 1);
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        var dueOnlyPage = service.levelPromotionPreviews(null, null, "2008", null, null, new PageRequest(0, 20));
        assertThat(dueOnlyPage.content()).extracting(LevelPromotionPreview::uid).contains(134);
        LevelPromotionPreview preview = dueOnlyPage.content().stream()
                .filter(item -> item.uid() == 134)
                .findFirst()
                .orElseThrow();
        assertThat(preview.reformLevelRollingDue()).isTrue();
        assertThat(preview.levelPromotionDue()).isTrue();
        assertThat(preview.rollbackEligible()).isTrue();
        assertThat(preview.applyEligible()).isFalse();
        assertThat(preview.qualifiedYearsForLevel()).isLessThan(5);
        assertThat(preview.nextLevelAssessmentStartYear()).isEqualTo("2008");
        assertThat(preview.note()).contains("套改后级别滚动");
    }

    @Test
    void levelPromotionPreviewExcludesReformRollingOutside2007To2010() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot current = history(
                "2026", "01", "职务变化", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2026)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(134, 1)));
        stubLevelPromotionListContext(repository, Map.of(134, current), Map.of());
        when(repository.findLatestHistory(134)).thenReturn(Optional.of(current));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(current));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2001.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0190", "正科级领导职务", "2005.04", 0),
                        new WageReformPosition("01A0", "副科级领导职务", "1998.03", 4)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            int reformYears = invocation.getArgument(2);
            if ("0190".equals(positionCode) && reformYears == 20) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
            }
            if ("01A0".equals(positionCode) && appointmentYears == 5 && reformYears == 20) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "18", "7"));
        });
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            int reformYears = invocation.getArgument(2);
            if ("0190".equals(positionCode) && reformYears == 20) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
            }
            if ("01A0".equals(positionCode) && appointmentYears == 5 && reformYears == 20) {
                return Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "22", "5"));
            }
            return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "18", "7"));
        });
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 20 && step == 1) {
                return 950;
            }
            if (level == 20 && step == 2) {
                return 1030;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            if (start == end && start == 2007) {
                return 1;
            }
            if (start == end) {
                return 0;
            }
            return Math.max(0, end - start + 1);
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        var page = service.levelPromotionPreviews(null, null, "2026", true, false, new PageRequest(0, 20));

        assertThat(page.content()).isEmpty();
        LevelPromotionPreview preview = service.levelPromotionPreviews(null, null, "2026", null, null, new PageRequest(0, 20))
                .content()
                .stream()
                .filter(item -> item.uid() == 134)
                .findFirst()
                .orElseGet(() -> {
                    when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2026)))
                            .thenReturn(List.of(new LevelPromotionCandidateRow(134, 1)));
                    return service.levelPromotionPreviews(null, null, "2026", null, null, new PageRequest(0, 20))
                            .content()
                            .stream()
                            .filter(item -> item.uid() == 134)
                            .findFirst()
                            .orElseThrow();
                });
    }

    @Test
    void levelPromotionProcessedNoteDoesNotIncludeIneligibilityMessage() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot processed = history(
                "2008", "01", "正常级别", "0190", "正科级领导职务", "21", "4", "2008", "2008", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2008)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(201, 2)));
        stubLevelPromotionListContext(repository, Map.of(201, processed), Map.of());
        when(repository.findLatestHistory(201)).thenReturn(Optional.of(processed));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(processed));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        var page = service.levelPromotionPreviews(null, null, "2008", null, null, new PageRequest(0, 20));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().rollbackEligible()).isTrue();
        assertThat(page.content().getFirst().note())
                .isEqualTo("已处理")
                .doesNotContain("可执行还原")
                .doesNotContain("当前最近工资变动")
                .doesNotContain("差额未超过5年")
                .doesNotContain("暂不符合级别晋升条件");
    }

    @Test
    void levelPromotionPreviewsOnlyIncludeExactFiveQualifiedYearsWithSpanOverFive() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot eligible = history(
                "2024", "01", "职务变化", "0190", "正科级领导职务", "21", "4", "2018", "2020");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2024)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(1, 1)));
        stubLevelPromotionListContext(
                repository,
                Map.of(1, eligible),
                Map.of(1, List.of(
                        new PersonnelAssessmentYear(2018, "称职"),
                        new PersonnelAssessmentYear(2019, "称职"),
                        new PersonnelAssessmentYear(2020, "称职"),
                        new PersonnelAssessmentYear(2021, "称职"),
                        new PersonnelAssessmentYear(2022, "称职"),
                        new PersonnelAssessmentYear(2023, "基本称职"))));
        when(repository.findLatestHistory(1)).thenReturn(Optional.of(eligible));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(eligible));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.countQualifiedAssessmentYears("001", "00040", 2018, 2023)).thenReturn(5);
        when(repository.countQualifiedAssessmentYears("001", "00040", 2020, 2023)).thenReturn(2);
        when(repository.assessmentYears("001", "00040", 2018, 2023))
                .thenReturn(Set.of(2018, 2019, 2020, 2021, 2022, 2023));

        var page = service.levelPromotionPreviews(null, null, "2024", null, null, new PageRequest(0, 20));

        assertThat(page.content()).extracting(LevelPromotionPreview::uid).containsExactly(1);
        LevelPromotionPreview preview = page.content().getFirst();
        assertThat(preview.applyEligible()).isTrue();
        assertThat(preview.levelPromotionDue()).isTrue();
        assertThat(preview.qualifiedYearsForLevel()).isEqualTo(5);
        assertThat(preview.note()).contains("差额超过5年").contains("累计5年");
    }

    @Test
    void levelPromotionPreviewsExcludeOverQualifiedYearsFromList() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot overQualified = history(
                "2024", "01", "职务变化", "0190", "正科级领导职务", "21", "4", "2017", "2020");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2024)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(2, 1)));
        stubLevelPromotionListContext(
                repository,
                Map.of(2, overQualified),
                Map.of(2, assessmentYears(2017, 2018, 2019, 2020, 2021, 2022, 2023)));
        when(repository.findLatestHistory(2)).thenReturn(Optional.of(overQualified));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(overQualified));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.countQualifiedAssessmentYears("001", "00040", 2017, 2023)).thenReturn(6);
        when(repository.countQualifiedAssessmentYears("001", "00040", 2020, 2023)).thenReturn(2);
        when(repository.assessmentYears("001", "00040", 2017, 2023))
                .thenReturn(Set.of(2017, 2018, 2019, 2020, 2021, 2022, 2023));

        var page = service.levelPromotionPreviews(null, null, "2024", null, null, new PageRequest(0, 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void levelPromotionPreviewsFilterByApplyAndProcessedStatus() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot applyCandidate = history(
                "2024", "01", "职务变化", "0190", "正科级领导职务", "21", "4", "2018", "2020");
        PayrollHistorySnapshot processed = history(
                "2024", "01", "正常级别", "0190", "正科级领导职务", "21", "4", "2024", "2024", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2024)))
                .thenReturn(List.of(
                        new LevelPromotionCandidateRow(1, 1),
                        new LevelPromotionCandidateRow(2, 2)));
        stubLevelPromotionListContext(
                repository,
                Map.of(1, applyCandidate, 2, processed),
                Map.of(1, List.of(
                        new PersonnelAssessmentYear(2018, "称职"),
                        new PersonnelAssessmentYear(2019, "称职"),
                        new PersonnelAssessmentYear(2020, "称职"),
                        new PersonnelAssessmentYear(2021, "称职"),
                        new PersonnelAssessmentYear(2022, "称职"),
                        new PersonnelAssessmentYear(2023, "基本称职"))));
        when(repository.findLatestHistory(1)).thenReturn(Optional.of(applyCandidate));
        when(repository.findLatestHistory(2)).thenReturn(Optional.of(processed));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(applyCandidate));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.countQualifiedAssessmentYears("001", "00040", 2018, 2023)).thenReturn(5);
        when(repository.countQualifiedAssessmentYears("001", "00040", 2020, 2023)).thenReturn(2);
        when(repository.assessmentYears("001", "00040", 2018, 2023))
                .thenReturn(Set.of(2018, 2019, 2020, 2021, 2022, 2023));

        var applyOnly = service.levelPromotionPreviews(null, null, "2024", true, false, new PageRequest(0, 20));
        var processedOnly = service.levelPromotionPreviews(null, null, "2024", false, true, new PageRequest(0, 20));

        assertThat(applyOnly.content()).extracting(LevelPromotionPreview::uid).containsExactly(1);
        assertThat(processedOnly.content()).extracting(LevelPromotionPreview::uid).containsExactly(2);
    }

    @Test
    void levelPromotionPreviewsExcludeNormalStepPromotionProcessedRecords() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot stepProcessed = history(
                "2024", "01", "正常档次", "0190", "正科级领导职务", "21", "4", "2024", "2024", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2024)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(301, 2)));
        stubLevelPromotionListContext(repository, Map.of(301, stepProcessed), Map.of());
        when(repository.findLatestHistory(301)).thenReturn(Optional.of(stepProcessed));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(stepProcessed));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        var page = service.levelPromotionPreviews(null, null, "2024", null, null, new PageRequest(0, 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void levelPromotionPreviewsExcludeTransferInSalaryDeterminationRecords() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot transferIn = history(
                "2024", "01", "调入定资", "0190", "正科级领导职务", "21", "4", "2018", "2020", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2024)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(401, 1)));
        stubLevelPromotionListContext(
                repository,
                Map.of(401, transferIn),
                Map.of(401, List.of(
                        new PersonnelAssessmentYear(2018, "称职"),
                        new PersonnelAssessmentYear(2019, "称职"),
                        new PersonnelAssessmentYear(2020, "称职"),
                        new PersonnelAssessmentYear(2021, "称职"),
                        new PersonnelAssessmentYear(2022, "称职"),
                        new PersonnelAssessmentYear(2023, "称职"))));
        when(repository.findLatestHistory(401)).thenReturn(Optional.of(transferIn));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(transferIn));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        var page = service.levelPromotionPreviews(null, null, "2024", null, null, new PageRequest(0, 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void levelPromotionPreviewsExcludeReformRollingProcessedRecordsFromProcessedFilter() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot rollingProcessed = history(
                "2008", "01", "级别滚动", "0190", "正科级领导职务", "21", "4", "2008", "2008", "2001.01");

        when(repository.findLevelPromotionCandidateRows(any(), any(), any(), eq(2008)))
                .thenReturn(List.of(new LevelPromotionCandidateRow(402, 2)));
        stubLevelPromotionListContext(repository, Map.of(402, rollingProcessed), Map.of());
        when(repository.findLatestHistory(402)).thenReturn(Optional.of(rollingProcessed));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(rollingProcessed));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        var page = service.levelPromotionPreviews(null, null, "2008", false, true, new PageRequest(0, 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void positionChangePromotionPreviewsExcludeSamePositionAndKeepPendingChanges() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot unchanged = history(
                "2024", "01", "正常档次", "0190", "正科级领导职务", "21", "4", "2006", "2006");
        PayrollHistorySnapshot pending = history(
                "2024", "01", "正常档次", "01A0", "副科级领导职务", "22", "5", "2006", "2006");

        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(100, 101));
        when(repository.findPositionChangePromotionPersonnelUids(any(), any(), any(), any())).thenReturn(List.of(101));
        when(repository.countPositionChangePromotionPersonnel(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(100)).thenReturn(Optional.of(unchanged));
        when(repository.findLatestHistory(101)).thenReturn(Optional.of(pending));
        when(repository.findCurrentPositionChangeCandidate("001", "00040"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2024.01")));
        when(repository.findPositionLevelRange("0190"))
                .thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("01A0".equals(positionCode)) {
                return 1200;
            }
            if ("0190".equals(positionCode)) {
                return 1500;
            }
            return 0;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });

        var page = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20));

        assertThat(page.content()).hasSize(1);
        PositionChangePromotionListItem preview = page.content().getFirst();
        assertThat(preview.currentPositionCode()).isEqualTo("01A0");
        assertThat(preview.newPositionCode()).isEqualTo("0190");
        assertThat(preview.applyEligible()).isTrue();
        assertThat(preview.rollbackEligible()).isFalse();
    }

    @Test
    void positionChangePromotionListsPendingFromCandidateRows() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot pending = history(
                "2024", "01", "正常档次", "0504", "机关中级工", "", "2", "2018", "2018", "2018.06", "05");

        when(repository.findPositionChangePromotionCandidateRows(any(), any(), any()))
                .thenReturn(List.of(new PositionChangePromotionCandidateRow(
                        101, 1, "0504", "机关中级工", "0503", "机关高级工",
                        null, null, "正常档次", "2022.09")));
        when(repository.findLatestHistory(101)).thenReturn(Optional.of(pending));
        when(repository.findPositionLevelRange("0503"))
                .thenReturn(Optional.of(new PositionLevelRange("0503", 0, 0)));
        when(repository.findPositionLevelRange("0504"))
                .thenReturn(Optional.of(new PositionLevelRange("0504", 0, 0)));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("0503".equals(positionCode)) {
                return 910;
            }
            if ("0504".equals(positionCode)) {
                return 903;
            }
            return 0;
        });
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);

        var page = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().personCode()).isEqualTo("00040");
        assertThat(page.content().getFirst().currentPositionCode()).isEqualTo("0504");
        assertThat(page.content().getFirst().newPositionCode()).isEqualTo("0503");
    }

    @Test
    void governmentWorkerPositionChangePreviewIncludesTechnicalGradeIncreaseInTotal() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        when(accessControlService.hasPermission("PAYROLL_WRITE")).thenReturn(true);
        PayrollService service = new PayrollService(repository, accessControlService, true);

        PayrollHistorySnapshot latest = workerHistoryWithAmounts(
                "history-id", "010", "00172", "0504", "机关中级工", "5", 1899, 975, 4132);

        when(repository.findPersonnelUidByCurrentHistoryId("history-id")).thenReturn(Optional.of(101));
        when(repository.findLatestHistory(101)).thenReturn(Optional.of(latest));
        when(repository.findPositionChangePromotionCandidateRows(any(), any(), any()))
                .thenReturn(List.of(new PositionChangePromotionCandidateRow(
                        101, 1, "0504", "机关中级工", "0503", "机关高级工",
                        null, null, "正常档次", "2019.09")));
        when(repository.findCurrentPositionChangeCandidatesByUids(List.of(101)))
                .thenReturn(Map.of(101, new PositionChangeCandidate("0503", "机关高级工", "2019.09")));
        when(repository.findOrganizationPayrollPolicy("010"))
                .thenReturn(Optional.of(new OrganizationPayrollPolicy("", "", "")));
        when(repository.findPositionLevelRange(anyString()))
                .thenReturn(Optional.of(new PositionLevelRange("0504", 0, 0)));
        when(repository.findPositionLevelRanges(any())).thenReturn(Map.of());
        when(repository.findPositionSalaries(anyString(), any())).thenReturn(Map.of());
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary("0504", "201607")).thenReturn(900);
        when(repository.positionSalary("0503", "201607")).thenReturn(927);
        when(repository.positionGradeSalary(eq("0504"), eq("5"), eq("0"), eq("201607"))).thenReturn(999);
        when(repository.positionGradeSalary(eq("0503"), anyString(), eq("0"), eq("201607"))).thenAnswer(invocation -> {
            int grade = Integer.parseInt(invocation.getArgument(1));
            return grade == 8 ? 1000 : grade * 50;
        });
        when(repository.technicalGradeSalary("0504", "201607")).thenReturn(975);
        when(repository.technicalGradeSalary("0503", "201607")).thenReturn(1200);
        when(repository.hasAllowanceStandardForPosition(eq("201607"), eq("010"), anyString())).thenReturn(true);
        when(repository.performanceAllowance("010", "0504", "201607")).thenReturn(BigDecimal.valueOf(486));
        when(repository.performanceAllowance("010", "0503", "201607")).thenReturn(BigDecimal.valueOf(520));
        when(repository.subsidyAllowance("010", "0504", "201607")).thenReturn(729);
        when(repository.subsidyAllowance("010", "0503", "201607")).thenReturn(780);
        when(repository.retainedAllowance(anyString())).thenReturn(43);

        PositionChangePromotionPreview preview = service.positionChangePromotionDetail("history-id");

        assertThat(preview.gradeSalaryIncrease()).isZero();
        assertThat(preview.totalIncrease() - preview.netPositionSalaryIncrease() - preview.gradeSalaryIncrease())
                .isEqualTo(225 + 34 + 51);
    }

    @Test
    void positionChangePromotionExcludesWhenAnchorHasNoNextAppointment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);

        when(repository.findPositionChangePromotionCandidateRows(any(), any(), any())).thenReturn(List.of());

        var page = service.positionChangePromotionPreviews("010", "00052", new PageRequest(0, 20));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void rollbackEducationPromotionWritesOperationAuditLog() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        OperationLogService operationLogService = mock(OperationLogService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true, operationLogService);
        PayrollHistorySnapshot current = history("2024", "06", "学历变化", "0180", "副县处级领导职务", "18", "6", "2014", "2016");

        when(accessControlService.hasPermission("PAYROLL_WRITE")).thenReturn(true);
        when(repository.findCurrentHistoryById("history-id")).thenReturn(Optional.of(current));
        when(repository.findPredecessorHistoryId("001", "00040", "history-id")).thenReturn(Optional.of("prev-id"));

        service.rollbackEducationPromotion("history-id");

        verify(operationLogService).record(
                eq("ROLLBACK_PAYROLL_CHANGE"),
                eq("hisbase"),
                eq("history-id"),
                org.mockito.ArgumentMatchers.contains("001-00040"));
    }

    @Test
    void positionChangePromotionPreviewsIncludeRollbackCandidates() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot processed = history(
                "2024", "06", "职级套改", "2307", "一级主任科员", "20", "6", "2006", "2006");

        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.findPositionChangePromotionPersonnelUids(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.countPositionChangePromotionPersonnel(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(100)).thenReturn(Optional.of(processed));
        when(repository.findCurrentPositionChangeCandidate("001", "00040"))
                .thenReturn(Optional.empty());
        when(repository.findProcessedPositionChangeDisplay("001", "00040"))
                .thenReturn(Optional.of(new PositionChangeDisplayPair("01A0", "副科级领导职务", "2307", "一级主任科员")));
        when(repository.findPredecessorHistoryId("history-id")).thenReturn(Optional.of("predecessor-id"));
        when(repository.findPayrollHistoryById("predecessor-id")).thenReturn(Optional.of(
                history("2024", "05", "正常档次", "01A0", "副科级领导职务", "22", "5", "2006", "2006")));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenReturn(1500);
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenReturn(3000);
        when(repository.findPositionChangePredecessor("history-id")).thenReturn(Optional.of(
                history("2024", "05", "正常档次", "01A0", "副科级领导职务", "22", "5", "2006", "2006")));

        var page = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20));

        assertThat(page.content()).hasSize(1);
        PositionChangePromotionListItem preview = page.content().getFirst();
        assertThat(preview.changeType()).isEqualTo("职级套改");
        assertThat(preview.rollbackEligible()).isTrue();
        assertThat(preview.applyEligible()).isFalse();
        assertThat(preview.currentPositionCode()).isEqualTo("01A0");
        assertThat(preview.currentPositionName()).isEqualTo("副科级领导职务");
        assertThat(preview.newPositionCode()).isEqualTo("2307");
        assertThat(preview.newPositionName()).isEqualTo("一级主任科员");
    }

    @Test
    void positionChangePromotionPrefersPendingChangeOverRollbackCandidate() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot pendingWithPositionChangeHistory = history(
                "2024", "06", "职级套改", "01A0", "副科级领导职务", "22", "5", "2006", "2006");

        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.findPositionChangePromotionPersonnelUids(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.countPositionChangePromotionPersonnel(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(100)).thenReturn(Optional.of(pendingWithPositionChangeHistory));
        when(repository.findCurrentPositionChangeCandidate("001", "00040"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2024.05")));
        when(repository.findPositionLevelRange("0190"))
                .thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("01A0".equals(positionCode)) {
                return 1200;
            }
            if ("0190".equals(positionCode)) {
                return 1500;
            }
            return 0;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });

        PositionChangePromotionListItem preview = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20))
                .content()
                .getFirst();

        assertThat(preview.applyEligible()).isTrue();
        assertThat(preview.rollbackEligible()).isFalse();
    }

    @Test
    void positionChangePromotionRollbackUsesPredecessorHistoryIdFirst() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot processed = history(
                "2024", "06", "职级套改", "2307", "一级主任科员", "20", "6", "2006", "2006");
        PayrollHistorySnapshot predecessor = history(
                "2024", "05", "正常档次", "0190", "正科级领导职务", "22", "5", "2006", "2006");

        when(repository.findPositionChangePromotionPersonnelUids(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.countPositionChangePromotionPersonnel(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(100)).thenReturn(Optional.of(processed));
        when(repository.findCurrentPositionChangeCandidate("001", "00040")).thenReturn(Optional.empty());
        when(repository.findProcessedPositionChangeDisplay("001", "00040")).thenReturn(Optional.empty());
        when(repository.findProcessedPositionChangeDisplayById("history-id")).thenReturn(Optional.empty());
        when(repository.findPredecessorHistoryId("history-id")).thenReturn(Optional.of("b00009e6-7523-11f1-8749-d0c1b51fd762"));
        when(repository.findPayrollHistoryById("b00009e6-7523-11f1-8749-d0c1b51fd762")).thenReturn(Optional.of(predecessor));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenReturn(1500);
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenReturn(3000);

        PositionChangePromotionListItem preview = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20))
                .content()
                .getFirst();

        assertThat(preview.rollbackEligible()).isTrue();
        assertThat(preview.applyEligible()).isFalse();
        assertThat(preview.currentPositionName()).isEqualTo("正科级领导职务");
        assertThat(preview.newPositionName()).isEqualTo("一级主任科员");
    }

    @Test
    void positionChangePromotionRollbackUsesAppointmentHistoryWhenPayrollPredecessorMissing() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot processed = history(
                "2024", "06", "职级套改", "2307", "一级主任科员", "20", "6", "2006", "2006");

        when(repository.findPositionChangePromotionPersonnelUids(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.countPositionChangePromotionPersonnel(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(100)).thenReturn(Optional.of(processed));
        when(repository.findCurrentPositionChangeCandidate("001", "00040")).thenReturn(Optional.empty());
        when(repository.findProcessedPositionChangeDisplay("001", "00040")).thenReturn(Optional.empty());
        when(repository.findProcessedPositionChangeDisplayById("history-id")).thenReturn(Optional.empty());
        when(repository.findPredecessorHistoryId("history-id")).thenReturn(Optional.empty());
        when(repository.findPositionChangePredecessor("history-id")).thenReturn(Optional.empty());
        when(repository.findPreviousDistinctAppointment("001", "00040", "2307", "202406"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2024.05")));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenReturn(1500);
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenReturn(3000);

        PositionChangePromotionListItem preview = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20))
                .content()
                .getFirst();

        assertThat(preview.rollbackEligible()).isTrue();
        assertThat(preview.applyEligible()).isFalse();
        assertThat(preview.currentPositionName()).isEqualTo("正科级领导职务");
        assertThat(preview.newPositionName()).isEqualTo("一级主任科员");
    }

    @Test
    void positionChangePromotionTreatsProcessedRankConversionAsRollbackEvenWhenAppointmentStillDiffers() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot processedWithPendingAppointment = history(
                "2024", "06", "职级套改", "2307", "一级主任科员", "20", "6", "2006", "2006");

        when(repository.findPositionChangePromotionPersonnelUids(any(), any(), any(), any())).thenReturn(List.of(100));
        when(repository.countPositionChangePromotionPersonnel(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(100)).thenReturn(Optional.of(processedWithPendingAppointment));
        when(repository.findCurrentPositionChangeCandidate("001", "00040"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2024.07")));
        when(repository.findProcessedPositionChangeDisplay("001", "00040")).thenReturn(Optional.empty());
        when(repository.findProcessedPositionChangeDisplayById("history-id")).thenReturn(Optional.empty());
        when(repository.findPositionChangePredecessor("history-id")).thenReturn(Optional.of(
                history("2024", "05", "正常档次", "0190", "正科级领导职务", "22", "5", "2006", "2006")));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenReturn(1500);
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenReturn(3000);

        PositionChangePromotionListItem preview = service.positionChangePromotionPreviews(null, null, new PageRequest(0, 20))
                .content()
                .getFirst();

        assertThat(preview.applyEligible()).isFalse();
        assertThat(preview.rollbackEligible()).isTrue();
        assertThat(preview.currentPositionName()).isEqualTo("正科级领导职务");
        assertThat(preview.newPositionName()).isEqualTo("一级主任科员");
    }

    @Test
    void wageProjectionSkipsPositionChangeWhenAppointmentRecordWasModified() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot stalePositionChange = history(
                "2007", "08", "职务变化", "0190", "正科级领导职务", "22", "2", "2006", "2006", "2007.07");

        when(repository.findLatestHistory(8792)).thenReturn(Optional.of(stalePositionChange));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(stalePositionChange));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A0", "副科级领导职务", "1998.03")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A0", "副科级领导职务", "1998.03", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(18);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "23", "3")));
        when(repository.findPositionAtOrBefore("001", "00040", "200801"))
                .thenReturn(Optional.of(new PositionChangeCandidate("01B0", "科员", "2007.07")));
        when(repository.findPositionAtPeriod("001", "00040", "200707"))
                .thenReturn(Optional.of(new PositionChangeCandidate("01B0", "科员", "2007.07")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("01B0", "科员", "2007.07")));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(8792, "200801");

        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("采用历史记录"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("200708 职务变化")
                && line.contains("采用任职记录 2007.07 职务"));
        assertThat(preview.positionCode()).isEqualTo("01B0");
        assertThat(preview.levelStepDisplay()).isEqualTo("23-3");
    }

    @Test
    void wageProjectionAppliesUnprocessedAppointmentPositionChange() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2020.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202101"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2020.07")));
        when(repository.findPositionLevelRange("0180")).thenReturn(Optional.of(new PositionLevelRange("0180", 18, 12)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3259, "202101");

        assertThat(preview.positionCode()).isEqualTo("0180");
        assertThat(preview.levelStepDisplay()).isEqualTo("18-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202008 职务变化")
                && line.contains("采用任职记录 2020.07 职务")
                && line.contains("试算为 18-1"));
    }

    @Test
    void wageProjectionKeepsLevelWhenSameLayerPositionChangeAlreadyReachedMinimumLevel() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0191", "正科级非领导职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3260)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0191", "正科级非领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0191", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0190", "正科级领导职务", "2008.04")));
        when(repository.findPositionAtOrBefore("001", "00040", "200805"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2008.04")));
        when(repository.findPositionLevelRange("0190")).thenReturn(Optional.of(new PositionLevelRange("0190", 18, 12)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3260, "200805");

        assertThat(preview.positionCode()).isEqualTo("0190");
        assertThat(preview.levelStepDisplay()).isEqualTo("18-4");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("200805 职务变化")
                && line.contains("试算为 18-4"));
    }

    @Test
    void wageProjectionAppliesAppointmentAtReformStartMonthToNextPayrollMonth() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0191", "正科级非领导职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3260)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0191", "正科级非领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0191", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("01A1", "副科级非领导职务", "2006.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "200608"))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A1", "副科级非领导职务", "2006.07")));
        when(repository.findPositionLevelRange("01A1"))
                .thenReturn(Optional.of(new PositionLevelRange("01A1", 24, 17)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3260, "200608");

        assertThat(preview.positionCode()).isEqualTo("01A1");
        assertThat(preview.positionName()).contains("副科级非领导");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("200608 职务变化")
                && line.contains("副科级非领导职务"));
    }

    @Test
    void wageProjectionAppliesYearStartPositionChangeBeforeJanuaryStepPromotion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2011", "12", "正常档次", "01A1", "副科级非领导职务", "24", "4", "2008", "2010", "2007.07");

        when(repository.findLatestHistory(3260)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A1", "副科级非领导职务", "2007.07")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A1", "副科级非领导职务", "2007.07", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01A1", 0, 99, 0, 99, "24", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2011.12")));
        when(repository.findPositionLevelRange("0191")).thenReturn(Optional.of(new PositionLevelRange("0191", 22, 16)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(2);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of(2010, 2011));
        when(repository.hasAllowanceStandardForPosition(anyString(), anyString(), anyString())).thenReturn(true);

        WageProjectionPreview preview = service.wageProjection(3260, "201201");

        int positionStepIndex = indexOfStepContaining(preview.stepDetails(), "201201", "职务变化");
        int stepPromotionIndex = indexOfStepContaining(preview.stepDetails(), "201201", "晋升档次");
        assertThat(positionStepIndex).isGreaterThanOrEqualTo(0);
        assertThat(stepPromotionIndex).isGreaterThan(positionStepIndex);
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201201 职务变化")
                && line.contains("2011.12"));
    }

    @Test
    void projectionHistoryAuditStopsAfterSameMonthPositionChangeBeforeStepPromotion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = historyWithId(
                "reform-id", "2006", "07", "套改", "01A1", "副科级非领导职务", "24", "4", "2006", "2006", "2007.07");
        PayrollHistorySnapshot positionRecord = historyWithId(
                "position-id", "2012", "01", "职务变化", "0191", "正科级非领导职务", "22", "1", "2008", "2010", "2011.12");
        PayrollHistorySnapshot stepRecord = historyWithId(
                "step-id", "2012", "01", "正常档次", "0191", "正科级非领导职务", "22", "5", "2008", "2012", "2011.12");

        when(repository.findLatestHistory(3260)).thenReturn(Optional.of(stepRecord));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(stepRecord, positionRecord, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A1", "副科级非领导职务", "2007.07")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A1", "副科级非领导职务", "2007.07", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01A1", 0, 99, 0, 99, "24", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2011.12")));
        when(repository.findPositionLevelRange("01A1")).thenReturn(Optional.of(new PositionLevelRange("01A1", 24, 17)));
        when(repository.findPositionLevelRange("0191")).thenReturn(Optional.of(new PositionLevelRange("0191", 22, 16)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.findCalculationFields()).thenReturn(List.of());
        when(repository.findHistoryValuesById(anyString(), eq("001"), eq("00040"), anyString(), anyString()))
                .thenReturn(Optional.of(Map.of("hj2", 0)));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(2);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of(2010, 2011));
        when(repository.hasAllowanceStandardForPosition(anyString(), anyString(), anyString())).thenReturn(true);

        List<PayrollHistoryProjectionAudit> audits = service.projectionHistoryAudits(3260);
        PayrollHistoryProjectionAudit positionAudit = audits.stream()
                .filter(item -> "position-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();
        PayrollHistoryProjectionAudit stepAudit = audits.stream()
                .filter(item -> "step-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();

        long positionChangeStepsAt201201 = positionAudit.stepDetails().stream()
                .filter(step -> "201201".equals(step.period())
                        && step.description() != null
                        && step.description().contains("职务变化"))
                .count();
        assertThat(positionChangeStepsAt201201).isEqualTo(1);
        assertThat(positionAudit.stepDetails().stream()
                .noneMatch(step -> "201201".equals(step.period())
                        && step.description() != null
                        && step.description().contains("晋升档次"))).isTrue();
        assertThat(stepAudit.stepDetails().stream()
                .anyMatch(step -> "201201".equals(step.period())
                        && step.description() != null
                        && step.description().contains("晋升档次"))).isTrue();
        WageProjectionStepDetail positionStep = positionAudit.stepDetails().stream()
                .filter(step -> "201201".equals(step.period())
                        && step.description() != null
                        && step.description().contains("职务变化"))
                .findFirst()
                .orElseThrow();
        assertThat(positionStep.levelStepDisplay()).isEqualTo("22-1");
        assertThat(positionAudit.projectedTotal()).isEqualByComparingTo(positionStep.total());
    }

    @Test
    void wageProjectionTreatsOtherCivilServiceSequenceToPoliceAsPoliceConversion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0401", "其他公务员序列职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4810)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0401", "其他公务员序列职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0401", "其他公务员序列职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0401", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("2105", "警员职务", "2020.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202101"))
                .thenReturn(Optional.of(new PositionChangeCandidate("2105", "警员职务", "2020.07")));
        when(repository.findPositionLevelRange("2105")).thenReturn(Optional.of(new PositionLevelRange("2105", 5, 1)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (20 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4810, "202101");

        assertThat(preview.positionCode()).isEqualTo("2105");
        assertThat(preview.levelStepDisplay()).isEqualTo("5-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202008 警员套改")
                && line.contains("按警员套改")
                && line.contains("试算为 5-1"));
    }

    @Test
    void wageProjectionAppliesNormalPositionPolicyWithinPoliceSequences() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0401", "其他公务员序列职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4811)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0401", "其他公务员序列职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0401", "其他公务员序列职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0401", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("2105", "警员职务", "2020.07"),
                        new PositionChangeCandidate("2104", "上一级警员职务", "2021.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202201"))
                .thenReturn(Optional.of(new PositionChangeCandidate("2104", "上一级警员职务", "2021.07")));
        when(repository.findPositionLevelRange("2105")).thenReturn(Optional.of(new PositionLevelRange("2105", 5, 1)));
        when(repository.findPositionLevelRange("2104")).thenReturn(Optional.of(new PositionLevelRange("2104", 4, 1)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (20 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4811, "202201");

        assertThat(preview.positionCode()).isEqualTo("2104");
        assertThat(preview.levelStepDisplay()).isEqualTo("4-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202008 警员套改"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202108 职务变化")
                && line.contains("按职务晋升政策")
                && line.contains("试算为 4-1"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("202108 警员套改"));
    }

    @Test
    void wageProjectionKeepsConvertedLevelWhenPoliceReturnsToSameLayerCivilServicePosition() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0401", "其他公务员序列职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4812)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0401", "其他公务员序列职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0401", "其他公务员序列职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0401", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("2105", "警员职务", "2020.07"),
                        new PositionChangeCandidate("0180", "副县处级领导职务", "2021.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202201"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2021.07")));
        when(repository.findPositionLevelRange("2105")).thenReturn(Optional.of(new PositionLevelRange("2105", 5, 1)));
        when(repository.findPositionLevelRange("0180")).thenReturn(Optional.of(new PositionLevelRange("0180", 18, 12)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (20 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4812, "202201");

        assertThat(preview.positionCode()).isEqualTo("0180");
        assertThat(preview.levelStepDisplay()).isEqualTo("12-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202108 警员回到其他类")
                && line.contains("试算为 12-1"));
    }

    @Test
    void wageProjectionPromotesOneLevelWhenPoliceReturnsToHigherCivilServicePositionAfterReachingMinimum() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0401", "其他公务员序列职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4813)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0401", "其他公务员序列职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0401", "其他公务员序列职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0401", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("2105", "警员职务", "2020.07"),
                        new PositionChangeCandidate("0170", "正县处级领导职务", "2021.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202201"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0170", "正县处级领导职务", "2021.07")));
        when(repository.findPositionLevelRange("2105")).thenReturn(Optional.of(new PositionLevelRange("2105", 5, 1)));
        when(repository.findPositionLevelRange("0170")).thenReturn(Optional.of(new PositionLevelRange("0170", 12, 8)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (20 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4813, "202201");

        assertThat(preview.positionCode()).isEqualTo("0170");
        assertThat(preview.levelStepDisplay()).isEqualTo("11-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202108 警员回到其他类")
                && line.contains("试算为 11-1"));
    }

    @Test
    void wageProjectionAppliesDisciplinaryDemotionWhenPoliceReturnsToLowerCivilServicePosition() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0401", "其他公务员序列职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4814)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0401", "其他公务员序列职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0401", "其他公务员序列职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0401", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("2105", "警员职务", "2020.07"),
                        new PositionChangeCandidate("0190", "正科级领导职务", "2021.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202201"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2021.07")));
        when(repository.findPositionLevelRange("2105")).thenReturn(Optional.of(new PositionLevelRange("2105", 5, 1)));
        when(repository.findPositionLevelRange("0190")).thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.hasDemotionDisciplinaryRecord("001", "00040", "202107")).thenReturn(true);
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (20 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4814, "202201");

        assertThat(preview.positionCode()).isEqualTo("0190");
        assertThat(preview.level()).isEqualTo("14");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202108 撤职处分")
                && line.contains("警员等级先加 7")
                && line.contains("降低 1 个职务层次")
                && line.contains("逐级就近就低"));
    }

    @Test
    void wageProjectionAppliesDisciplinaryDemotionBeforePoliceConversionWhenLayerDrops() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0180", "副县处级领导职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4815)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0180", "副县处级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0180", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("2107", "警员职务", "2021.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202201"))
                .thenReturn(Optional.of(new PositionChangeCandidate("2107", "警员职务", "2021.07")));
        when(repository.findPositionLevelRange("2107")).thenReturn(Optional.of(new PositionLevelRange("2107", 7, 1)));
        when(repository.hasDemotionDisciplinaryRecord("001", "00040", "202107")).thenReturn(true);
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (20 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4815, "202201");

        assertThat(preview.positionCode()).isEqualTo("2107");
        assertThat(preview.level()).isEqualTo("20");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202108 撤职处分")
                && line.contains("降低 1 个职务层次")
                && line.contains("逐级就近就低"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("202108 警员套改"));
    }

    @Test
    void wageProjectionAppliesDisciplinaryDemotionWhenLowerPositionHasSamePeriodSanction() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0180", "副县处级领导职务", "18", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4820)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0180", "副县处级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0180", 0, 99, 0, 99, "18", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0190", "正科级领导职务", "2020.07")));
        when(repository.findPositionAtOrBefore("001", "00040", "202101"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2020.07")));
        when(repository.findPositionLevelRange("0190")).thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.hasDemotionDisciplinaryRecord("001", "00040", "202007")).thenReturn(true);
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4820, "202101");

        assertThat(preview.positionCode()).isEqualTo("0190");
        assertThat(preview.level()).isEqualTo("20");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202008 撤职处分")
                && line.contains("降低 1 个职务层次")
                && line.contains("逐级就近就低"));
    }

    @Test
    void wageProjectionAppliesLegacyPolicePositionPolicyFromAdministrativeToPrefix02() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01A1", "副科级非领导职务", "24", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(4830)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A1", "副科级非领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A1", "副科级非领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01A1", 0, 99, 0, 99, "24", "4")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new PositionChangeCandidate("0204", "四级警长", "2010.01")));
        when(repository.findPositionAtOrBefore("001", "00040", "201002"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0204", "四级警长", "2010.01")));
        when(repository.findPositionLevelRange("0204")).thenReturn(Optional.of(new PositionLevelRange("0204", 17, 23)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(4830, "201002");

        assertThat(preview.positionCode()).isEqualTo("0204");
        assertThat(preview.levelStepDisplay()).isEqualTo("17-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201002 职务变化")
                && line.contains("0204 四级警长")
                && line.contains("试算为 17-1"));
    }

    @Test
    void wageProjectionKeepsCurrentReformWhenCurrentLevelIsHigherAndStepIsNotLowerThanLowerPosition() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0180", "副县处级领导职务", "21", "5", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(2919)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0180", "副县处级领导职务", "2004.01", 0),
                        new WageReformPosition("0190", "正科级领导职务", "1998.01", 7)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("0180".equals(positionCode)) {
                return Optional.of(new WageReformStandard("0180", 0, 99, 0, 99, "21", "5"));
            }
            if ("0190".equals(positionCode)) {
                if ((int) invocation.getArgument(1) == 2) {
                    return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "5"));
                }
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "5"));
            }
            return Optional.empty();
        });
        when(repository.findPositionAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 21 && step == 5) {
                return 900;
            }
            if (level == 22 && step == 5) {
                return 920;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(2919, "200607");

        assertThat(preview.levelStepDisplay()).isEqualTo("21-5");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("已比照原任低一职务"));
    }

    @Test
    void wageProjectionUsesPreviousPositionReformWithoutRequiringOneLowerLayer() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0180", "副县处级领导职务", "22", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(2920)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0180", "副县处级领导职务", "2004.01", 0),
                        new WageReformPosition("0190", "正科级领导职务", "1998.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("0180".equals(positionCode)) {
                return Optional.of(new WageReformStandard("0180", 0, 99, 0, 99, "22", "4"));
            }
            if ("0190".equals(positionCode)) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "5"));
            }
            return Optional.empty();
        });
        when(repository.findPositionAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(2920, "200607");

        assertThat(preview.levelStepDisplay()).isEqualTo("20-1");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("原任低一职务")
                && line.contains("高套一级"));
    }

    @Test
    void wageProjectionUsesEducationRegularizationWhenReformLevelAndSalaryAreLower() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01B0", "科员", "23", "2", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("01B0", 0, 99, 0, 99, "23", "2"));
        stubCivilServantGradeSalary(repository);
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2003.07")));
        when(repository.findEducationRegularizationStandard("01B0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "3")));
        when(repository.findEducationRecordsBetween("001", "00040", "200607", "200607")).thenReturn(List.of());

        WageProjectionPreview preview = service.wageProjection(8793, "200607");

        assertThat(preview.levelStepDisplay()).isEqualTo("22-3");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("低于相同学历新参加工作人员转正定级标准")
                && line.contains("22-3"));
    }

    @Test
    void wageProjectionUsesEducationRegularizationSalaryWhenReformLevelIsHigherButSalaryIsLower() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "1", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "1"));
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String levelValue = invocation.getArgument(0);
            String stepValue = invocation.getArgument(1);
            if (levelValue == null || levelValue.toString().isBlank() || stepValue == null || stepValue.toString().isBlank()) {
                return 0;
            }
            int level = Integer.parseInt(levelValue.toString().trim());
            int step = Integer.parseInt(stepValue.toString().trim());
            if (level == 21 && step == 11) {
                return 950;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.highestGradeStepForLevel(anyString())).thenAnswer(invocation -> highestGradeStepForTest(invocation.getArgument(0)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2003.07")));
        when(repository.findEducationRegularizationStandard("0190", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "20")));
        when(repository.findEducationRecordsBetween("001", "00040", "200607", "200607")).thenReturn(List.of());

        WageProjectionPreview preview = service.wageProjection(8794, "200607");

        assertThat(preview.levelStepDisplay()).isEqualTo("21-10");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("套改级别高于相同学历新参加工作人员定级级别")
                && line.contains("就近就高套入套改级别"));
    }

    @Test
    void wageProjectionPromotesStepInsteadOfLevelAtHighestPositionLevel() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0180", "副县处级领导职务", "12", "3", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(8795)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0180", "副县处级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0180", 0, 99, 0, 99, "12", "3")));
        when(repository.findPositionAtOrBefore("001", "00040", "201101"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0180", "副县处级领导职务", "2004.01")));
        when(repository.findPositionLevelRange("0180")).thenReturn(Optional.of(new PositionLevelRange("0180", 18, 12)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });

        WageProjectionPreview preview = service.wageProjection(8795, "201101");

        assertThat(preview.levelStepDisplay()).isEqualTo("12-6");
        assertThat(preview.levelAssessmentStartYear()).isEqualTo("2011");
        assertThat(preview.stepAssessmentStartYear()).isEqualTo("2010");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2011 年")
                && line.contains("已达到所任职务最高级别")
                && line.contains("按级别晋升口径")
                && line.contains("晋升档次到 12-6"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2011 年") && line.contains("晋升级别 12-"));
    }

    @Test
    void wageProjectionResetsStepAssessmentYearWhenLevelIncreaseExceedsStepDifference() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(8796)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4")))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "20", "1")));
        when(repository.findPositionAtOrBefore("001", "00040", "200801"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findPositionLevelRange("0190")).thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.policeOfficerGradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });

        WageProjectionPreview preview = service.wageProjection(8796, "200801");

        assertThat(preview.levelStepDisplay()).isEqualTo("20-1");
        assertThat(preview.levelAssessmentStartYear()).isEqualTo("2007");
        assertThat(preview.stepAssessmentStartYear()).isEqualTo("2007");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2007 年")
                && line.contains("晋升级别")
                && line.contains("级别考核起算年（xckhndjb）更新为 2007")
                && line.contains("档次考核起算年（xckhndzw）更新为 2007"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2008 年") && line.contains("晋升档次"));
    }

    @Test
    void wageProjectionRecordsSeparateStepsForAllowanceStandardAndStepPromotion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "2", "2011", "2012", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "2"));
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "201401"))
                .thenReturn(List.of());
        when(repository.findAllowanceStandardPeriodsBetween(eq("001"), eq("200607"), eq("201401")))
                .thenReturn(List.of("201110", "201401"));
        when(repository.hasAllowanceStandard("201110", "001", "0190")).thenReturn(true);
        when(repository.hasAllowanceStandard("201401", "001", "0190")).thenReturn(true);
        when(repository.hasAllowanceStandardForPosition(eq("201110"), eq("001"), eq("0190"))).thenReturn(true);
        when(repository.hasAllowanceStandardForPosition(eq("201401"), eq("001"), eq("0190"))).thenReturn(true);
        when(repository.latestAllowanceStandardAtOrBefore(eq("201401"), eq("001"), eq("0190"))).thenReturn("201401");
        when(repository.latestAllowanceStandardAtOrBefore(eq("201110"), eq("001"), eq("0190"))).thenReturn("201110");
        when(repository.performanceAllowance("001", "0190", "201110")).thenReturn(BigDecimal.valueOf(900));
        when(repository.performanceAllowance("001", "0190", "201401")).thenReturn(BigDecimal.valueOf(1100));
        when(repository.subsidyAllowance("001", "0190", "201110")).thenReturn(280);
        when(repository.subsidyAllowance("001", "0190", "201401")).thenReturn(320);
        when(repository.findPositionLevelRange("0190")).thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            int years = Math.max(0, end - start + 1);
            if (start == 2006) {
                return Math.min(4, years);
            }
            if (start == end && end == 2009) {
                return 2;
            }
            return years;
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });

        WageProjectionPreview preview = service.wageProjection(8794, "201401");

        List<String> descriptionsAt201401 = preview.stepDetails().stream()
                .filter(step -> "201401".equals(step.period()))
                .map(WageProjectionStepDetail::description)
                .toList();
        assertThat(descriptionsAt201401).hasSizeGreaterThanOrEqualTo(2);
        assertThat(descriptionsAt201401)
                .anyMatch(line -> line.contains("津补贴调标"));
        assertThat(descriptionsAt201401)
                .anyMatch(line -> line.contains("晋升档次")
                        || line.contains("晋升级别")
                        || line.contains("档差"));
        assertThat(preview.explanationLines())
                .anyMatch(line -> line.contains("201401") && line.contains("津补贴调标"));
        assertThat(preview.explanationLines())
                .anyMatch(line -> line.contains("2014") && (line.contains("晋升档次") || line.contains("晋升级别")));

        WageProjectionStepDetail stepPromotion = preview.stepDetails().stream()
                .filter(step -> "201401".equals(step.period())
                        && step.description() != null
                        && (step.description().contains("晋升档次") || step.description().contains("档差")))
                .findFirst()
                .orElseThrow();
        WageProjectionStepDetail allowanceStep = preview.stepDetails().stream()
                .filter(step -> "201401".equals(step.period())
                        && step.description() != null
                        && step.description().contains("津补贴调标"))
                .findFirst()
                .orElseThrow();
        assertThat(stepPromotion.allowanceStandardYearMonth()).isEqualTo("201110");
        assertThat(allowanceStep.allowanceStandardYearMonth()).isEqualTo("201401");
        assertThat(stepPromotion.components().stream()
                .filter(component -> "DFBT2".equals(component.fieldName()))
                .findFirst()
                .orElseThrow()
                .amount()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(allowanceStep.components().stream()
                .filter(component -> "DFBT2".equals(component.fieldName()))
                .findFirst()
                .orElseThrow()
                .amount()).isEqualByComparingTo(BigDecimal.valueOf(1100));
    }

    @Test
    void wageProjectionAppliesBasicSalaryStandardAdjustment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.findAllowanceStandardPeriodsBetween("001", "200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.hasBasicSalaryStandardForSource("201807", "GRADE")).thenReturn(true);
        when(repository.hasAllowanceStandard("201807", "001", "0190")).thenReturn(true);
        when(repository.civilServantGradeSalary(eq("21"), eq("4"), eq("0"), anyString())).thenReturn(3200);
        when(repository.positionSalary("0190", "201807")).thenReturn(1800);
        when(repository.positionGradeSalary("0190", "4", "0", "201807")).thenReturn(0);
        when(repository.performanceAllowance("001", "0190", "201807")).thenReturn(BigDecimal.valueOf(2380));
        when(repository.subsidyAllowance("001", "0190", "201807")).thenReturn(545);

        WageProjectionPreview preview = service.wageProjection(8794, "202101");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201807 工资调标")
                && line.contains("tbnd）调整为 201807")
                && line.contains("职务工资 1800")
                && line.contains("级别/薪级工资 3200"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201807 津补贴调标")
                && line.contains("jbtbz）调整为 201807")
                && line.contains("生活性补贴 2380")
                && line.contains("工作性津贴 545"));
        assertThat(preview.salaryStandardYearMonth()).isEqualTo("201807");
        assertThat(preview.positionSalary()).isEqualTo(1800);
        assertThat(preview.gradeSalary()).isEqualTo(3200);
        List<WageProjectionStepDetail> stepsAt201807 = preview.stepDetails().stream()
                .filter(step -> "201807".equals(step.period()))
                .toList();
        assertThat(stepsAt201807).hasSize(1);
        assertThat(stepsAt201807.getFirst().description())
                .contains("工资调标")
                .contains("津补贴调标");
    }

    @Test
    void calculationPreviewUsesProjectedSalaryStandardAtTargetPeriod() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.findAllowanceStandardPeriodsBetween("001", "200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.hasBasicSalaryStandardForSource("201807", "GRADE")).thenReturn(true);
        when(repository.hasBasicSalaryStandardForSource("201607", "GRADE")).thenReturn(true);
        when(repository.civilServantGradeSalary(eq("21"), eq("4"), anyString(), eq("201807"))).thenReturn(3200);
        when(repository.civilServantGradeSalary(eq("21"), eq("4"), anyString(), eq("201607"))).thenReturn(900);
        when(repository.positionSalary("0190", "201807")).thenReturn(1800);
        when(repository.positionSalary("0190", "201607")).thenReturn(1100);
        when(repository.positionGradeSalary("0190", "4", "0", "201807")).thenReturn(0);
        when(repository.positionGradeSalary("0190", "4", "0", "201607")).thenReturn(0);
        when(repository.hasAllowanceStandard("201807", "001", "0190")).thenReturn(true);
        when(repository.performanceAllowance("001", "0190", "201607")).thenReturn(BigDecimal.valueOf(1100));
        when(repository.performanceAllowance("001", "0190", "201807")).thenReturn(BigDecimal.valueOf(2380));
        when(repository.subsidyAllowance("001", "0190", "201607")).thenReturn(320);
        when(repository.subsidyAllowance("001", "0190", "201807")).thenReturn(545);
        when(repository.findLatestHistoryValues(8794)).thenReturn(Map.of());
        when(repository.findCalculationFields()).thenReturn(List.of());
        when(repository.findMatchedPositionStandards(latest)).thenReturn(List.of());
        when(repository.findMatchedAllowanceStandards(latest)).thenReturn(List.of());
        when(repository.mapPositionSalaryCode("0190")).thenReturn("0190");
        when(repository.technicalGradeSalary(anyString(), anyString())).thenReturn(0);

        PayrollCalculationPreview current = service.calculationPreview(8794);
        PayrollCalculationPreview projected = service.calculationPreview(8794, "202101");

        assertThat(componentAmount(current, "ZWGZSE2")).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(componentAmount(current, "JBGZSE2")).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(componentAmount(projected, "ZWGZSE2")).isEqualByComparingTo(BigDecimal.valueOf(1800));
        assertThat(componentAmount(projected, "JBGZSE2")).isEqualByComparingTo(BigDecimal.valueOf(3200));
        assertThat(componentAmount(projected, "DFBT2")).isEqualByComparingTo(BigDecimal.valueOf(2380));
        assertThat(componentAmount(projected, "SDBT")).isEqualByComparingTo(BigDecimal.valueOf(545));
        assertThat(projected.calculationPeriod()).isEqualTo("202101");
    }

    @Test
    void wageProjectionAppliesRankAllowanceChangeFromNextMonth() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findRankAllowanceAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new RankAllowanceChange("三级警督", "2006.01", "jx")));
        when(repository.latestRankAllowanceStandardAtOrBefore("200607")).thenReturn("200607");
        when(repository.findRankAllowanceChangesBetween("001", "00040", "200607", "202101"))
                .thenReturn(List.of(new RankAllowanceChange("二级警督", "2020.07", "jx")));
        when(repository.resolveRankAllowanceStandardLb(eq("三级警督"), any())).thenReturn("jx");
        when(repository.resolveRankAllowanceStandardLb(eq("二级警督"), any())).thenReturn("jx");
        when(repository.rankAllowanceByRank("200607", "三级警督", "jx")).thenReturn(100);
        when(repository.rankAllowanceByRank("200607", "二级警督", "jx")).thenReturn(150);

        WageProjectionPreview preview = service.wageProjection(8794, "202101");

        assertThat(preview.rankName()).isEqualTo("二级警督");
        assertThat(preview.rankAllowanceStandardYearMonth()).isEqualTo("200607");
        assertThat(preview.rankAllowance()).isEqualTo(150);
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("202008 警衔变化")
                && line.contains("二级警督")
                && line.contains("津贴 150"));
    }

    @Test
    void wageProjectionAppliesRankAllowanceBeforeLaterPositionChangeWhenAppointmentIsNextMonth() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2008", "10", "职务变化", "0191", "正科级非领导职务", "21", "1", "2008", "2006", "2007.07");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "2"));
        when(repository.findLatestHistory(134)).thenReturn(Optional.of(latest));
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findPositionChangesBetween(eq("001"), eq("00040"), anyString(), eq("200811"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("0190", "正科级领导职务", "2008.09"),
                        new PositionChangeCandidate("0191", "正科级非领导职务", "2008.10")));
        when(repository.findPositionLevelRange(anyString())).thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.findRankAllowanceChangesBetween("001", "00040", "200607", "200811"))
                .thenReturn(List.of(new RankAllowanceChange("三级警督", "2008.09", "jx")));
        when(repository.findRankAllowanceAtOrBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.latestRankAllowanceStandardAtOrBefore(anyString())).thenReturn("200607");
        when(repository.resolveRankAllowanceStandardLb(eq("三级警督"), any())).thenReturn("jx");
        when(repository.rankAllowanceByRank(anyString(), eq("三级警督"), eq("jx"))).thenReturn(220);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(134, "200811");

        int rankStepIndex = indexOfStepContaining(preview.stepDetails(), "200810", "警衔变化");
        int positionStepIndex = indexOfStepContaining(preview.stepDetails(), "200811", "职务变化");
        assertThat(rankStepIndex).isGreaterThanOrEqualTo(0);
        assertThat(positionStepIndex).isGreaterThanOrEqualTo(0);
        assertThat(rankStepIndex).isLessThan(positionStepIndex);
        assertThat(preview.stepDetails().stream()
                .filter(step -> "200811".equals(step.period()))
                .map(WageProjectionStepDetail::description)
                .filter(description -> description != null && description.contains("职务变化"))
                .count()).isEqualTo(1);
    }

    private static int indexOfStepContaining(
            List<WageProjectionStepDetail> steps,
            String period,
            String keyword) {
        for (int index = 0; index < steps.size(); index++) {
            WageProjectionStepDetail step = steps.get(index);
            if (period.equals(step.period())
                    && step.description() != null
                    && step.description().contains(keyword)) {
                return index;
            }
        }
        return -1;
    }

    @Test
    void wageProjectionAdjustsRankAllowanceWhenNewStandardStarts() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findRankAllowanceAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new RankAllowanceChange("三级警督", "2006.01", "jx")));
        when(repository.latestRankAllowanceStandardAtOrBefore("200607")).thenReturn("200607");
        when(repository.findRankAllowanceStandardPeriodsBetween("200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.hasRankAllowanceStandardForCategory("201807", "警")).thenReturn(true);
        when(repository.resolveRankAllowanceStandardLb(eq("三级警督"), any())).thenReturn("jx");
        when(repository.rankAllowanceByRank("200607", "三级警督", "jx")).thenReturn(100);
        when(repository.rankAllowanceByRank("201807", "三级警督", "jx")).thenReturn(220);

        WageProjectionPreview preview = service.wageProjection(8794, "202101");

        assertThat(preview.rankName()).isEqualTo("三级警督");
        assertThat(preview.rankAllowanceStandardYearMonth()).isEqualTo("201807");
        assertThat(preview.rankAllowance()).isEqualTo(220);
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201807 调整警衔津贴")
                && line.contains("三级警督")
                && line.contains("津贴 220"));
    }

    @Test
    void wageProjectionSkipsRankAllowanceStandardWhenCategoryDoesNotMatchCurrentRank() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findRankAllowanceAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new RankAllowanceChange("三级警督", "2006.01", "jx")));
        when(repository.latestRankAllowanceStandardAtOrBefore("200607")).thenReturn("200607");
        when(repository.findRankAllowanceStandardPeriodsBetween("200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.hasRankAllowanceStandardForCategory("201807", "警")).thenReturn(false);
        when(repository.resolveRankAllowanceStandardLb(eq("三级警督"), any())).thenReturn("jx");
        when(repository.rankAllowanceByRank("200607", "三级警督", "jx")).thenReturn(100);

        WageProjectionPreview preview = service.wageProjection(8794, "202101");

        assertThat(preview.rankName()).isEqualTo("三级警督");
        assertThat(preview.rankAllowanceStandardYearMonth()).isEqualTo("200607");
        assertThat(preview.rankAllowance()).isEqualTo(100);
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("201807 调整警衔津贴"));
    }

    private static int indexOfLineContaining(List<String> lines, String text) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(text)) {
                return i;
            }
        }
        return -1;
    }

    private static BigDecimal componentAmount(PayrollCalculationPreview preview, String fieldName) {
        return preview.calculatedComponents().stream()
                .filter(component -> fieldName.equals(component.fieldName()))
                .map(PayrollPreviewComponent::amount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private static int highestGradeStepForTest(String gradeLevel) {
        if (gradeLevel == null || gradeLevel.toString().isBlank()) {
            return 99;
        }
        return switch (String.valueOf(Integer.parseInt(gradeLevel.trim()))) {
            case "1" -> 6;
            case "2" -> 7;
            case "3", "23", "24" -> 8;
            case "4", "22" -> 9;
            case "5", "21" -> 10;
            case "6", "7", "8", "9", "10", "20" -> 11;
            case "11", "19" -> 12;
            case "12", "17", "18" -> 13;
            case "13", "14", "15", "16" -> 14;
            case "25" -> 7;
            case "26", "27" -> 6;
            default -> 99;
        };
    }

    private static void stubCivilServantGradeSalary(PayrollRepository repository) {
        when(repository.highestGradeStepForLevel(anyString())).thenAnswer(invocation -> highestGradeStepForTest(invocation.getArgument(0)));
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String levelValue = invocation.getArgument(0);
            String stepValue = invocation.getArgument(1);
            if (levelValue == null || levelValue.toString().isBlank() || stepValue == null || stepValue.toString().isBlank()) {
                return 0;
            }
            int level = Integer.parseInt(levelValue.toString().trim());
            int step = Integer.parseInt(stepValue.toString().trim());
            String extraValue = invocation.getArgument(2);
            int extra = extraValue == null || extraValue.toString().isBlank() ? 0 : Integer.parseInt(extraValue.toString().trim());
            int highest = highestGradeStepForTest(levelValue);
            int effectiveStep = step >= highest ? step + Math.max(0, extra) : step;
            return (30 - level) * 100 + effectiveStep * 10;
        });
    }

    private static void stubLevelPromotionListContext(
            PayrollRepository repository,
            Map<Integer, PayrollHistorySnapshot> histories,
            Map<Integer, List<PersonnelAssessmentYear>> assessments) {
        when(repository.findLatestHistoriesByUids(any())).thenAnswer(invocation -> {
            List<Integer> uids = invocation.getArgument(0);
            Map<Integer, PayrollHistorySnapshot> result = new java.util.LinkedHashMap<>();
            for (Integer uid : uids) {
                PayrollHistorySnapshot history = histories.get(uid);
                if (history != null) {
                    result.put(uid, history);
                }
            }
            return result;
        });
        when(repository.findAssessmentYearsByUids(any(), anyInt(), anyInt())).thenReturn(assessments);
    }

    private static List<PersonnelAssessmentYear> assessmentYears(int... years) {
        return IntStream.of(years)
                .mapToObj(year -> new PersonnelAssessmentYear(year, "称职"))
                .toList();
    }

    private static void stubReformStart(
            PayrollRepository repository,
            PayrollHistorySnapshot latest,
            WageReformStandard reformStandard) {
        int uid = "0190".equals(latest.positionCode()) ? 8794 : 8793;
        when(repository.findLatestHistory(uid)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findStoredWageReformSnapshot(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate(latest.positionCode(), latest.positionName(), latest.positionStartYearMonth())));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition(latest.positionCode(), latest.positionName(), latest.positionStartYearMonth(), 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findPositionAtPeriod("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findPersonnelEducationCode("001", "00040")).thenReturn(Optional.empty());
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(reformStandard));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenReturn(Optional.of(reformStandard));
        when(repository.findFirstWageReformStandardForPosition(anyString())).thenReturn(Optional.of(reformStandard));
        when(repository.findPositionAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate(latest.positionCode(), latest.positionName(), latest.positionStartYearMonth())));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());
        when(repository.findAllowanceStandardPeriodsBetween(anyString(), anyString(), anyString())).thenReturn(List.of());
        when(repository.hasAllowanceStandardForPosition(anyString(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    void normalPromotionPreviewUsesSelectedPromotionYearForCalculationPeriod() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2025", "01", "正常档次", "0190", "正科级领导职务", "21", "2", "2008", "2025", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9003));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9003)).thenReturn(Optional.of(latest));
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(2);

        NormalPromotionPreview preview = service.normalPromotionPreviews(null, null, false, "2026", PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.calculationPeriod()).isEqualTo("202601");
        assertThat(preview.assessmentPeriod()).isEqualTo("2025");
    }

    @Test
    void normalPromotionDueOnlyFilterPaginatesAfterEligibilityCheck() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot eligibleHistory = history(
                "2025", "01", "调资", "0190", "正科级领导职务", "21", "2", "2008", "2024", "2004.01");
        PayrollHistorySnapshot ineligibleHistory = history(
                "2025", "01", "调资", "0190", "正科级领导职务", "21", "2", "2008", "2024", "2004.01");
        PayrollHistorySnapshot processedHistory = history(
                "2026", "01", "正常档次", "0190", "正科级领导职务", "21", "3", "2008", "2026", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(OrganizationScope.unrestricted());
        when(repository.findNormalPromotionCandidateUids(any(), any(), any(), eq(2026)))
                .thenReturn(List.of(9001, 9002, 9003));
        when(repository.findLatestHistoriesByUids(any())).thenAnswer(invocation -> {
            List<Integer> uids = invocation.getArgument(0);
            Map<Integer, PayrollHistorySnapshot> map = new java.util.LinkedHashMap<>();
            if (uids.contains(9001)) {
                map.put(9001, eligibleHistory);
            }
            if (uids.contains(9002)) {
                map.put(9002, ineligibleHistory);
            }
            if (uids.contains(9003)) {
                map.put(9003, processedHistory);
            }
            return map;
        });
        when(repository.findAssessmentYearsByUids(any(), anyInt(), anyInt())).thenReturn(Map.of(
                9001, assessmentYears(2024, 2025),
                9002, List.of(),
                9003, assessmentYears(2024, 2025)));
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        stubCivilServantGradeSalary(repository);

        PageResponse<NormalPromotionPreview> pendingPage = service.normalPromotionPreviews(
                null, null, true, false, "2026", PageRequest.of(0, 1));
        assertThat(pendingPage.totalElements()).isEqualTo(1);
        assertThat(pendingPage.content()).hasSize(1);
        assertThat(pendingPage.content().getFirst().uid()).isEqualTo(9001);
        assertThat(pendingPage.content().getFirst().eligible()).isTrue();

        PageResponse<NormalPromotionPreview> processedPage = service.normalPromotionPreviews(
                null, null, false, true, "2026", PageRequest.of(0, 10));
        assertThat(processedPage.totalElements()).isEqualTo(1);
        assertThat(processedPage.content().getFirst().uid()).isEqualTo(9003);
        assertThat(processedPage.content().getFirst().rollbackEligible()).isTrue();

        verify(repository, never()).findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any());
    }

    @Test
    void educationPromotionPreservesStepAssessmentYearWhenLevelIncreaseWithinStepDifference() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01B0", "科员", "23", "2", "2000", "2001", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9001));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9001)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2003.07")));
        when(repository.findEducationRegularizationStandard("01B0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "1")));
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> gradeSalaryForStepDifferenceTest(
                invocation.getArgument(0), invocation.getArgument(1)));
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> gradeSalaryForStepDifferenceTest(
                invocation.getArgument(0), invocation.getArgument(1)));

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.promotedLevel()).isEqualTo("22");
        assertThat(preview.nextLevelAssessmentStartYear()).isEqualTo("2003");
        assertThat(preview.nextStepAssessmentStartYear()).isEqualTo("2001");
        assertThat(preview.note()).contains("未引起执行工资职务层次变化但引起级别变化");
        assertThat(preview.note()).contains("未超过下一级别一个工资档差");
    }

    @Test
    void educationPromotionResetsStepAssessmentYearWhenLevelIncreaseExceedsStepDifference() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01B0", "科员", "23", "2", "2000", "2001", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9002));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9002)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2003.07")));
        when(repository.findEducationRegularizationStandard("01B0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "1")));
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.nextStepAssessmentStartYear()).isEqualTo("2003");
        assertThat(preview.note()).contains("超过下一级别一个工资档差");
    }

    @Test
    void educationPromotionResetsStepAssessmentYearWhenPromotedTwentyFiveTwoToTwentyFourThree() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01B0", "科员", "25", "2", "2000", "2010", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9003));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9003)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2008.07")));
        when(repository.findEducationRegularizationStandard("01B0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "24", "3")));
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> gradeSalaryTwentyFiveTwoToTwentyFourThree(
                invocation.getArgument(0), invocation.getArgument(1)));
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> gradeSalaryTwentyFiveTwoToTwentyFourThree(
                invocation.getArgument(0), invocation.getArgument(1)));

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.promotedLevel()).isEqualTo("24");
        assertThat(preview.promotedStep()).isEqualTo("3");
        assertThat(preview.nextLevelAssessmentStartYear()).isEqualTo("2008");
        assertThat(preview.nextStepAssessmentStartYear()).isEqualTo("2008");
        assertThat(preview.note()).contains("未引起执行工资职务层次变化但引起级别变化");
        assertThat(preview.note()).contains("超过下一级别一个工资档差");
    }

    @Test
    void educationPromotionAppliesStandardStepWhenSameLevelBelowStandardTreatment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2007", "07", "正常档次", "01B0", "科员", "24", "1", "2006", "2006", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(3270));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(3270)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200707"))
                .thenReturn(Optional.of(new EducationPromotionSource("41", "硕士研究生", "2007.07")));
        when(repository.findEducationRegularizationStandard("01B0", "41"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("41", "硕士研究生", "01B0", "科员", "24", "3")));
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 24 && step == 1) {
                return 400;
            }
            if (level == 24 && step == 3) {
                return 620;
            }
            if (level == 24 && step == 4) {
                return 700;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 24 && step == 1) {
                return 400;
            }
            if (level == 24 && step == 3) {
                return 620;
            }
            if (level == 24 && step == 4) {
                return 700;
            }
            return (30 - level) * 100 + step * 10;
        });

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.promotedLevel()).isEqualTo("24");
        assertThat(preview.promotedStep()).isEqualTo("3");
        assertThat(preview.note()).contains("级别未变，按定级标准调整档次");
        assertThat(preview.note()).doesNotContain("档次均未变");
    }

    @Test
    void educationPromotionPreservesLevelAssessmentYearWhenPositionHierarchyChangesByOneLevel() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01C0", "科员", "25", "2", "2000", "2010", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9004));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9004)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2008.07")));
        when(repository.findEducationRegularizationStandard("01C0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "24", "1")));
        when(repository.positionSalary(eq("01C0"), anyString())).thenReturn(100);
        when(repository.positionSalary(eq("01B0"), anyString())).thenReturn(300);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 25 && step == 2) {
                return 400;
            }
            if (level == 24 && step == 1) {
                return 500;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 25 && step == 2) {
                return 400;
            }
            if (level == 24 && step == 1) {
                return 500;
            }
            return (30 - level) * 100 + step * 10;
        });

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.promotedPositionCode()).isEqualTo("01B0");
        assertThat(preview.promotedLevel()).isEqualTo("24");
        assertThat(preview.nextLevelAssessmentStartYear()).isEqualTo("2000");
        assertThat(preview.note()).contains("执行工资职务层次变动且仅晋升一个级别");
    }

    @Test
    void educationPromotionResetsLevelAssessmentYearWhenPositionHierarchyChangesByTwoOrMoreLevels() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01C0", "科员", "27", "2", "2000", "2010", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9005));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9005)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2008.07")));
        when(repository.findEducationRegularizationStandard("01C0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "1")));
        when(repository.positionSalary(eq("01C0"), anyString())).thenReturn(100);
        when(repository.positionSalary(eq("01B0"), anyString())).thenReturn(300);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 27 && step == 2) {
                return 300;
            }
            if (level == 25 && step == 1) {
                return 500;
            }
            return (30 - level) * 100 + step * 10;
        });
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            if (level == 27 && step == 2) {
                return 300;
            }
            if (level == 25 && step == 1) {
                return 500;
            }
            return (30 - level) * 100 + step * 10;
        });

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.promotedPositionCode()).isEqualTo("01B0");
        assertThat(preview.promotedLevel()).isEqualTo("25");
        assertThat(preview.nextLevelAssessmentStartYear()).isEqualTo("2008");
        assertThat(preview.note()).contains("执行工资职务层次变动且晋升级别达到两级及以上");
    }

    @Test
    void educationPromotionPreservesStepAssessmentYearWhenOnlyPositionHierarchyChanges() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01C0", "办事员", "25", "4", "2000", "2010", "2004.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9006));
        when(repository.countPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9006)).thenReturn(Optional.of(latest));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2008.07")));
        when(repository.findEducationRegularizationStandard("01C0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "4")));
        when(repository.positionSalary(eq("01C0"), anyString())).thenReturn(100);
        when(repository.positionSalary(eq("01B0"), anyString())).thenReturn(300);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });

        EducationPromotionPreview preview = service.educationPromotionPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.promotedPositionCode()).isEqualTo("01B0");
        assertThat(preview.promotedLevel()).isEqualTo("25");
        assertThat(preview.promotedStep()).isEqualTo("4");
        assertThat(preview.nextLevelAssessmentStartYear()).isEqualTo("2000");
        assertThat(preview.nextStepAssessmentStartYear()).isEqualTo("2010");
        assertThat(preview.note()).contains("仅执行工资职务层次变化");
        assertThat(preview.note()).contains("档次考核年限沿用原起算年");
    }

    @Test
    void regularizationPreviewUsesStandardPositionWhenRegularizationAppointmentMissing() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2007", "07", "见习", "01FF", "见习期", "0", "0", "2007", "2007", "2007.01");

        when(accessControlService.organizationScope(any())).thenReturn(new OrganizationScope(true, Set.of("001")));
        when(repository.findProbationPersonnelUidsWithCurrentPayroll(any(), any(), any(), any())).thenReturn(List.of(9100));
        when(repository.countProbationPersonnelWithCurrentPayroll(any(), any(), any())).thenReturn(1L);
        when(repository.findLatestHistory(9100)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2007.07");
        when(repository.findPositionAtPeriod("001", "00040", "200707")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "200707")).thenReturn(Optional.empty());
        when(repository.findLatestEducationForPromotion("001", "00040", "200707"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2007.07")));
        when(repository.findEducationRegularizationStandard(anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });

        RegularizationPreview preview = service.regularizationPreviews("001", null, PageRequest.of(0, 10))
                .content()
                .getFirst();

        assertThat(preview.eligible()).isTrue();
        assertThat(preview.regularPositionCode()).isEqualTo("01B0");
        assertThat(preview.regularLevel()).isEqualTo("25");
        assertThat(preview.note()).contains("按转正定级标准确认执行工资职务");
    }

    @Test
    void wageProjectionUsesRegularizationStandardWhenRegularizationAppointmentMissing() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2007", "08", "转正定级", "01B0", "科员", "25", "2", "2007", "2007", "2007.07");

        when(repository.findLatestHistory(9101)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2007.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findPositionAtPeriod("001", "00040", "200707")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "200707")).thenReturn(Optional.empty());
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200707"), org.mockito.ArgumentMatchers.anySet()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Set<String> prefixes = invocation.getArgument(3);
                    if (prefixes.contains("01")) {
                        return Optional.of(new PositionChangeCandidate("01B0", "科员", "2007.07"));
                    }
                    return Optional.empty();
                });
        when(repository.findLatestEducationForPromotion("001", "00040", "200707"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2007.07")));
        when(repository.findEducationRegularizationStandard(anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(9101, "200808");

        assertThat(preview.positionCode()).isEqualTo("01B0");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("按转正定级标准确认执行工资职务"));
    }

    @Test
    void wageProjectionMatchesReformStandardWhenNoPositionBeforeReformUsesRegularizationTenure() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01B0", "科员", "25", "2", "2006", "2006", "2007.07");

        when(repository.findLatestHistory(9102)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findPositionAtPeriod("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "1998.10")));
        when(repository.findEducationRegularizationStandard(anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard("01B0", 9, 15)).thenReturn(Optional.empty());
        when(repository.findNearestWageReformStandard("01B0", 9, 15))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 5, 10, 10, 20, "25", "2")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01B0", "科员", "1998.10", 0)));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(9102, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("按转正定级标准确认执行工资职务"));
        org.mockito.Mockito.verify(repository).findNearestWageReformStandard("01B0", 9, 15);
    }

    @Test
    void wageProjectionUsesProbationSalaryWhenStillOnProbationAtWageReform() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01FF", "见习期", "0", "0", "2006", "2006", "2006.01");

        when(repository.findLatestHistory(3266)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2007.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestInternPositionBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("01FF", "见习期", "2006.01")));
        when(repository.findPositionAtOrBefore("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("01B0", "科员", "2006.09")));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01B0", "科员", "2006.09")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findLatestEducationForPromotion("001", "00040", "200607"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2005.07")));
        when(repository.findInternSalaryStandard(eq("01FF"), eq("31"), eq("本科"), eq("200607")))
                .thenReturn(Optional.of(new InternSalaryStandard("200607", "31", "本科", "01B0", "科员", "2", "25", 720, 820)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(1);
        when(repository.positionSalary(anyString(), anyString())).thenReturn(0);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3266, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("套改时仍处试用期")
                && line.contains("见习工资标准")
                && line.contains("720")
                && line.contains("见习期"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("按转正定级标准确认执行工资职务"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2006.09") && line.contains("科员"));
        org.mockito.Mockito.verify(repository).findLatestInternPositionBefore("001", "00040", "200607");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findWageReformStandard(anyString(), anyInt(), anyInt());
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce())
                .findInternSalaryStandard("01FF", "31", "本科", "200607");
    }

    @Test
    void wageProjectionUsesProbationSalaryWhenRegularizationShortlyAfterReformWithoutPreReformAppointment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = history("2006", "07", "套改", "01FF", "见习期", "0", "0", "2006", "2006", "2006.01");

        when(repository.findLatestHistory(3266)).thenReturn(Optional.of(reform));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2006.09");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(reform));
        when(repository.findLatestInternPositionBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.findLatestPositionBefore(eq("001"), eq("00040"), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findPositionAtPeriod("001", "00040", "200609")).thenReturn(Optional.of(
                new PositionChangeCandidate("0191", "正科级非领导职务", "2006.09")));
        when(repository.findPositionAtOrBefore(eq("001"), eq("00040"), eq("200607")))
                .thenReturn(Optional.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2006.09")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findLatestEducationForPromotion(eq("001"), eq("00040"), eq("200607")))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2005.07")));
        when(repository.findLatestEducationForPromotion(eq("001"), eq("00040"), eq("200609")))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2005.07")));
        when(repository.findEducationRegularizationStandard(org.mockito.ArgumentMatchers.anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.findInternSalaryStandard(eq("01FF"), eq("31"), eq("本科"), eq("200607")))
                .thenReturn(Optional.of(new InternSalaryStandard("200607", "31", "本科", "01B0", "科员", "2", "25", 680, 780)));
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(820);
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(1);
        when(repository.positionSalary(anyString(), anyString())).thenReturn(0);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3266, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("套改时仍处试用期")
                && line.contains("680"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("转正定级")
                && line.contains("采用转正任职")
                && line.contains("0191")
                && line.contains("25"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("职务变化")
                && line.contains("0191"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2006.07 及以后转正")
                && line.contains("按学历转正定级标准确定起点")
                && line.contains("0191"));
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findWageReformStandard(anyString(), anyInt(), anyInt());
    }

    @Test
    void wageProjectionForInstitutionPersonnelIgnoresAdministrativePositions() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2024", "12", "月末结转", "0801", "技工二级", "", "8", "2020", "2020", "2010.06");

        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2005.06");
        when(repository.findPositionAtOrBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.findLatestInternPositionBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Set<String> prefixes = invocation.getArgument(3);
                    if (prefixes.contains("07") && !prefixes.contains("01")) {
                        return Optional.of(new PositionChangeCandidate("0801", "技工二级", "2008.01"));
                    }
                    return Optional.of(new PositionChangeCandidate("01B0", "科员", "2003.01"));
                });
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(12);
        when(repository.findWageReformStandard(eq("0801"), anyInt(), eq(12)))
                .thenReturn(Optional.of(new WageReformStandard("0801", 0, 99, 0, 99, "0", "6")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("01B0", "科员", "2010.01"),
                        new PositionChangeCandidate("0802", "技工一级", "2015.06")));
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findBasicSalaryStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        Method wageProjectionStart = PayrollService.class.getDeclaredMethod("wageProjectionStart", PayrollHistorySnapshot.class);
        wageProjectionStart.setAccessible(true);
        Object start = wageProjectionStart.invoke(service, latest);

        assertThat(start).isNotNull();
        assertThat(start.getClass().getMethod("eligible").invoke(start)).isEqualTo(true);
        assertThat(start.getClass().getMethod("positionCode").invoke(start)).isEqualTo("0801");
        assertThat(start.getClass().getMethod("note").invoke(start).toString())
                .contains("事业")
                .contains("忽略行政职务");

        Method wageProjectionEvents = PayrollService.class.getDeclaredMethod(
                "wageProjectionEvents",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                PayrollHistorySnapshot.class);
        wageProjectionEvents.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> events = (List<Object>) wageProjectionEvents.invoke(
                service, "001", "00040", "200607", "202412", "2005.06", latest);

        assertThat(events).hasSize(1);
        Object event = events.getFirst();
        assertThat(event.getClass().getMethod("position").invoke(event)).isNotNull();
        PositionChangeCandidate position = (PositionChangeCandidate) event.getClass().getMethod("position").invoke(event);
        assertThat(position.positionCode()).isEqualTo("0802");
    }

    @Test
    void wageProjectionTreatsInstitutionPersonnelByAppointedPositionNotZwbm2() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2024", "12", "月末结转", "01B0", "科员", "25", "2", "2020", "2020", "2010.06");

        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2005.06");
        when(repository.findPositionAtOrBefore("001", "00040", "202412"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0801", "技工二级", "2008.01")));
        when(repository.findPositionAtOrBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.findLatestInternPositionBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("202412"), org.mockito.ArgumentMatchers.anySet()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Set<String> prefixes = invocation.getArgument(3);
                    if (prefixes.contains("07") && !prefixes.contains("01")) {
                        return Optional.of(new PositionChangeCandidate("0801", "技工二级", "2008.01"));
                    }
                    return Optional.empty();
                });

        Method wageProjectionStart = PayrollService.class.getDeclaredMethod("wageProjectionStart", PayrollHistorySnapshot.class);
        wageProjectionStart.setAccessible(true);
        Object start = wageProjectionStart.invoke(service, latest);

        assertThat(start).isNotNull();
        assertThat(start.getClass().getMethod("eligible").invoke(start)).isEqualTo(true);
        assertThat(start.getClass().getMethod("positionCode").invoke(start)).isEqualTo("0801");
        assertThat(start.getClass().getMethod("period").invoke(start)).isEqualTo("200801");
        assertThat(start.getClass().getMethod("note").invoke(start).toString())
                .contains("事业")
                .contains("忽略此前行政职务");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce())
                .findPositionAtOrBefore("001", "00040", "202412");
    }

    @Test
    void wageProjectionUsesProbationSalaryWhenRegularizationIn2007WithoutPreReformAppointment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = history("2006", "07", "套改", "01FF", "见习期", "0", "0", "2006", "2006", "2006.01");

        when(repository.findLatestHistory(2806)).thenReturn(Optional.of(reform));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2007.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(reform));
        when(repository.findLatestInternPositionBefore("001", "00040", "200607")).thenReturn(Optional.empty());
        when(repository.findLatestPositionBefore(eq("001"), eq("00040"), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01B0", "科员", "2006.03")));
        when(repository.findLatestPositionBefore(eq("001"), eq("00040"), eq("200707"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01B0", "科员", "2006.03")));
        when(repository.findPositionAtPeriod("001", "00040", "200707")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore(eq("001"), eq("00040"), eq("200607"))).thenReturn(Optional.empty());
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findLatestEducationForPromotion(eq("001"), eq("00040"), eq("200607")))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2005.07")));
        when(repository.findLatestEducationForPromotion(eq("001"), eq("00040"), eq("200707")))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2005.07")));
        when(repository.findEducationRegularizationStandard(org.mockito.ArgumentMatchers.anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.findInternSalaryStandard(eq("01FF"), eq("31"), eq("本科"), eq("200607")))
                .thenReturn(Optional.of(new InternSalaryStandard("200607", "31", "本科", "01B0", "科员", "2", "25", 700, 800)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(1);
        when(repository.positionSalary(anyString(), anyString())).thenReturn(0);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(2806, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("套改时仍处试用期")
                && line.contains("700"));
        assertThat(preview.stepDetails()).isNotEmpty();
        assertThat(preview.stepDetails().getFirst().components().stream()
                .filter(component -> "JBGZSE2".equals(component.fieldName()))
                .findFirst()
                .orElseThrow()
                .caption()).isEqualTo("试用期工资");
        assertThat(preview.stepDetails().getFirst().components().stream()
                .filter(component -> "JBGZSE2".equals(component.fieldName()))
                .findFirst()
                .orElseThrow()
                .amount()).isEqualByComparingTo(new BigDecimal("700"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("200707 转正定级"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2006.07 及以后转正")
                && line.contains("按转正定级标准确认执行工资职务")
                && line.contains("科员"));
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findWageReformStandard(anyString(), anyInt(), anyInt());
    }

    @Test
    void wageProjectionUsesPersonnelEducationWhenRegularizationAppointmentMissing() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01FF", "见习期", "25", "2", "2006", "2006", "2007.07");

        when(repository.findLatestHistory(9103)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findPositionAtPeriod("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findLatestEducationForPromotion(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.findPersonnelEducationCode("001", "00040")).thenReturn(Optional.of("31"));
        when(repository.findEducationRegularizationStandard(anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard("01B0", 9, 15))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 5, 10, 10, 20, "25", "2")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01B0", "科员", "1998.10", 0)));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(9103, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("按转正定级标准确认执行工资职务"));
        org.mockito.Mockito.verify(repository).findPersonnelEducationCode("001", "00040");
    }

    @Test
    void wageProjectionUsesAdministrativePrefixWhenMissingRegularizationAppointmentButHasPreReformPosition() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard(eq("0190"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4")));
        when(repository.findNearestWageReformStandard(eq("0190"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3259, "200801");

        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("未能按基本信息匹配 2006 套改标准"));
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("按转正定级标准确认执行工资职务"));
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findEducationRegularizationStandard(anyString(), anyString());
        org.mockito.Mockito.verify(repository).findWageReformStandard(eq("0190"), anyInt(), eq(15));
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findWageReformStandard(eq("01B0"), anyInt(), anyInt());
    }

    @Test
    void wageProjectionKeepsCurrentReformStepWhenHigherRankAlreadyMeetsEducationSalary() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0191", "正科级非领导职务", "21", "3", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0191", "正科级非领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0191", "正科级非领导职务", "2004.01", 0),
                        new WageReformPosition("0190", "副科级领导职务", "1998.10", 0)));
        when(repository.findStoredWageReformSnapshot(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard(eq("0191"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0191", 0, 99, 0, 99, "21", "3")));
        when(repository.findNearestWageReformStandard(eq("0191"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0191", 0, 99, 0, 99, "21", "3")));
        when(repository.findWageReformStandard(eq("0190"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findNearestWageReformStandard(eq("0190"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findLatestEducationForPromotion("001", "00040", "199810"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "1998.10")));
        when(repository.findEducationRegularizationStandard(anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "3")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3259, "200801");

        assertThat(preview.levelStepDisplay()).isEqualTo("21-3");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("不作低一职务套改调整"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("已比照学历"));
    }

    @Test
    void wageProjectionUsesStoredWageReformLowerPositionAndEducationForUid3259StyleCase() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.findStoredWageReformSnapshot("001", "00040"))
                .thenReturn(Optional.of(new StoredWageReformSnapshot(
                        Optional.of(new WageReformPosition("01B0", "科员", "1998.10", 0)),
                        Optional.of(new EducationPromotionSource("31", "本科", "1998.10")),
                        "21",
                        "4",
                        "")));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard(eq("0190"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4")));
        when(repository.findNearestWageReformStandard(eq("0190"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "21", "4")));
        when(repository.findWageReformStandard(eq("01B0"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2")));
        when(repository.findNearestWageReformStandard(eq("01B0"), anyInt(), eq(15)))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2")));
        when(repository.findLatestEducationForPromotion("001", "00040", "199810"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "1998.10")));
        when(repository.findEducationRegularizationStandard(anyString(), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "3")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());
        when(repository.performanceAllowance(anyString(), anyString(), anyString())).thenReturn(BigDecimal.ZERO);
        when(repository.subsidyAllowance(anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.retainedAllowance(anyString())).thenReturn(0);
        when(repository.mapPositionSalaryCode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.positionSalary(anyString(), anyString())).thenReturn(380);
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);

        WageProjectionPreview preview = service.wageProjection(3259, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("已比照原任低一职务"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("已比照学历"));
        assertThat(preview.stepDetails()).isNotEmpty();
        assertThat(preview.stepDetails().getFirst().components()).isNotEmpty();
        assertThat(preview.stepDetails().getLast().description()).contains("目标年月工资明细");
    }

    @Test
    void wageProjectionUsesAdministrativePrefixForRegularizationStandardWhenMissingPreReformAppointment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "2105", "警员", "8", "1", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(9104)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findPositionAtPeriod("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "199810")).thenReturn(Optional.empty());
        when(repository.findLatestEducationForPromotion("001", "00040", "199810"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "1998.10")));
        when(repository.findEducationRegularizationStandard("01B0", "31"))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "2")));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2")));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2")));
        when(repository.findFirstWageReformStandardForPosition(anyString()))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01B0", "科员", "1998.10", 0)));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.positionSalary(anyString(), anyString())).thenReturn(100);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(9104, "200801");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("按转正定级标准确认执行工资职务"));
        org.mockito.Mockito.verify(repository).findEducationRegularizationStandard("01B0", "31");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findEducationRegularizationStandard(eq("2105"), eq("31"));
    }

    @Test
    void wageProjectionSkipsReformLevelRollingIn2008AfterClerkHierarchyChangeWithoutLevelChange() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "01B0", "科员", "25", "4", "2006", "2010", "1998.10");

        when(repository.findLatestHistory(3254)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01C0", "办事员", "1998.10")));
        when(repository.findWageReformStandard(eq("01C0"), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01C0", 0, 99, 0, 99, "25", "4")));
        when(repository.findWageReformStandard(eq("01B0"), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "24", "4")));
        when(repository.findNearestWageReformStandard(eq("01C0"), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01C0", 0, 99, 0, 99, "25", "4")));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(15);
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01C0", "办事员", "1998.10", 0)));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(new EducationPromotionSource("31", "本科", "2007.07")));
        when(repository.findLatestEducationForPromotion(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "2007.07")));
        when(repository.findEducationRegularizationStandard(eq("01C0"), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "25", "4")));
        when(repository.positionSalary(eq("01C0"), anyString())).thenReturn(100);
        when(repository.positionSalary(eq("01B0"), anyString())).thenReturn(300);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int startYear = invocation.getArgument(2);
            int endYear = invocation.getArgument(3);
            if (startYear == endYear && startYear == 2007) {
                return 1;
            }
            return 0;
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of(2007));

        WageProjectionPreview preview = service.wageProjection(3254, "201001");

        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2008 年") && line.contains("套改后级别滚动"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("200708") && line.contains("学历变动"));
        assertThat(preview.level()).isEqualTo("25");
        assertThat(preview.stepOrSalaryLevel()).isEqualTo("4");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findWageReformStandard(eq("01B0"), anyInt(), anyInt());
    }

    private static int gradeSalaryTwentyFiveTwoToTwentyFourThree(Object levelValue, Object stepValue) {
        int level = Integer.parseInt(levelValue.toString().trim());
        int step = Integer.parseInt(stepValue.toString().trim());
        if (level == 25 && step == 2) {
            return 500;
        }
        if (level == 24 && step == 1) {
            return 520;
        }
        if (level == 24 && step == 2) {
            return 540;
        }
        if (level == 24 && step == 3) {
            return 620;
        }
        return (30 - level) * 100 + step * 10;
    }

    private static int gradeSalaryForStepDifferenceTest(Object levelValue, Object stepValue) {
        int level = Integer.parseInt(levelValue.toString().trim());
        int step = Integer.parseInt(stepValue.toString().trim());
        if (level == 23 && step == 2) {
            return 100;
        }
        if (level == 22 && step == 1) {
            return 105;
        }
        if (level == 22 && step == 2) {
            return 113;
        }
        return (30 - level) * 100 + step * 10;
    }

    @Test
    void wageProjectionStepUsesSalaryStandardAtPeriodNotLatestRecord() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = historyWithIdAndStandards(
                "latest-id", "2024", "07", "正常档次", "0190", "正科级领导职务", "21", "2",
                "2024", "2024", "2004.01", "202407", "202407", 8000);
        PayrollHistorySnapshot reform = historyWithIdAndStandards(
                "reform-id", "2006", "07", "套改", "0190", "正科级领导职务", "22", "3",
                "2006", "2006", "2004.01", "200607", "200607", 380, 954, 1334);

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findFirstWageReformStandardForPosition(anyString()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.latestPositionSalaryStandardAtOrBefore("200607")).thenReturn("200607");
        when(repository.latestBasicSalaryStandardAtOrBefore("200607")).thenReturn("200607");
        when(repository.latestBasicSalaryStandardAtOrBefore("202407")).thenReturn("202407");
        when(repository.latestAllowanceStandardAtOrBefore(eq("200607"), anyString(), anyString())).thenReturn("200607");
        when(repository.latestRankAllowanceStandardAtOrBefore(anyString())).thenReturn("");
        when(repository.mapPositionSalaryCode("0190")).thenReturn("0190");
        when(repository.positionSalary(eq("0190"), eq("200607"))).thenReturn(380);
        when(repository.positionSalary(eq("0190"), eq("202407"))).thenReturn(9999);
        when(repository.positionGradeSalary(eq("0190"), eq("3"), eq("0"), anyString())).thenReturn(0);
        when(repository.civilServantGradeSalary(eq("22"), eq("3"), eq("0"), eq("200607"))).thenReturn(954);
        when(repository.civilServantGradeSalary(eq("22"), eq("3"), eq("0"), eq("202407"))).thenReturn(9999);
        when(repository.performanceAllowance(anyString(), anyString(), anyString())).thenReturn(BigDecimal.ZERO);
        when(repository.subsidyAllowance(anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.retainedAllowance(anyString())).thenReturn(0);
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(3259, "200607");

        assertThat(preview.stepDetails()).isNotEmpty();
        assertThat(preview.stepDetails().getFirst().salaryStandardYearMonth()).isEqualTo("200607");
        assertThat(preview.stepDetails().getFirst().allowanceStandardYearMonth()).isEqualTo("200607");
        assertThat(preview.stepDetails().getFirst().total()).isEqualByComparingTo(BigDecimal.valueOf(1334));
    }

    @Test
    void wageProjectionKeepsTbndOnPromotionButUpdatesOnStandardAdjustment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.latestPositionSalaryStandardAtOrBefore(anyString())).thenReturn("200607");
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "201201"))
                .thenReturn(List.of("201207"));
        when(repository.hasBasicSalaryStandardForSource("201207", "GRADE")).thenReturn(true);
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.mapPositionSalaryCode("0190")).thenReturn("0190");
        when(repository.positionSalary(eq("0190"), anyString())).thenReturn(380);
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), eq("200607"))).thenReturn(900);
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), eq("201207"))).thenReturn(1200);
        when(repository.performanceAllowance(anyString(), anyString(), anyString())).thenReturn(BigDecimal.ZERO);
        when(repository.subsidyAllowance(anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.retainedAllowance(anyString())).thenReturn(0);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(8794, "201201");

        List<WageProjectionStepDetail> steps = preview.stepDetails();
        WageProjectionStepDetail beforeStandard = steps.stream()
                .filter(step -> step.description() != null && step.description().contains("晋升级别"))
                .findFirst()
                .orElseThrow();
        WageProjectionStepDetail afterStandard = steps.stream()
                .filter(step -> step.description() != null && step.description().contains("调标"))
                .findFirst()
                .orElseThrow();

        assertThat(beforeStandard.salaryStandardYearMonth()).isEqualTo("200607");
        assertThat(afterStandard.salaryStandardYearMonth()).isEqualTo("201207");
    }

    @Test
    void wageProjectionResolvesAllowanceStandardAtEachHistoricalStep() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.latestPositionSalaryStandardAtOrBefore(anyString())).thenReturn("200607");
        when(repository.latestBasicSalaryStandardAtOrBefore(anyString())).thenReturn("200607");
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "201307"))
                .thenReturn(List.of());
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findAllowanceStandardPeriodsBetween(eq("001"), eq("200607"), eq("201307")))
                .thenReturn(List.of("201007"));
        when(repository.hasAllowanceStandard("201007", "001", "0190")).thenReturn(true);
        when(repository.mapPositionSalaryCode("0190")).thenReturn("0190");
        when(repository.positionSalary(eq("0190"), anyString())).thenReturn(380);
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.civilServantGradeSalary(anyString(), anyString(), anyString(), eq("200607"))).thenReturn(900);
        when(repository.retainedAllowance(anyString())).thenReturn(0);
        when(repository.latestAllowanceStandardAtOrBefore(eq("200607"), eq("001"), eq("0190"))).thenReturn("200607");
        when(repository.latestAllowanceStandardAtOrBefore(eq("201307"), eq("001"), eq("0190"))).thenReturn("201007");
        when(repository.performanceAllowance("001", "0190", "200607")).thenReturn(BigDecimal.ZERO);
        when(repository.performanceAllowance("001", "0190", "201007")).thenReturn(BigDecimal.valueOf(1100));
        when(repository.subsidyAllowance("001", "0190", "200607")).thenReturn(0);
        when(repository.subsidyAllowance("001", "0190", "201007")).thenReturn(320);
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(8794, "201307");

        WageProjectionStepDetail targetStep = preview.stepDetails().stream()
                .filter(step -> "201307".equals(step.period()))
                .findFirst()
                .orElseThrow();
        assertThat(targetStep.allowanceStandardYearMonth()).isEqualTo("201007");
        assertThat(componentAmount(targetStep.components(), "DFBT2")).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(componentAmount(targetStep.components(), "SDBT")).isEqualByComparingTo(BigDecimal.valueOf(320));
    }

    private static BigDecimal componentAmount(List<PayrollPreviewComponent> components, String fieldName) {
        return components.stream()
                .filter(component -> fieldName.equals(component.fieldName()))
                .map(PayrollPreviewComponent::amount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    @Test
    void projectionHistoryAuditUsesRecordSalaryStandardForWageReformMonth() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = historyWithIdAndStandards(
                "reform-id", "2006", "07", "套改", "0190", "正科级领导职务", "22", "3",
                "2006", "2006", "2004.01", "200607", "200607", 380, 954, 1334);
        PayrollHistorySnapshot latest = historyWithIdAndStandards(
                "latest-id", "2021", "01", "正常档次", "0190", "正科级领导职务", "21", "2",
                "2021", "2021", "2004.01", "201607", "201607", 8000);

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findFirstWageReformStandardForPosition(anyString()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findCalculationFields()).thenReturn(List.of(
                payrollField("zwgzse2", "职务工资", false),
                payrollField("jbgzse2", "级别工资", false),
                payrollField("dfbt2", "地方补贴", true),
                payrollField("blfb2", "保留补贴", true)));
        when(repository.findHistoryValuesById(eq("reform-id"), eq("001"), eq("00040"), eq("2006"), eq("07")))
                .thenReturn(Optional.of(Map.of(
                        "zwgzse2", 380,
                        "jbgzse2", 954,
                        "dfbt2", 0,
                        "blfb2", 0,
                        "hj2", 1334)));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("zwgzse2"))).thenReturn(new BigDecimal("380"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("jbgzse2"))).thenReturn(new BigDecimal("954"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("dfbt2"))).thenReturn(BigDecimal.ZERO);
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("blfb2"))).thenReturn(BigDecimal.ZERO);
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("hj2"))).thenReturn(new BigDecimal("1334"));
        when(repository.mapPositionSalaryCode("0190")).thenReturn("0190");
        when(repository.positionSalary(eq("0190"), anyString())).thenAnswer(invocation -> {
            String standard = invocation.getArgument(1);
            return "200607".equals(standard) ? 380 : 9999;
        });
        when(repository.positionGradeSalary(eq("0190"), eq("3"), eq("0"), anyString())).thenReturn(0);
        when(repository.civilServantGradeSalary(eq("22"), eq("3"), eq("0"), anyString())).thenAnswer(invocation -> {
            String standard = invocation.getArgument(3);
            return "200607".equals(standard) ? 954 : 9999;
        });
        when(repository.performanceAllowance(anyString(), anyString(), anyString())).thenReturn(BigDecimal.ZERO);
        when(repository.subsidyAllowance(anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.retainedAllowance(anyString())).thenReturn(0);
        when(repository.floatingSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });

        List<PayrollHistoryProjectionAudit> audits = service.projectionHistoryAudits(3259);
        PayrollHistoryProjectionAudit reformAudit = audits.stream()
                .filter(item -> "reform-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();

        assertThat(reformAudit.projectedTotal()).isEqualByComparingTo(BigDecimal.valueOf(1334));
        assertThat(reformAudit.matched()).isTrue();
    }

    @Test
    void projectionHistoryAuditResolvesAllowanceStandardAtHistoryPeriod() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = historyWithIdAndStandards(
                "reform-id", "2006", "07", "套改", "0190", "正科级领导职务", "22", "3",
                "2006", "2006", "2004.01", "200607", "200607", 380, 954, 1334);
        PayrollHistorySnapshot allowanceRecord = historyWithIdAndStandards(
                "allowance-id", "2013", "07", "正常档次", "0190", "正科级领导职务", "22", "3",
                "2006", "2006", "2004.01", "200607", "200607", 380, 954, 2554);
        PayrollHistorySnapshot latest = historyWithIdAndStandards(
                "latest-id", "2021", "01", "正常档次", "0190", "正科级领导职务", "21", "2",
                "2021", "2021", "2004.01", "201607", "201607", 8000);

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest, allowanceRecord, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findFirstWageReformStandardForPosition(anyString()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findCalculationFields()).thenReturn(List.of(
                payrollField("zwgzse2", "职务工资", false),
                payrollField("jbgzse2", "级别工资", false),
                payrollField("dfbt2", "生活性补贴", true),
                payrollField("sdbt", "工作性津贴", true),
                payrollField("blfb2", "保留补贴", true)));
        when(repository.findHistoryValuesById(eq("allowance-id"), eq("001"), eq("00040"), eq("2013"), eq("07")))
                .thenReturn(Optional.of(Map.of(
                        "zwgzse2", 380,
                        "jbgzse2", 954,
                        "dfbt2", 1100,
                        "sdbt", 320,
                        "blfb2", 0,
                        "hj2", 2554)));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("zwgzse2"))).thenReturn(new BigDecimal("380"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("jbgzse2"))).thenReturn(new BigDecimal("954"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("dfbt2"))).thenReturn(new BigDecimal("1100"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("sdbt"))).thenReturn(new BigDecimal("320"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("blfb2"))).thenReturn(BigDecimal.ZERO);
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("hj2"))).thenReturn(new BigDecimal("2554"));
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());
        when(repository.findBasicSalaryStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findAllowanceStandardPeriodsBetween(eq("001"), anyString(), anyString()))
                .thenReturn(List.of("201007"));
        when(repository.hasAllowanceStandard("201007", "001", "0190")).thenReturn(true);
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.latestPositionSalaryStandardAtOrBefore(anyString())).thenReturn("200607");
        when(repository.latestBasicSalaryStandardAtOrBefore(anyString())).thenReturn("200607");
        when(repository.findStoredWageReformSnapshot(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.mapPositionSalaryCode("0190")).thenReturn("0190");
        when(repository.positionSalary(eq("0190"), anyString())).thenReturn(380);
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.civilServantGradeSalary(eq("22"), eq("3"), eq("0"), anyString())).thenReturn(954);
        when(repository.latestAllowanceStandardAtOrBefore(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String period = invocation.getArgument(0);
            return period.compareTo("201007") >= 0 ? "201007" : "200607";
        });
        when(repository.performanceAllowance("001", "0190", "200607")).thenReturn(BigDecimal.ZERO);
        when(repository.performanceAllowance("001", "0190", "201007")).thenReturn(BigDecimal.valueOf(1100));
        when(repository.subsidyAllowance("001", "0190", "200607")).thenReturn(0);
        when(repository.subsidyAllowance("001", "0190", "201007")).thenReturn(320);
        when(repository.retainedAllowance(anyString())).thenReturn(0);
        when(repository.floatingSalary(anyString(), anyString(), anyString(), anyString())).thenReturn(0);
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });

        List<PayrollHistoryProjectionAudit> audits = service.projectionHistoryAudits(3259);
        PayrollHistoryProjectionAudit allowanceAudit = audits.stream()
                .filter(item -> "allowance-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();

        assertThat(allowanceAudit.projectionEligible()).isTrue();
        assertThat(allowanceAudit.stepDetails()).isNotEmpty();
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce())
                .performanceAllowance("001", "0190", "201007");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.atLeastOnce())
                .subsidyAllowance("001", "0190", "201007");
        PayrollComponentDifference dfbt2Difference = allowanceAudit.componentDifferences().stream()
                .filter(item -> "DFBT2".equals(item.fieldName()))
                .findFirst()
                .orElseThrow();
        PayrollComponentDifference sdbtDifference = allowanceAudit.componentDifferences().stream()
                .filter(item -> "SDBT".equals(item.fieldName()))
                .findFirst()
                .orElseThrow();
        assertThat(dfbt2Difference.calculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(1100));
        assertThat(sdbtDifference.calculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(320));
    }

    @Test
    void projectionHistoryAuditMatchesSameMonthRollingBeforeStepPromotion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = historyWithId(
                "reform-id", "2006", "07", "套改", "0190", "正科级领导职务", "22", "3", "2006", "2006", "2004.01");
        PayrollHistorySnapshot levelRecord = historyWithId(
                "level-id", "2010", "01", "正常级别", "0190", "正科级领导职务", "21", "1", "2010", "2010", "2004.01");
        PayrollHistorySnapshot stepRecord = historyWithId(
                "step-id", "2010", "01", "正常档次", "0190", "正科级领导职务", "21", "2", "2010", "2010", "2004.01");

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(stepRecord));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(stepRecord, levelRecord, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0190", "正科级领导职务", "2004.01", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findFirstWageReformStandardForPosition(anyString()))
                .thenReturn(Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "22", "3")));
        when(repository.findPositionAtOrBefore("001", "00040", "201001"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findCalculationFields()).thenReturn(List.of());
        when(repository.findHistoryValuesById(anyString(), eq("001"), eq("00040"), anyString(), anyString()))
                .thenReturn(Optional.of(Map.of("hj2", 0)));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        stubCivilServantGradeSalary(repository);
        when(repository.gradeSalary(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            int level = Integer.parseInt(invocation.getArgument(0));
            int step = Integer.parseInt(invocation.getArgument(1));
            return (30 - level) * 100 + step * 10;
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            if (start == 2006 && end == 2009) {
                return 5;
            }
            if (start == end && end == 2009) {
                return 1;
            }
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });
        when(repository.highestGradeStepForLevel(anyString())).thenReturn(14);

        List<PayrollHistoryProjectionAudit> audits = service.projectionHistoryAudits(3259);
        PayrollHistoryProjectionAudit levelAudit = audits.stream()
                .filter(item -> "level-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();
        PayrollHistoryProjectionAudit stepAudit = audits.stream()
                .filter(item -> "step-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();

        assertThat(levelAudit.structureMismatches()).noneMatch(line -> line.startsWith("档次"));
        assertThat(stepAudit.structureMismatches()).noneMatch(line -> line.startsWith("档次"));
        WageProjectionStepDetail stepDetail = stepAudit.stepDetails().stream()
                .filter(step -> "201001".equals(step.period()))
                .reduce((first, second) -> second)
                .orElseThrow();
        assertThat(stepAudit.projectedTotal()).isEqualByComparingTo(stepDetail.total());
    }

    @Test
    void wageProjectionUsesPositionAllowanceRowWhenStandardPeriodHasNoMatchingRow() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2010", "01", "正常档次", "0191", "正科级非领导职务", "21", "1", "2008", "2008", "2007.07");

        stubReformStart(repository, latest, new WageReformStandard("0191", 0, 99, 0, 99, "21", "1"));
        when(repository.findAllowanceStandardPeriodsBetween(eq("001"), eq("200607"), eq("200901")))
                .thenReturn(List.of("200707", "200810"));
        when(repository.hasAllowanceStandard("200810", "001", "0191")).thenReturn(true);
        when(repository.hasAllowanceStandardForPosition("200810", "001", "0191")).thenReturn(false);
        when(repository.hasAllowanceStandardForPosition("200707", "001", "0191")).thenReturn(true);
        when(repository.latestAllowanceStandardAtOrBefore(eq("200901"), eq("001"), eq("0191"))).thenReturn("200810");
        when(repository.latestAllowanceStandardWithPositionRowAtOrBefore(eq("200901"), eq("001"), eq("0191")))
                .thenReturn("200707");
        when(repository.performanceAllowance("001", "0191", "200707")).thenReturn(BigDecimal.valueOf(900));
        when(repository.performanceAllowance("001", "0191", "200810")).thenReturn(BigDecimal.ZERO);
        when(repository.subsidyAllowance("001", "0191", "200707")).thenReturn(280);
        when(repository.subsidyAllowance("001", "0191", "200810")).thenReturn(0);
        when(repository.findPositionLevelRange("0191")).thenReturn(Optional.of(new PositionLevelRange("0191", 22, 16)));
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(8793, "200901");

        WageProjectionStepDetail targetStep = preview.stepDetails().stream()
                .filter(step -> "200901".equals(step.period()))
                .reduce((first, second) -> second)
                .orElseThrow();
        assertThat(targetStep.allowanceStandardYearMonth()).isEqualTo("200707");
        assertThat(targetStep.components().stream()
                .filter(component -> "DFBT2".equals(component.fieldName()))
                .findFirst()
                .orElseThrow()
                .amount()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(targetStep.components().stream()
                .filter(component -> "SDBT".equals(component.fieldName()))
                .findFirst()
                .orElseThrow()
                .amount()).isEqualByComparingTo(BigDecimal.valueOf(280));
    }

    @Test
    void projectionHistoryAuditUsesAllowanceStandardFromMatchingPositionChangeStep() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot reform = historyWithIdAndStandards(
                "reform-id", "2006", "07", "套改", "01A0", "副科级领导职务", "23", "3",
                "2006", "2006", "1998.03", "200607", "200607", 1334);
        PayrollHistorySnapshot positionRecord = historyWithIdAndStandards(
                "position-id", "2008", "10", "职务变化", "0191", "正科级非领导职务", "21", "1",
                "2008", "2006", "2007.07", "200607", "200810", 2554);

        when(repository.findLatestHistory(134)).thenReturn(Optional.of(positionRecord));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(positionRecord, reform));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("01A0", "副科级领导职务", "1998.03")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("01A0", "副科级领导职务", "1998.03", 4)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(16);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(new WageReformStandard("01A0", 0, 99, 0, 99, "23", "3")));
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(repository.findFirstWageReformStandardForPosition(anyString())).thenReturn(Optional.empty());
        when(repository.findPositionChangesBetween(eq("001"), eq("00040"), anyString(), eq("200810"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("0190", "正科级领导职务", "2008.09"),
                        new PositionChangeCandidate("0191", "正科级非领导职务", "2008.10")));
        when(repository.findPositionLevelRange(anyString())).thenReturn(Optional.of(new PositionLevelRange("0190", 22, 16)));
        when(repository.findAllowanceStandardPeriodsBetween(eq("001"), eq("200607"), eq("200810")))
                .thenReturn(List.of("200707", "200810"));
        when(repository.hasAllowanceStandard("200707", "001", "0191")).thenReturn(true);
        when(repository.hasAllowanceStandard("200810", "001", "0191")).thenReturn(true);
        when(repository.hasAllowanceStandardForPosition("200707", "001", "0191")).thenReturn(true);
        when(repository.hasAllowanceStandardForPosition("200810", "001", "0191")).thenReturn(true);
        when(repository.latestAllowanceStandardAtOrBefore(eq("200810"), eq("001"), eq("0191"))).thenReturn("200810");
        when(repository.latestAllowanceStandardAtOrBefore(eq("200707"), eq("001"), eq("0191"))).thenReturn("200707");
        when(repository.performanceAllowance("001", "0191", "200707")).thenReturn(BigDecimal.valueOf(900));
        when(repository.performanceAllowance("001", "0191", "200810")).thenReturn(BigDecimal.valueOf(1200));
        when(repository.subsidyAllowance("001", "0191", "200707")).thenReturn(280);
        when(repository.subsidyAllowance("001", "0191", "200810")).thenReturn(320);
        when(repository.findCalculationFields()).thenReturn(List.of(
                payrollField("zwgzse2", "职务工资", false),
                payrollField("jbgzse2", "级别工资", false),
                payrollField("dfbt2", "生活性补贴", true),
                payrollField("sdbt", "工作性津贴", true),
                payrollField("hj2", "合计", false)));
        when(repository.findHistoryValuesById(eq("position-id"), eq("001"), eq("00040"), eq("2008"), eq("10")))
                .thenReturn(Optional.of(Map.of(
                        "zwgzse2", 380,
                        "jbgzse2", 954,
                        "dfbt2", 900,
                        "sdbt", 280,
                        "hj2", 2554)));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("zwgzse2"))).thenReturn(new BigDecimal("380"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("jbgzse2"))).thenReturn(new BigDecimal("954"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("dfbt2"))).thenReturn(new BigDecimal("900"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("sdbt"))).thenReturn(new BigDecimal("280"));
        when(repository.decimalValue(org.mockito.ArgumentMatchers.any(), eq("hj2"))).thenReturn(new BigDecimal("2554"));
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString())).thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findBasicSalaryStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString())).thenReturn(List.of());
        when(repository.findPositionChangesBetween(eq("001"), eq("00040"), eq("200607"), eq("200810"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("0190", "正科级领导职务", "2008.09"),
                        new PositionChangeCandidate("0191", "正科级非领导职务", "2008.10")));
        stubCivilServantGradeSalary(repository);
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        List<PayrollHistoryProjectionAudit> audits = service.projectionHistoryAudits(134);
        PayrollHistoryProjectionAudit positionAudit = audits.stream()
                .filter(item -> "position-id".equals(item.historyId()))
                .findFirst()
                .orElseThrow();

        WageProjectionStepDetail positionStep = positionAudit.stepDetails().stream()
                .filter(step -> "200810".equals(step.period())
                        && step.description() != null
                        && step.description().contains("职务变化"))
                .findFirst()
                .orElseThrow();
        assertThat(positionStep.allowanceStandardYearMonth()).isEqualTo("200707");
        PayrollComponentDifference dfbt2 = positionAudit.componentDifferences().stream()
                .filter(item -> "DFBT2".equals(item.fieldName()))
                .findFirst()
                .orElse(null);
        if (dfbt2 != null) {
            assertThat(dfbt2.calculatedAmount()).isEqualByComparingTo(BigDecimal.valueOf(900));
        }
    }

    private static PayrollHistorySnapshot historyWithIdAndStandards(
            String id,
            String year,
            String month,
            String type,
            String positionCode,
            String positionName,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear,
            String positionStartYearMonth,
            String salaryStandardYearMonth,
            String allowanceStandardYearMonth,
            int storedTotal) {
        return historyWithIdAndStandards(
                id, year, month, type, positionCode, positionName, level, step,
                levelStartYear, stepStartYear, positionStartYearMonth,
                salaryStandardYearMonth, allowanceStandardYearMonth, 0, 0, storedTotal);
    }

    private static PayrollHistorySnapshot historyWithIdAndStandards(
            String id,
            String year,
            String month,
            String type,
            String positionCode,
            String positionName,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear,
            String positionStartYearMonth,
            String salaryStandardYearMonth,
            String allowanceStandardYearMonth,
            int storedPositionSalary,
            int storedGradeSalary,
            int storedTotal) {
        PayrollHistorySnapshot base = historyWithId(
                id, year, month, type, positionCode, positionName, level, step,
                levelStartYear, stepStartYear, positionStartYearMonth);
        return new PayrollHistorySnapshot(
                base.id(),
                base.organizationCode(),
                base.personCode(),
                base.name(),
                base.calculationYear(),
                base.calculationMonth(),
                base.calculationType(),
                base.organizationType(),
                base.organizationPerformanceEnabled(),
                base.individualPerformanceApproved(),
                base.approvalOrganization(),
                base.workStartYearMonth(),
                base.positionStartYearMonth(),
                base.salaryYears(),
                base.interruptedSalaryYears(),
                base.levelAssessmentStartYear(),
                base.stepAssessmentStartYear(),
                base.teachingStartYearMonth(),
                base.teachingInterruptedYears(),
                base.raisePercentage(),
                base.rankAllowanceStandardYearMonth(),
                base.rankName(),
                base.positionCode(),
                base.positionName(),
                base.positionSalaryGrade(),
                base.floatingStep(),
                base.gradeSalaryLevel(),
                base.gradeSalaryStep(),
                salaryStandardYearMonth,
                allowanceStandardYearMonth,
                base.postAllowanceStandardYearMonth(),
                base.postAllowanceCategory(),
                storedPositionSalary,
                storedGradeSalary,
                base.storedTechnicalGradeSalary(),
                base.storedPerformanceAllowance(),
                base.storedSubsidyAllowance(),
                base.storedRetainedAllowance(),
                base.storedTeachingAllowance(),
                base.storedSalaryIncrease(),
                base.storedRankAllowance(),
                base.storedFloatingSalary(),
                base.storedBonusBalance(),
                base.storedPostAllowance(),
                base.storedRetainedSpecialPostAllowance(),
                base.storedPgbc(),
                base.storedYearAllowance(),
                storedTotal);
    }

    private static PayrollHistorySnapshot workerHistoryWithAmounts(
            String id,
            String organizationCode,
            String personCode,
            String positionCode,
            String positionName,
            String positionSalaryGrade,
            int storedPositionSalary,
            int storedTechnicalGradeSalary,
            int storedTotal) {
        PayrollHistorySnapshot base = historyWithId(
                id, "2026", "07", "正常档次", positionCode, positionName, "", positionSalaryGrade,
                "2025", "2025", "2017.09");
        return new PayrollHistorySnapshot(
                base.id(),
                organizationCode,
                personCode,
                base.name(),
                base.calculationYear(),
                base.calculationMonth(),
                base.calculationType(),
                "05",
                base.organizationPerformanceEnabled(),
                base.individualPerformanceApproved(),
                base.approvalOrganization(),
                base.workStartYearMonth(),
                base.positionStartYearMonth(),
                base.salaryYears(),
                base.interruptedSalaryYears(),
                base.levelAssessmentStartYear(),
                base.stepAssessmentStartYear(),
                base.teachingStartYearMonth(),
                base.teachingInterruptedYears(),
                base.raisePercentage(),
                base.rankAllowanceStandardYearMonth(),
                base.rankName(),
                base.positionCode(),
                base.positionName(),
                positionSalaryGrade,
                base.floatingStep(),
                base.gradeSalaryLevel(),
                base.gradeSalaryStep(),
                "201607",
                "201607",
                base.postAllowanceStandardYearMonth(),
                base.postAllowanceCategory(),
                storedPositionSalary,
                0,
                storedTechnicalGradeSalary,
                486,
                729,
                43,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                storedTotal);
    }

    private static PayrollHistorySnapshot historyWithId(
            String id,
            String year,
            String month,
            String type,
            String positionCode,
            String positionName,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear,
            String positionStartYearMonth) {
        PayrollHistorySnapshot base = history(
                year, month, type, positionCode, positionName, level, step, levelStartYear, stepStartYear, positionStartYearMonth);
        return new PayrollHistorySnapshot(
                id,
                base.organizationCode(),
                base.personCode(),
                base.name(),
                base.calculationYear(),
                base.calculationMonth(),
                base.calculationType(),
                base.organizationType(),
                base.organizationPerformanceEnabled(),
                base.individualPerformanceApproved(),
                base.approvalOrganization(),
                base.workStartYearMonth(),
                base.positionStartYearMonth(),
                base.salaryYears(),
                base.interruptedSalaryYears(),
                base.levelAssessmentStartYear(),
                base.stepAssessmentStartYear(),
                base.teachingStartYearMonth(),
                base.teachingInterruptedYears(),
                base.raisePercentage(),
                base.rankAllowanceStandardYearMonth(),
                base.rankName(),
                base.positionCode(),
                base.positionName(),
                base.positionSalaryGrade(),
                base.floatingStep(),
                base.gradeSalaryLevel(),
                base.gradeSalaryStep(),
                base.salaryStandardYearMonth(),
                base.allowanceStandardYearMonth(),
                base.postAllowanceStandardYearMonth(),
                base.postAllowanceCategory(),
                base.storedPositionSalary(),
                base.storedGradeSalary(),
                base.storedTechnicalGradeSalary(),
                base.storedPerformanceAllowance(),
                base.storedSubsidyAllowance(),
                base.storedRetainedAllowance(),
                base.storedTeachingAllowance(),
                base.storedSalaryIncrease(),
                base.storedRankAllowance(),
                base.storedFloatingSalary(),
                base.storedBonusBalance(),
                base.storedPostAllowance(),
                base.storedRetainedSpecialPostAllowance(),
                base.storedPgbc(),
                base.storedYearAllowance(),
                base.storedTotal());
    }

    private static PayrollHistorySnapshot history(
            String year,
            String month,
            String type,
            String positionCode,
            String positionName,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear) {
        return history(year, month, type, positionCode, positionName, level, step, levelStartYear, stepStartYear, "2017.02");
    }

    private static PayrollHistorySnapshot history(
            String year,
            String month,
            String type,
            String positionCode,
            String positionName,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear,
            String positionStartYearMonth) {
        return history(year, month, type, positionCode, positionName, level, step, levelStartYear, stepStartYear, positionStartYearMonth, "01");
    }

    private static PayrollHistorySnapshot history(
            String year,
            String month,
            String type,
            String positionCode,
            String positionName,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear,
            String positionStartYearMonth,
            String organizationType) {
        return new PayrollHistorySnapshot(
                "history-id",
                "001",
                "00040",
                "测试人员",
                year,
                month,
                type,
                organizationType,
                1,
                "",
                "",
                "1991.10",
                positionStartYearMonth,
                35,
                0,
                levelStartYear,
                stepStartYear,
                "",
                0,
                0,
                "",
                "",
                positionCode,
                positionName,
                step,
                "",
                level,
                "",
                "201607",
                "201607",
                "",
                "",
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                0);
    }

    @Test
    void wageProjectionIncludesWorkerPositionChangesForInstitutionPersonnel() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2024", "12", "月末结转", "0801", "技工二级", "", "8", "2020", "2020", "2010.06");

        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2005.06");
        when(repository.findPositionAtOrBefore("001", "00040", "202412"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0801", "技工二级", "2008.01")));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("202412"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0801", "技工二级", "2008.01")));

        Method wageProjectionEvents = PayrollService.class.getDeclaredMethod(
                "wageProjectionEvents",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                PayrollHistorySnapshot.class);
        wageProjectionEvents.setAccessible(true);
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("0505", "机关初级工", "2007.12"),
                        new PositionChangeCandidate("0802", "技工一级", "2015.06")));

        @SuppressWarnings("unchecked")
        List<Object> events = (List<Object>) wageProjectionEvents.invoke(
                service, "001", "00040", "200607", "202412", "2005.06", latest);

        assertThat(events).hasSize(2);
        List<String> positionCodes = events.stream()
                .map(event -> {
                    try {
                        PositionChangeCandidate position = (PositionChangeCandidate) event.getClass().getMethod("position").invoke(event);
                        return position.positionCode();
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .toList();
        assertThat(positionCodes).containsExactly("0505", "0802");
    }

    @Test
    void wageProjectionIgnoresInstitutionPositionChangesForGovernmentWorker() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2024", "12", "月末结转", "0505", "机关初级工", "", "4", "2020", "2020", "2007.12", "05");

        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2006.07");
        when(repository.findPositionAtOrBefore("001", "00040", "202412"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2007.12")));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());

        Method wageProjectionEvents = PayrollService.class.getDeclaredMethod(
                "wageProjectionEvents",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                PayrollHistorySnapshot.class);
        wageProjectionEvents.setAccessible(true);
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("0505", "机关初级工", "2007.12"),
                        new PositionChangeCandidate("0802", "技工一级", "2015.06"),
                        new PositionChangeCandidate("0504", "机关中级工", "2018.06")));

        @SuppressWarnings("unchecked")
        List<Object> events = (List<Object>) wageProjectionEvents.invoke(
                service, "001", "00040", "200607", "202412", "2006.07", latest);

        assertThat(events).hasSize(2);
        List<String> positionCodes = events.stream()
                .map(event -> {
                    try {
                        PositionChangeCandidate position = (PositionChangeCandidate) event.getClass().getMethod("position").invoke(event);
                        return position.positionCode();
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .toList();
        assertThat(positionCodes).containsExactly("0505", "0504");
    }

    @Test
    void wageProjectionPromotesGovernmentWorkerEveryTwoYearsNotAnnually() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2012", "01", "月末", "0505", "机关初级工", "", "3", "2007", "2007", "2006.07", "05");

        when(repository.findLatestHistory(6001)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2006.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findPositionAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2006.07")));
        when(repository.findPositionAtPeriod("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2006.07")));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findLatestInternPositionBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findEducationRegularizationStandard(anyString(), anyString()))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "0505", "机关初级工", "", "3")));
        when(repository.findPersonnelEducationCode("001", "00040")).thenReturn(Optional.of("31"));
        when(repository.findPersonnelRegularizationDates("001", "00040"))
                .thenReturn(Optional.of(new PersonnelRegularizationDates("1990.01", "", "2006.07", "")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findBasicSalaryStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findAllowanceStandardPeriodsBetween(anyString(), anyString(), anyString())).thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.positionGradeSalary(eq("0505"), anyString(), anyString(), anyString())).thenReturn(500);
        when(repository.positionSalary(anyString(), anyString())).thenReturn(0);
        when(repository.hasBasicSalaryStandardForSource(anyString(), eq("WORKER_GRADE"))).thenReturn(true);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return Math.max(0, end - start + 1);
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });

        WageProjectionPreview preview = service.wageProjection(6001, "201201");

        long workerPromotions = preview.explanationLines().stream()
                .filter(line -> line.contains("晋升一档岗位工资"))
                .count();
        long salaryLevelPromotions = preview.explanationLines().stream()
                .filter(line -> line.contains("晋升薪级"))
                .count();
        assertThat(workerPromotions).isEqualTo(2);
        assertThat(salaryLevelPromotions).isZero();
        assertThat(preview.baseSalarySource()).isEqualTo("WORKER_GRADE");
    }

    @Test
    void governmentWorkerPositionChangeUsesNearestHigherGrade() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);

        when(repository.findOrganizationPayrollPolicy("001"))
                .thenReturn(Optional.of(new OrganizationPayrollPolicy("", "", "")));
        when(repository.intValue(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionSalary(anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("0504".equals(positionCode)) {
                return 900;
            }
            if ("0505".equals(positionCode)) {
                return 800;
            }
            return 0;
        });
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int grade = Integer.parseInt(invocation.getArgument(1));
            if ("0505".equals(positionCode)) {
                return grade * 100 + 100;
            }
            if ("0504".equals(positionCode)) {
                return grade * 60 + 340;
            }
            return 0;
        });
        when(repository.technicalGradeSalary(anyString(), anyString())).thenReturn(0);

        Method method = PayrollService.class.getDeclaredMethod(
                "governmentWorkerPositionChangeResult",
                String.class,
                String.class,
                String.class,
                PositionChangeCandidate.class,
                String.class,
                String.class,
                String.class);
        method.setAccessible(true);
        Object result = method.invoke(
                service,
                "0505",
                "4",
                "0",
                new PositionChangeCandidate("0504", "机关中级工", "2020.06"),
                "2020",
                "201607",
                "001");

        assertThat(result).isNotNull();
        assertThat(result.getClass().getMethod("eligible").invoke(result)).isEqualTo(true);
        assertThat(result.getClass().getMethod("promotedGradeStep").invoke(result)).isEqualTo("3");
        assertThat(result.getClass().getMethod("promotedPositionSalary").invoke(result)).isEqualTo(1420);
        assertThat(String.valueOf(result.getClass().getMethod("note").invoke(result))).contains("就近就高");
    }

    @Test
    void wageProjectionSkipsWorkerPositionChangeWhenAppointmentMatchesCurrentPosition() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2020", "12", "调标晋升", "0505", "机关初级工", "", "2", "2006", "2006", "2007.01", "05");
        Class<?> stateClass = Class.forName("com.dxsoft.rsgzgl.payroll.PayrollService$WageProjectionState");
        java.lang.reflect.Constructor<?> stateConstructor = stateClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                String.class);
        stateConstructor.setAccessible(true);
        Object state = stateConstructor.newInstance(
                "0505",
                "机关初级工",
                "",
                "2",
                "0",
                "2006",
                "2006",
                "WORKER_GRADE",
                "200607",
                "200607",
                null,
                null,
                0,
                null);
        PositionChangeCandidate duplicateAppointment = new PositionChangeCandidate("0505", "机关初级工", "2007.01");
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();

        Method method = PayrollService.class.getDeclaredMethod(
                "applyWageProjectionPositionChange",
                stateClass,
                PositionChangeCandidate.class,
                String.class,
                String.class,
                List.class,
                PayrollHistorySnapshot.class);
        method.setAccessible(true);
        Object next = method.invoke(
                service,
                state,
                duplicateAppointment,
                "001",
                "00040",
                lines,
                latest);

        assertThat(next.getClass().getMethod("stepOrSalaryLevel").invoke(next)).isEqualTo("2");
        assertThat(next.getClass().getMethod("positionCode").invoke(next)).isEqualTo("0505");
        assertThat(lines).isEmpty();
    }

    @Test
    void wageProjectionEventsSkipDuplicatePostRegularizationAppointment() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);

        Method duplicateCheck = PayrollService.class.getDeclaredMethod(
                "isDuplicatePostRegularizationAppointment",
                PositionChangeCandidate.class,
                String.class,
                String.class);
        duplicateCheck.setAccessible(true);

        assertThat(duplicateCheck.invoke(
                service,
                new PositionChangeCandidate("0505", "机关初级工", "2007.01"),
                "200612",
                "0505")).isEqualTo(true);
        assertThat(duplicateCheck.invoke(
                service,
                new PositionChangeCandidate("0505", "机关初级工", "2006.12"),
                "200612",
                "0505")).isEqualTo(false);
        assertThat(duplicateCheck.invoke(
                service,
                new PositionChangeCandidate("0504", "机关中级工", "2018.06"),
                "200612",
                "0505")).isEqualTo(false);
    }

    @Test
    void wageProjectionAppliesWorkerPositionChangeNearestHigherGrade() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2020", "12", "月末", "0505", "机关初级工", "", "4", "2018", "2018", "2007.12", "05");

        when(repository.findLatestHistory(6001)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2006.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findPositionAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2007.12")));
        when(repository.findPositionAtPeriod("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2007.12")));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findLatestInternPositionBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findEducationRegularizationStandard(anyString(), anyString()))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "0505", "机关初级工", "", "3")));
        when(repository.findPersonnelEducationCode("001", "00040")).thenReturn(Optional.of("31"));
        when(repository.findPersonnelRegularizationDates("001", "00040"))
                .thenReturn(Optional.of(new PersonnelRegularizationDates("1990.01", "", "2006.07", "")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new PositionChangeCandidate("0505", "机关初级工", "2007.12"),
                        new PositionChangeCandidate("0504", "机关中级工", "2018.06")));
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findBasicSalaryStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findAllowanceStandardPeriodsBetween(anyString(), anyString(), anyString())).thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.findOrganizationPayrollPolicy("001"))
                .thenReturn(Optional.of(new OrganizationPayrollPolicy("", "", "")));
        when(repository.positionSalary(anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("0504".equals(positionCode)) {
                return 900;
            }
            if ("0505".equals(positionCode)) {
                return 800;
            }
            return 0;
        });
        when(repository.positionGradeSalary(anyString(), anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int grade = Integer.parseInt(invocation.getArgument(1));
            if ("0505".equals(positionCode)) {
                return grade * 100 + 100;
            }
            if ("0504".equals(positionCode)) {
                return grade * 60 + 340;
            }
            return 0;
        });
        when(repository.technicalGradeSalary(anyString(), anyString())).thenReturn(0);
        when(repository.hasBasicSalaryStandardForSource(anyString(), eq("WORKER_GRADE"))).thenReturn(true);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(0);
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Set.of());

        WageProjectionPreview preview = service.wageProjection(6001, "202012");

        assertThat(preview.stepOrSalaryLevel()).isEqualTo("3");
        assertThat(preview.positionCode()).isEqualTo("0504");
        assertThat(preview.explanationLines()).anyMatch(line ->
                line.contains("0504") && line.contains("4") && line.contains("3"));
    }

    @Test
    void workerWageReformAppliesLowerPositionAndConsidersEducation() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2024", "07", "调标晋升", "0504", "机关中级工", "", "2", "2022", "2022", "2004.09", "05");
        WageReformStandard currentStandard = new WageReformStandard("0504", 1, 4, 12, 14, "", "2", "机关中级工");

        when(repository.findOrganizationPayrollPolicy("001"))
                .thenReturn(Optional.of(new OrganizationPayrollPolicy("", "", "")));
        when(repository.findWageReformPositionsBefore(eq("001"), eq("00040"), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0504", "机关中级工", "2004.09", 0),
                        new WageReformPosition("0505", "机关初级工", "1998.09", 0)));
        when(repository.findWageReformStandard("0505", 9, 14))
                .thenReturn(Optional.of(new WageReformStandard("0505", 1, 99, 12, 14, "", "5", "机关初级工")));
        when(repository.findLatestEducationForPromotion(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new EducationPromotionSource("23", "大学本科毕业", "1995.08")));
        when(repository.findEducationRegularizationStandard(eq("0504"), eq("23")))
                .thenReturn(Optional.of(new EducationRegularizationStandard(
                        "23", "大学本科毕业", "0505", "机关初级工", "", "3")));
        when(repository.technicalGradeSalary(anyString(), eq("200607"))).thenReturn(0);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionGradeSalary(anyString(), anyString(), eq("0"), eq("200607"))).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int grade = Integer.parseInt(invocation.getArgument(1));
            if ("0504".equals(positionCode)) {
                return switch (grade) {
                    case 2 -> 598;
                    case 3 -> 626;
                    case 4 -> 654;
                    default -> grade * 28 + 540;
                };
            }
            if ("0505".equals(positionCode)) {
                return switch (grade) {
                    case 3 -> 578;
                    case 5 -> 634;
                    default -> grade * 26 + 500;
                };
            }
            return 0;
        });

        Method method = Arrays.stream(PayrollService.class.getDeclaredMethods())
                .filter(candidate -> "wageReformSelection".equals(candidate.getName()) && candidate.getParameterCount() == 5)
                .findFirst()
                .orElseThrow();
        method.setAccessible(true);
        Object selection = method.invoke(
                service,
                latest,
                currentStandard,
                14,
                "1995.08",
                null);

        assertThat(selection).isNotNull();
        assertThat(selection.getClass().getMethod("step").invoke(selection)).isEqualTo("4");
        assertThat(selection.getClass().getMethod("positionCode").invoke(selection)).isEqualTo("0504");
        String note = String.valueOf(selection.getClass().getMethod("note").invoke(selection));
        assertThat(note).contains("原任低一").contains("就近就高");
    }

    @Test
    void workerWageReformAppliesEducationHigherStepOnSamePosition() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2024", "07", "调标晋升", "0505", "机关初级工", "", "2", "2022", "2022", "1998.09", "05");
        WageReformStandard currentStandard = new WageReformStandard("0505", 1, 99, 12, 14, "", "2", "机关初级工");

        when(repository.findOrganizationPayrollPolicy("001"))
                .thenReturn(Optional.of(new OrganizationPayrollPolicy("", "", "")));
        when(repository.findWageReformPositionsBefore(eq("001"), eq("00040"), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(new WageReformPosition("0505", "机关初级工", "1998.09", 0)));
        when(repository.findLatestEducationForPromotion(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new EducationPromotionSource("23", "大学本科毕业", "1995.08")));
        when(repository.findEducationRegularizationStandard(eq("0505"), eq("23")))
                .thenReturn(Optional.of(new EducationRegularizationStandard(
                        "23", "大学本科毕业", "0505", "机关初级工", "", "4")));
        when(repository.technicalGradeSalary(anyString(), eq("200607"))).thenReturn(0);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.positionGradeSalary(eq("0505"), anyString(), eq("0"), eq("200607"))).thenAnswer(invocation -> {
            int grade = Integer.parseInt(invocation.getArgument(1));
            return grade * 26 + 500;
        });

        Method method = Arrays.stream(PayrollService.class.getDeclaredMethods())
                .filter(candidate -> "wageReformSelection".equals(candidate.getName()) && candidate.getParameterCount() == 5)
                .findFirst()
                .orElseThrow();
        method.setAccessible(true);
        Object selection = method.invoke(
                service,
                latest,
                currentStandard,
                14,
                "1995.08",
                null);

        assertThat(selection).isNotNull();
        assertThat(selection.getClass().getMethod("step").invoke(selection)).isEqualTo("4");
        assertThat(String.valueOf(selection.getClass().getMethod("note").invoke(selection)))
                .contains("学历")
                .contains("定级档次");
    }

    @Test
    void governmentWorkerRegularizationDoesNotUseCivilServantNonInternPath() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);

        Method method = PayrollService.class.getDeclaredMethod(
                "isNonInternProbationRegularization",
                String.class,
                String.class,
                String.class);
        method.setAccessible(true);

        assertThat(method.invoke(service, "0505", "见习工资", "新增见习")).isEqualTo(false);
        assertThat(method.invoke(service, "0504", "见习工资", "新增见习")).isEqualTo(false);
        assertThat(method.invoke(service, "01B0", "见习工资", "新增见习")).isEqualTo(true);
    }

    @Test
    void governmentWorkerUsesWorkerInternAndRegularizationLookupWhenAppointmentMissing() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2012", "01", "调标晋升", "0505", "机关初级工", "", "2", "2007", "2007", "2006.12", "05");
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2006.12");
        when(repository.findLatestEducationForPromotion("001", "00040", "200612")).thenReturn(Optional.of(
                new EducationPromotionSource("05", "高中毕业", "2006.07")));
        when(repository.findLatestEducationForPromotion("001", "00040", "200607")).thenReturn(Optional.of(
                new EducationPromotionSource("05", "高中毕业", "2006.07")));

        Method internFallback = PayrollService.class.getDeclaredMethod(
                "internProbationFallbackPositionCode", PayrollHistorySnapshot.class);
        internFallback.setAccessible(true);
        Method workerLookup = PayrollService.class.getDeclaredMethod(
                "governmentWorkerRegularizationLookupPosition", PayrollHistorySnapshot.class);
        workerLookup.setAccessible(true);
        Method resolveLookup = PayrollService.class.getDeclaredMethod(
                "resolveRegularizationStandardLookupCode",
                PayrollHistorySnapshot.class,
                String.class,
                PositionChangeCandidate.class,
                boolean.class);
        resolveLookup.setAccessible(true);
        Method educationSuffix = PayrollService.class.getDeclaredMethod(
                "educationPositionSuffix", String.class);
        educationSuffix.setAccessible(true);

        assertThat(educationSuffix.invoke(service, "高中毕业")).isEqualTo("5");
        assertThat(educationSuffix.invoke(service, "大学本科毕业")).isEqualTo("4");
        assertThat(internFallback.invoke(service, latest)).isEqualTo("05F5");
        assertThat(workerLookup.invoke(service, latest)).isEqualTo("0505");
        assertThat(resolveLookup.invoke(service, latest, "01FF", null, false)).isEqualTo("0505");
        assertThat(resolveLookup.invoke(service, latest, "01FF", null, true)).isEqualTo("0505");
    }

    @Test
    void normalizeInternProbationPositionCodeReplacesFfSuffixWithEducationDigit() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history(
                "2006", "07", "套改", "0505", "机关初级工", "", "2", "2006", "2006", "2006.07", "05");

        Method normalize = PayrollService.class.getDeclaredMethod(
                "normalizeInternProbationPositionCode",
                PayrollHistorySnapshot.class,
                String.class,
                String.class);
        normalize.setAccessible(true);

        assertThat(normalize.invoke(service, latest, "05FF", "高中毕业")).isEqualTo("05F5");
        PayrollHistorySnapshot civilServant = history(
                "2006", "07", "套改", "01A0", "科员", "", "2", "2006", "2006", "2006.07", "01");
        assertThat(normalize.invoke(service, civilServant, "01FF", "大学本科毕业")).isEqualTo("01F4");
        assertThat(normalize.invoke(service, latest, "05F5", "高中毕业")).isEqualTo("05F5");
    }

    @Test
    void newPersonnelPreviewTreatsMarkedPersonnelWithoutTransferDeterminationAsPending() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        NewPersonnelSalaryCandidate candidate = new NewPersonnelSalaryCandidate(
                186, "001", "测试单位", "00040", "测试退伍",
                "2015.12", "部队退伍", "01A0", "科员", "2015.12",
                "1990.01", "", "", "", 0, "已定工资",
                "tip-id", "职务变化", 5000, "25", "3", "2010", "2010", "2024", "12");

        when(repository.hasTransferInSalaryDetermination("001", "00040")).thenReturn(false);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });

        Method previewMethod = PayrollService.class.getDeclaredMethod("toNewPersonnelSalaryPreview", NewPersonnelSalaryCandidate.class);
        previewMethod.setAccessible(true);
        NewPersonnelSalaryPreview preview = (NewPersonnelSalaryPreview) previewMethod.invoke(service, candidate);

        assertThat(preview.applyEligible()).isTrue();
        assertThat(preview.rollbackEligible()).isFalse();
        assertThat(preview.calculationPeriod()).isEqualTo("2016.01");
        assertThat(preview.standardNote()).contains("同等条件推算");
        assertThat(preview.standardNote()).contains("退伍定资");
    }

    @Test
    void resolveNewPersonnelApplyPeriodUsesNextMonthForDeterminationExceptIntern() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });

        Method method = PayrollService.class.getDeclaredMethod(
                "resolveNewPersonnelApplyPeriod", String.class, String.class);
        method.setAccessible(true);

        assertThat(method.invoke(service, "2015.12", "退伍定资")).isEqualTo("201601");
        assertThat(method.invoke(service, "2015.12", "新进工资")).isEqualTo("201601");
        assertThat(method.invoke(service, "2015.12", "调入定资")).isEqualTo("201601");
        assertThat(method.invoke(service, "2015.12", "转业定资")).isEqualTo("201601");
        assertThat(method.invoke(service, "2015.12", "见习工资")).isEqualTo("201512");
    }

    @Test
    void salaryExecutionPeriodHelpersApplyNextMonthForPositionAndEducation() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });

        Method positionMethod = PayrollService.class.getDeclaredMethod(
                "salaryExecutionPeriodForPositionChange", String.class);
        positionMethod.setAccessible(true);
        Method educationMethod = PayrollService.class.getDeclaredMethod(
                "salaryExecutionPeriodForEducation", String.class);
        educationMethod.setAccessible(true);
        Method standardMethod = PayrollService.class.getDeclaredMethod(
                "salaryExecutionPeriodForStandard", String.class);
        standardMethod.setAccessible(true);

        assertThat(positionMethod.invoke(service, "2016.07")).isEqualTo("201608");
        assertThat(educationMethod.invoke(service, "2016.07")).isEqualTo("201608");
        assertThat(standardMethod.invoke(service, "201601")).isEqualTo("201601");
    }

    @Test
    void wageProjectionEventsOrdersVeteranDeterminationBeforeSalaryStandardAtJoinMonth() throws Exception {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2015", "12", "月末", "0505", "机关初级工", "", "6", "2010", "2010", "2006.07", "05");
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });

        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "201601"))
                .thenReturn(List.of("201601"));
        when(repository.findAllowanceStandardPeriodsBetween(anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findPersonnelJoinByOrgPerson("001", "00040")).thenReturn(Optional.of(
                new PayrollRepository.PersonnelJoinSnapshot(
                        186, "001", "00040", "李铁军", "部队退伍", "2015.12", "1990.01",
                        "已定工资", "", "", "0505", "机关初级工", "tip-id")));

        Method wageProjectionEvents = PayrollService.class.getDeclaredMethod(
                "wageProjectionEvents",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                PayrollHistorySnapshot.class);
        wageProjectionEvents.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> events = (List<Object>) wageProjectionEvents.invoke(
                service, "001", "00040", "200607", "201601", "2006.07", latest);

        List<Object> eventsAt201601 = new java.util.ArrayList<>();
        for (Object event : events) {
            if ("201601".equals(event.getClass().getMethod("period").invoke(event))) {
                eventsAt201601.add(event);
            }
        }
        assertThat(eventsAt201601).hasSizeGreaterThanOrEqualTo(2);
        assertThat(eventsAt201601.getFirst().getClass().getMethod("personnelDeterminationChangeType").invoke(eventsAt201601.getFirst()))
                .isEqualTo("退伍定资");
        assertThat(eventsAt201601.get(1).getClass().getMethod("basicSalaryStandardYearMonth").invoke(eventsAt201601.get(1)))
                .isEqualTo("201601");
        assertThat((int) eventsAt201601.getFirst().getClass().getMethod("sortOrder").invoke(eventsAt201601.getFirst()))
                .isLessThan((int) eventsAt201601.get(1).getClass().getMethod("sortOrder").invoke(eventsAt201601.get(1)));
    }

    @Test
    void wageProjectionSeparatesVeteranDeterminationAndSalaryStandardAt201601() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService, true);
        PayrollHistorySnapshot latest = history("2015", "12", "月末", "0505", "机关初级工", "", "6", "2010", "2014", "2006.07", "05");

        when(repository.findLatestHistory(186)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2006.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findPositionAtOrBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2006.07")));
        when(repository.findPositionAtPeriod("001", "00040", "200607"))
                .thenReturn(Optional.of(new PositionChangeCandidate("0505", "机关初级工", "2006.07")));
        when(repository.findLatestPositionBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.empty());
        when(repository.findLatestInternPositionBefore(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findEducationRegularizationStandard(anyString(), anyString()))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "0505", "机关初级工", "", "3")));
        when(repository.findPersonnelEducationCode("001", "00040")).thenReturn(Optional.of("31"));
        when(repository.findPersonnelRegularizationDates("001", "00040"))
                .thenReturn(Optional.of(new PersonnelRegularizationDates("1990.01", "", "2006.07", "")));
        when(repository.findPersonnelJoinByOrgPerson("001", "00040")).thenReturn(Optional.of(
                new PayrollRepository.PersonnelJoinSnapshot(
                        186, "001", "00040", "李铁军", "部队退伍", "2015.12", "1990.01",
                        "已定工资", "", "", "0505", "机关初级工", "tip-id")));
        when(repository.findPositionChangesBetween(anyString(), anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceChangesBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceStandardPeriodsBetween(anyString(), anyString())).thenReturn(List.of());
        when(repository.findBasicSalaryStandardPeriodsBetween(anyString(), anyString()))
                .thenReturn(List.of("201601"));
        when(repository.findAllowanceStandardPeriodsBetween(anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findEducationRecordsBetween(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(repository.findRankAllowanceAtOrBefore(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.hasBasicSalaryStandardForSource("201601", "WORKER_GRADE")).thenReturn(true);
        when(repository.positionGradeSalary(eq("0505"), eq("6"), anyString(), eq("201601"))).thenReturn(2808);
        when(repository.positionGradeSalary(eq("0505"), eq("7"), anyString(), eq("201601"))).thenReturn(2854);
        when(repository.positionSalary(anyString(), anyString())).thenReturn(0);
        when(repository.intValue(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
        });
        when(repository.countQualifiedAssessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            if (end == 2007 || end == 2009 || end == 2011) {
                return 2;
            }
            if (end == 2015 && start <= 2014) {
                return 2;
            }
            return 0;
        });
        when(repository.assessmentYears(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int start = invocation.getArgument(2);
            int end = invocation.getArgument(3);
            return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toSet());
        });

        WageProjectionPreview preview = service.wageProjection(186, "201601");

        List<WageProjectionStepDetail> stepsAt201601 = preview.stepDetails().stream()
                .filter(step -> "201601".equals(step.period()))
                .filter(step -> !"目标年月".equals(step.changeCategory()))
                .toList();
        assertThat(stepsAt201601).hasSizeGreaterThanOrEqualTo(2);
        int veteranIndex = IntStream.range(0, stepsAt201601.size())
                .filter(index -> "退伍定资".equals(stepsAt201601.get(index).changeCategory()))
                .findFirst()
                .orElseThrow();
        int salaryIndex = IntStream.range(0, stepsAt201601.size())
                .filter(index -> stepsAt201601.get(index).changeCategory().contains("工资调"))
                .findFirst()
                .orElseThrow();
        assertThat(veteranIndex).isLessThan(salaryIndex);
        assertThat(stepsAt201601.get(veteranIndex).step()).isEqualTo("6");
        assertThat(stepsAt201601.get(salaryIndex).step()).isEqualTo("7");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2016.01 退伍定资"));
    }

    private static PayrollFieldMetadata payrollField(String fieldName, String caption, boolean allowance) {
        return new PayrollFieldMetadata(
                null,
                null,
                null,
                "hisbase",
                fieldName,
                null,
                null,
                null,
                caption,
                caption,
                caption,
                null,
                null,
                "AUTO",
                null,
                allowance,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
