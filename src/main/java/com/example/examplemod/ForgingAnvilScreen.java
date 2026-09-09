package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Separate Anvil screen foundation.
 * The final Head/Core/Rod combination rules are intentionally not implemented yet.
 */
public class ForgingAnvilScreen extends Screen {
    public ForgingAnvilScreen() {
        super(Component.literal("Forging Anvil"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Rhythm Forging"), button -> {
            if (minecraft != null) minecraft.setScreen(new RhythmForgingScreen(ForgingMetal.IRON));
        }).bounds(centerX - 70, height / 2 + 35, 140, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 100;
        int top = height / 2 - 75;
        graphics.fill(left, top, left + 200, top + 150, 0xEE4A4A4A);
        graphics.drawCenteredString(font, "FORGING ANVIL", width / 2, top + 12, 0xFFFFFF);
        graphics.drawCenteredString(font, "HEAD", width / 2 - 55, top + 48, 0xDDDDDD);
        graphics.drawCenteredString(font, "CORE", width / 2, top + 48, 0xDDDDDD);
        graphics.drawCenteredString(font, "ROD", width / 2 + 55, top + 48, 0xDDDDDD);
        graphics.drawCenteredString(font, "Final combination rules: not locked yet", width / 2, top + 105, 0xBBBBBB);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
