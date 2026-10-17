package dev.packeditor.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/** PNG helpers. Always returns TYPE_INT_ARGB images. */
public final class Png {
    private Png() {}

    public static BufferedImage read(byte[] data) throws IOException {
        BufferedImage in = ImageIO.read(new ByteArrayInputStream(data));
        if (in == null) throw new IOException("not a readable image");
        return toArgb(in);
    }

    public static BufferedImage toArgb(BufferedImage in) {
        if (in.getType() == BufferedImage.TYPE_INT_ARGB) return in;
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < in.getHeight(); y++) {
            for (int x = 0; x < in.getWidth(); x++) out.setRGB(x, y, in.getRGB(x, y));
        }
        return out;
    }

    public static byte[] write(BufferedImage img) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (!ImageIO.write(img, "png", bos)) throw new IOException("no PNG writer available");
        return bos.toByteArray();
    }
}
