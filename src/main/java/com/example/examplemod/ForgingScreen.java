package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ForgingScreen extends Screen {
    // Temporary selectors until the real forge inventory reads actual items.
    private ForgingMetal selectedMetal = ForgingMetal.IRON;
    private HeadBlueprintType selectedBlueprint = HeadBlueprintType.SWORD;
    private MonsterMaterial selectedMonsterMaterial = MonsterMaterial.ROTTEN_FLESH;

    public ForgingScreen() {
        super(Component.literal("Forging"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        addRenderableWidget(Button.builder(metalButtonText(), button -> {
            cycleMetal();
            button.setMessage(metalButtonText());
        }).bounds(centerX - 110, startY - 45, 220, 20).build());

        addRenderableWidget(Button.builder(blueprintButtonText(), button -> {
            cycleBlueprint();
            button.setMessage(blueprintButtonText());
        }).bounds(centerX - 110, startY - 20, 220, 20).build());

        addRenderableWidget(Button.builder(monsterMaterialButtonText(), button -> {
            cycleMonsterMaterial();
            button.setMessage(monsterMaterialButtonText());
        }).bounds(centerX - 110, startY + 5, 220, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Forge Head - Timing Bar"), button -> {
            if (minecraft != null) minecraft.setScreen(new TimingBarScreen(selectedMetal, selectedBlueprint, selectedMonsterMaterial));
        }).bounds(centerX - 110, startY + 35, 220, 20).build());

        // Core Blueprint is universal: only metal + monster material need selecting.
        addRenderableWidget(Button.builder(Component.literal("Forge Core - Timing Bar"), button -> {
            if (minecraft != null) minecraft.setScreen(new CoreTimingBarScreen(selectedMetal, selectedMonsterMaterial));
        }).bounds(centerX - 110, startY + 60, 220, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Minigame 2 - Rhythm Forging"), button -> {
            if (minecraft != null) minecraft.setScreen(new RhythmForgingScreen(selectedMetal));
        }).bounds(centerX - 110, startY + 85, 220, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(centerX - 50, startY + 115, 100, 20).build());
    }

    private Component metalButtonText() {
        return Component.literal("Metal: " + selectedMetal.displayName() + "   Difficulty " + selectedMetal.difficulty() + "/3");
    }

    private Component blueprintButtonText() {
        return Component.literal("Head Blueprint: " + formatName(selectedBlueprint.name()));
    }

    private Component monsterMaterialButtonText() {
        return Component.literal("Monster Material: " + formatName(selectedMonsterMaterial.name()));
    }

    private void cycleMetal() {
        ForgingMetal[] values = ForgingMetal.values();
        selectedMetal = values[(selectedMetal.ordinal() + 1) % values.length];
    }

    private void cycleBlueprint() {
        HeadBlueprintType[] values = HeadBlueprintType.values();
        selectedBlueprint = values[(selectedBlueprint.ordinal() + 1) % values.length];
    }

    private void cycleMonsterMaterial() {
        MonsterMaterial[] values = MonsterMaterial.values();
        selectedMonsterMaterial = values[(selectedMonsterMaterial.ordinal() + 1) % values.length];
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, "FORGING TEST", width / 2, height / 2 - 145, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "Head Blueprint is typed | Core Blueprint is universal", width / 2, height / 2 - 132, 0xDDDDDD);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
