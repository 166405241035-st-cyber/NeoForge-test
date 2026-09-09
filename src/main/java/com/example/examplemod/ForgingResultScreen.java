package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ForgingResultScreen extends Screen {
    private final ForgingResult result;
    private final ForgedHeadResult headResult;

    public ForgingResultScreen(ForgingResult result) {
        this(result, null);
    }

    public ForgingResultScreen(ForgingResult result, ForgedHeadResult headResult) {
        super(Component.literal("Forging Result"));
        this.result = result;
        this.headResult = headResult;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Continue"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ForgingScreen());
            }
        }).bounds(this.width / 2 - 50, this.height / 2 + 95, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int top = this.height / 2 - 125;
        int left = centerX - 155;
        int right = centerX + 155;
        int bottom = this.height / 2 + 130;

        guiGraphics.fill(left, top, right, bottom, 0xD0000000);
        guiGraphics.drawCenteredString(this.font, "FORGING COMPLETE", centerX, top + 14, 0xFFFFFF);

        if (headResult != null) {
            guiGraphics.drawCenteredString(this.font,
                    headResult.metal().displayName() + " " + formatName(headResult.blueprint().name()) + " Head",
                    centerX, top + 34, 0x55FFFF);
            guiGraphics.drawCenteredString(this.font,
                    "Effect: " + headResult.effect().displayName() + " " + roman(headResult.tier()),
                    centerX, top + 50, 0xAAFF55);
            guiGraphics.drawCenteredString(this.font,
                    "Source: " + formatName(headResult.monsterMaterial().name()),
                    centerX, top + 66, 0xBBBBBB);
        }

        int statsTop = headResult == null ? top + 42 : top + 88;
        guiGraphics.drawCenteredString(this.font, "SCORE: " + result.score(), centerX, statsTop, 0xFFFF55);
        guiGraphics.drawCenteredString(this.font, String.format("ACCURACY: %.1f%%", result.accuracy()), centerX, statsTop + 18, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "MAX COMBO: x" + result.maxCombo(), centerX, statsTop + 36, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "PERFECT: " + result.perfectCount(), centerX, statsTop + 60, 0x55FF55);
        guiGraphics.drawCenteredString(this.font, "GREAT: " + result.greatCount(), centerX, statsTop + 76, 0xAAFF55);
        guiGraphics.drawCenteredString(this.font, "GOOD: " + result.goodCount(), centerX, statsTop + 92, 0xFFFF55);
        guiGraphics.drawCenteredString(this.font, "MISS: " + result.missCount(), centerX, statsTop + 108, 0xFF5555);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private static String roman(EffectTier tier) {
        return switch (tier) {
            case I -> "I";
            case II -> "II";
            case III -> "III";
        };
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
