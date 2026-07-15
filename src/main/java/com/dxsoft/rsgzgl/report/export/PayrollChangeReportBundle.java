package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterPageModel;
import java.util.List;

record PayrollChangeReportBundle(
        List<PayrollChangeComparison> comparisons,
        List<ApprovalSheetModel> approvalSheets,
        List<RegisterPageModel> registerPages) {
}
