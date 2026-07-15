package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.RegisterPageModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
class PayrollChangeRegisterHtmlRenderer {

    private final TemplateEngine templateEngine;
    private final String stylesheet;

    PayrollChangeRegisterHtmlRenderer(@Qualifier("reportTemplateEngine") TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.stylesheet = ReportHtmlSupport.loadClasspathText("/report/register-print.css");
    }

    String renderDocument(List<RegisterPageModel> pages) {
        Context context = new Context();
        context.setVariable("stylesheet", stylesheet);
        context.setVariable("pages", pages);
        return templateEngine.process("payroll-change-register-document", context);
    }

    String renderPreviewBody(List<RegisterPageModel> pages) {
        Context context = new Context();
        context.setVariable("pages", pages);
        return templateEngine.process("payroll-change-register-preview", context);
    }
}
