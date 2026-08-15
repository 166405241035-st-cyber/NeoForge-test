package com.example.examplemod;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimingBarScreen extends Screen {
    // ==================== EASY TUNING VALUES ====================
    // These values are grouped here so the minigame can be balanced later.
    private static final int BAR_WIDTH = 300;       // Total red timing bar width in pixels.
    private static final int BAR_HEIGHT = 24;       // Timing bar height in pixels.
    private static final int CURSOR_WIDTH = 4;      // WIDTH/THICKNESS OF THE WHITE MOVING LINE. Change this to make the white line thicker/thinner.
    private static final int TOTAL_ROUNDS = 10;     // Number of hits before the result screen opens.

    private static final float CURSOR_SPEED = 4.0F; // Speed of the white moving line. Higher = faster/harder.

    private static final int GREEN_MIN_WIDTH = 45;  // Smallest possible green target width.
    private static final int GREEN_MAX_WIDTH = 100; // Largest possible green target width.

    // Base score awarded for each hit grade.
    private static final int PERFECT_SCORE = 100;
    private static final int GREAT_SCORE = 75;
    private static final int GOOD_SCORE = 50;
    private static final int MISS_SCORE = 0;

    // Accuracy contribution for each grade. These are percentages before averaging all rounds.
    private static final int PERFECT_ACCURACY = 100;
    private static final int GREAT_ACCURACY = 75;
    private static final int GOOD_ACCURACY = 50;
    private static final int MISS_ACCURACY = 0;

    // Every successful combo step after the first adds this many bonus points.
    // Example: COMBO_BONUS_PER_STEP = 5 -> combo x2 gives +5, x3 gives +10, etc.
    private static final int COMBO_BONUS_PER_STEP = 5;
    // ============================================================

    private final Random random = new Random();
    private int greenStart;
    private int greenWidth;
    private float cursorPosition;
    private boolean movingRight = true;
    private String resultText = "Press SPACE when the marker is in the green zone";
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
        randomizeGreenZone();
    }

    private void randomizeGreenZone() {
        // Random target size between GREEN_MIN_WIDTH and GREEN_MAX_WIDTH.
        greenWidth = GREEN_MIN_WIDTH + random.nextInt(GREEN_MAX_WIDTH - GREEN_MIN_WIDTH + 1);
        greenStart = random.nextInt(BAR_WIDTH - greenWidth + 1);
    }

    @Override
    public void tick() {
        if (finished) {
            return;
        }

        // CURSOR_SPEED controls how fast the WHITE LINE moves across the bar.
        cursorPosition += movingRight ? CURSOR_SPEED : -CURSOR_SPEED;

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

            // Hit grade boundaries inside the green zone:
            // center 20% -> PERFECT, center-ish 55% -> GREAT, remaining green area -> GOOD.
            if (normalizedDistance <= 0.20F) {
                resultText = "PERFECT!";
                resultColor = 0x55FF55;
                perfectCount++;
                baseScore = PERFECT_SCORE;
                accuracyPoints = PERFECT_ACCURACY;
            } else if (normalizedDistance <= 0.55F) {
                resultText = "GREAT!";
                resultColor = 0xAAFF55;
                greatCount++;
                baseScore = GREAT_SCORE;
                accuracyPoints = GREAT_ACCURACY;
            } else {
                resultText = "GOOD!";
                resultColor = 0xFFFF55;
                goodCount++;
                baseScore = GOOD_SCORE;
                accuracyPoints = GOOD_ACCURACY;
            }

            currentCombo++;
            maxCombo = Math.max(maxCombo, currentCombo);
        } else {
            resultText = "MISS!";
            resultColor = 0xFF5555;
            missCount++;
            baseScore = MISS_SCORE;
            accuracyPoints = MISS_ACCURACY;
            currentCombo = 0;
        }

        // FINAL SCORE FOR THIS HIT = base grade score + combo bonus.
        int comboBonus = baseScore > 0 ? Math.max(0, currentCombo - 1) * COMBO_BONUS_PER_STEP : 0;
        score += baseScore + comboBonus;
        weightedAccuracyPoints += accuracyPoints;
        round++;

        if (round >= TOTAL_ROUNDS) {
            finished = true;
            openResultScreen();
        } else {
            randomizeGreenZone();
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
        // Intentionally empty so the world behind the real-time minigame stays sharp.
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
        guiGraphics.drawCenteredString(this.font, "ROUND " + (Math.min(round + 1, TOTAL_ROUNDS)) + " / " + TOTAL_ROUNDS,
                this.width / 2, barY - 56, 0xDDDDDD);
        guiGraphics.drawCenteredString(this.font, resultText, this.width / 2, barY - 38, resultColor);

        guiGraphics.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, 0xFF111111);
        guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFAA2222);
        guiGraphics.fill(barX + greenStart, barY, barX + greenStart + greenWidth, barY + BAR_HEIGHT, 0xFF22AA44);

        // This draws the WHITE MOVING LINE. CURSOR_WIDTH above controls its thickness.
        int cursorX = barX + Math.round(cursorPosition);
        guiGraphics.fill(cursorX, barY - 5, cursorX + CURSOR_WIDTH, barY + BAR_HEIGHT + 5, 0xFFFFFFFF);

        guiGraphics.drawString(this.font, "Score: " + score, barX, barY + 38, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Combo: x" + currentCombo, barX + 115, barY + 38, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Max: x" + maxCombo, barX + 225, barY + 38, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "SPACE = HIT   |   ESC = BACK", this.width / 2, barY + 60, 0xDDDDDD);

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
