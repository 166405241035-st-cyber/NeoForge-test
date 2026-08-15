package com.example.examplemod;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RhythmForgingScreen extends Screen {
    // ==================== EASY TUNING VALUES ====================
    private static final int TOTAL_ROUNDS = 10;
    private static final int TARGET_RADIUS = 22;
    private static final int APPROACH_START_RADIUS = 70;
    private static final float APPROACH_SPEED = 2.2F;

    private static final int PERFECT_SCORE = 100;
    private static final int GREAT_SCORE = 75;
    private static final int GOOD_SCORE = 50;
    private static final int COMBO_BONUS_PER_STEP = 5;

    private static final float PERFECT_WINDOW = 5.0F;
    private static final float GREAT_WINDOW = 12.0F;
    private static final float GOOD_WINDOW = 22.0F;

    // Full-screen translucent black overlay.
    // 0x00 = invisible, 0xFF = fully opaque. 0x88 is medium transparency.
    private static final int SCREEN_OVERLAY_COLOR = 0x88000000;
    // ============================================================

    private final Random random = new Random();

    private int targetX;
    private int targetY;
    private Direction direction;
    private float approachRadius;

    private int round;
    private int score;
    private int currentCombo;
    private int maxCombo;
    private int perfectCount;
    private int greatCount;
    private int goodCount;
    private int missCount;
    private int weightedAccuracyPoints;
    private String resultText = "Press the shown W/A/S/D key at the right time";
    private int resultColor = 0xFFFFFF;
    private boolean finished;

    public RhythmForgingScreen() {
        super(Component.literal("Rhythm Forging"));
        spawnTarget();
    }

    private void spawnTarget() {
        int marginX = 90;
        int topMargin = 80;
        int bottomMargin = 80;

        int usableWidth = Math.max(1, this.width - marginX * 2);
        int usableHeight = Math.max(1, this.height - topMargin - bottomMargin);

        targetX = marginX + random.nextInt(usableWidth);
        targetY = topMargin + random.nextInt(usableHeight);
        direction = Direction.values()[random.nextInt(Direction.values().length)];
        approachRadius = APPROACH_START_RADIUS;
    }

    @Override
    protected void init() {
        spawnTarget();
    }

    @Override
    public void tick() {
        if (finished) {
            return;
        }

        approachRadius -= APPROACH_SPEED;

        if (approachRadius < TARGET_RADIUS - GOOD_WINDOW) {
            registerMiss("TOO LATE!");
        }
    }

    private void attemptHit(int keyCode) {
        if (finished) {
            return;
        }

        Direction pressedDirection = Direction.fromKey(keyCode);
        if (pressedDirection == null) {
            return;
        }

        if (pressedDirection != direction) {
            registerMiss("WRONG KEY!");
            return;
        }

        float timingDistance = Math.abs(approachRadius - TARGET_RADIUS);

        if (timingDistance <= PERFECT_WINDOW) {
            registerHit("PERFECT!", 0xFF66FF66, PERFECT_SCORE, 100, HitGrade.PERFECT);
        } else if (timingDistance <= GREAT_WINDOW) {
            registerHit("GREAT!", 0xFF22CC55, GREAT_SCORE, 75, HitGrade.GREAT);
        } else if (timingDistance <= GOOD_WINDOW) {
            registerHit("GOOD!", 0xFFFFCC33, GOOD_SCORE, 50, HitGrade.GOOD);
        } else {
            registerMiss("TOO EARLY!");
        }
    }

    private void registerHit(String text, int color, int baseScore, int accuracyPoints, HitGrade grade) {
        resultText = text + " +" + baseScore;
        resultColor = color;

        if (grade == HitGrade.PERFECT) perfectCount++;
        if (grade == HitGrade.GREAT) greatCount++;
        if (grade == HitGrade.GOOD) goodCount++;

        currentCombo++;
        maxCombo = Math.max(maxCombo, currentCombo);
        int comboBonus = Math.max(0, currentCombo - 1) * COMBO_BONUS_PER_STEP;
        score += baseScore + comboBonus;
        weightedAccuracyPoints += accuracyPoints;
        finishRound();
    }

    private void registerMiss(String reason) {
        resultText = reason + "  MISS!";
        resultColor = 0xFFFF5555;
        missCount++;
        currentCombo = 0;
        finishRound();
    }

    private void finishRound() {
        round++;
        if (round >= TOTAL_ROUNDS) {
            finished = true;
            openResultScreen();
        } else {
            spawnTarget();
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
        if (Direction.fromKey(keyCode) != null) {
            attemptHit(keyCode);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally empty so Minecraft does not apply its menu blur.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dark translucent layer over the whole minigame.
        // The world remains visible underneath, like Minigame 1.
        guiGraphics.fill(0, 0, this.width, this.height, SCREEN_OVERLAY_COLOR);

        // Slightly darker HUD box for readability.
        guiGraphics.fill(12, 12, 220, 68, 0xB0000000);
        guiGraphics.drawString(this.font, "RHYTHM FORGING", 22, 22, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Round: " + Math.min(round + 1, TOTAL_ROUNDS) + "/" + TOTAL_ROUNDS, 22, 36, 0xDDDDDD);
        guiGraphics.drawString(this.font, "Score: " + score + "   Combo: x" + currentCombo, 22, 50, 0xDDDDDD);

        guiGraphics.drawCenteredString(this.font, resultText, this.width / 2, 22, resultColor);

        drawTarget(guiGraphics);

        guiGraphics.drawCenteredString(this.font, "W=UP   A=LEFT   S=DOWN   D=RIGHT   |   ESC=BACK",
                this.width / 2, this.height - 24, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawTarget(GuiGraphics guiGraphics) {
        int r = TARGET_RADIUS;
        guiGraphics.fill(targetX - r, targetY - r, targetX + r, targetY + r, 0xCC222222);
        guiGraphics.fill(targetX - r + 3, targetY - r + 3, targetX + r - 3, targetY + r - 3, 0xCCEEEEEE);
        guiGraphics.fill(targetX - r + 6, targetY - r + 6, targetX + r - 6, targetY + r - 6, 0xCC333333);

        int ar = Math.max(1, Math.round(approachRadius));
        int thickness = 2;
        int color = 0xFFFFFFFF;
        guiGraphics.fill(targetX - ar, targetY - ar, targetX + ar, targetY - ar + thickness, color);
        guiGraphics.fill(targetX - ar, targetY + ar - thickness, targetX + ar, targetY + ar, color);
        guiGraphics.fill(targetX - ar, targetY - ar, targetX - ar + thickness, targetY + ar, color);
        guiGraphics.fill(targetX + ar - thickness, targetY - ar, targetX + ar, targetY + ar, color);

        guiGraphics.drawCenteredString(this.font, direction.symbol + "  " + direction.keyName, targetX, targetY - 4, 0xFFFFFF);
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

    private enum HitGrade {
        PERFECT, GREAT, GOOD
    }

    private enum Direction {
        UP(GLFW.GLFW_KEY_W, "W", "^"),
        LEFT(GLFW.GLFW_KEY_A, "A", "<"),
        DOWN(GLFW.GLFW_KEY_S, "S", "v"),
        RIGHT(GLFW.GLFW_KEY_D, "D", ">");

        private final int keyCode;
        private final String keyName;
        private final String symbol;

        Direction(int keyCode, String keyName, String symbol) {
            this.keyCode = keyCode;
            this.keyName = keyName;
            this.symbol = symbol;
        }

        private static Direction fromKey(int keyCode) {
            for (Direction direction : values()) {
                if (direction.keyCode == keyCode) {
                    return direction;
                }
            }
            return null;
        }
    }
}
