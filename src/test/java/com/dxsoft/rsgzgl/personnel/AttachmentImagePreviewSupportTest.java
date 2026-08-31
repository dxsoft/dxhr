package com.dxsoft.rsgzgl.personnel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttachmentImagePreviewSupportTest {

    @Test
    void ensurePreviewScalesLargeImage(@TempDir Path tempDir) throws IOException {
        Path source = tempDir.resolve("photo.jpg");
        BufferedImage image = new BufferedImage(2400, 1800, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(0);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, random.nextInt());
            }
        }
        ImageIO.write(image, "jpg", source.toFile());
        assertTrue(Files.size(source) > 512L * 1024L);

        Path cache = AttachmentImagePreviewSupport.previewCachePath(tempDir, "photo.jpg");
        Path preview = AttachmentImagePreviewSupport.ensurePreview(source, cache, "photo.jpg");

        assertTrue(Files.exists(preview));
        assertTrue(Files.size(preview) < Files.size(source));
        BufferedImage scaled = ImageIO.read(preview.toFile());
        assertTrue(Math.max(scaled.getWidth(), scaled.getHeight()) <= 1600);
    }
}
