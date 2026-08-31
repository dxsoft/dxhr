package com.dxsoft.rsgzgl.personnel;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

final class AttachmentImagePreviewSupport {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp");
    private static final long SCALE_THRESHOLD_BYTES = 512L * 1024L;
    private static final int MAX_EDGE = 1600;
    private static final float JPEG_QUALITY = 0.82f;

    private AttachmentImagePreviewSupport() {
    }

    static boolean isPreviewableImage(String originalName) {
        return IMAGE_EXTENSIONS.contains(SubrecordAttachmentStorage.extensionOf(originalName));
    }

    static Path previewCachePath(Path rootDirectory, String storedName) {
        return rootDirectory.resolve(".preview-cache").resolve(storedName + ".preview.jpg");
    }

    static Path ensurePreview(Path sourcePath, Path cachePath, String originalName) throws IOException {
        if (!shouldScale(sourcePath, originalName)) {
            return sourcePath;
        }
        Files.createDirectories(cachePath.getParent());
        if (Files.isRegularFile(cachePath)) {
            if (Files.getLastModifiedTime(cachePath).compareTo(Files.getLastModifiedTime(sourcePath)) >= 0) {
                return cachePath;
            }
        }
        byte[] scaled = scaleToJpeg(sourcePath);
        Files.write(cachePath, scaled);
        return cachePath;
    }

    static boolean shouldScale(Path sourcePath, String originalName) throws IOException {
        if (!isPreviewableImage(originalName)) {
            return false;
        }
        return Files.size(sourcePath) > SCALE_THRESHOLD_BYTES;
    }

    private static byte[] scaleToJpeg(Path sourcePath) throws IOException {
        BufferedImage source = readImage(sourcePath);
        int width = source.getWidth();
        int height = source.getHeight();
        int maxEdge = Math.max(width, height);
        if (maxEdge > MAX_EDGE) {
            double ratio = MAX_EDGE / (double) maxEdge;
            width = Math.max(1, (int) Math.round(width * ratio));
            height = Math.max(1, (int) Math.round(height * ratio));
        }
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return encodeJpeg(target);
    }

    private static BufferedImage readImage(Path sourcePath) throws IOException {
        try (InputStream input = Files.newInputStream(sourcePath)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IOException("无法读取图片。");
            }
            return image;
        }
    }

    private static byte[] encodeJpeg(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("当前环境不支持 JPEG 预览。");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            return output.toByteArray();
        } catch (IOException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new UncheckedIOException(new IOException(exception));
        }
    }
}
