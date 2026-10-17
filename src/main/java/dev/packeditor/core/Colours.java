package dev.packeditor.core;

/** Small colour maths helpers. No Minecraft dependencies. */
public final class Colours {
    private Colours() {}

    public static int r(int rgb) { return (rgb >> 16) & 255; }
    public static int g(int rgb) { return (rgb >> 8) & 255; }
    public static int b(int rgb) { return rgb & 255; }

    public static int rgb(int r, int g, int b) {
        return (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    public static int clamp255(int v) { return v < 0 ? 0 : Math.min(v, 255); }

    public static double clamp01(double v) { return v < 0 ? 0 : Math.min(v, 1); }

    /** Perceptual-ish luminance in 0..1. */
    public static double lum(int r, int g, int b) {
        return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
    }

    public static double lum(int rgb) { return lum(r(rgb), g(rgb), b(rgb)); }

    /** Returns {hue 0..360, saturation 0..1, lightness 0..1}. */
    public static double[] toHsl(int r, int g, int b) {
        double rf = r / 255.0, gf = g / 255.0, bf = b / 255.0;
        double max = Math.max(rf, Math.max(gf, bf));
        double min = Math.min(rf, Math.min(gf, bf));
        double l = (max + min) / 2.0;
        double h = 0, s = 0;
        double d = max - min;
        if (d > 1e-9) {
            s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
            if (max == rf) h = ((gf - bf) / d) + (gf < bf ? 6 : 0);
            else if (max == gf) h = ((bf - rf) / d) + 2;
            else h = ((rf - gf) / d) + 4;
            h *= 60;
        }
        return new double[] {h, s, l};
    }

    public static int fromHsl(double h, double s, double l) {
        h = ((h % 360) + 360) % 360;
        s = clamp01(s);
        l = clamp01(l);
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double hp = h / 60.0;
        double x = c * (1 - Math.abs(hp % 2 - 1));
        double r1 = 0, g1 = 0, b1 = 0;
        if (hp < 1) { r1 = c; g1 = x; }
        else if (hp < 2) { r1 = x; g1 = c; }
        else if (hp < 3) { g1 = c; b1 = x; }
        else if (hp < 4) { g1 = x; b1 = c; }
        else if (hp < 5) { r1 = x; b1 = c; }
        else { r1 = c; b1 = x; }
        double m = l - c / 2.0;
        return rgb((int) Math.round((r1 + m) * 255), (int) Math.round((g1 + m) * 255), (int) Math.round((b1 + m) * 255));
    }

    public static int mix(int a, int b, double t) {
        t = clamp01(t);
        return rgb(
                (int) Math.round(r(a) + (r(b) - r(a)) * t),
                (int) Math.round(g(a) + (g(b) - g(a)) * t),
                (int) Math.round(b(a) + (b(b) - b(a)) * t));
    }

    public static String hex(int rgb) { return String.format("%06X", rgb & 0xFFFFFF); }

    /** Parses "RRGGBB" or "#RRGGBB". */
    public static int parseHex(String s) {
        if (s == null) throw new IllegalArgumentException("empty colour");
        String t = s.trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (t.length() != 6) throw new IllegalArgumentException("colour must be 6 hex digits");
        try {
            return Integer.parseInt(t, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad hex colour: " + s);
        }
    }
}
