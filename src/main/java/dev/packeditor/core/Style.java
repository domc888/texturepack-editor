package dev.packeditor.core;

/**
 * The 12 built-in styles. Each one recolours a texture pixel by pixel while keeping the
 * original shading and alpha.
 */
public enum Style {
    CRIMSON("Crimson", 355, 1.0, 0.75, 1.0, 0.0),
    FROST("Frost", 195, 1.0, 0.70, 1.0, 0.04),
    TOXIC("Toxic", 110, 1.0, 0.80, 1.0, 0.0),
    ROYAL("Royal", 268, 1.0, 0.65, 1.0, 0.0),
    GILDED("Gilded", 45, 1.0, 0.80, 1.05, 0.02),
    SAKURA("Sakura", 335, 0.8, 0.55, 0.70, 0.30),
    OCEAN("Ocean", 215, 1.0, 0.75, 1.0, 0.0),
    MIDNIGHT("Midnight", 235, 1.0, 0.50, 0.65, 0.0),
    NEON("Neon", 160, 1.0, 1.0, 1.0, 0.05),
    PASTEL("Pastel", 280, 0.5, 0.35, 0.50, 0.50),
    MONOCHROME("Monochrome", -1, 0, 0, 1, 0),
    INVERTED("Inverted", -2, 0, 0, 1, 0);

    public final String label;
    private final double hue;
    private final double satMul;
    private final double satMin;
    private final double lMul;
    private final double lAdd;

    Style(String label, double hue, double satMul, double satMin, double lMul, double lAdd) {
        this.label = label;
        this.hue = hue;
        this.satMul = satMul;
        this.satMin = satMin;
        this.lMul = lMul;
        this.lAdd = lAdd;
    }

    /** Recolours one ARGB pixel. Fully transparent pixels are returned untouched. */
    public int apply(int argb) {
        int a = argb >>> 24;
        if (a == 0) return argb;
        int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        if (hue == -2) {
            return (a << 24) | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
        }
        if (hue == -1) {
            int v = (int) Math.round(Colours.lum(r, g, b) * 255);
            return (a << 24) | (v << 16) | (v << 8) | v;
        }
        double[] hsl = Colours.toHsl(r, g, b);
        double s = Colours.clamp01(Math.max(hsl[1], satMin) * satMul);
        double l = Colours.clamp01(hsl[2] * lMul + lAdd);
        return (a << 24) | Colours.fromHsl(hue, s, l);
    }

    public static Style byName(String name) {
        for (Style s : values()) {
            if (s.name().equalsIgnoreCase(name) || s.label.equalsIgnoreCase(name)) return s;
        }
        throw new IllegalArgumentException("unknown style: " + name);
    }
}
