package com.dxsoft.rsgzgl.backup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.springframework.stereotype.Component;

@Component
class BackupPackageInspector {

    static final String NEW_MANIFEST = "rsgzgl-backup.json";
    static final String NEW_MARKER = "rsgzgl-backup.id";
    static final String NEW_FORMAT_ID = "RSGZGL_BACKUP_V1";
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\"format\"\\s*:\\s*\"([^\"]+)\"");

    BackupInspectResult inspect(Path archive) throws IOException {
        Path extractDir = Files.createTempDirectory("rsgzgl-bak-inspect-");
        try {
            unzip(archive, extractDir);
            return inspectExtracted(extractDir);
        } finally {
            deleteRecursively(extractDir);
        }
    }

    BackupInspectResult inspectExtracted(Path extractDir) throws IOException {
        Optional<Path> marker2026 = findFile(extractDir, "xxbak2026.id");
        Optional<Path> marker2025 = findFile(extractDir, "xxbak2025.id");
        Optional<Path> marker2016 = findFile(extractDir, "xxbak2016.id");
        Optional<Path> markerOld = findFile(extractDir, "xxbak.id");
        Optional<Path> newManifest = findFile(extractDir, NEW_MANIFEST);
        Optional<Path> newMarker = findFile(extractDir, NEW_MARKER);

        if (newManifest.isPresent() || newMarker.isPresent()) {
            String version = NEW_FORMAT_ID;
            if (newManifest.isPresent()) {
                String json = Files.readString(newManifest.get(), StandardCharsets.UTF_8);
                Matcher matcher = FORMAT_PATTERN.matcher(json);
                if (matcher.find()) {
                    version = matcher.group(1);
                }
            } else {
                version = Files.readString(newMarker.get(), StandardCharsets.UTF_8).trim();
            }
            List<String> tables = listRelative(extractDir, "tables").stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".csv"))
                    .sorted()
                    .toList();
            List<String> tableNames = tables.stream()
                    .map(BackupPackageInspector::csvTableName)
                    .toList();
            return new BackupInspectResult(
                    BackupFormat.NEW,
                    "新系统备份",
                    newManifest.map(p -> NEW_MANIFEST).orElse(NEW_MARKER),
                    version,
                    tables,
                    BackupTableScopes.matchScopes(tableNames),
                    null,
                    "已识别为新系统备份包（" + version + "）。");
        }

        if (marker2026.isPresent() || marker2025.isPresent() || marker2016.isPresent() || markerOld.isPresent()) {
            String markerName = marker2026.map(p -> p.getFileName().toString())
                    .or(() -> marker2025.map(p -> p.getFileName().toString()))
                    .or(() -> marker2016.map(p -> p.getFileName().toString()))
                    .orElse("xxbak.id");
            String legacyVersion = marker2026.isPresent() ? "2026"
                    : marker2025.isPresent() ? "2025"
                    : marker2016.isPresent() ? "2016"
                    : "legacy";
            List<String> dbfs = listFiles(extractDir).stream()
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.toLowerCase(Locale.ROOT).endsWith("2.dbf"))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            String orgHint = findFile(extractDir, "cyxx2.dbf").isPresent() ? "含 cyxx2.dbf（单位信息快照）" : null;
            List<String> legacyTables = collectDbfTableNames(extractDir);
            return new BackupInspectResult(
                    BackupFormat.LEGACY,
                    "旧系统备份",
                    markerName,
                    legacyVersion,
                    dbfs,
                    BackupTableScopes.matchScopes(legacyTables),
                    orgHint,
                    "已识别为旧系统备份包（标识文件 " + markerName + "），将按旧办法从 DBF 恢复。");
        }

        List<String> anyDbf = listFiles(extractDir).stream()
                .map(p -> p.getFileName().toString())
                .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".dbf"))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return new BackupInspectResult(
                BackupFormat.UNKNOWN,
                "未知格式",
                null,
                null,
                anyDbf,
                List.of(),
                null,
                "无法识别备份格式：未找到 rsgzgl-backup.json / rsgzgl-backup.id，也未找到 xxbak*.id 标识文件。");
    }

    static String csvTableName(String relativeCsvPath) {
        String name = relativeCsvPath;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        if (name.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    void unzip(Path archive, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path out = targetDir.resolve(entry.getName()).normalize();
                if (!out.startsWith(targetDir)) {
                    throw new IOException("非法压缩包路径: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                    continue;
                }
                Files.createDirectories(out.getParent());
                try (InputStream in = zip.getInputStream(entry)) {
                    Files.copy(in, out);
                }
            }
        }
    }

    Optional<Path> findFile(Path root, String fileName) throws IOException {
        String want = fileName.toLowerCase(Locale.ROOT);
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(want))
                    .findFirst();
        }
    }

    List<Path> listFiles(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile).toList();
        }
    }

    List<String> listRelative(Path root, String childDir) throws IOException {
        Path dir = root.resolve(childDir);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> childDir + "/" + p.getFileName())
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
    }

    static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            }
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }

    static List<String> collectDbfTableNames(Path extractDir) throws IOException {
        List<String> names = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(extractDir)) {
            stream.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.toLowerCase(Locale.ROOT).endsWith("2.dbf"))
                    .forEach(name -> {
                        String base = name.substring(0, name.length() - 4);
                        if (base.toLowerCase(Locale.ROOT).endsWith("2")) {
                            names.add(base.substring(0, base.length() - 1));
                        }
                    });
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }
}
