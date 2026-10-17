package dev.packeditor.core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

/** Applies a look to a texture, and builds the little previews shown on look cards. */
public final class Looks {
    public static final int PREVIEW_SIZE = 16;

    private Looks() {}

    /** Result for one image. For PACK looks the pack's own image is used as-is. */
    public static BufferedImage apply(BufferedImage original, LookSpec look) {
        return switch (look.kind) {
            case VANILLA, PACK -> original;
            case STYLE -> Recolour.style(original, look.style);
            case CUSTOM, RAMP -> Recolour.ramp(original, look.ramp);
        };
    }

    /** Source bytes for one texture path under this look (the pack's file or the vanilla one). */
    public static Optional<byte[]> source(String path, LookSpec look, TextureSource vanilla, UserPacks packs) {
        if (look.kind == LookSpec.Kind.PACK) return packs.read(look.pack, path);
        return vanilla.vanilla(path);
    }

    /**
     * A 16x16 ARGB preview of the target under the look, or null if the texture isn't available.
     * Animated strips use their first frame.
     */
    public static int[] preview(Target t, LookSpec look, TextureSource vanilla, UserPacks packs) {
        try {
            Optional<byte[]> bytes = source(t.previewPath(), look, vanilla, packs);
            if (bytes.isEmpty() && look.kind != LookSpec.Kind.PACK) {
                // try the other paths of the target (some may be missing in this game version)
                for (String p : t.paths()) {
                    bytes = source(p, look, vanilla, packs);
                    if (bytes.isPresent()) break;
                }
            }
            if (bytes.isEmpty()) return null;
            BufferedImage img = apply(Png.read(bytes.get()), look);
            return sample(img, PREVIEW_SIZE);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    static int[] sample(BufferedImage img, int n) {
        int w = img.getWidth();
        int h = img.getHeight() > w ? w : img.getHeight(); // first frame of a vertical strip
        int[] out = new int[n * n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                int sx = Math.min(w - 1, (int) ((x + 0.5) * w / n));
                int sy = Math.min(h - 1, (int) ((y + 0.5) * h / n));
                out[y * n + x] = img.getRGB(sx, sy);
            }
        }
        return out;
    }
}
