package com.dxsoft.rsgzgl.payroll;

import java.util.Map;

public record BasicStandardRecord(
        String standardType,
        Map<String, Object> values
) {
}
