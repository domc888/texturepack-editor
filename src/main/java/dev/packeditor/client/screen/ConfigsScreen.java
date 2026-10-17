package dev.packeditor.client.screen;

import dev.packeditor.client.Draw;
import dev.packeditor.client.Editor;
import dev.packeditor.core.ConfigStore;
import dev.packeditor.core.Presets;
import dev.packeditor.core.Setup;
import dev.packeditor.core.SetupCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Save setups, switch between them, and share them as short codes. */
public class ConfigsScreen extends Screen {
    private static final int ROWS_PER_PAGE = 6;

    private record Row(String name, Setup setup, boolean preset) {}

    private final Screen parent;
    private final Editor ed = Editor.get();
    private int page;
    private String nameText = "";
    private final List<Row> shown = new ArrayList<>();
    private int listTop, left, listW;

    public ConfigsScreen(Screen parent) {
        super(Component.literal("Configs"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 16, 360);
        left = (this.width - panelW) / 2;
        listW = panelW;
        int top = Math.max(8, this.height / 2 - 120);

        this.addRenderableWidget(Button.builder(Component.literal("< Back"), b -> this.onClose())
                .bounds(left, top, 46, 16).build());

        // save current
        EditBox name = new EditBox(this.font, left, top + 34, panelW - 50, 16, Component.literal("Setup name"));
        name.setMaxLength(32);
        name.setHint(Component.literal("Name this setup"));
        name.setValue(nameText);
        name.setResponder(s -> nameText = s);
        this.addRenderableWidget(name);
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> save())
                .bounds(left + panelW - 46, top + 34, 46, 16).build());

        // share codes
        int half = (panelW - 4) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Copy my code"), b -> {
            ed.copyToClipboard(SetupCodec.encode(ed.working));
            ed.status = "Code copied. Send it to a friend.";
        }).bounds(left, top + 56, half, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Paste friend's code"), b -> paste())
                .bounds(left + half + 4, top + 56, half, 18).build());

        // list
        listTop = top + 94;
        List<Row> all = new ArrayList<>();
        for (Presets.Preset p : Presets.all()) all.add(new Row(p.name(), p.setup(), true));
        for (Map.Entry<String, Setup> e : ed.config.saved.entrySet()) all.add(new Row(e.getKey(), e.getValue(), false));
        int pages = Math.max(1, (all.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));
        shown.clear();
        for (int i = page * ROWS_PER_PAGE; i < Math.min(all.size(), (page + 1) * ROWS_PER_PAGE); i++) shown.add(all.get(i));

        for (int i = 0; i < shown.size(); i++) {
            final Row row = shown.get(i);
            int y = listTop + i * 22;
            int useX = row.preset() ? left + listW - 40 : left + listW - 40 - 50;
            this.addRenderableWidget(Button.builder(Component.literal("Use"), b -> use(row))
                    .bounds(useX, y, 38, 18).build());
            if (!row.preset()) {
                this.addRenderableWidget(Button.builder(Component.literal("Delete"), b -> {
                    ed.config.saved.remove(row.name());
                    persist();
                    ed.status = "Deleted '" + row.name() + "'.";
                    this.rebuildWidgets();
                }).bounds(left + listW - 48, y, 48, 18).build());
            }
        }
        int py = listTop + ROWS_PER_PAGE * 22 + 4;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; this.rebuildWidgets(); })
                .bounds(left, py, 20, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; this.rebuildWidgets(); })
                .bounds(left + 24, py, 20, 16).build());
    }

    private void save() {
        String n = ConfigStore.cleanName(nameText);
        if (n.isEmpty()) {
            ed.status = "Type a name first.";
            return;
        }
        if (!ed.config.saved.containsKey(n) && ed.config.saved.size() >= ConfigStore.MAX_SAVED) {
            ed.status = "Too many saved setups. Delete one first.";
            return;
        }
        ed.config.saved.put(n, ed.working.copy());
        persist();
        ed.status = "Saved '" + n + "'.";
        this.rebuildWidgets();
    }

    private void use(Row row) {
        ed.working = row.setup().copy();
        ed.apply();
        this.rebuildWidgets();
    }

    private void paste() {
        try {
            ed.working = SetupCodec.decode(ed.readClipboard());
            ed.apply();
            ed.status = "Friend's setup equipped. " + ed.status;
        } catch (IllegalArgumentException e) {
            ed.status = "Can't use that code: " + e.getMessage();
        }
        this.rebuildWidgets();
    }

    private void persist() {
        try {
            ed.config.save(ed.configFile);
        } catch (Exception e) {
            Editor.LOG.error("Couldn't save config", e);
            ed.status = "Couldn't save: " + e.getMessage();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int top = Math.max(8, this.height / 2 - 120);
        Draw.text(g, this.font, "Configs", left + 54, top + 4, 0xFFFFFFFF);
        Draw.text(g, this.font, "Save current setup", left, top + 22, 0xFFB0B0B0);
        Draw.text(g, this.font, "Presets and saved setups (Use = equip now)", left, top + 80, 0xFFB0B0B0);

        for (int i = 0; i < shown.size(); i++) {
            Row r = shown.get(i);
            int y = listTop + i * 22;
            Draw.rect(g, left, y, 3, 18, r.preset() ? 0xFFB38CFF : 0xFF55FF55);
            String label = r.name().length() > 24 ? r.name().substring(0, 23) + "." : r.name();
            Draw.text(g, this.font, label + (r.preset() ? "  PRESET" : ""), left + 8, y + 5, 0xFFFFFFFF);
        }
        if (!ed.status.isEmpty()) Draw.text(g, this.font, ed.status, left + 50, listTop + ROWS_PER_PAGE * 22 + 8, 0xFF8CFF8C);
    }

    @Override
    public void onClose() {
        Editor.open(parent);
    }
}
