package com.dxsoft.rsgzgl.report.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
class ReportPdfService {

    private volatile FontCache fontCache;

    @PostConstruct
    void warmFontCache() {
        fontCache = resolveFontCache();
    }

    byte[] renderPdf(String html) {
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
        for (FontRegistration registration : cache.registrations()) {
            builder.useFont(registration.file(), registration.family());
        }
    }

    private FontCache resolveFontCache() {
        List<Path> preferredTtf = List.of(
                Path.of("C:/Windows/Fonts/simhei.ttf"),
                Path.of("C:/Windows/Fonts/simkai.ttf"),
                Path.of("C:/Windows/Fonts/simfang.ttf"),
                Path.of("C:/Windows/Fonts/msyh.ttf"),
                Path.of("C:/Windows/Fonts/msyhbd.ttf"),
                Path.of("C:/Windows/Fonts/simsunb.ttf"));
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
            throw new IllegalStateException("未找到可用于 PDF 的中文字体，请安装黑体/楷体/微软雅黑或 Noto CJK");
        }

        List<FontRegistration> registrations = new ArrayList<>();
        for (String family : List.of("SimSun", "Songti SC", "SimHei", "Microsoft YaHei", "Noto Sans CJK SC")) {
            registrations.add(new FontRegistration(primary, family));
        }

        Map<String, Path> extras = new LinkedHashMap<>();
        extras.put("SimHei", Path.of("C:/Windows/Fonts/simhei.ttf"));
        extras.put("Microsoft YaHei", Path.of("C:/Windows/Fonts/msyh.ttf"));
        extras.put("KaiTi", Path.of("C:/Windows/Fonts/simkai.ttf"));
        for (Map.Entry<String, Path> entry : extras.entrySet()) {
            Path path = entry.getValue();
            if (Files.isRegularFile(path) && !path.toFile().equals(primary)) {
                registrations.add(new FontRegistration(path.toFile(), entry.getKey()));
            }
        }
        return new FontCache(registrations);
    }

    private record FontRegistration(File file, String family) {
    }

    private record FontCache(List<FontRegistration> registrations) {
    }
}
