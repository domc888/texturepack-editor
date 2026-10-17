package dev.packeditor.client.screen;

import dev.packeditor.client.Draw;
import dev.packeditor.client.Editor;
import dev.packeditor.core.Colours;
import dev.packeditor.core.LookSpec;
import dev.packeditor.core.Ramp;
import dev.packeditor.core.Style;
import dev.packeditor.core.Target;
import dev.packeditor.core.Target.Category;
import dev.packeditor.core.Targets;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Pick a look for one target: vanilla, the 12 styles, a custom colour, colours copied from a
 * dropped screenshot, or textures from one of your own packs.
 */
public class TargetScreen extends Screen {
    private static final int CARD_W = 62;
    private static final int CARD_H = 66;
    private static final int GAP = 4;
    private static final long MAX_SHOT_BYTES = 40L * 1024 * 1024;

    private final Screen parent;
    private final Editor ed = Editor.get();
    private final List<Target> targets;
    private Target current;
    private int page;
    private String hexText = "";
    private String pathText = "";

    // layout, computed in init()
    private int panelL, panelT, panelR, panelB, contentX, gridY, cols, perPage;
    private final List<LookSpec> visible = new ArrayList<>();
    private int lookCount;

    public TargetScreen(Screen parent, Category category) {
        super(Component.literal(category.label));
        this.parent = parent;
        this.targets = Targets.in(category);
        this.current = targets.get(0);
    }

