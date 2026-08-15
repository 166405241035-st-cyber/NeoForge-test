package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ForgingScreen extends Screen {
    public ForgingScreen() {
        super(Component.literal("Forging"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 45;

        this.addRenderableWidget(Button.builder(Component.literal("Minigame 1 - Timing Bar"), button -> {
            // Minigame logic will be added after the menu itself is verified.
        }).bounds(centerX - 100, startY, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Minigame 2 - Rhythm Forging"), button -> {
            // Minigame logic will be added later.
        }).bounds(centerX - 100, startY + 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(centerX - 50, startY + 70, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, "FORGING", this.width / 2, this.height / 2 - 75, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
