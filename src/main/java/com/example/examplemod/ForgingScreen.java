package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ForgingScreen extends Screen {
    // Temporary selection until the real forge inventory reads the metal item.
    private ForgingMetal selectedMetal = ForgingMetal.IRON;

    public ForgingScreen() {
        super(Component.literal("Forging"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 45;

        this.addRenderableWidget(Button.builder(metalButtonText(), button -> {
            cycleMetal();
            button.setMessage(metalButtonText());
        }).bounds(centerX - 100, startY - 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Minigame 1 - Timing Bar"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new TimingBarScreen(selectedMetal));
            }
        }).bounds(centerX - 100, startY, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Minigame 2 - Rhythm Forging"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new RhythmForgingScreen(selectedMetal));
            }
        }).bounds(centerX - 100, startY + 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(centerX - 50, startY + 70, 100, 20).build());
    }

    private Component metalButtonText() {
        return Component.literal("Metal: " + selectedMetal.displayName() + "   Difficulty " + selectedMetal.difficulty() + "/3");
    }

    private void cycleMetal() {
        ForgingMetal[] metals = ForgingMetal.values();
        selectedMetal = metals[(selectedMetal.ordinal() + 1) % metals.length];
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, "FORGING", this.width / 2, this.height / 2 - 105, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
