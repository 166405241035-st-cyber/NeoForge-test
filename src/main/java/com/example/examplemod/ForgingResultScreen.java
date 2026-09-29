package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

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
            if (this.minecraft != null) this.minecraft.setScreen(null);
        }).bounds(this.width / 2 - 50, (height - Math.min(248, height - 10)) / 2 + Math.min(248, height - 10) - 27, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ForgingResultArt.Frame frame = ForgingResultArt.draw(guiGraphics, font, width, height, "FORGING COMPLETE");
        if (headResult != null) {
            ForgingResultArt.detail(guiGraphics, font, frame,
                    headResult.metal().displayName() + " " + formatName(headResult.blueprint().name()) + " Head",
                    headResult.effect().displayName() + " " + roman(headResult.tier()),
                    "Source: " + formatName(headResult.monsterMaterial().name()));
        }
        ForgingResultArt.score(guiGraphics, font, frame, result);
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
