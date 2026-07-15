package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
class PayrollChangeApprovalHtmlRenderer {

    private final TemplateEngine templateEngine;
    private final String stylesheet;

    PayrollChangeApprovalHtmlRenderer(@Qualifier("reportTemplateEngine") TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.stylesheet = ReportHtmlSupport.loadClasspathText("/report/approval-print.css");
    }

    String renderDocument(List<ApprovalSheetModel> sheets) {
        Context context = new Context();
        context.setVariable("stylesheet", stylesheet);
        context.setVariable("sheets", sheets);
        return templateEngine.process("payroll-change-approval-document", context);
    }

    String renderPreviewBody(List<ApprovalSheetModel> sheets) {
        Context context = new Context();
        context.setVariable("sheets", sheets);
        return templateEngine.process("payroll-change-approval-preview", context);
    }
}
