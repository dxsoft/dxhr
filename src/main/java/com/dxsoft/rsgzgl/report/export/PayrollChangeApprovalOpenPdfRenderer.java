package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalRow;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalTotals;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSmartCopy;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * OpenPDF approval sheets tuned to match {@code approval-print.css} / legacy VFP form geometry.
 */
@Component
class PayrollChangeApprovalOpenPdfRenderer {

    private static final Logger log = LoggerFactory.getLogger(PayrollChangeApprovalOpenPdfRenderer.class);

    private static final float MM = 72f / 25.4f;
    // Document margins + body/sign heights must fit one A4 (HTML sheet is self-contained 297mm).
    private static final float MARGIN_TOP = 12f * MM;
    private static final float MARGIN_SIDE = 12f * MM;
    private static final float MARGIN_BOTTOM = 8f * MM;
    // Use page bottom whitespace: taller body so 合计/绩效比例 match item row height.
    private static final float BODY_HEIGHT = 154f * MM;
    private static final float SIGN_HEIGHT = 42f * MM;
    private static final float SIGN_HEIGHT_INTERN = 46f * MM;
    /** Taller signature block for 事业转正定级表. */
    private static final float SIGN_HEIGHT_REGULARIZATION = 54f * MM;
    /** Unified meta row height (fits two-line「参加工作/时间」). */
    private static final float META_ROW_HEIGHT = 11.5f * MM;
    private static final float BORDER_PT = 1f;
    private static final Color BORDER = new Color(0x11, 0x18, 0x27);
    private static final int PARALLEL_CHUNK_SIZE = 80;

    private final ExecutorService reportPdfRenderExecutor;
    private volatile FontCache fonts;

    PayrollChangeApprovalOpenPdfRenderer(
            @Qualifier("reportPdfRenderExecutor") ExecutorService reportPdfRenderExecutor) {
        this.reportPdfRenderExecutor = reportPdfRenderExecutor;
    }

    @PostConstruct
    void warmFonts() {
        fonts = resolveFonts();
    }

    byte[] render(List<ApprovalSheetModel> sheets) {
        if (sheets == null || sheets.isEmpty()) {
            throw new IllegalArgumentException("审批表内容为空");
        }
        long startedAt = System.nanoTime();
        byte[] pdf = sheets.size() <= PARALLEL_CHUNK_SIZE
                ? renderSequential(sheets)
                : renderParallelChunks(sheets);
        log.info(
                "openpdf approval sheets={} bytes={} elapsed={}ms",
                sheets.size(),
                pdf.length,
                (System.nanoTime() - startedAt) / 1_000_000L);
        return pdf;
    }

    private byte[] renderParallelChunks(List<ApprovalSheetModel> sheets) {
        List<CompletableFuture<byte[]>> futures = new ArrayList<>();
        for (int from = 0; from < sheets.size(); from += PARALLEL_CHUNK_SIZE) {
            int to = Math.min(from + PARALLEL_CHUNK_SIZE, sheets.size());
            List<ApprovalSheetModel> chunk = List.copyOf(sheets.subList(from, to));
            futures.add(CompletableFuture.supplyAsync(() -> renderSequential(chunk), reportPdfRenderExecutor));
        }
        return mergePdfParts(futures.stream().map(CompletableFuture::join).toList());
    }

