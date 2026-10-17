package dev.packeditor.core;

import java.util.Objects;

/**
 * One chosen look for a target, stored as a short string:
 * <pre>
 * vanilla
 * style:CRIMSON
 * custom:FF8800
 * ramp:112233,445566,AABBCC     (colours copied from a screenshot)
 * pack:SomePack.zip             (textures taken from one of your own packs; local only)
 * </pre>
 */
public final class LookSpec {
    public enum Kind { VANILLA, STYLE, CUSTOM, RAMP, PACK }

    public static final LookSpec VANILLA = new LookSpec(Kind.VANILLA, null, null, 0, null);

    public final Kind kind;
    public final Style style;
    public final Ramp ramp;
    public final int rgb;
    public final String pack;

    private LookSpec(Kind kind, Style style, Ramp ramp, int rgb, String pack) {
        this.kind = kind;
        this.style = style;
        this.ramp = ramp;
        this.rgb = rgb;
        this.pack = pack;
    }

    public static LookSpec style(Style s) { return new LookSpec(Kind.STYLE, s, null, 0, null); }

    public static LookSpec custom(int rgb) { return new LookSpec(Kind.CUSTOM, null, Ramp.fromColour(rgb), rgb & 0xFFFFFF, null); }

    public static LookSpec ramp(Ramp r) { return new LookSpec(Kind.RAMP, null, r, 0, null); }

    public static LookSpec pack(String name) {
        if (name == null || name.isBlank() || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0
                || name.indexOf('\n') >= 0 || name.contains("..")) {
            throw new IllegalArgumentException("bad pack name");
        }
        return new LookSpec(Kind.PACK, null, null, 0, name);
    }

    public static LookSpec parse(String s) {
        String t = s.trim();
        if (t.equalsIgnoreCase("vanilla")) return VANILLA;
        int i = t.indexOf(':');
        if (i < 0) throw new IllegalArgumentException("bad look: " + s);
        String kind = t.substring(0, i).toLowerCase();
        String arg = t.substring(i + 1);
        return switch (kind) {
            case "style" -> style(Style.byName(arg));
            case "custom" -> custom(Colours.parseHex(arg));
            case "ramp" -> ramp(Ramp.parse(arg));
            case "pack" -> pack(arg);
            default -> throw new IllegalArgumentException("bad look kind: " + kind);
        };
    }

    public String spec() {
        return switch (kind) {
            case VANILLA -> "vanilla";
            case STYLE -> "style:" + style.name();
            case CUSTOM -> "custom:" + Colours.hex(rgb);
            case RAMP -> "ramp:" + ramp.spec();
            case PACK -> "pack:" + pack;
        };
    }

    /** Short name for look cards. */
    public String label() {
        return switch (kind) {
            case VANILLA -> "Vanilla";
            case STYLE -> style.label;
            case CUSTOM -> "#" + Colours.hex(rgb);
            case RAMP -> "Copied";
            case PACK -> pack.endsWith(".zip") ? pack.substring(0, pack.length() - 4) : pack;
        };
    }

    /** Small tag under the name on look cards. */
    public String tag() {
        return switch (kind) {
            case VANILLA -> "VANILLA";
            case STYLE -> "STYLE";
            case CUSTOM -> "CUSTOM";
            case RAMP -> "DROPPED";
            case PACK -> "PACK";
        };
    }

    @Override public boolean equals(Object o) { return o instanceof LookSpec l && l.spec().equals(spec()); }

    @Override public int hashCode() { return Objects.hash(spec()); }

    @Override public String toString() { return spec(); }
}
