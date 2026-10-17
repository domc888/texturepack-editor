package dev.packeditor.client.screen;

import dev.packeditor.client.Draw;
import dev.packeditor.client.Editor;
import dev.packeditor.core.Target.Category;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Home screen: PvP / Blocks / Gear, Configs, Reset all, Apply. */
public class MainScreen extends Screen {
    private final Screen parent;
    private final Editor ed = Editor.get();

    public MainScreen(Screen parent) {
        super(Component.literal("Pack Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ed.packs.list(); // touch the folder early
        int bw = 104, bh = 34, gap = 8;
        int cx = this.width / 2;
        int rowW = 3 * bw + 2 * gap;
        int x0 = cx - rowW / 2;
        int y0 = this.height / 2 - 44;

        int i = 0;
        for (Category c : Category.values()) {
            final Category cat = c;
            this.addRenderableWidget(Button.builder(Component.literal(cat.label),
                    b -> Editor.open(new TargetScreen(this, cat)))
                    .bounds(x0 + i * (bw + gap), y0, bw, bh).build());
            i++;
        }

        int y1 = y0 + bh + gap;
        this.addRenderableWidget(Button.builder(Component.literal("Configs"),
                b -> Editor.open(new ConfigsScreen(this)))
                .bounds(x0, y1, bw, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset all"), b -> {
            ed.working.clear();
            ed.status = "All looks reset. Press Apply to update the game.";
            this.rebuildWidgets();
        }).bounds(x0 + bw + gap, y1, bw, 22).build());
        int pending = ed.pendingChanges();
        this.addRenderableWidget(Button.builder(Component.literal(pending > 0 ? "Apply (" + pending + ")" : "Apply"), b -> {
            ed.apply();
            this.rebuildWidgets();
        }).bounds(x0 + 2 * (bw + gap), y1, bw, 22).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(cx - 40, y1 + 22 + gap, 80, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int top = this.height / 2 - 44;
        Draw.centered(g, this.font, "Pack Editor", cx, top - 30, 0xFFFFFFFF);
        Draw.centered(g, this.font, "Pick a look for each item, then press Apply.", cx, top - 16, 0xFFB0B0B0);
        if (!ed.status.isEmpty()) Draw.centered(g, this.font, ed.status, cx, top + 34 + 8 + 22 + 8 + 28, 0xFF8CFF8C);
    }

    @Override
    public void onClose() {
        Editor.open(parent);
    }
}
