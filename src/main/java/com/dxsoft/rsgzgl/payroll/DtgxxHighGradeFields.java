package com.dxsoft.rsgzgl.payroll;

public record DtgxxHighGradeFields(int fixedStep, int pendingStep, int fixedLevel, int pendingLevel) {
    static DtgxxHighGradeFields empty() {
        return new DtgxxHighGradeFields(0, 0, 0, 0);
    }

    static DtgxxHighGradeFields fromSnapshot(WageReform2006DtgxxSnapshot snapshot) {
        if (snapshot == null) {
            return empty();
        }
        return new DtgxxHighGradeFields(
                snapshot.fixedStep(),
                snapshot.pendingStep(),
                snapshot.fixedLevel(),
                snapshot.pendingLevel());
    }
}