    @Override
    protected void init() {
        int pw = Math.min(this.width - 16, 440);
        int ph = Math.min(this.height - 16, 270);
        panelL = (this.width - pw) / 2;
        panelT = (this.height - ph) / 2;
        panelR = panelL + pw;
        panelB = panelT + ph;
        contentX = panelL + 104;
        int contentW = panelR - 6 - contentX;

        this.addRenderableWidget(Button.builder(Component.literal("< Back"), b -> this.onClose())
                .bounds(panelL + 6, panelT + 6, 46, 16).build());

        for (int i = 0; i < targets.size(); i++) {
            final Target t = targets.get(i);
            this.addRenderableWidget(Button.builder(Component.literal(t.name()), b -> {
                current = t;
                page = 0;
                this.rebuildWidgets();
            }).bounds(panelL + 6, panelT + 30 + i * 20, 92, 18).build());
        }

        // custom colour row
        EditBox hex = new EditBox(this.font, contentX + 52, panelT + 26, 56, 16, Component.literal("Hex colour"));
        hex.setMaxLength(7);
        hex.setValue(hexText);
        hex.setResponder(s -> hexText = s);
        this.addRenderableWidget(hex);
        this.addRenderableWidget(Button.builder(Component.literal("Set"), b -> setCustom())
                .bounds(contentX + 112, panelT + 26, 28, 16).build());

        // screenshot row
        EditBox path = new EditBox(this.font, contentX, panelT + 46, contentW - 56, 16, Component.literal("Screenshot path"));
        path.setMaxLength(512);
        path.setHint(Component.literal("Drop a screenshot here, or paste its file path"));
        path.setValue(pathText);
        path.setResponder(s -> pathText = s);
        this.addRenderableWidget(path);
        this.addRenderableWidget(Button.builder(Component.literal("Load"), b -> loadShot(cleanPath(pathText)))
                .bounds(contentX + contentW - 52, panelT + 46, 52, 16).build());

        // look cards
        gridY = panelT + 68;
        int gridBottom = panelB - 42;
        cols = Math.max(1, (contentW + GAP) / (CARD_W + GAP));
        int rows = Math.max(1, (gridBottom - gridY + GAP) / (CARD_H + GAP));
        perPage = cols * rows;
        List<LookSpec> looks = buildLooks();
        lookCount = looks.size();
        int pages = Math.max(1, (looks.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));
        visible.clear();
        for (int k = page * perPage; k < Math.min(looks.size(), (page + 1) * perPage); k++) visible.add(looks.get(k));
        for (int k = 0; k < visible.size(); k++) {
            final LookSpec look = visible.get(k);
            int x = contentX + (k % cols) * (CARD_W + GAP);
            int y = gridY + (k / cols) * (CARD_H + GAP);
            this.addRenderableWidget(Button.builder(Component.empty(), b -> select(look))
                    .bounds(x, y, CARD_W, CARD_H).build());
        }

        // bottom row
        int by = panelB - 22;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; this.rebuildWidgets(); })
                .bounds(contentX, by, 20, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; this.rebuildWidgets(); })
                .bounds(contentX + 24, by, 20, 16).build());
        int pending = ed.pendingChanges();
        this.addRenderableWidget(Button.builder(Component.literal(pending > 0 ? "Apply (" + pending + ")" : "Apply"), b -> {
            ed.apply();
            this.rebuildWidgets();
        }).bounds(panelR - 6 - 72, by, 72, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            ed.working.set(current.id(), LookSpec.VANILLA);
            ed.status = current.name() + " reset. Press Apply.";
            this.rebuildWidgets();
        }).bounds(panelR - 6 - 72 - 4 - 46, by, 46, 16).build());
    }

    private List<LookSpec> buildLooks() {
        List<LookSpec> out = new ArrayList<>();
        out.add(LookSpec.VANILLA);
        for (Style s : Style.values()) out.add(LookSpec.style(s));
        LookSpec now = ed.working.get(current.id());
        if ((now.kind == LookSpec.Kind.CUSTOM || now.kind == LookSpec.Kind.RAMP) && !out.contains(now)) out.add(now);
        for (String pack : ed.packs.list()) {
            if (ed.packs.has(pack, current)) out.add(LookSpec.pack(pack));
        }
        return out;
    }

    private void select(LookSpec look) {
        ed.working.set(current.id(), look);
        ed.status = current.name() + ": " + look.label() + ". Press Apply.";
        this.rebuildWidgets();
    }

    private void setCustom() {
        try {
            select(LookSpec.custom(Colours.parseHex(hexText)));
        } catch (IllegalArgumentException e) {
            ed.status = "Type a colour like FF8800";
        }
    }

    private static String cleanPath(String s) {
        String t = s == null ? "" : s.trim();
        if (t.length() > 1 && t.startsWith("\"") && t.endsWith("\"")) t = t.substring(1, t.length() - 1);
        return t;
    }

    /** Copies the dominant colours of a picture onto the current target. */
    private void loadShot(String file) {
        if (file.isEmpty()) {
            ed.status = "Drop a screenshot here, or paste its path.";
            return;
        }
        try {
            Path p = Path.of(file);
            if (!Files.isRegularFile(p)) throw new IllegalArgumentException("file not found");
            if (Files.size(p) > MAX_SHOT_BYTES) throw new IllegalArgumentException("file is too big");
            BufferedImage img = ImageIO.read(p.toFile());
            if (img == null) throw new IllegalArgumentException("not a PNG/JPG image");
            Ramp ramp = Ramp.fromImage(img);
            ed.working.set(current.id(), LookSpec.ramp(ramp));
            ed.status = "Colours copied onto " + current.name() + ". Press Apply.";
        } catch (Exception e) {
            ed.status = "Couldn't use that picture: " + e.getMessage();
        }
        this.rebuildWidgets();
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        if (!files.isEmpty()) loadShot(files.get(0).toString());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);

        Draw.text(g, this.font, this.title.getString() + "  -  " + current.name(), panelL + 58, panelT + 10, 0xFFFFFFFF);
        Draw.text(g, this.font, "Custom #", contentX, panelT + 30, 0xFFB0B0B0);

        // highlight the open tab
        int ti = targets.indexOf(current);
        Draw.frame(g, panelL + 5, panelT + 29 + ti * 20, 94, 20, 1, 0xFFB38CFF);

        // custom colour swatch
        try {
            int rgb = Colours.parseHex(hexText);
            Draw.rect(g, contentX + 144, panelT + 26, 16, 16, 0xFF000000 | rgb);
        } catch (IllegalArgumentException ignored) {
            // no swatch for an invalid colour
        }

        LookSpec selected = ed.working.get(current.id());
        LookSpec applied = ed.config.applied.get(current.id());
        for (int k = 0; k < visible.size(); k++) {
            LookSpec look = visible.get(k);
            int x = contentX + (k % cols) * (CARD_W + GAP);
            int y = gridY + (k / cols) * (CARD_H + GAP);

            int px = x + (CARD_W - 32) / 2;
            int py = y + 6;
            Draw.rect(g, px - 2, py - 2, 36, 36, 0xFF1A1A1A);
            int[] pix = ed.preview(current, look);
            if (pix != null) Draw.pixels(g, pix, 16, px, py, 2);
            else Draw.centered(g, this.font, "?", x + CARD_W / 2, py + 12, 0xFF808080);

            String name = look.label();
            if (name.length() > 10) name = name.substring(0, 9) + ".";
            Draw.centered(g, this.font, name, x + CARD_W / 2, y + CARD_H - 24, 0xFFFFFFFF);
            Draw.centered(g, this.font, look.tag(), x + CARD_W / 2, y + CARD_H - 13, tagColour(look));

            if (look.equals(selected)) Draw.frame(g, x - 1, y - 1, CARD_W + 2, CARD_H + 2, 2, 0xFF55FF55);
            if (look.equals(applied)) Draw.rect(g, x + CARD_W - 8, y + 3, 5, 5, 0xFF55FF55);
        }

        int pages = Math.max(1, (lookCount + perPage - 1) / perPage);
        Draw.text(g, this.font, "Page " + (page + 1) + "/" + pages, contentX + 50, panelB - 18, 0xFFB0B0B0);
        if (!ed.status.isEmpty()) Draw.text(g, this.font, ed.status, contentX, panelB - 36, 0xFF8CFF8C);
    }

    private static int tagColour(LookSpec look) {
        return switch (look.kind) {
            case VANILLA -> 0xFF909090;
            case STYLE -> 0xFFFFAA00;
            case PACK -> 0xFF55FFFF;
            case CUSTOM, RAMP -> 0xFFB38CFF;
        };
    }

    @Override
    public void onClose() {
        Editor.open(parent);
    }
}
