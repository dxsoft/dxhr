package com.dxsoft.rsgzgl.payroll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.math.BigDecimal;
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("2007-2010 套改后级别滚动"));
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
    void wageProjectionSkipsPositionChangeWhenAppointmentRecordWasModified() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
    void wageProjectionTreatsOtherCivilServiceSequenceToPoliceAsPoliceConversion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("原任低一职务"));
    }

    @Test
    void wageProjectionUsesPreviousPositionReformWithoutRequiringOneLowerLayer() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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

        assertThat(preview.levelStepDisplay()).isEqualTo("21-11");
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("套改级别高于相同学历新参加工作人员定级级别")
                && line.contains("就近就高套入套改级别"));
    }

    @Test
    void wageProjectionPromotesStepInsteadOfLevelAtHighestPositionLevel() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        assertThat(preview.explanationLines()).noneMatch(line -> line.contains("2008 年") && line.contains("晋升档次"));
    }

    @Test
    void wageProjectionAppliesBasicSalaryStandardAdjustment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.hasBasicSalaryStandardForSource("201807", "GRADE")).thenReturn(true);
        when(repository.hasAllowanceStandard("201807", "001", "0190")).thenReturn(true);
        when(repository.civilServantGradeSalary(eq("21"), eq("4"), eq("0"), anyString())).thenReturn(3200);
        when(repository.positionSalary("0190", "201807")).thenReturn(1800);
        when(repository.positionGradeSalary("0190", "4", "0", "201807")).thenReturn(0);
        when(repository.performanceAllowance("001", "0190", "201807")).thenReturn(BigDecimal.valueOf(2380));
        when(repository.subsidyAllowance("001", "0190", "201807")).thenReturn(545);

        WageProjectionPreview preview = service.wageProjection(8794, "202101");

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201807 调整基本工资标准")
                && line.contains("执行标准 201807")
                && line.contains("职务工资 1800")
                && line.contains("级别/薪级工资 3200"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("201807 同步调整公务员津补贴标准")
                && line.contains("生活性补贴 2380")
                && line.contains("工作性津贴 545"));
        assertThat(preview.salaryStandardYearMonth()).isEqualTo("201807");
        assertThat(preview.positionSalary()).isEqualTo(1800);
        assertThat(preview.gradeSalary()).isEqualTo(3200);
    }

    @Test
    void calculationPreviewUsesProjectedSalaryStandardAtTargetPeriod() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        stubReformStart(repository, latest, new WageReformStandard("0190", 0, 99, 0, 99, "21", "4"));
        when(repository.findBasicSalaryStandardPeriodsBetween("200607", "202101"))
                .thenReturn(List.of("201807"));
        when(repository.hasBasicSalaryStandardForSource("201807", "GRADE")).thenReturn(true);
        when(repository.hasBasicSalaryStandardForSource("201607", "GRADE")).thenReturn(true);
        when(repository.civilServantGradeSalary("21", "4", "0", "201807")).thenReturn(3200);
        when(repository.civilServantGradeSalary("21", "4", "0", "201607")).thenReturn(900);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
    void wageProjectionAdjustsRankAllowanceWhenNewStandardStarts() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
            int effectiveStep = step >= highest ? highest + Math.max(0, extra) : step;
            return (30 - level) * 100 + effectiveStep * 10;
        });
    }

    private static void stubReformStart(
            PayrollRepository repository,
            PayrollHistorySnapshot latest,
            WageReformStandard reformStandard) {
        int uid = "0190".equals(latest.positionCode()) ? 8794 : 8793;
        when(repository.findLatestHistory(uid)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
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
    }

    @Test
    void educationPromotionPreservesStepAssessmentYearWhenLevelIncreaseWithinStepDifference() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
    void educationPromotionPreservesLevelAssessmentYearWhenPositionHierarchyChangesByOneLevel() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
        PayrollHistorySnapshot latest = history("2007", "08", "转正定级", "01B0", "科员", "25", "2", "2007", "2007", "2007.07");

        when(repository.findLatestHistory(9101)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("2007.07");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findPositionAtPeriod("001", "00040", "200707")).thenReturn(Optional.empty());
        when(repository.findPositionAtOrBefore("001", "00040", "200707")).thenReturn(Optional.empty());
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
    void wageProjectionUsesPersonnelEducationWhenRegularizationAppointmentMissing() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
    void wageProjectionAppliesLowerPositionAndEducationForPreReformPromotion() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
        PayrollHistorySnapshot latest = history("2006", "07", "套改", "0190", "正科级领导职务", "21", "4", "2006", "2006", "2004.01");

        when(repository.findLatestHistory(3259)).thenReturn(Optional.of(latest));
        when(repository.findRegularizationYearMonth("001", "00040")).thenReturn("1998.10");
        when(repository.findHistoryChain("001", "00040")).thenReturn(List.of(latest));
        when(repository.findLatestPositionBefore(anyString(), anyString(), eq("200607"), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(Optional.of(new PositionChangeCandidate("0190", "正科级领导职务", "2004.01")));
        when(repository.findWageReformPositionsBefore(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(
                        new WageReformPosition("0190", "正科级领导职务", "2004.01", 0),
                        new WageReformPosition("01B0", "科员", "1998.10", 0)));
        when(repository.calculatedWageReformYears("001", "00040")).thenReturn(20);
        when(repository.findWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            int appointmentYears = invocation.getArgument(1);
            if ("0190".equals(positionCode)) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "26", "2"));
            }
            if ("01B0".equals(positionCode)) {
                return Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2"));
            }
            return Optional.empty();
        });
        when(repository.findNearestWageReformStandard(anyString(), anyInt(), anyInt())).thenAnswer(invocation -> {
            String positionCode = invocation.getArgument(0);
            if ("0190".equals(positionCode)) {
                return Optional.of(new WageReformStandard("0190", 0, 99, 0, 99, "26", "2"));
            }
            if ("01B0".equals(positionCode)) {
                return Optional.of(new WageReformStandard("01B0", 0, 99, 0, 99, "25", "2"));
            }
            return Optional.empty();
        });
        when(repository.findLatestEducationForPromotion("001", "00040", "199810"))
                .thenReturn(Optional.of(new EducationPromotionSource("31", "本科", "1998.10")));
        when(repository.findEducationRegularizationStandard(eq("0190"), eq("31")))
                .thenReturn(Optional.of(new EducationRegularizationStandard("31", "本科", "01B0", "科员", "22", "3")));
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

        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("原任低一职务"));
        assertThat(preview.explanationLines()).anyMatch(line -> line.contains("相同学历新参加工作人员转正定级标准"));
        assertThat(preview.levelStepDisplay()).isEqualTo("22-3");
    }

    @Test
    void wageProjectionUsesAdministrativePrefixWhenMissingRegularizationAppointmentButHasPreReformPosition() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
    void wageProjectionUsesAdministrativePrefixForRegularizationStandardWhenMissingPreReformAppointment() {
        PayrollRepository repository = mock(PayrollRepository.class);
        AccessControlService accessControlService = mock(AccessControlService.class);
        PayrollService service = new PayrollService(repository, accessControlService);
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
        PayrollService service = new PayrollService(repository, accessControlService);
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
        return new PayrollHistorySnapshot(
                "history-id",
                "001",
                "00040",
                "测试人员",
                year,
                month,
                type,
                "01",
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
}