    private byte[] mergePdfParts(List<byte[]> parts) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document();
            PdfSmartCopy copy = new PdfSmartCopy(document, output);
            document.open();
            for (byte[] part : parts) {
                PdfReader reader = new PdfReader(new ByteArrayInputStream(part));
                for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                    copy.addPage(copy.getImportedPage(reader, page));
                }
                reader.close();
            }
            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new IllegalStateException("合并 OpenPDF 审批表失败", exception);
        }
    }

    private byte[] renderSequential(List<ApprovalSheetModel> sheets) {
        FontCache cache = fonts == null ? resolveFonts() : fonts;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, MARGIN_SIDE, MARGIN_SIDE, MARGIN_TOP, MARGIN_BOTTOM);
            PdfWriter.getInstance(document, output);
            document.open();
            for (int i = 0; i < sheets.size(); i++) {
                if (i > 0) {
                    document.newPage();
                }
                renderSheet(document, sheets.get(i), cache);
            }
            document.close();
            return output.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("生成 OpenPDF 审批表失败", exception);
        }
    }

    private void renderSheet(Document document, ApprovalSheetModel sheet, FontCache cache) throws DocumentException {
        if (sheet.regularizationForm() && sheet.institution()) {
            renderInstitutionRegularizationSheet(document, sheet, cache);
            return;
        }
        if (sheet.internForm() && sheet.institution()) {
            renderInstitutionInternSheet(document, sheet, cache);
            return;
        }
        if (sheet.internForm()) {
            renderAgencyInternSheet(document, sheet, cache);
            return;
        }
        if (sheet.judicialForm()) {
            renderJudicialSheet(document, sheet, cache);
            return;
        }
        renderStandardSheet(document, sheet, cache);
    }

    private void renderJudicialSheet(Document document, ApprovalSheetModel sheet, FontCache cache)
            throws DocumentException {
        document.add(topline(
                phrasePair("个人编码：", text(sheet.personCode()), cache),
                new Phrase("", cache.song(10)),
                phrasePair("档案号：", text(sheet.archiveNumber()), cache)));
        document.add(judicialTitle(sheet.reportTitle(), cache));

        PdfPTable frame = newJudicialFrame();
        addMeta(frame, "姓名", text(sheet.name()), cache, 1, 1, false, true);
        addMeta(frame, "性别", text(sheet.gender()), cache, 1, 1, false, true);
        addMeta(frame, "出生日期", text(sheet.birthDate()), cache, 1, 1, false, true);
        addMeta(frame, "学历", text(sheet.education()), cache, 1, 1, true, true, true, false);

        addMeta(frame, "工作单位", text(sheet.organizationName()), cache, 1, 3, false, false);
        addMeta(frame, workStartLabelPhrase(cache), text(sheet.workStartDate()), cache, 1, 1, false, false);
        addMeta(frame, "工作年限", text(sheet.workYears()), cache, 1, 1, true, false);

        addMeta(frame, "现任职务层次", text(sheet.currentPositionName()), cache, 1, 5, false, false);
        addMeta(frame, "任职时间", text(sheet.positionStartDate()), cache, 1, 1, true, false);

        addMeta(frame, "现任法律职务", emptyText(sheet.legalPositionName()), cache, 1, 5, false, false);
        addMeta(frame, twoLineLabel("任法律职务", "时间", cache), emptyText(sheet.legalPositionStartDate()), cache, 1, 1, true, false);

        float bodyHeight = 148f * MM;
        frame.addCell(bodyMainCell(judicialComponentTable(sheet, cache, bodyHeight), bodyHeight));
        frame.addCell(bodySideCell(basisPanel(sheet, cache, BasisMode.JUDICIAL), bodyHeight));
        frame.addCell(signatureCell(sheet, cache, SIGN_HEIGHT, 8));
        document.add(frame);
    }

    private Paragraph judicialTitle(String reportTitle, FontCache cache) {
        Paragraph paragraph = new Paragraph(text(reportTitle), cache.hei(18));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(14f);
        paragraph.setSpacingAfter(16f);
        paragraph.setLeading(26f);
        return paragraph;
    }

    private PdfPTable judicialComponentTable(ApprovalSheetModel sheet, FontCache cache, float bodyHeight) {
        PdfPTable table = new PdfPTable(new float[]{8, 24, 28, 28, 12});
        table.setWidthPercentage(100);
        table.setExtendLastRow(false);

        List<ApprovalRow> rows = sheet.rows();
        int headerRows = 2;
        int contentRows = headerRows + rows.size() + 1;
        float rowH = bodyHeight / Math.max(contentRows, 1);

        PdfPCell projectHeader = wrapGridCell("项目", cache.song(11), Element.ALIGN_CENTER, true, true, rowH);
        projectHeader.setColspan(2);
        projectHeader.setRowspan(2);
        table.addCell(projectHeader);

        PdfPCell changeHeader = wrapGridCell("变动情况", cache.song(11), Element.ALIGN_CENTER, false, true, rowH);
        changeHeader.setColspan(2);
        table.addCell(changeHeader);

        PdfPCell increaseHeader = wrapGridCell("增资额", cache.song(11), Element.ALIGN_CENTER, false, true, rowH);
        increaseHeader.setRowspan(2);
        table.addCell(increaseHeader);

        table.addCell(wrapGridCell("套改前", cache.song(11), Element.ALIGN_CENTER, false, false, rowH));
        table.addCell(wrapGridCell("套改后", cache.song(11), Element.ALIGN_CENTER, false, false, rowH));

        for (int index = 0; index < rows.size(); index++) {
            ApprovalRow row = rows.get(index);
            String groupLabel = row.groupLabel();
            if (groupLabel != null && !groupLabel.isBlank()) {
                int span = 1;
                for (int look = index + 1; look < rows.size(); look++) {
                    if (!groupLabel.equals(rows.get(look).groupLabel())) {
                        break;
                    }
                    span++;
                }
                boolean groupStart = index == 0 || !groupLabel.equals(rows.get(index - 1).groupLabel());
                if (groupStart) {
                    PdfPCell group = wrapGridCell(groupLabel, cache.song(11), Element.ALIGN_CENTER, true, false, rowH);
                    group.setRowspan(span);
                    group.setNoWrap(false);
                    table.addCell(group);
                }
                table.addCell(wrapGridCell(text(row.label()), cache.song(10), Element.ALIGN_LEFT, false, false, rowH));
            } else {
                PdfPCell label = wrapGridCell(text(row.label()), cache.song(11), Element.ALIGN_LEFT, true, false, rowH);
                label.setColspan(2);
                table.addCell(label);
            }
            table.addCell(wrapGridCell(text(row.beforeText()), cache.songBold(11), Element.ALIGN_CENTER, false, false, rowH));
            table.addCell(wrapGridCell(text(row.afterText()), cache.songBold(11), Element.ALIGN_CENTER, false, false, rowH));
            table.addCell(wrapGridCell(text(row.differenceText()), cache.songBold(11), Element.ALIGN_CENTER, false, false, rowH));
        }

        ApprovalTotals totals = sheet.totals();
        PdfPCell totalLabel = wrapGridCell("月工资合计", cache.song(11), Element.ALIGN_LEFT, true, false, rowH);
        totalLabel.setColspan(2);
        table.addCell(totalLabel);
        table.addCell(wrapGridCell(
                ReportFormatSupport.formatMoney(totals.beforeAmount()),
                cache.songBold(11),
                Element.ALIGN_CENTER,
                false,
                false,
                rowH));
        table.addCell(wrapGridCell(
                ReportFormatSupport.formatMoney(totals.afterAmount()),
                cache.songBold(11),
                Element.ALIGN_CENTER,
                false,
                false,
                rowH));
        table.addCell(wrapGridCell(
                ReportFormatSupport.formatMoney(totals.difference()),
                cache.songBold(11),
                Element.ALIGN_CENTER,
                false,
                false,
                rowH));
        return table;
    }

    /** Like {@link #gridCell} but allows wrapping for long judicial labels. */
    private PdfPCell wrapGridCell(
            String text, Font font, int align, boolean firstCol, boolean firstRow, float height) {
        PdfPCell cell = gridCell(text, font, align, firstCol, firstRow, height);
        cell.setNoWrap(false);
        cell.setFixedHeight(height);
        return cell;
    }

    private String emptyText(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "——".equals(value.trim())) {
            return "";
        }
        return value.trim();
    }

    private void renderStandardSheet(Document document, ApprovalSheetModel sheet, FontCache cache)
            throws DocumentException {
        document.add(topline(
                phrasePair("单位编码：", text(sheet.organizationCode()), cache),
                phrasePair("个人编码：", text(sheet.personCode()), cache),
                phrasePair("档案号：", text(sheet.archiveNumber()), cache)));
        document.add(title(sheet.reportTitle(), cache));

        PdfPTable frame = newFrame();
        addMeta(frame, "姓名", text(sheet.name()), cache, 1, 1, false, true);
        addMeta(frame, "性别", text(sheet.gender()), cache, 1, 1, false, true);
        addMeta(frame, "出生日期", text(sheet.birthDate()), cache, 1, 1, false, true);
        addMeta(frame, "学历", text(sheet.education()), cache, 1, 1, true, true, true, false);

        addMeta(frame, "工作单位", text(sheet.organizationName()), cache, 1, 3, false, false);
        addMeta(frame, workStartLabelPhrase(cache), text(sheet.workStartDate()), cache, 1, 1, false, false);
        addMeta(frame, "工作年限", text(sheet.workYears()), cache, 1, 1, true, false);

        addMeta(frame, "现任职务", text(sheet.currentPositionName()), cache, 1, 5, false, false);
        addMeta(frame, "任职时间", text(sheet.positionStartDate()), cache, 1, 1, true, false);

        frame.addCell(bodyMainCell(componentTable(sheet, cache, true), BODY_HEIGHT));
        frame.addCell(bodySideCell(basisPanel(sheet, cache, BasisMode.STANDARD), BODY_HEIGHT));
        frame.addCell(signatureCell(sheet, cache, SIGN_HEIGHT, 8));
        document.add(frame);
    }

    private void renderAgencyInternSheet(Document document, ApprovalSheetModel sheet, FontCache cache)
            throws DocumentException {
        document.add(topline(
                phrasePair("单位编码：", text(sheet.organizationCode()), cache),
                phrasePair("个人编码：", text(sheet.personCode()), cache),
                new Phrase("", cache.song(10))));
        document.add(title(sheet.reportTitle(), cache));

        PdfPTable frame = newFrame();
        addMeta(frame, "姓名", text(sheet.name()), cache, 1, 1, false, true);
        addMeta(frame, "学历", blankDash(sheet.education()), cache, 1, 1, false, true, true, false);
        addMeta(frame, "出生日期", text(sheet.birthDate()), cache, 1, 1, false, true);
        addMeta(frame, "性别", text(sheet.gender()), cache, 1, 1, true, true);

        addMeta(frame, "学历类别", blankDash(sheet.educationCategory()), cache, 1, 1, false, false);
        addMeta(frame, "毕业学校", blankDash(sheet.school()), cache, 1, 3, false, false);
        addMeta(frame, "学制", blankDash(sheet.studyYears()), cache, 1, 1, true, false);

        addMeta(frame, "毕业时间", blankDash(sheet.graduationDate()), cache, 1, 1, false, false);
        addMeta(frame, workStartLabelPhrase(cache), blankDash(sheet.workStartDate()), cache, 1, 1, false, false);
        addMeta(frame, "现工作单位", text(sheet.organizationName()), cache, 1, 3, true, false);

        addMeta(frame, "现任职务", blankDash(sheet.currentPositionName()), cache, 1, 3, false, false);
        addMeta(frame, "见习期", formatRange(sheet.probationFrom(), sheet.probationTo()), cache, 1, 1, false, false);
        addMeta(frame, "熟练期、学徒期", blankDash(sheet.apprenticePeriod()), cache, 1, 1, true, false);

        frame.addCell(bodyMainCell(componentTable(sheet, cache, false), BODY_HEIGHT));
        frame.addCell(bodySideCell(basisPanel(sheet, cache, BasisMode.AGENCY_INTERN), BODY_HEIGHT));
        frame.addCell(signatureCell(sheet, cache, SIGN_HEIGHT, 8));
        document.add(frame);
    }

    private void renderInstitutionRegularizationSheet(Document document, ApprovalSheetModel sheet, FontCache cache)
            throws DocumentException {
        document.setMargins(MARGIN_SIDE, MARGIN_SIDE, 12f * MM, 10f * MM);

        document.add(topline(
                phrasePair("单位编码：", text(sheet.organizationCode()), cache),
                phrasePair("个人编码：", text(sheet.personCode()), cache),
                phrasePair("档案号：", text(sheet.archiveNumber()), cache)));
        document.add(title(sheet.reportTitle(), cache));

        // Wider label cols for 出生日期/最高学历/现任职务 (cols 5 & 7, 1-based).
        PdfPTable table = new PdfPTable(new float[]{10, 11, 8, 8, 12, 10, 13, 9.3f, 9.4f, 9.3f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(2);

        String highestEducation = firstNonBlank(sheet.education(), sheet.degree());
        String educationCategory = firstNonBlank(sheet.educationCategory(), "");
        float metaH = 13.5f * MM;

        addIntern(table, "姓名", blank(sheet.name()), cache, 1, 1, metaH, true);
        addIntern(table, "性别", blank(sheet.gender()), cache, 1, 1, metaH, true);
        addIntern(table, "出生日期", blank(sheet.birthDate()), cache, 1, 1, metaH, true);
        addIntern(table, "最高学历", blank(highestEducation), cache, 1, 3, metaH, true);

        addIntern(table, "毕业学校", blank(sheet.school()), cache, 1, 2, metaH, true);
        addIntern(table, "学制", blank(sheet.studyYears()), cache, 1, 1, metaH, true);
        // Narrow 毕业时间 title; give leftover width to its value.
        addIntern(table, "学历类别", blank(educationCategory), cache, 1, 2, metaH, true);
        addIntern(table, "毕业时间", blank(sheet.graduationDate()), cache, 1, 1, metaH, true);

        addIntern(table, twoLineLabel("参加工作", "时间", cache), blank(sheet.workStartDate()), cache, 1, 1, metaH);
        addIntern(table, twoLineLabel("现工作", "单位", cache), blank(sheet.organizationName()), cache, 1, 3, metaH);
        addIntern(table, "现任职务", blank(sheet.currentPositionName()), cache, 1, 3, metaH, true);

        addIntern(table, "见习期", formatRange(sheet.probationFrom(), sheet.probationTo()), cache, 1, 3, metaH, true);
        addIntern(table, twoLineLabel("熟练期、", "学徒期", cache), blankDash(sheet.apprenticePeriod()), cache, 1, 2, metaH);
        addIntern(table, twoLineLabel("下次晋档起始", "考核年度", cache), blank(sheet.stepYear()), cache, 1, 2, metaH);

        PdfPCell section = plainCell("转正定级工资待遇", cache.hei(14), Element.ALIGN_CENTER);
        section.setColspan(10);
        section.setMinimumHeight(11f * MM);
        section.setPadding(6f);
        table.addCell(section);

        PdfPCell wage = wrap(institutionRegularizationWageTable(sheet, cache));
        wage.setColspan(10);
        wage.setBorderWidth(BORDER_PT);
        wage.setBorderColor(BORDER);
        wage.setPadding(0);
        table.addCell(wage);

        addIntern(table, "基础性绩效工资：奖励性绩效工资", ratioOrDefault(sheet), cache, 6, 4);

        PdfPCell sign = wrap(signatureTable(sheet, cache, SIGN_HEIGHT_REGULARIZATION));
        sign.setColspan(10);
        sign.setFixedHeight(SIGN_HEIGHT_REGULARIZATION);
        sign.setBorderWidth(BORDER_PT);
        sign.setBorderColor(BORDER);
        sign.setPadding(0);
        table.addCell(sign);

        document.add(table);
        document.setMargins(MARGIN_SIDE, MARGIN_SIDE, MARGIN_TOP, MARGIN_BOTTOM);
    }

    private void renderInstitutionInternSheet(Document document, ApprovalSheetModel sheet, FontCache cache)
            throws DocumentException {
        // Match CSS: slightly tighter vertical padding for institution intern.
        document.setMargins(MARGIN_SIDE, MARGIN_SIDE, 12f * MM, 10f * MM);

        document.add(topline(
                new Phrase("单位编码：" + text(sheet.organizationCode()), cache.song(10)),
                new Phrase("个人编码：" + text(sheet.personCode()), cache.song(10)),
                new Phrase("", cache.song(10))));
        document.add(title(sheet.reportTitle(), cache));

        PdfPTable table = new PdfPTable(new float[]{11, 12, 9, 8, 9, 12, 9.75f, 9.75f, 9.75f, 9.75f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(2);

        String education = firstNonBlank(sheet.education(), sheet.degree());
        String degreeCell = firstNonBlank(sheet.educationCategory(), sheet.degree());

        addIntern(table, "姓名", blank(sheet.name()), cache);
        addIntern(table, "性别", blank(sheet.gender()), cache);
        addIntern(table, "出生日期", blank(sheet.birthDate()), cache);
        addIntern(table, "学历", blank(education), cache, 1, 3);

        addIntern(table, "毕业学校", blank(sheet.school()), cache);
        addIntern(table, "学制", blank(sheet.studyYears()), cache);
        addIntern(table, "学位", blank(degreeCell), cache);
        addIntern(table, "毕业时间", blank(sheet.graduationDate()), cache);
        addIntern(table, "参加工作时间", blank(sheet.workStartDate()), cache);

        addIntern(table, "现工作单位", blank(sheet.organizationName()), cache, 1, 4);
        addIntern(table, "现任职务", blank(sheet.currentPositionName()), cache, 1, 4);

        addIntern(table, "见习期", formatRange(sheet.probationFrom(), sheet.probationTo()), cache, 1, 4);
        addIntern(table, "熟练期、学徒期", formatRange("", ""), cache, 1, 4);

        addIntern(table, "下次晋档起始考核年度", emptyYear(sheet.stepYear()), cache, 1, 4);
        addIntern(table, "下次级别晋升起始考核年度", emptyYear(sheet.levelYear()), cache, 1, 4);

        PdfPCell section = plainCell("见习期工资待遇", cache.hei(14), Element.ALIGN_CENTER);
        section.setColspan(10);
        section.setMinimumHeight(11f * MM);
        section.setPadding(6f);
        table.addCell(section);

        PdfPCell wage = wrap(institutionWageTable(sheet, cache));
        wage.setColspan(10);
        wage.setBorderWidth(BORDER_PT);
        wage.setBorderColor(BORDER);
        wage.setPadding(0);
        table.addCell(wage);

        addIntern(table, "基础性绩效工资：奖励性绩效工资", ratioOrDefault(sheet), cache, 6, 4);

        PdfPCell sign = wrap(signatureTable(sheet, cache, SIGN_HEIGHT_INTERN));
        sign.setColspan(10);
        sign.setFixedHeight(SIGN_HEIGHT_INTERN);
        sign.setBorderWidth(BORDER_PT);
        sign.setBorderColor(BORDER);
        sign.setPadding(0);
        table.addCell(sign);

        document.add(table);
        // restore default margins for subsequent standard sheets in same document
        document.setMargins(MARGIN_SIDE, MARGIN_SIDE, MARGIN_TOP, MARGIN_BOTTOM);
    }

    private PdfPTable newFrame() {
        // Right block (学历/年限/任职 + 考核依据) starts earlier so its left edge aligns with upper栏.
        // Label cols stay wide enough for 出生日期/工作单位/现任职务 one-line titles.
        return newFrame(new float[]{11, 13, 11, 13, 11, 13, 15, 13});
    }

    /** 法检表：加宽首列标题，并加大姓名区总宽使「性别」后移（从性别值列匀出）。 */
    private PdfPTable newJudicialFrame() {
        return newFrame(new float[]{16, 14, 9, 11, 11, 13, 14, 12});
    }

    private PdfPTable newFrame(float[] relativeWidths) {
        PdfPTable frame = new PdfPTable(relativeWidths);
        frame.setWidthPercentage(100);
        frame.setSpacingBefore(2);
        // Keep meta + body + signature on one page (avoid signature alone on next page).
        frame.setSplitLate(false);
        frame.setSplitRows(false);
        return frame;
    }

    private PdfPTable componentTable(ApprovalSheetModel sheet, FontCache cache, boolean withRatio) {
        PdfPTable table = new PdfPTable(new float[]{34, 26, 26, 14});
        table.setWidthPercentage(100);
        // Do not stretch the last data row — that hid 合计/绩效比例 when body height was tight.
        table.setExtendLastRow(false);

        boolean showRatio = withRatio && sheet.institution();
        // header + wage items + 月工资合计 + optional 绩效比例 — all same height.
        int contentRows = 1 + sheet.rows().size() + 1 + (showRatio ? 1 : 0);
        float rowH = BODY_HEIGHT / Math.max(contentRows, 1);

        // OpenPDF has no border-collapse: only draw left+top so adjacent edges stay 1pt.
        table.addCell(gridCell("项目", cache.song(11), Element.ALIGN_CENTER, true, true, rowH));
        table.addCell(gridCell("变动前", cache.song(11), Element.ALIGN_CENTER, false, true, rowH));
        table.addCell(gridCell("变动后", cache.song(11), Element.ALIGN_CENTER, false, true, rowH));
        table.addCell(gridCell("增资额", cache.song(11), Element.ALIGN_CENTER, false, true, rowH));

        for (ApprovalRow row : sheet.rows()) {
            table.addCell(gridCell(text(row.label()), cache.song(11), Element.ALIGN_LEFT, true, false, rowH));
            table.addCell(gridCell(text(row.beforeText()), cache.songBold(11), Element.ALIGN_CENTER, false, false, rowH));
            table.addCell(gridCell(text(row.afterText()), cache.songBold(11), Element.ALIGN_CENTER, false, false, rowH));
            table.addCell(gridCell(text(row.differenceText()), cache.songBold(11), Element.ALIGN_CENTER, false, false, rowH));
        }

        ApprovalTotals totals = sheet.totals();
        table.addCell(gridCell("月工资合计", cache.song(11), Element.ALIGN_LEFT, true, false, rowH));
        table.addCell(gridCell(
                ReportFormatSupport.formatMoney(totals.beforeAmount()),
                cache.songBold(11),
                Element.ALIGN_CENTER,
                false,
                false,
                rowH));
        table.addCell(gridCell(
                ReportFormatSupport.formatMoney(totals.afterAmount()),
                cache.songBold(11),
                Element.ALIGN_CENTER,
                false,
                false,
                rowH));
        table.addCell(gridCell(
                ReportFormatSupport.formatMoney(totals.difference()),
                cache.songBold(11),
                Element.ALIGN_CENTER,
                false,
                false,
                rowH));

        if (showRatio) {
            PdfPCell label = gridCell(
                    "基础性绩效工资与奖励性绩效工资比例",
                    cache.song(10),
                    Element.ALIGN_CENTER,
                    true,
                    false,
                    rowH);
            label.setNoWrap(false);
            label.setColspan(2);
            table.addCell(label);
            PdfPCell value = gridCell(ratioOrDefault(sheet), cache.songBold(10), Element.ALIGN_CENTER, false, false, rowH);
            value.setColspan(2);
            table.addCell(value);
        }
        return table;
    }

    private PdfPTable basisPanel(ApprovalSheetModel sheet, FontCache cache, BasisMode mode) {
        PdfPTable panel = new PdfPTable(1);
        panel.setWidthPercentage(100);

        PdfPTable years = new PdfPTable(new float[]{68, 32});
        years.setWidthPercentage(100);
        if (mode == BasisMode.AGENCY_INTERN) {
            addYear(years, "下次晋档\n起始考核年度", text(sheet.stepYear()), cache);
            addYear(years, "下次级别晋升\n起始考核年度", text(sheet.levelYear()), cache);
        } else if (mode == BasisMode.JUDICIAL) {
            addYear(years, "下一次晋档\n起始年度", text(sheet.stepYear()), cache);
            addYear(years, "下一次晋级\n起始年度", text(sheet.levelYear()), cache);
        } else if (sheet.institution()) {
            addYear(years, "下次薪级晋升\n起始考核年度", text(sheet.stepYear()), cache);
        } else {
            addYear(years, "下次档次晋升\n起始考核年度", text(sheet.stepYear()), cache);
            addYear(years, "下次级别晋升\n起始考核年度", text(sheet.levelYear()), cache);
        }
        PdfPCell yearsWrap = wrap(years);
        yearsWrap.setBorder(PdfPCell.NO_BORDER);
        yearsWrap.setPadding(0);
        panel.addCell(yearsWrap);

        Paragraph basis = new Paragraph();
        basis.setLeading(17f);
        basis.add(new Chunk("工资变动原因及依据\n", cache.song(10)));
        basis.add(new Chunk(text(sheet.basisTitle()) + "\n", cache.songBold(11)));
        basis.add(new Chunk("执行时间：\n", cache.song(10)));
        basis.add(new Chunk(text(sheet.executionPeriod()) + "\n", cache.songBold(11)));
        if ((mode == BasisMode.STANDARD || mode == BasisMode.JUDICIAL) && sheet.basisDetailLines() != null) {
            for (String line : sheet.basisDetailLines()) {
                if (line != null && !line.isBlank()) {
                    basis.add(new Chunk(line.trim() + "\n", cache.song(10)));
                }
            }
        }
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(PdfPCell.NO_BORDER);
        textCell.setPadding(5f);
        textCell.addElement(basis);
        panel.addCell(textCell);
        return panel;
    }

    private void addYear(PdfPTable years, String label, String value, FontCache cache) {
        PdfPCell l = new PdfPCell(new Phrase(label, cache.song(10)));
        l.setHorizontalAlignment(Element.ALIGN_CENTER);
        l.setVerticalAlignment(Element.ALIGN_MIDDLE);
        l.setMinimumHeight(16f * MM);
        l.setPadding(3f);
        applySharedBorders(l, true, true, false, true);
        years.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, cache.songBold(10)));
        v.setHorizontalAlignment(Element.ALIGN_CENTER);
        v.setVerticalAlignment(Element.ALIGN_MIDDLE);
        v.setMinimumHeight(16f * MM);
        v.setPadding(3f);
        applySharedBorders(v, false, true, false, true);
        years.addCell(v);
    }

    private PdfPTable signatureTable(ApprovalSheetModel sheet, FontCache cache, float height) {
        // ~26px label + 30% + 30% + 34% boxes (CSS ratios)
        PdfPTable table = new PdfPTable(new float[]{7, 29, 7, 29, 7, 31});
        table.setWidthPercentage(100);
        table.addCell(verticalLabel("单位意见", cache.song(13), height, true, false));
        table.addCell(signatureBox("同 意", null, cache, height, false, false));
        table.addCell(verticalLabel("主管部门意见", cache.song(13), height, false, false));
        table.addCell(signatureBox("同意单位意见", null, cache, height, false, false));
        table.addCell(verticalLabel("批准机关意见", cache.song(13), height, false, false));
        table.addCell(signatureBox(
                "同意主管部门意见",
                "从 " + text(sheet.executionYear()) + " 年 " + text(sheet.executionMonth()) + " 月执行",
                cache,
                height,
                false,
                true));
        return table;
    }

    private PdfPCell signatureBox(
            String title, String extra, FontCache cache, float height, boolean first, boolean last) {
        float dateBand = 12f * MM;
        float bodyBand = Math.max(height - dateBand, 18f * MM);

        Paragraph content = new Paragraph();
        content.setAlignment(Element.ALIGN_CENTER);
        content.setLeading(15f);
        content.add(new Chunk(title + "\n", cache.songBold(16)));
        if (extra != null && !extra.isBlank()) {
            content.add(Chunk.NEWLINE);
            content.add(new Chunk(extra + "\n", cache.songBold(12)));
        }

        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);
        PdfPCell body = new PdfPCell();
        body.setBorder(PdfPCell.NO_BORDER);
        body.setFixedHeight(bodyBand);
        body.setVerticalAlignment(Element.ALIGN_MIDDLE);
        body.setHorizontalAlignment(Element.ALIGN_CENTER);
        body.setPaddingTop(4f);
        body.setPaddingBottom(2f);
        body.addElement(content);
        inner.addCell(body);

        PdfPCell date = new PdfPCell(new Phrase("年　　月　　日", cache.song(12)));
        date.setBorder(PdfPCell.NO_BORDER);
        date.setFixedHeight(dateBand);
        date.setHorizontalAlignment(Element.ALIGN_RIGHT);
        date.setVerticalAlignment(Element.ALIGN_MIDDLE);
        date.setPaddingRight(8f);
        date.setPaddingBottom(4f);
        inner.addCell(date);

        PdfPCell cell = wrap(inner);
        cell.setFixedHeight(height);
        applySharedBorders(cell, false, false, last, true);
        cell.setPadding(0);
        return cell;
    }

    private PdfPCell verticalLabel(String text, Font font, float height, boolean first, boolean last) {
        StringBuilder vertical = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (i > 0) {
                vertical.append('\n');
            }
            vertical.append(text.charAt(i));
        }
        PdfPCell cell = new PdfPCell(new Phrase(vertical.toString(), font));
        cell.setFixedHeight(height);
        cell.setLeading(14f, 0f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        applySharedBorders(cell, false, false, last, true);
        return cell;
    }

    private PdfPTable institutionRegularizationWageTable(ApprovalSheetModel sheet, FontCache cache) {
        String[][] left = {
                {"执行工资岗位等级", internRowValue(sheet, "执行工资岗位等级")},
                {"薪级", internRowValue(sheet, "薪级")},
                {"岗位工资", internRowValue(sheet, "岗位工资")},
                {"薪级工资", internRowValue(sheet, "薪级工资")},
                {"教护提高部分", internRowValue(sheet, "教护提高部分")},
                {"教护龄津贴", internRowValue(sheet, "教护龄津贴")},
                {"保留副补", internRowValue(sheet, "保留副补")},
                {"保留奖金", internRowValue(sheet, "保留奖金")},
                {"工改保留津贴", internRowValue(sheet, "工改保留津贴")}
        };
        String[][] right = {
                {"基础绩效", internRowValue(sheet, "基础绩效")},
                {"浮动工资", internRowValue(sheet, "浮动工资")},
                {"特殊岗位津贴", internRowValue(sheet, "特殊岗位津贴")},
                {"特岗保留部分", internRowValue(sheet, "特岗保留部分")},
                {"农村学校教师补贴", internRowValue(sheet, "农村学校教师补贴")},
                {"其它补贴", internRowValue(sheet, "其它补贴")},
                {"", ""},
                {"", ""},
                {"月工资合计", internTotal(sheet.totals().afterAmount())}
        };
        return buildInstitutionWagePairTable(left, right, cache, true);
    }

    private PdfPTable institutionWageTable(ApprovalSheetModel sheet, FontCache cache) {
        String[][] left = {
                {"执行工资岗位等级", internRowValue(sheet, "执行工资岗位等级")},
                {"见习工资", internRowValue(sheet, "见习工资")},
                {"薪级工资", internRowValue(sheet, "薪级工资")},
                {"教护提高部分", internRowValue(sheet, "教护提高部分")},
                {"教护龄津贴", internRowValue(sheet, "教护龄津贴")},
                {"保留副补", internRowValue(sheet, "保留副补")},
                {"保留奖金", internRowValue(sheet, "保留奖金")},
                {"工改保留津贴", internRowValue(sheet, "工改保留津贴")}
        };
        String[][] right = {
                {"基础绩效", internRowValue(sheet, "基础绩效")},
                {"警衔津贴", internRowValue(sheet, "警衔津贴")},
                {"特殊岗位津贴", internRowValue(sheet, "特殊岗位津贴")},
                {"特岗保留部分", internRowValue(sheet, "特岗保留部分")},
                {"农村学校教师补贴", internRowValue(sheet, "农村学校教师补贴")},
                {"其它补贴", internRowValue(sheet, "其它补贴")},
                {"", ""},
                {"月工资合计", internTotal(sheet.totals().afterAmount())}
        };
        return buildInstitutionWagePairTable(left, right, cache, false);
    }

    private PdfPTable buildInstitutionWagePairTable(
            String[][] left, String[][] right, FontCache cache, boolean keepEmptyRightBorder) {
        PdfPTable wage = new PdfPTable(new float[]{28, 22, 28, 22});
        wage.setWidthPercentage(100);
        for (int i = 0; i < left.length; i++) {
            PdfPCell l = plainCell(left[i][0], cache.song(11), Element.ALIGN_LEFT);
            l.setMinimumHeight(10f * MM);
            l.setPaddingLeft(8f);
            wage.addCell(l);
            PdfPCell lv = plainCell(left[i][1], cache.songBold(11), Element.ALIGN_CENTER);
            lv.setMinimumHeight(10f * MM);
            wage.addCell(lv);
            if (right[i][0].isEmpty()) {
                if (keepEmptyRightBorder) {
                    // Keep 其它补贴 label right-border through empty rows down to 月工资合计.
                    PdfPCell emptyLabel = plainCell("", cache.song(11), Element.ALIGN_LEFT);
                    emptyLabel.setMinimumHeight(10f * MM);
                    wage.addCell(emptyLabel);
                    PdfPCell emptyValue = plainCell("", cache.song(11), Element.ALIGN_CENTER);
                    emptyValue.setMinimumHeight(10f * MM);
                    wage.addCell(emptyValue);
                } else {
                    PdfPCell empty = plainCell("", cache.song(11), Element.ALIGN_CENTER);
                    empty.setColspan(2);
                    empty.setMinimumHeight(10f * MM);
                    wage.addCell(empty);
                }
            } else {
                PdfPCell r = plainCell(right[i][0], cache.song(11), Element.ALIGN_LEFT);
                r.setMinimumHeight(10f * MM);
                r.setPaddingLeft(8f);
                wage.addCell(r);
                PdfPCell rv = plainCell(right[i][1], cache.songBold(11), Element.ALIGN_CENTER);
                rv.setMinimumHeight(10f * MM);
                wage.addCell(rv);
            }
        }
        return wage;
    }

    private PdfPTable topline(Phrase left, Phrase middle, Phrase right) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(2);
        table.addCell(borderless(left, Element.ALIGN_LEFT));
        table.addCell(borderless(middle, Element.ALIGN_CENTER));
        table.addCell(borderless(right, Element.ALIGN_RIGHT));
        return table;
    }

    private Phrase phrasePair(String label, String value, FontCache cache) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label, cache.song(10)));
        phrase.add(new Chunk(value, cache.songBold(10)));
        return phrase;
    }

    private Paragraph title(String reportTitle, FontCache cache) {
        Paragraph paragraph = new Paragraph(text(reportTitle), cache.hei(22));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(10f);
        paragraph.setSpacingAfter(16f);
        paragraph.setLeading(24f);
        return paragraph;
    }

    private Phrase workStartLabelPhrase(FontCache cache) {
        return twoLineLabel("参加工作", "时间", cache);
    }

    private Phrase twoLineLabel(String firstLine, String secondLine, FontCache cache) {
        Phrase phrase = new Phrase();
        phrase.setLeading(13f);
        phrase.add(new Chunk(firstLine + "\n", cache.song(11)));
        phrase.add(new Chunk(secondLine, cache.song(11)));
        return phrase;
    }

    private void addMeta(PdfPTable table, String label, String value, FontCache cache) {
        addMeta(table, label, value, cache, 1, 1, false, false);
    }

    private void addMeta(
            PdfPTable table, String label, String value, FontCache cache, int labelSpan, int valueSpan) {
        addMeta(table, label, value, cache, labelSpan, valueSpan, false, false);
    }

    private void addMeta(
            PdfPTable table,
            String label,
            String value,
            FontCache cache,
            int labelSpan,
            int valueSpan,
            boolean rowEnd,
            boolean firstRow) {
        addMeta(table, new Phrase(label == null ? "" : label, cache.song(11)), value, cache, labelSpan, valueSpan, rowEnd, firstRow, true, true);
    }

    private void addMeta(
            PdfPTable table,
            String label,
            String value,
            FontCache cache,
            int labelSpan,
            int valueSpan,
            boolean rowEnd,
            boolean firstRow,
            boolean labelNoWrap,
            boolean valueNoWrap) {
        addMeta(
                table,
                new Phrase(label == null ? "" : label, cache.song(11)),
                value,
                cache,
                labelSpan,
                valueSpan,
                rowEnd,
                firstRow,
                labelNoWrap,
                valueNoWrap);
    }

    private void addMeta(
            PdfPTable table,
            Phrase label,
            String value,
            FontCache cache,
            int labelSpan,
            int valueSpan,
            boolean rowEnd,
            boolean firstRow) {
        addMeta(table, label, value, cache, labelSpan, valueSpan, rowEnd, firstRow, false, true);
    }

    private void addMeta(
            PdfPTable table,
            Phrase label,
            String value,
            FontCache cache,
            int labelSpan,
            int valueSpan,
            boolean rowEnd,
            boolean firstRow,
            boolean labelNoWrap,
            boolean valueNoWrap) {
        PdfPCell l = metaCell(label, Element.ALIGN_CENTER, false, firstRow, labelNoWrap);
        l.setColspan(labelSpan);
        table.addCell(l);
        PdfPCell v = metaCell(
                new Phrase(value == null ? "" : value, cache.songBold(11)),
                Element.ALIGN_CENTER,
                rowEnd,
                firstRow,
                valueNoWrap);
        v.setColspan(valueSpan);
        table.addCell(v);
    }

    private PdfPCell metaCell(
            Phrase phrase, int align, boolean drawRight, boolean firstRow, boolean noWrap) {
        PdfPCell cell = new PdfPCell(phrase == null ? new Phrase("") : phrase);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        cell.setFixedHeight(META_ROW_HEIGHT);
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setNoWrap(noWrap);
        if (!noWrap) {
            cell.setLeading(12f, 0f);
        }
        // left+bottom; first row also top. No right except row end — avoids thick stacked lines.
        applySharedBorders(cell, false, !firstRow, drawRight, true);
        return cell;
    }

    private void addIntern(PdfPTable table, String label, String value, FontCache cache) {
        addIntern(table, label, value, cache, 1, 1, 10.5f * MM, true);
    }

    private void addIntern(
            PdfPTable table, String label, String value, FontCache cache, int labelSpan, int valueSpan) {
        addIntern(table, label, value, cache, labelSpan, valueSpan, 10.5f * MM, true);
    }

    private void addIntern(
            PdfPTable table,
            String label,
            String value,
            FontCache cache,
            int labelSpan,
            int valueSpan,
            float height,
            boolean labelNoWrap) {
        addIntern(table, new Phrase(label == null ? "" : label, cache.song(11)), value, cache, labelSpan, valueSpan, height, labelNoWrap);
    }

    private void addIntern(
            PdfPTable table, Phrase label, String value, FontCache cache, int labelSpan, int valueSpan, float height) {
        addIntern(table, label, value, cache, labelSpan, valueSpan, height, false);
    }

    private void addIntern(
            PdfPTable table,
            Phrase label,
            String value,
            FontCache cache,
            int labelSpan,
            int valueSpan,
            float height,
            boolean labelNoWrap) {
        PdfPCell l = new PdfPCell(label == null ? new Phrase("") : label);
        l.setHorizontalAlignment(Element.ALIGN_CENTER);
        l.setVerticalAlignment(Element.ALIGN_MIDDLE);
        l.setColspan(labelSpan);
        l.setMinimumHeight(height);
        l.setPadding(4f);
        l.setBorderWidth(BORDER_PT);
        l.setBorderColor(BORDER);
        l.setUseAscender(true);
        l.setUseDescender(true);
        l.setNoWrap(labelNoWrap);
        if (!labelNoWrap) {
            l.setLeading(12f, 0f);
        }
        table.addCell(l);
        PdfPCell v = plainCell(value, cache.songBold(11), Element.ALIGN_CENTER);
        v.setColspan(valueSpan);
        v.setMinimumHeight(height);
        v.setPadding(4f);
        table.addCell(v);
    }

    private PdfPCell bodyMainCell(PdfPTable nested, float height) {
        PdfPCell cell = wrap(nested);
        cell.setColspan(6);
        cell.setFixedHeight(height);
        // Match CSS: no bottom (signature draws it); no top (meta row already has bottom).
        cell.setBorderWidthLeft(BORDER_PT);
        cell.setBorderWidthRight(BORDER_PT);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthBottom(0);
        cell.setBorderColor(BORDER);
        cell.setPadding(0);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private PdfPCell bodySideCell(PdfPTable nested, float height) {
        PdfPCell cell = wrap(nested);
        cell.setColspan(2);
        cell.setFixedHeight(height);
        // Match CSS: border-left 0 (shared with main), no top/bottom to avoid thick seams.
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(BORDER_PT);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthBottom(0);
        cell.setBorderColor(BORDER);
        cell.setPadding(0);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private PdfPCell signatureCell(ApprovalSheetModel sheet, FontCache cache, float height, int colspan) {
        // Outer cell has no border — signature table cells draw the frame (avoids double thick line).
        PdfPCell cell = wrap(signatureTable(sheet, cache, height));
        cell.setColspan(colspan);
        cell.setFixedHeight(height);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(0);
        return cell;
    }

    private PdfPCell wrap(PdfPTable nested) {
        PdfPCell cell = new PdfPCell(nested);
        cell.setPadding(0);
        return cell;
    }

    /**
     * Nested grid cell without border-collapse: draw only left+top.
     * First column omits left (parent frame), first row omits top (meta bottom).
     */
    private PdfPCell gridCell(
            String text, Font font, int align, boolean firstCol, boolean firstRow, float height) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // Keep padding small so fixed row heights stay inside BODY_HEIGHT (合计/比例不能被挤掉).
        cell.setPaddingTop(1.5f);
        cell.setPaddingBottom(1.5f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setFixedHeight(height);
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setNoWrap(true);
        applySharedBorders(cell, firstCol, firstRow, false, false);
        return cell;
    }

    /**
     * OpenPDF has no border-collapse. Draw left/top only so shared edges stay 1pt;
     * optionally draw right/bottom for outer edges (signature boxes).
     */
    private void applySharedBorders(
            PdfPCell cell, boolean omitLeft, boolean omitTop, boolean drawRight, boolean drawBottom) {
        cell.setBorderColor(BORDER);
        cell.setBorderWidthLeft(omitLeft ? 0 : BORDER_PT);
        cell.setBorderWidthTop(omitTop ? 0 : BORDER_PT);
        cell.setBorderWidthRight(drawRight ? BORDER_PT : 0);
        cell.setBorderWidthBottom(drawBottom ? BORDER_PT : 0);
    }

    private PdfPCell plainCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setBorderWidth(BORDER_PT);
        cell.setBorderColor(BORDER);
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        return cell;
    }

    private PdfPCell borderless(Phrase phrase, int align) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setPadding(1f);
        return cell;
    }

    private String internRowValue(ApprovalSheetModel sheet, String label) {
        for (ApprovalRow row : sheet.rows()) {
            if (label.equals(row.label())) {
                return internValue(row);
            }
        }
        return "——";
    }

    private String internValue(ApprovalRow row) {
        String value = row.afterText();
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "0".equals(value.trim())
                || "——".equals(value.trim())) {
            return "——";
        }
        String normalized = value.trim().replace(",", "");
        return "0".equals(normalized) ? "——" : normalized;
    }

    private String internTotal(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "——";
        }
        return new DecimalFormat("#0").format(value);
    }

    private String ratioOrDefault(ApprovalSheetModel sheet) {
        return sheet.performanceRatio() == null || sheet.performanceRatio().isBlank()
                ? "7:3"
                : sheet.performanceRatio();
    }

    private String formatRange(String from, String to) {
        String start = sanitizeDash(from);
        String end = sanitizeDash(to);
        if (start.isEmpty() && end.isEmpty()) {
            return "至";
        }
        if (end.isEmpty()) {
            return start + " 至";
        }
        if (start.isEmpty()) {
            return "至 " + end;
        }
        return start + " 至 " + end;
    }

    private String sanitizeDash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "-".equals(trimmed) || "——".equals(trimmed)) {
            return "";
        }
        return trimmed;
    }

    private String emptyYear(String value) {
        String trimmed = sanitizeDash(value);
        return trimmed;
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankDash(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return "——";
        }
        return value.trim();
    }

    private String firstNonBlank(String primary, String secondary) {
        if (primary != null && !primary.isBlank() && !"-".equals(primary.trim())) {
            return primary.trim();
        }
        if (secondary != null && !secondary.isBlank() && !"-".equals(secondary.trim())) {
            return secondary.trim();
        }
        return "";
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private FontCache resolveFonts() {
        BaseFont song = loadBaseFont(List.of(
                "C:/Windows/Fonts/simsun.ttc,0",
                "C:/Windows/Fonts/simsun.ttc,1",
                "C:/Windows/Fonts/simfang.ttf",
                "C:/Windows/Fonts/msyh.ttf",
                "/usr/share/fonts/google-noto-cjk/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0"));
        BaseFont hei = loadBaseFont(List.of(
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/msyh.ttf",
                "C:/Windows/Fonts/simsun.ttc,0",
                "/usr/share/fonts/google-noto-cjk/NotoSansCJK-Bold.ttc,0",
                "/usr/share/fonts/google-noto-cjk/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0"));
        if (song == null) {
            throw new IllegalStateException("未找到可用于 OpenPDF 的宋体字体");
        }
        if (hei == null) {
            hei = song;
        }
        return new FontCache(song, hei);
    }

    private BaseFont loadBaseFont(List<String> candidates) {
        for (String candidate : candidates) {
            String path = candidate.contains(",") ? candidate.substring(0, candidate.indexOf(',')) : candidate;
            if (!Files.isRegularFile(Path.of(path))) {
                continue;
            }
            try {
                return BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (DocumentException | IOException ignored) {
                // try next
            }
        }
        return null;
    }

    private enum BasisMode {
        STANDARD,
        AGENCY_INTERN,
        JUDICIAL
    }

    private record FontCache(BaseFont songFont, BaseFont heiFont) {
        Font song(float size) {
            return new Font(songFont, size, Font.NORMAL);
        }

        Font songBold(float size) {
            // Fake bold via Font.BOLD — OpenPDF synthesizes stroke for CJK TTF
            return new Font(songFont, size, Font.BOLD);
        }

        Font hei(float size) {
            return new Font(heiFont, size, Font.NORMAL);
        }
    }
}
