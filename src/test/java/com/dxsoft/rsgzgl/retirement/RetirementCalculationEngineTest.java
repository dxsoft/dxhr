package com.dxsoft.rsgzgl.retirement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dxsoft.rsgzgl.payroll.PayrollRepository;
import com.dxsoft.rsgzgl.payroll.PayrollRoundingPolicy;
import com.dxsoft.rsgzgl.retirement.RetirementRepository.RetirementSeedRow;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RetirementCalculationEngineTest {

    @Test
    void basicRetirementFeeUsesPolicyRoundingForConversionAndTeachingRaise() {
        RetirementRepository retirementRepository = mock(RetirementRepository.class);
        PayrollRepository payrollRepository = mock(PayrollRepository.class);
        RetirementCalculationEngine engine = new RetirementCalculationEngine(retirementRepository, payrollRepository);

        when(payrollRepository.roundingPolicy()).thenReturn(PayrollRoundingPolicy.from("0", "1"));
        when(payrollRepository.positionSalary("0101", "200607")).thenReturn(1000);
        when(payrollRepository.positionGradeSalary("0101", "01", "0", "200607")).thenReturn(0);
        when(payrollRepository.gradeSalary("18", "01", "200607")).thenReturn(500);
        when(payrollRepository.technicalGradeSalary(anyString(), anyString())).thenReturn(0);
        when(retirementRepository.lookupConversionRatio("行政管理人员", 30, "退休")).thenReturn(85);
        when(retirementRepository.organizationEducationCategory("001")).thenReturn(0);
        when(retirementRepository.findActiveAllowanceStandard(anyString(), anyString(), anyInt())).thenReturn(Optional.empty());
        when(retirementRepository.findRetirementAllowanceStandard(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        RetirementSeedRow seed = new RetirementSeedRow(
                1,
                "001",
                "测试单位",
                "00001",
                "张三",
                "",
                "男",
                "196001",
                "01",
                "",
                "行政管理人员",
                "198501",
                0,
                30,
                "本科",
                0,
                "汉",
                "",
                "",
                "",
                "",
                "",
                10,
                "",
                0,
                "0101",
                "科员",
                "01",
                "18",
                "0",
                "200607",
                "201401",
                1000,
                500,
                0,
                0,
                50,
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
                0);

        RetirementWageCalculation result = engine.calculate(seed, "2020.01", "退休");

        assertThat(result.teachingRaise()).isEqualTo(150);
        assertThat(result.convertedWageBase()).isEqualTo(1445);
        assertThat(result.basicRetirementFee()).isEqualTo(1445);
    }
}
