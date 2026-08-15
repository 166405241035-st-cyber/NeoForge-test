package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ForgingResultScreen extends Screen {
    private final ForgingResult result;

    public ForgingResultScreen(ForgingResult result) {
        super(Component.literal("Forging Result"));
        this.result = result;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Continue"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ForgingScreen());
            }
        }).bounds(this.width / 2 - 50, this.height / 2 + 75, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Keep the world visible and sharp, matching the minigame screen.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int top = this.height / 2 - 105;
        int left = centerX - 135;
        int right = centerX + 135;
        int bottom = this.height / 2 + 110;

        guiGraphics.fill(left, top, right, bottom, 0xD0000000);
        guiGraphics.drawCenteredString(this.font, "FORGING COMPLETE", centerX, top + 15, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "SCORE: " + result.score(), centerX, top + 42, 0xFFFF55);
        guiGraphics.drawCenteredString(this.font, String.format("ACCURACY: %.1f%%", result.accuracy()), centerX, top + 62, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "MAX COMBO: x" + result.maxCombo(), centerX, top + 82, 0xFFFFFF);

        guiGraphics.drawCenteredString(this.font, "PERFECT: " + result.perfectCount(), centerX, top + 108, 0x55FF55);
        guiGraphics.drawCenteredString(this.font, "GREAT: " + result.greatCount(), centerX, top + 124, 0xAAFF55);
        guiGraphics.drawCenteredString(this.font, "GOOD: " + result.goodCount(), centerX, top + 140, 0xFFFF55);
        guiGraphics.drawCenteredString(this.font, "MISS: " + result.missCount(), centerX, top + 156, 0xFF5555);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
