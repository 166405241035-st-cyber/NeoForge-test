package com.example.examplemod;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimingBarScreen extends Screen {
    // ==================== EASY TUNING VALUES ====================
    private static final int BAR_WIDTH = 300;
    private static final int BAR_HEIGHT = 24;
    private static final int CURSOR_WIDTH = 3;
    private static final int TOTAL_ROUNDS = 10;

    // WHITE LINE RANDOM SPEED RANGE. A new speed is picked after every hit.
    private static final float CURSOR_MIN_SPEED = 6.0F;
    private static final float CURSOR_MAX_SPEED = 10.0F;

    private static final int GREEN_MIN_WIDTH = 45;
    private static final int GREEN_MAX_WIDTH = 100;

    // Base score awarded for each visible colored zone.
    private static final int PERFECT_SCORE = 100;
    private static final int GREAT_SCORE = 75;
    private static final int GOOD_SCORE = 50;
    private static final int MISS_SCORE = 0;

    private static final int PERFECT_ACCURACY = 100;
    private static final int GREAT_ACCURACY = 75;
    private static final int GOOD_ACCURACY = 50;
    private static final int MISS_ACCURACY = 0;

    private static final int COMBO_BONUS_PER_STEP = 5;

    // How much of the target is used for each grade.
    // PERFECT_RATIO = center 20%, GREAT_RATIO = area up to 55%, remaining target = GOOD.
    private static final float PERFECT_RATIO = 0.20F;
    private static final float GREAT_RATIO = 0.55F;

    // Visible grade colors.
    private static final int GOOD_ZONE_COLOR = 0xFFFFCC33;    // Yellow = GOOD
    private static final int GREAT_ZONE_COLOR = 0xFF22AA44;   // Green = GREAT
    private static final int PERFECT_ZONE_COLOR = 0xFF66FF66; // Bright green = PERFECT
    // ============================================================

    private final Random random = new Random();
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

    public TimingBarScreen() {
        super(Component.literal("Timing Bar"));
        randomizeRound();
    }

    private void randomizeRound() {
        greenWidth = GREEN_MIN_WIDTH + random.nextInt(GREEN_MAX_WIDTH - GREEN_MIN_WIDTH + 1);
        greenStart = random.nextInt(BAR_WIDTH - greenWidth + 1);

        // Random decimal speed between CURSOR_MIN_SPEED and CURSOR_MAX_SPEED.
        cursorSpeed = CURSOR_MIN_SPEED + random.nextFloat() * (CURSOR_MAX_SPEED - CURSOR_MIN_SPEED);
    }

    @Override
    public void tick() {
        if (finished) {
            return;
        }

        cursorPosition += movingRight ? cursorSpeed : -cursorSpeed;

        if (cursorPosition >= BAR_WIDTH - CURSOR_WIDTH) {
            cursorPosition = BAR_WIDTH - CURSOR_WIDTH;
            movingRight = false;
        } else if (cursorPosition <= 0) {
            cursorPosition = 0;
            movingRight = true;
        }
    }

    private void attemptHit() {
        if (finished) {
            return;
        }

        float cursorCenter = cursorPosition + CURSOR_WIDTH / 2.0F;
        float greenEnd = greenStart + greenWidth;
        int baseScore;
        int accuracyPoints;

        if (cursorCenter >= greenStart && cursorCenter <= greenEnd) {
            float greenCenter = greenStart + greenWidth / 2.0F;
            float distance = Math.abs(cursorCenter - greenCenter);
            float normalizedDistance = distance / (greenWidth / 2.0F);

            if (normalizedDistance <= PERFECT_RATIO) {
                resultText = "PERFECT! +" + PERFECT_SCORE;
                resultColor = PERFECT_ZONE_COLOR;
                perfectCount++;
                baseScore = PERFECT_SCORE;
                accuracyPoints = PERFECT_ACCURACY;
            } else if (normalizedDistance <= GREAT_RATIO) {
                resultText = "GREAT! +" + GREAT_SCORE;
                resultColor = GREAT_ZONE_COLOR;
                greatCount++;
                baseScore = GREAT_SCORE;
                accuracyPoints = GREAT_ACCURACY;
            } else {
                resultText = "GOOD! +" + GOOD_SCORE;
                resultColor = GOOD_ZONE_COLOR;
                goodCount++;
                baseScore = GOOD_SCORE;
                accuracyPoints = GOOD_ACCURACY;
            }

            currentCombo++;
            maxCombo = Math.max(maxCombo, currentCombo);
        } else {
            resultText = "MISS! +" + MISS_SCORE;
            resultColor = 0xFF5555;
            missCount++;
            baseScore = MISS_SCORE;
            accuracyPoints = MISS_ACCURACY;
            currentCombo = 0;
        }

        int comboBonus = baseScore > 0 ? Math.max(0, currentCombo - 1) * COMBO_BONUS_PER_STEP : 0;
        score += baseScore + comboBonus;
        weightedAccuracyPoints += accuracyPoints;
        round++;

        if (round >= TOTAL_ROUNDS) {
            finished = true;
            openResultScreen();
        } else {
            randomizeRound();
        }
    }

    private void openResultScreen() {
        if (this.minecraft == null) {
            return;
        }

        double accuracy = weightedAccuracyPoints / (double) TOTAL_ROUNDS;
        ForgingResult result = new ForgingResult(
                score,
                accuracy,
                maxCombo,
                perfectCount,
                greatCount,
                goodCount,
                missCount);
        this.minecraft.setScreen(new ForgingResultScreen(result));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            attemptHit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Keep the world behind the minigame sharp.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int barX = (this.width - BAR_WIDTH) / 2;
        int barY = this.height / 2 - BAR_HEIGHT / 2;

        int panelPaddingX = 28;
        int panelTop = barY - 92;
        int panelBottom = barY + 82;
        int panelLeft = barX - panelPaddingX;
        int panelRight = barX + BAR_WIDTH + panelPaddingX;

        guiGraphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xB0000000);
        guiGraphics.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0xFFAAAAAA);
        guiGraphics.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0xFFAAAAAA);
        guiGraphics.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0xFFAAAAAA);
        guiGraphics.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0xFFAAAAAA);

        guiGraphics.drawCenteredString(this.font, "TIMING FORGING", this.width / 2, barY - 72, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "ROUND " + Math.min(round + 1, TOTAL_ROUNDS) + " / " + TOTAL_ROUNDS,
                this.width / 2, barY - 56, 0xDDDDDD);
        guiGraphics.drawCenteredString(this.font, resultText, this.width / 2, barY - 38, resultColor);

        // Red = MISS area outside the target.
        guiGraphics.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, 0xFF111111);
        guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFAA2222);

        // Draw the target from outside -> inside so players can SEE the score grade before pressing SPACE.
        // Yellow outer target = GOOD.
        guiGraphics.fill(barX + greenStart, barY, barX + greenStart + greenWidth, barY + BAR_HEIGHT, GOOD_ZONE_COLOR);

        float halfWidth = greenWidth / 2.0F;
        float center = greenStart + halfWidth;

        // Green middle target = GREAT. normalizedDistance <= GREAT_RATIO.
        int greatLeft = barX + Math.round(center - halfWidth * GREAT_RATIO);
        int greatRight = barX + Math.round(center + halfWidth * GREAT_RATIO);
        guiGraphics.fill(greatLeft, barY, greatRight, barY + BAR_HEIGHT, GREAT_ZONE_COLOR);

        // Bright green center target = PERFECT. normalizedDistance <= PERFECT_RATIO.
        int perfectLeft = barX + Math.round(center - halfWidth * PERFECT_RATIO);
        int perfectRight = barX + Math.round(center + halfWidth * PERFECT_RATIO);
        guiGraphics.fill(perfectLeft, barY, perfectRight, barY + BAR_HEIGHT, PERFECT_ZONE_COLOR);

        int cursorX = barX + Math.round(cursorPosition);
        guiGraphics.fill(cursorX, barY - 5, cursorX + CURSOR_WIDTH, barY + BAR_HEIGHT + 5, 0xFFFFFFFF);

        guiGraphics.drawString(this.font, "Score: " + score, barX, barY + 38, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Combo: x" + currentCombo, barX + 115, barY + 38, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Max: x" + maxCombo, barX + 225, barY + 38, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "YELLOW=GOOD  GREEN=GREAT  BRIGHT=PERFECT", this.width / 2, barY + 54, 0xDDDDDD);
        guiGraphics.drawCenteredString(this.font, "SPACE = HIT   |   ESC = BACK", this.width / 2, barY + 68, 0xDDDDDD);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ForgingScreen());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
