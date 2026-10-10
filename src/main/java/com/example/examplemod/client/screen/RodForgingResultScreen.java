package com.example.examplemod.client.screen;

import com.example.examplemod.client.ui.ForgingResultArt;
import com.example.examplemod.forging.result.ForgedRodResult;
import com.example.examplemod.forging.result.ForgingResult;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RodForgingResultScreen extends Screen {
    private final ForgingResult result;
    private final ForgedRodResult rodResult;

    public RodForgingResultScreen(ForgingResult result, ForgedRodResult rodResult) {
        super(Component.literal("Rod Forging Result"));
        this.result = result;
        this.rodResult = rodResult;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Continue"), button -> {
            if (minecraft != null) minecraft.setScreen(null);
        }).bounds(width / 2 - 50, ((height - Math.min(248, height - 10)) / 2 + Math.min(248, height - 10) - 27), 100, 20).build());
    }

    @Override public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ForgingResultArt.Frame frame = ForgingResultArt.draw(guiGraphics, font, width, height, "ROD FORGING COMPLETE");
        ForgingResultArt.detail(guiGraphics, font, frame,
                rodResult.metal().displayName() + " Forged Rod",
                "Effect " + rodResult.tier().name(),
                "Source: " + formatName(rodResult.monsterMaterial().name()));
        ForgingResultArt.score(guiGraphics, font, frame, result);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private static String formatName(String value) {
        String[] words = value.toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    @Override public boolean isPauseScreen() { return false; }
}
