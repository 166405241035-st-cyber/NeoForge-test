package com.example.examplemod.client.ui;

import com.example.examplemod.forging.result.ForgingResult;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Shared forged-steel result panel for furnace and anvil rewards. */
public final class ForgingResultArt {
    public static final int TEXT = 0xFFF4E9D8;
    public static final int MUTED = 0xFFA8B0B8;
    public static final int COPPER = 0xFFD27A34;
    public static final int GOOD = 0xFFFFCC53;
    public static final int GREAT = 0xFF6ED48A;
    public static final int PERFECT = 0xFFB2F59B;
    public static final int MISS = 0xFFF17E79;

    private ForgingResultArt() {}

    public record Frame(int left, int top, int width, int height) {
        public int center() { return left + width / 2; }
        public int buttonY() { return top + height - 27; }
    }

    public static Frame draw(GuiGraphics g, Font font, int screenWidth, int screenHeight, String title) {
        int w = Math.min(330, screenWidth - 16);
        int h = Math.min(248, screenHeight - 10);
        int x = (screenWidth - w) / 2, y = (screenHeight - h) / 2;
        g.fill(0, 0, screenWidth, screenHeight, 0x990B0E12);
        g.fill(x, y, x + w, y + h, 0xFF0B0E12);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF59636E);
        g.fill(x + 5, y + 5, x + w - 5, y + h - 5, COPPER);
        g.fill(x + 7, y + 7, x + w - 7, y + h - 7, 0xFF20252B);
        g.fill(x + 13, y + 12, x + w - 13, y + 31, 0xFF101419);
        g.fill(x + 17, y + 14, x + w - 17, y + 15, COPPER);
        g.drawCenteredString(font, title, x + w / 2, y + 20, TEXT);
        g.fill(x + 14, y + 88, x + w - 14, y + 89, 0xFF59636E);
        g.fill(x + 32, y + 89, x + w - 32, y + 90, COPPER);
        g.fill(x + 14, y + h - 36, x + w - 14, y + h - 35, COPPER);
        return new Frame(x, y, w, h);
    }

    public static void score(GuiGraphics g, Font font, Frame frame, ForgingResult result) {
        int cx = frame.center(), y = frame.top();
        g.drawCenteredString(font, "SCORE  " + result.score(), cx, y + 100, GOOD);
        g.drawCenteredString(font, String.format("ACCURACY  %.1f%%", result.accuracy()), cx, y + 120, TEXT);
        g.drawCenteredString(font, "MAX COMBO  x" + result.maxCombo(), cx, y + 137, MUTED);
        int cell = (frame.width() - 28) / 4;
        int start = frame.left() + 14;
        int[] values = {result.perfectCount(), result.greatCount(), result.goodCount(), result.missCount()};
        String[] labels = {"PERFECT", "GREAT", "GOOD", "MISS"};
        int[] colors = {PERFECT, GREAT, GOOD, MISS};
        for (int i = 0; i < 4; i++) {
            int x = start + i * cell, center = x + cell / 2;
            g.fill(x + 2, y + 157, x + cell - 2, y + 190, 0xFF101419);
            g.fill(x + 3, y + 157, x + cell - 3, y + 159, colors[i]);
            g.drawCenteredString(font, labels[i], center, y + 164, MUTED);
            g.drawCenteredString(font, Integer.toString(values[i]), center, y + 178, colors[i]);
        }
    }

    public static void detail(GuiGraphics g, Font font, Frame frame, String item, String effect, String source) {
        int cx = frame.center(), y = frame.top();
        g.drawCenteredString(font, item, cx, y + 43, 0xFF80D5E5);
        g.drawCenteredString(font, effect, cx, y + 59, GREAT);
        g.drawCenteredString(font, source, cx, y + 74, MUTED);
    }
}
