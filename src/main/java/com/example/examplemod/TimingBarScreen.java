package com.example.examplemod;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TimingBarScreen extends Screen {
    private static final int BAR_WIDTH = 300;
    private static final int BAR_HEIGHT = 24;
    private static final int CURSOR_WIDTH = 3;
    private static final int TOTAL_ROUNDS = 10;
    private static final float CURSOR_MIN_SPEED = 6.0F;
    private static final float CURSOR_MAX_SPEED = 12.0F;
    private static final int GREEN_MIN_WIDTH = 45;
    private static final int GREEN_MAX_WIDTH = 100;
    private static final float SPEED_PER_DIFFICULTY_STEP = 1.5F;
    private static final int TARGET_SHRINK_PER_DIFFICULTY_STEP = 10;
    private static final int PERFECT_SCORE = 100;
    private static final int GREAT_SCORE = 75;
    private static final int GOOD_SCORE = 50;
    private static final int MISS_SCORE = 0;
    private static final int COMBO_BONUS_PER_STEP = 5;
    private static final float PERFECT_RATIO = 0.20F;
    private static final float GREAT_RATIO = 0.55F;
    private static final int GOOD_ZONE_COLOR = 0xFFFFCC33;
    private static final int GREAT_ZONE_COLOR = 0xFF22AA44;
    private static final int PERFECT_ZONE_COLOR = 0xFF66FF66;

    private final Random random = new Random();
    private final ForgingMetal metal;
    private final HeadBlueprintType blueprint;
    private final MonsterMaterial monsterMaterial;
    private int greenStart;
    private int greenWidth;
    private float cursorPosition;
    private float cursorSpeed;
    private boolean movingRight = true;
    private String resultText = "Press SPACE on the best colored zone";
    private int resultColor = 0xFFFFFF;
    private int round;
    private int score;
    private int currentCombo;
    private int maxCombo;
    private int perfectCount;
    private int greatCount;
    private int goodCount;
    private int missCount;
    private int weightedAccuracyPoints;
    private boolean finished;

    public TimingBarScreen() { this(ForgingMetal.IRON, HeadBlueprintType.SWORD, MonsterMaterial.ROTTEN_FLESH); }
    public TimingBarScreen(ForgingMetal metal) { this(metal, HeadBlueprintType.SWORD, MonsterMaterial.ROTTEN_FLESH); }

    public TimingBarScreen(ForgingMetal metal, HeadBlueprintType blueprint, MonsterMaterial monsterMaterial) {
        super(Component.literal("Timing Bar"));
        this.metal = metal;
        this.blueprint = blueprint;
        this.monsterMaterial = monsterMaterial;
        randomizeRound();
    }

    private void randomizeRound() {
        int difficultySteps = metal.difficulty() - 1;
        int minWidth = Math.max(20, GREEN_MIN_WIDTH - difficultySteps * TARGET_SHRINK_PER_DIFFICULTY_STEP);
        int maxWidth = Math.max(minWidth, GREEN_MAX_WIDTH - difficultySteps * TARGET_SHRINK_PER_DIFFICULTY_STEP);
        greenWidth = minWidth + random.nextInt(maxWidth - minWidth + 1);
        greenStart = random.nextInt(BAR_WIDTH - greenWidth + 1);
        float speedBonus = difficultySteps * SPEED_PER_DIFFICULTY_STEP;
        float minSpeed = CURSOR_MIN_SPEED + speedBonus;
        float maxSpeed = CURSOR_MAX_SPEED + speedBonus;
        cursorSpeed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
    }

    @Override
    public void tick() {
        if (finished) return;
        cursorPosition += movingRight ? cursorSpeed : -cursorSpeed;
        if (cursorPosition >= BAR_WIDTH - CURSOR_WIDTH) { cursorPosition = BAR_WIDTH - CURSOR_WIDTH; movingRight = false; }
        else if (cursorPosition <= 0) { cursorPosition = 0; movingRight = true; }
    }

    private void attemptHit() {
        if (finished) return;
        float cursorCenter = cursorPosition + CURSOR_WIDTH / 2.0F;
        float greenEnd = greenStart + greenWidth;
        int baseScore;
        int accuracyPoints;
        if (cursorCenter >= greenStart && cursorCenter <= greenEnd) {
            float greenCenter = greenStart + greenWidth / 2.0F;
            float distance = Math.abs(cursorCenter - greenCenter);
            float normalizedDistance = distance / (greenWidth / 2.0F);
            if (normalizedDistance <= PERFECT_RATIO) {
                resultText = "PERFECT! +" + PERFECT_SCORE; resultColor = PERFECT_ZONE_COLOR; perfectCount++; baseScore = PERFECT_SCORE; accuracyPoints = 100;
            } else if (normalizedDistance <= GREAT_RATIO) {
                resultText = "GREAT! +" + GREAT_SCORE; resultColor = GREAT_ZONE_COLOR; greatCount++; baseScore = GREAT_SCORE; accuracyPoints = 75;
            } else {
                resultText = "GOOD! +" + GOOD_SCORE; resultColor = GOOD_ZONE_COLOR; goodCount++; baseScore = GOOD_SCORE; accuracyPoints = 50;
            }
            currentCombo++; maxCombo = Math.max(maxCombo, currentCombo);
        } else {
            resultText = "MISS! +" + MISS_SCORE; resultColor = 0xFF5555; missCount++; baseScore = MISS_SCORE; accuracyPoints = 0; currentCombo = 0;
        }
        int comboBonus = baseScore > 0 ? Math.max(0, currentCombo - 1) * COMBO_BONUS_PER_STEP : 0;
        score += baseScore + comboBonus;
        weightedAccuracyPoints += accuracyPoints;
        round++;
        if (round >= TOTAL_ROUNDS) { finished = true; openResultScreen(); } else randomizeRound();
    }

    private void openResultScreen() {
        if (minecraft == null) return;
        double accuracy = weightedAccuracyPoints / (double) TOTAL_ROUNDS;
        ForgingResult result = new ForgingResult(score, accuracy, maxCombo, perfectCount, greatCount, goodCount, missCount);
        ForgingEffect effect = EffectPool.randomEffect(monsterMaterial, blueprint, random);
        EffectTier tier = EffectTierRoller.roll(accuracy, random);
        ForgedHeadResult headResult = new ForgedHeadResult(metal, blueprint, monsterMaterial, effect, tier);
        if (minecraft.player != null) {
            ItemStack forgedHead = ForgedHeadItem.create(headResult);
            if (!minecraft.player.getInventory().add(forgedHead)) minecraft.player.drop(forgedHead, false);
        }
        minecraft.setScreen(new ForgingResultScreen(result, headResult));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) { attemptHit(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int barX = (width - BAR_WIDTH) / 2;
        int barY = height / 2 - BAR_HEIGHT / 2;
        int panelTop = barY - 105;
        int panelBottom = barY + 82;
        int panelLeft = barX - 28;
        int panelRight = barX + BAR_WIDTH + 28;
        guiGraphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xB0000000);
        guiGraphics.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0xFFAAAAAA);
        guiGraphics.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0xFFAAAAAA);
        guiGraphics.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0xFFAAAAAA);
        guiGraphics.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0xFFAAAAAA);
        guiGraphics.drawCenteredString(font, "TIMING FORGING - " + metal.displayName() + " [Difficulty " + metal.difficulty() + "/3]", width / 2, barY - 88, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, blueprint.name() + " HEAD + " + monsterMaterial.name(), width / 2, barY - 74, 0xCCCCCC);
        guiGraphics.drawCenteredString(font, "ROUND " + Math.min(round + 1, TOTAL_ROUNDS) + " / " + TOTAL_ROUNDS, width / 2, barY - 56, 0xDDDDDD);
        guiGraphics.drawCenteredString(font, resultText, width / 2, barY - 38, resultColor);
        guiGraphics.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, 0xFF111111);
        guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFAA2222);
        guiGraphics.fill(barX + greenStart, barY, barX + greenStart + greenWidth, barY + BAR_HEIGHT, GOOD_ZONE_COLOR);
        float halfWidth = greenWidth / 2.0F;
        float center = greenStart + halfWidth;
        int greatLeft = barX + Math.round(center - halfWidth * GREAT_RATIO);
        int greatRight = barX + Math.round(center + halfWidth * GREAT_RATIO);
        guiGraphics.fill(greatLeft, barY, greatRight, barY + BAR_HEIGHT, GREAT_ZONE_COLOR);
        int perfectLeft = barX + Math.round(center - halfWidth * PERFECT_RATIO);
        int perfectRight = barX + Math.round(center + halfWidth * PERFECT_RATIO);
        guiGraphics.fill(perfectLeft, barY, perfectRight, barY + BAR_HEIGHT, PERFECT_ZONE_COLOR);
        int cursorX = barX + Math.round(cursorPosition);
        guiGraphics.fill(cursorX, barY - 5, cursorX + CURSOR_WIDTH, barY + BAR_HEIGHT + 5, 0xFFFFFFFF);
        guiGraphics.drawString(font, "Score: " + score, barX, barY + 38, 0xFFFFFF);
        guiGraphics.drawString(font, "Combo: x" + currentCombo, barX + 115, barY + 38, 0xFFFFFF);
        guiGraphics.drawString(font, "Max: x" + maxCombo, barX + 225, barY + 38, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, "YELLOW=GOOD  GREEN=GREAT  BRIGHT=PERFECT", width / 2, barY + 54, 0xDDDDDD);
        guiGraphics.drawCenteredString(font, "SPACE = HIT   |   ESC = BACK", width / 2, barY + 68, 0xDDDDDD);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(null); }
    @Override public boolean isPauseScreen() { return false; }
}
