package dev.packeditor.core;

import java.util.List;

/**
 * Something that can be given a different look: one or more vanilla texture files
 * (paths relative to assets/minecraft/). Paths that don't exist in the running game
 * version are skipped, so a wrong guess never breaks the rest.
 */
public record Target(String id, Category category, String name, List<String> paths) {

    public enum Category {
        PVP("PvP"), BLOCKS("Blocks"), GEAR("Gear");

        public final String label;

        Category(String label) { this.label = label; }
    }

    public static Target of(String id, Category c, String name, String... paths) {
        return new Target(id, c, name, List.of(paths));
    }

    /** The texture used for the small preview on look cards. */
    public String previewPath() { return paths.get(0); }
}
