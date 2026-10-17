package dev.packeditor.core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dark-to-light colour gradient. Recolouring maps each source pixel's brightness onto the
 * ramp, which keeps the texture's shading but takes its colours from the ramp.
 */
public final class Ramp {
    private final int[] stops; // RGB, sorted dark -> light

    private Ramp(int[] stops) { this.stops = stops; }

    public static Ramp of(int... rgb) {
        if (rgb.length < 2 || rgb.length > 8) throw new IllegalArgumentException("a ramp needs 2 to 8 colours");
        Integer[] boxed = new Integer[rgb.length];
        for (int i = 0; i < rgb.length; i++) boxed[i] = rgb[i] & 0xFFFFFF;
        Arrays.sort(boxed, (x, y) -> Double.compare(Colours.lum(x), Colours.lum(y)));
        int[] out = new int[boxed.length];
        for (int i = 0; i < out.length; i++) out[i] = boxed[i];
        return new Ramp(out);
    }

    /** Dark shade, the colour itself, light shade. */
    public static Ramp fromColour(int rgb) {
        rgb &= 0xFFFFFF;
        return new Ramp(new int[] {Colours.mix(rgb, 0x000000, 0.65), rgb, Colours.mix(rgb, 0xFFFFFF, 0.70)});
    }

    public int[] stops() { return stops.clone(); }

    /** Colour at position t (0 = darkest, 1 = lightest). */
    public int at(double t) {
        t = Colours.clamp01(t);
        double pos = t * (stops.length - 1);
        int i = (int) Math.floor(pos);
        if (i >= stops.length - 1) return stops[stops.length - 1];
        return Colours.mix(stops[i], stops[i + 1], pos - i);
    }

    /** Picks up to 5 dominant, preferably saturated colours from a picture (e.g. a screenshot). */
    public static Ramp fromImage(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        long total = (long) w * h;
        int stride = (int) Math.max(1, Math.round(Math.sqrt(total / 250_000.0)));
        Map<Integer, long[]> buckets = new HashMap<>();
        for (int y = 0; y < h; y += stride) {
            for (int x = 0; x < w; x += stride) {
                int argb = img.getRGB(x, y);
                if ((argb >>> 24) < 128) continue;
                int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
                int key = ((r >> 3) << 10) | ((g >> 3) << 5) | (b >> 3);
                long[] e = buckets.computeIfAbsent(key, k -> new long[4]);
                e[0]++;
                e[1] += r;
                e[2] += g;
                e[3] += b;
            }
        }
        if (buckets.isEmpty()) throw new IllegalArgumentException("image has no visible pixels");

        record Cand(int rgb, double score) {}
        List<Cand> cands = new ArrayList<>();
        for (long[] e : buckets.values()) {
            int r = (int) (e[1] / e[0]), g = (int) (e[2] / e[0]), b = (int) (e[3] / e[0]);
            double sat = (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b))) / 255.0;
            cands.add(new Cand(Colours.rgb(r, g, b), e[0] * (0.25 + sat)));
        }
        cands.sort((p, q) -> Double.compare(q.score, p.score));

        List<Integer> picked = new ArrayList<>();
        for (Cand c : cands) {
            boolean far = true;
            for (int p : picked) {
                int dr = Colours.r(p) - Colours.r(c.rgb), dg = Colours.g(p) - Colours.g(c.rgb), db = Colours.b(p) - Colours.b(c.rgb);
                if (dr * dr + dg * dg + db * db < 48 * 48) { far = false; break; }
            }
            if (far) picked.add(c.rgb);
            if (picked.size() == 5) break;
        }
        if (picked.size() == 1) return fromColour(picked.get(0));
        int[] arr = new int[picked.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = picked.get(i);
        return of(arr);
    }

    /** "RRGGBB,RRGGBB,..." */
    public String spec() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stops.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(Colours.hex(stops[i]));
        }
        return sb.toString();
    }

    public static Ramp parse(String spec) {
        String[] parts = spec.split(",");
        int[] rgb = new int[parts.length];
        for (int i = 0; i < parts.length; i++) rgb[i] = Colours.parseHex(parts[i]);
        return of(rgb);
    }
}
