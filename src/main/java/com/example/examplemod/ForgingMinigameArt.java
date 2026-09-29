package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;

/** Pixel-style forge details rendered with GUI rectangles, without external textures. */
public final class ForgingMinigameArt {
    private static final int DARK = 0xFF101419;
    private static final int STEEL = 0xFF59636E;
    private static final int COPPER = 0xFFD27A34;

    private ForgingMinigameArt() {}

    public static void backdrop(GuiGraphics g, int x, int y, int width, int height, int ticks) {
        g.fill(x, y, x + width, y + height, DARK);
        g.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF252B32);
        g.fill(x + 5, y + 5, x + width - 5, y + height - 5, 0xFF171B20);
        g.fill(x + 6, y + 6, x + width - 6, y + 8, COPPER);
        g.fill(x + 6, y + height - 8, x + width - 6, y + height - 6, COPPER);
        int glow = ticks % 40 < 20 ? 0xFF994622 : 0xFFB85D28;
        g.fill(x + 8, y + height - 13, x + 28, y + height - 10, glow);
        g.fill(x + width - 28, y + height - 13, x + width - 8, y + height - 10, glow);
    }

    public static void rail(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x - 5, y - 8, x + width + 5, y - 4, STEEL);
        g.fill(x - 5, y + height + 4, x + width + 5, y + height + 8, STEEL);
        g.fill(x - 5, y - 4, x + width + 5, y - 2, COPPER);
        g.fill(x - 5, y + height + 2, x + width + 5, y + height + 4, COPPER);
        for (int i = 0; i <= width; i += 25) {
            g.fill(x + i - 1, y - 8, x + i + 1, y - 5, DARK);
            g.fill(x + i - 1, y + height + 5, x + i + 1, y + height + 8, DARK);
        }
    }

    public static void hammer(GuiGraphics g, int x, int y, int height, boolean striking) {
        int top = striking ? y - 7 : y - 16;
        g.fill(x - 5, top, x + 8, top + 6, DARK);
        g.fill(x - 4, top + 1, x + 7, top + 5, STEEL);
        g.fill(x - 2, top + 1, x + 5, top + 2, 0xFFC5CCD1);
        g.fill(x + 1, top + 6, x + 3, y + height + 4, 0xFF7E502C);
        g.fill(x + 1, y, x + 3, y + height, 0xFFFFF2C6);
    }

    /** 0 = miss, 1 = good, 2 = great, 3 = perfect; -1 = unplayed. */
    public static void progress(GuiGraphics g, int centerX, int y, int[] grades, int completed) {
        int left = centerX - 77;
        for (int i = 0; i < grades.length; i++) {
            int x = left + i * 16;
            int color = i >= completed ? 0xFF39424B : switch (grades[i]) {
                case 3 -> 0xFF9AFF74;
                case 2 -> 0xFF45C96E;
                case 1 -> 0xFFFFCC53;
                default -> 0xFFAA4845;
            };
            g.fill(x - 1, y - 1, x + 11, y + 9, DARK);
            g.fill(x, y, x + 10, y + 8, color);
            g.fill(x + 2, y + 1, x + 8, y + 2, 0x66FFFFFF);
        }
    }

    public static void sparks(GuiGraphics g, int x, int y, int remaining, int color) {
        if (remaining <= 0) return;
        int reach = 13 - remaining;
        g.fill(x - reach - 3, y - 3, x - reach, y, color);
        g.fill(x + reach, y - 7, x + reach + 3, y - 4, color);
        g.fill(x - reach / 2, y + 5, x - reach / 2 + 2, y + 8, color);
        g.fill(x + reach / 2, y + 2, x + reach / 2 + 2, y + 5, color);
    }
}
