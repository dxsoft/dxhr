package com.dxsoft.rsgzgl.report.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ReportPdfService {

    private static final Logger log = LoggerFactory.getLogger(ReportPdfService.class);

    /** CSS 实际用到的中文字体族；映射到同一份 TTF，避免重复加载多套大字体。 */
    private static final List<String> FONT_FAMILIES = List.of("SimSun", "SimHei");

    private final ExecutorService reportPdfRenderExecutor;
    private volatile FontCache fontCache;

    ReportPdfService(@Qualifier("reportPdfRenderExecutor") ExecutorService reportPdfRenderExecutor) {
        this.reportPdfRenderExecutor = reportPdfRenderExecutor;
    }

    @PostConstruct
    void warmFontCache() {
        fontCache = resolveFontCache();
    }

    public byte[] renderPdf(String html) {
        String xhtml = sanitizeForXhtml(html);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerFonts(builder);
            builder.withHtmlContent(xhtml, "/");
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("生成 PDF 失败", exception);
        }
    }

    /**
     * Render multiple HTML documents in parallel and merge into one PDF, preserving order.
     */
    public byte[] renderAndMergePdfs(List<String> htmlDocuments) {
        if (htmlDocuments == null || htmlDocuments.isEmpty()) {
            throw new IllegalArgumentException("PDF 内容为空");
        }
        if (htmlDocuments.size() == 1) {
            return renderPdf(htmlDocuments.getFirst());
        }
        long startedAt = System.nanoTime();
        List<CompletableFuture<byte[]>> futures = new ArrayList<>(htmlDocuments.size());
        for (String html : htmlDocuments) {
            futures.add(CompletableFuture.supplyAsync(() -> renderPdf(html), reportPdfRenderExecutor));
        }
        List<byte[]> parts = new ArrayList<>(htmlDocuments.size());
        for (CompletableFuture<byte[]> future : futures) {
            parts.add(future.join());
        }
        long renderedAt = System.nanoTime();
        byte[] merged = mergePdfParts(parts);
        log.info(
                "renderAndMergePdfs parts={} render={}ms merge={}ms total={}ms bytes={}",
                parts.size(),
                (renderedAt - startedAt) / 1_000_000L,
                (System.nanoTime() - renderedAt) / 1_000_000L,
                (System.nanoTime() - startedAt) / 1_000_000L,
                merged.length);
        return merged;
    }

    private byte[] mergePdfParts(List<byte[]> parts) {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            merger.setDestinationStream(output);
            for (byte[] part : parts) {
                merger.addSource(new ByteArrayInputStream(part));
            }
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("合并 PDF 失败", exception);
        }
    }

    /**
     * OpenHTMLToPDF parses as XML; HTML-named entities like {@code &nbsp;} are not declared.
     */
    private String sanitizeForXhtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return html
                .replace("&nbsp;", "&#160;")
                .replace("&NBSP;", "&#160;")
                .replace("&ensp;", "&#8194;")
                .replace("&emsp;", "&#8195;")
                .replace("&thinsp;", "&#8201;")
                .replace("&mdash;", "&#8212;")
                .replace("&ndash;", "&#8211;")
                .replace("&ldquo;", "&#8220;")
                .replace("&rdquo;", "&#8221;")
                .replace("&lsquo;", "&#8216;")
                .replace("&rsquo;", "&#8217;")
                .replace("&copy;", "&#169;")
                .replace("&reg;", "&#174;")
                .replace("&trade;", "&#8482;")
                .replace("&times;", "&#215;")
                .replace("&divide;", "&#247;")
                .replace("&middot;", "&#183;");
    }

    private void registerFonts(PdfRendererBuilder builder) {
        FontCache cache = fontCache;
        if (cache == null) {
            cache = resolveFontCache();
            fontCache = cache;
        }
        File font = cache.fontFile();
        for (String family : FONT_FAMILIES) {
            builder.useFont(font, family);
        }
    }

    private FontCache resolveFontCache() {
        // 优先单文件 TTF；TTC 在 OpenHTMLToPDF 上解析更慢且兼容性较差。
        List<Path> preferredTtf = List.of(
                Path.of("C:/Windows/Fonts/simhei.ttf"),
                Path.of("C:/Windows/Fonts/msyh.ttf"),
                Path.of("C:/Windows/Fonts/simfang.ttf"),
                Path.of("C:/Windows/Fonts/simkai.ttf"));
        List<Path> fallbackTtc = List.of(
                Path.of("C:/Windows/Fonts/simsun.ttc"),
                Path.of("C:/Windows/Fonts/msyh.ttc"),
                Path.of("/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc"),
                Path.of("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"));

        File primary = null;
        for (Path path : preferredTtf) {
            if (Files.isRegularFile(path)) {
                primary = path.toFile();
                break;
            }
        }
        if (primary == null) {
            for (Path path : fallbackTtc) {
                if (Files.isRegularFile(path)) {
                    primary = path.toFile();
                    break;
                }
            }
        }
        if (primary == null) {
            throw new IllegalStateException("未找到可用于 PDF 的中文字体，请安装宋体/黑体/微软雅黑或 Noto CJK");
        }
        return new FontCache(primary);
    }

    private record FontCache(File fontFile) {
    }
}
