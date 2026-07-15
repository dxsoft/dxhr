package com.dxsoft.rsgzgl.payroll;

record WageReform2006Calculation(
        InitialPayrollHistoryMutation mutation,
        WageReform2006DtgxxSnapshot dtgxx,
        Integer wageReformYears,
        boolean eligible,
        String note) {
}
