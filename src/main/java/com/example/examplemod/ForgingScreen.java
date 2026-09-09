package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Visual foundation for the real Forge Block GUI.
 * Slots now follow the approved layout; actual persistent container syncing is the next step.
 */
public class ForgingScreen extends Screen {
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 210;

    public ForgingScreen() {
        super(Component.literal("Forge"));
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.literal("FORGE"), button -> {
            // The button will read the real slots after the server-backed container is connected.
        }).bounds(left + 82, top + 100, 76, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;

        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xEE555555);
        graphics.drawCenteredString(font, "FORGE", width / 2, top + 8, 0xFFFFFF);

        // Blueprint - blue slot.
        drawSlot(graphics, left + 24, top + 48, 0xFF3978C5);
        graphics.drawString(font, "Blueprint", left + 10, top + 73, 0xDDEEFF);

        // Monster material - yellow center slot.
        drawSlot(graphics, left + 108, top + 56, 0xFFE0B82F);
        graphics.drawCenteredString(font, "Monster", left + 117, top + 81, 0xFFF0C0);

        // Five same-metal slots around the monster material.
        drawSlot(graphics, left + 108, top + 29, 0xFF999999);
        drawSlot(graphics, left + 81, top + 45, 0xFF999999);
        drawSlot(graphics, left + 135, top + 45, 0xFF999999);
        drawSlot(graphics, left + 91, top + 75, 0xFF999999);
        drawSlot(graphics, left + 125, top + 75, 0xFF999999);

        // Fuel gauge + coal input on the right.
        graphics.fill(left + 196, top + 27, left + 211, top + 81, 0xFF222222);
        graphics.fill(left + 199, top + 55, left + 208, top + 78, 0xFFFF8A22);
        graphics.drawCenteredString(font, "FUEL", left + 203, top + 15, 0xFFFFFF);
        drawSlot(graphics, left + 194, top + 87, 0xFF777777);
        graphics.drawCenteredString(font, "Coal", left + 203, top + 109, 0xDDDDDD);

        // Arrow toward Forge button.
        graphics.fill(left + 114, top + 86, left + 120, top + 96, 0xFF111111);
        graphics.fill(left + 110, top + 93, left + 124, top + 97, 0xFF111111);

        // Player inventory representation, matching the Minecraft 9x3 + hotbar layout.
        graphics.drawString(font, "Inventory", left + 39, top + 132, 0xFFFFFF);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, left + 39 + col * 18, top + 143 + row * 18, 0xFF777777);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, left + 39 + col * 18, top + 199, 0xFF777777);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x - 1, y - 1, x + 19, y + 19, 0xFF202020);
        graphics.fill(x, y, x + 18, y + 18, color);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFF444444);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
