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
        int startY = this.height / 2 - 65;

        this.addRenderableWidget(Button.builder(metalButtonText(), button -> {
            cycleMetal();
            button.setMessage(metalButtonText());
        }).bounds(centerX - 110, startY - 45, 220, 20).build());

        this.addRenderableWidget(Button.builder(blueprintButtonText(), button -> {
            cycleBlueprint();
            button.setMessage(blueprintButtonText());
        }).bounds(centerX - 110, startY - 20, 220, 20).build());

        this.addRenderableWidget(Button.builder(monsterMaterialButtonText(), button -> {
            cycleMonsterMaterial();
            button.setMessage(monsterMaterialButtonText());
        }).bounds(centerX - 110, startY + 5, 220, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Forge Head - Timing Bar"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new TimingBarScreen(selectedMetal, selectedBlueprint, selectedMonsterMaterial));
            }
        }).bounds(centerX - 110, startY + 38, 220, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Minigame 2 - Rhythm Forging"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new RhythmForgingScreen(selectedMetal));
            }
        }).bounds(centerX - 110, startY + 68, 220, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(centerX - 50, startY + 105, 100, 20).build());
    }

    private Component metalButtonText() {
        return Component.literal("Metal: " + selectedMetal.displayName() + "   Difficulty " + selectedMetal.difficulty() + "/3");
    }

    private Component blueprintButtonText() {
        return Component.literal("Blueprint: " + formatName(selectedBlueprint.name()) + " Head");
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
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, "FORGING TEST", this.width / 2, this.height / 2 - 130, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Metal + Blueprint + Monster Material", this.width / 2, this.height / 2 - 117, 0xDDDDDD);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
