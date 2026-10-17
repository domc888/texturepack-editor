package dev.packeditor.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Every GUI drawing call in the mod goes through here, so if Mojang renames one of these
 * methods in a later version there is exactly one file to fix.
 */
public final class Draw {
    private Draw() {}

    public static void rect(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + h, argb);
    }

    public static void frame(GuiGraphicsExtractor g, int x, int y, int w, int h, int t, int argb) {
        rect(g, x, y, w, t, argb);
        rect(g, x, y + h - t, w, t, argb);
        rect(g, x, y + t, t, h - 2 * t, argb);
        rect(g, x + w - t, y + t, t, h - 2 * t, argb);
    }

    public static void text(GuiGraphicsExtractor g, Font font, String s, int x, int y, int argb) {
        g.text(font, Component.literal(s), x, y, argb);
    }

    public static void centered(GuiGraphicsExtractor g, Font font, String s, int cx, int y, int argb) {
        text(g, font, s, cx - font.width(s) / 2, y, argb);
    }

    /** Draws a 16x16 ARGB preview, merging runs of equal pixels into one rectangle. */
    public static void pixels(GuiGraphicsExtractor g, int[] argb, int n, int x, int y, int scale) {
        for (int py = 0; py < n; py++) {
            int px = 0;
            while (px < n) {
                int c = argb[py * n + px];
                int run = 1;
                while (px + run < n && argb[py * n + px + run] == c) run++;
                if ((c >>> 24) != 0) rect(g, x + px * scale, y + py * scale, run * scale, scale, c);
                px += run;
            }
        }
    }
}
