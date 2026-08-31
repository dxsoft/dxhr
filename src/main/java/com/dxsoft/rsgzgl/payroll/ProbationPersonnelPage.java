package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record ProbationPersonnelPage(
        List<Integer> uids,
        long total
) {
}
