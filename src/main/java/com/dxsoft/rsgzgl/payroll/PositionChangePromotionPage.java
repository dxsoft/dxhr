package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record PositionChangePromotionPage(
        List<PositionChangePromotionCandidateRow> rows,
        long total
) {
}
