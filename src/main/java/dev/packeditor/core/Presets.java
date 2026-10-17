package dev.packeditor.core;

import dev.packeditor.core.Target.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** One-click built-in setups. */
public final class Presets {
    private Presets() {}

    public record Preset(String name, Setup setup) {}

    public static List<Preset> all() {
        List<Preset> out = new ArrayList<>();
        out.add(new Preset("Crimson Everything", everything(Style.CRIMSON)));
        out.add(new Preset("Frost Everything", everything(Style.FROST)));
        out.add(new Preset("Monochrome Everything", everything(Style.MONOCHROME)));
        out.add(new Preset("Neon Crystal PvP", only(Style.NEON,
                Set.of("end_crystal", "respawn_anchor", "totem", "ender_pearl"))));
        out.add(new Preset("Midnight Blocks", blocks(Style.MIDNIGHT)));
        return out;
    }

    private static Setup everything(Style s) {
        Setup setup = new Setup();
        for (Target t : Targets.ALL) setup.set(t.id(), LookSpec.style(s));
        return setup;
    }

    private static Setup only(Style s, Set<String> ids) {
        Setup setup = new Setup();
        for (Target t : Targets.ALL) if (ids.contains(t.id())) setup.set(t.id(), LookSpec.style(s));
        return setup;
    }

    private static Setup blocks(Style s) {
        Setup setup = new Setup();
        for (Target t : Targets.in(Category.BLOCKS)) setup.set(t.id(), LookSpec.style(s));
        return setup;
    }
}
