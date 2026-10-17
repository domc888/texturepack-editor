package dev.packeditor.client;

import dev.packeditor.core.ConfigStore;
import dev.packeditor.core.Looks;
import dev.packeditor.core.LookSpec;
import dev.packeditor.core.PackWriter;
import dev.packeditor.core.Setup;
import dev.packeditor.core.Target;
import dev.packeditor.core.UserPacks;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.repository.PackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mod's state and all the places that touch game internals (opening screens, clipboard,
 * enabling the generated resource pack). Everything else is plain Java in dev.packeditor.core.
 */
public final class Editor {
    public static final Logger LOG = LoggerFactory.getLogger("packeditor");
    private static final int[] NO_PREVIEW = new int[0];
    private static Editor instance;

    public final Path configFile;
    public final Path packDir;
    public final UserPacks packs;
    public final ConfigStore config;
    private final McTextureSource vanilla = new McTextureSource();
    private final Map<String, int[]> previews = new HashMap<>();

    /** What the player is editing right now (may differ from config.applied until Apply). */
    public Setup working;
    public String status = "";

    private Editor() {
        FabricLoader fl = FabricLoader.getInstance();
        this.configFile = fl.getConfigDir().resolve("packeditor.txt");
        Path packsDir = fl.getGameDir().resolve("resourcepacks");
        this.packDir = packsDir.resolve(UserPacks.GENERATED_NAME);
        this.packs = new UserPacks(packsDir);
        this.config = ConfigStore.load(configFile);
        this.working = config.applied.copy();
    }

    public static synchronized Editor get() {
        if (instance == null) instance = new Editor();
        return instance;
    }

    // ------------------------------------------------------------------ screens / clipboard

    public static void open(Screen screen) {
        Minecraft.getInstance().gui.setScreen(screen);
    }

    public void copyToClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    public String readClipboard() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    // ------------------------------------------------------------------ previews

    /** 16x16 ARGB preview of a look, cached. Null if the texture isn't available. */
    public int[] preview(Target t, LookSpec look) {
        String key = t.id() + "|" + look.spec();
        int[] cached = previews.get(key);
        if (cached == null) {
            cached = Looks.preview(t, look, vanilla, packs);
            if (cached == null) cached = NO_PREVIEW;
            previews.put(key, cached);
        }
        return cached == NO_PREVIEW ? null : cached;
    }

    public void forgetPreviews() { previews.clear(); }

    // ------------------------------------------------------------------ apply

    public int pendingChanges() { return working.differences(config.applied); }

    /** Writes the generated pack, switches it on, and reloads resources. */
    public void apply() {
        try {
            PackWriter.Result r = PackWriter.write(packDir, working, vanilla, packs);
            config.applied = working.copy();
            config.save(configFile);
            for (String m : r.missing()) LOG.info("Texture not found in this game version, skipped: {}", m);
            enablePackAndReload();
            status = "Applied (" + r.filesWritten() + " textures).";
        } catch (Exception e) {
            LOG.error("Apply failed", e);
            status = "Apply failed: " + e.getMessage();
        }
    }

    private void enablePackAndReload() {
        Minecraft mc = Minecraft.getInstance();
        PackRepository repo = mc.getResourcePackRepository();
        repo.reload();
        String id = "file/" + UserPacks.GENERATED_NAME;
        if (repo.getPack(id) == null) {
            LOG.warn("Generated pack '{}' was not found by the game", id);
            status = "Pack was written but the game didn't find it. Enable it in Resource Packs.";
            return;
        }
        List<String> selected = new ArrayList<>(repo.getSelectedIds());
        if (!selected.contains(id)) {
            selected.add(id); // last = highest priority
            repo.setSelected(selected);
            mc.options.updateResourcePacks(repo);
        }
        mc.reloadResourcePacks();
    }
}
