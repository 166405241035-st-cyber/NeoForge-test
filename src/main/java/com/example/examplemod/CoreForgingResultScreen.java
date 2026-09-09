package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CoreForgingResultScreen extends Screen {
    private final ForgingResult result;
    private final ForgedCoreResult coreResult;

    public CoreForgingResultScreen(ForgingResult result, ForgedCoreResult coreResult) {
        super(Component.literal("Core Forging Result"));
        this.result = result;
        this.coreResult = coreResult;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Continue"), button -> {
            if (minecraft != null) minecraft.setScreen(new ForgingScreen());
        }).bounds(width / 2 - 50, height / 2 + 95, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int top = height / 2 - 125;
        guiGraphics.fill(centerX - 155, top, centerX + 155, height / 2 + 130, 0xD0000000);
        guiGraphics.drawCenteredString(font, "CORE FORGING COMPLETE", centerX, top + 14, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, coreResult.metal().displayName() + " Forged Core", centerX, top + 34, 0x55FFFF);
        guiGraphics.drawCenteredString(font, "Effect " + coreResult.tier().name(), centerX, top + 50, 0x55FFFF);
        guiGraphics.drawCenteredString(font, "Source: " + formatName(coreResult.monsterMaterial().name()), centerX, top + 66, 0xBBBBBB);
        int statsTop = top + 88;
        guiGraphics.drawCenteredString(font, "SCORE: " + result.score(), centerX, statsTop, 0xFFFF55);
        guiGraphics.drawCenteredString(font, String.format("ACCURACY: %.1f%%", result.accuracy()), centerX, statsTop + 18, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "MAX COMBO: x" + result.maxCombo(), centerX, statsTop + 36, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "PERFECT: " + result.perfectCount(), centerX, statsTop + 60, 0x55FF55);
        guiGraphics.drawCenteredString(font, "GREAT: " + result.greatCount(), centerX, statsTop + 76, 0xAAFF55);
        guiGraphics.drawCenteredString(font, "GOOD: " + result.goodCount(), centerX, statsTop + 92, 0xFFFF55);
        guiGraphics.drawCenteredString(font, "MISS: " + result.missCount(), centerX, statsTop + 108, 0xFF5555);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
