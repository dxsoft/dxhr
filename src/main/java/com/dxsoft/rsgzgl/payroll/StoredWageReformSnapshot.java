package com.dxsoft.rsgzgl.payroll;

import java.util.Optional;

public record StoredWageReformSnapshot(
        Optional<WageReformPosition> lowerPosition,
        Optional<EducationPromotionSource> education,
        String resultLevel,
        String resultStep,
        String remark) {
}
