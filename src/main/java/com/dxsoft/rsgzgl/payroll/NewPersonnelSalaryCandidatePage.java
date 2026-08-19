package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record NewPersonnelSalaryCandidatePage(
        List<NewPersonnelSalaryCandidate> rows,
        long total
) {
}
