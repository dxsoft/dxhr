package com.dxsoft.rsgzgl.payroll;

record RegularizationHighGradePolicy(int highGradeIncrement, boolean policeHighGradeEnabled) {
    static RegularizationHighGradePolicy empty() {
        return new RegularizationHighGradePolicy(0, false);
    }
}
