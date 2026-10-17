package dev.packeditor.core;

import java.awt.image.BufferedImage;

/** Image recolouring. Alpha is always preserved. */
public final class Recolour {
    private Recolour() {}

    public static BufferedImage style(BufferedImage src, Style style) {
        BufferedImage in = Png.toArgb(src);
        int w = in.getWidth(), h = in.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) out.setRGB(x, y, style.apply(in.getRGB(x, y)));
        }
        return out;
    }

    /** Maps each pixel's brightness onto the ramp (brightness range is normalised per image). */
    public static BufferedImage ramp(BufferedImage src, Ramp ramp) {
        BufferedImage in = Png.toArgb(src);
        int w = in.getWidth(), h = in.getHeight();

        int[] hist = new int[256];
        long count = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = in.getRGB(x, y);
                if ((argb >>> 24) == 0) continue;
                int bin = (int) Math.round(Colours.lum(argb & 0xFFFFFF) * 255);
                hist[bin]++;
                count++;
            }
        }
        double lo = 0, hi = 1;
        if (count > 0) {
            lo = percentile(hist, count, 0.02) / 255.0;
            hi = percentile(hist, count, 0.98) / 255.0;
            if (hi - lo < 0.05) {
                double mid = (hi + lo) / 2;
                lo = Math.max(0, mid - 0.15);
                hi = Math.min(1, mid + 0.15);
            }
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = in.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) {
                    out.setRGB(x, y, argb);
                    continue;
                }
                double t = (Colours.lum(argb & 0xFFFFFF) - lo) / (hi - lo);
                out.setRGB(x, y, (a << 24) | ramp.at(t));
            }
        }
        return out;
    }

    private static int percentile(int[] hist, long count, double p) {
        long target = (long) Math.floor(count * p);
        long acc = 0;
        for (int i = 0; i < hist.length; i++) {
            acc += hist[i];
            if (acc > target) return i;
        }
        return hist.length - 1;
    }
}
