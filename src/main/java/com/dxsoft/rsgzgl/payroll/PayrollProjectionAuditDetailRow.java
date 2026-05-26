package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollProjectionAuditDetailRow(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String historyId,
        String calculationPeriod,
        String changeType,
        Boolean projectionEligible,
        String note,
        Boolean matched,
        Integer storedTotal,
        BigDecimal projectedTotal,
        BigDecimal totalDifference,
        String structureMismatches,
        String componentDifferences
) {
    static PayrollProjectionAuditDetailRow of(
            Integer uid,
            String organizationCode,
            String personCode,
            String name,
            PayrollHistoryProjectionAudit audit) {
        return new PayrollProjectionAuditDetailRow(
                uid,
                organizationCode,
                personCode,
                name,
                audit.historyId(),
                audit.calculationPeriod(),
                audit.changeType(),
                audit.projectionEligible(),
                audit.note(),
                audit.matched(),
                audit.storedTotal(),
                audit.projectedTotal(),
                audit.totalDifference(),
                String.join("；", audit.structureMismatches()),
                audit.componentDifferences().stream()
                        .map(diff -> diff.caption() + "(" + diff.difference() + ")")
                        .reduce((left, right) -> left + "，" + right)
                        .orElse(""));
    }
}
