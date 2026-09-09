package com.example.examplemod;

import java.util.Random;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RhythmForgingScreen extends Screen {
    private static final int TOTAL_ROUNDS = 10;
    private static final int TARGET_RADIUS = 22;
    private static final int APPROACH_START_RADIUS = 70;
    private static final float APPROACH_SPEED = 2.2F;
    private static final float SPEED_PER_DIFFICULTY_STEP = 0.45F;
    private static final float WINDOW_SHRINK_PER_DIFFICULTY_STEP = 2.0F;
    private static final int PERFECT_SCORE = 100;
    private static final int GREAT_SCORE = 75;
    private static final int GOOD_SCORE = 50;
    private static final int COMBO_BONUS_PER_STEP = 5;
    private static final float PERFECT_WINDOW = 5.0F;
    private static final float GREAT_WINDOW = 12.0F;
    private static final float GOOD_WINDOW = 22.0F;
    private static final int SCREEN_OVERLAY_COLOR = 0x88000000;

    private final Random random = new Random();
    private final ForgingMetal metal;
    private int targetX, targetY;
    private Direction direction;
    private float approachRadius;
    private int round, score, currentCombo, maxCombo, perfectCount, greatCount, goodCount, missCount, weightedAccuracyPoints;
    private String resultText = "Press the shown W/A/S/D key at the right time";
    private int resultColor = 0xFFFFFF;
    private boolean finished;

    public RhythmForgingScreen() { this(ForgingMetal.IRON); }
    public RhythmForgingScreen(ForgingMetal metal) { super(Component.literal("Rhythm Forging")); this.metal = metal; spawnTarget(); }

    private float currentApproachSpeed() { return APPROACH_SPEED + (metal.difficulty() - 1) * SPEED_PER_DIFFICULTY_STEP; }
    private float currentPerfectWindow() { return Math.max(2.0F, PERFECT_WINDOW - (metal.difficulty() - 1) * 0.75F); }
    private float currentGreatWindow() { return Math.max(currentPerfectWindow() + 1.0F, GREAT_WINDOW - (metal.difficulty() - 1) * WINDOW_SHRINK_PER_DIFFICULTY_STEP); }
    private float currentGoodWindow() { return Math.max(currentGreatWindow() + 1.0F, GOOD_WINDOW - (metal.difficulty() - 1) * WINDOW_SHRINK_PER_DIFFICULTY_STEP); }

    private void spawnTarget() {
        int marginX = 90, topMargin = 80, bottomMargin = 80;
        int usableWidth = Math.max(1, width - marginX * 2);
        int usableHeight = Math.max(1, height - topMargin - bottomMargin);
        targetX = marginX + random.nextInt(usableWidth);
        targetY = topMargin + random.nextInt(usableHeight);
        direction = Direction.values()[random.nextInt(Direction.values().length)];
        approachRadius = APPROACH_START_RADIUS;
    }

    @Override protected void init() { spawnTarget(); }
    @Override public void tick() { if (!finished) { approachRadius -= currentApproachSpeed(); if (approachRadius < TARGET_RADIUS - currentGoodWindow()) registerMiss("TOO LATE!"); } }

    private void attemptHit(int keyCode) {
        if (finished) return;
        Direction pressedDirection = Direction.fromKey(keyCode);
        if (pressedDirection == null) return;
        if (pressedDirection != direction) { registerMiss("WRONG KEY!"); return; }
        float timingDistance = Math.abs(approachRadius - TARGET_RADIUS);
        if (timingDistance <= currentPerfectWindow()) registerHit("PERFECT!", 0xFF66FF66, PERFECT_SCORE, 100, HitGrade.PERFECT);
        else if (timingDistance <= currentGreatWindow()) registerHit("GREAT!", 0xFF22CC55, GREAT_SCORE, 75, HitGrade.GREAT);
        else if (timingDistance <= currentGoodWindow()) registerHit("GOOD!", 0xFFFFCC33, GOOD_SCORE, 50, HitGrade.GOOD);
        else registerMiss("TOO EARLY!");
    }

    private void registerHit(String text, int color, int baseScore, int accuracyPoints, HitGrade grade) {
        resultText = text + " +" + baseScore; resultColor = color;
        if (grade == HitGrade.PERFECT) perfectCount++;
        if (grade == HitGrade.GREAT) greatCount++;
        if (grade == HitGrade.GOOD) goodCount++;
        currentCombo++; maxCombo = Math.max(maxCombo, currentCombo);
        score += baseScore + Math.max(0, currentCombo - 1) * COMBO_BONUS_PER_STEP;
        weightedAccuracyPoints += accuracyPoints; finishRound();
    }

    private void registerMiss(String reason) { resultText = reason + "  MISS!"; resultColor = 0xFFFF5555; missCount++; currentCombo = 0; finishRound(); }
    private void finishRound() { round++; if (round >= TOTAL_ROUNDS) { finished = true; openResultScreen(); } else spawnTarget(); }
    private void openResultScreen() { if (minecraft == null) return; double accuracy = weightedAccuracyPoints / (double) TOTAL_ROUNDS; minecraft.setScreen(new ForgingResultScreen(new ForgingResult(score, accuracy, maxCombo, perfectCount, greatCount, goodCount, missCount))); }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { if (Direction.fromKey(keyCode) != null) { attemptHit(keyCode); return true; } return super.keyPressed(keyCode, scanCode, modifiers); }
    @Override public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, SCREEN_OVERLAY_COLOR);
        guiGraphics.fill(12, 12, 260, 80, 0xB0000000);
        guiGraphics.drawString(font, "RHYTHM FORGING - " + metal.displayName(), 22, 22, 0xFFFFFF);
        guiGraphics.drawString(font, "Difficulty: " + metal.difficulty() + "/3", 22, 36, 0xDDDDDD);
        guiGraphics.drawString(font, "Round: " + Math.min(round + 1, TOTAL_ROUNDS) + "/" + TOTAL_ROUNDS, 22, 50, 0xDDDDDD);
        guiGraphics.drawString(font, "Score: " + score + "   Combo: x" + currentCombo, 22, 64, 0xDDDDDD);
        guiGraphics.drawCenteredString(font, resultText, width / 2, 22, resultColor);
        drawTarget(guiGraphics);
        guiGraphics.drawCenteredString(font, "W=UP   A=LEFT   S=DOWN   D=RIGHT   |   ESC=BACK", width / 2, height - 24, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawTarget(GuiGraphics guiGraphics) {
        int r = TARGET_RADIUS;
        guiGraphics.fill(targetX-r,targetY-r,targetX+r,targetY+r,0xCC222222);
        guiGraphics.fill(targetX-r+3,targetY-r+3,targetX+r-3,targetY+r-3,0xCCEEEEEE);
        guiGraphics.fill(targetX-r+6,targetY-r+6,targetX+r-6,targetY+r-6,0xCC333333);
        int ar=Math.max(1,Math.round(approachRadius)), thickness=2, color=0xFFFFFFFF;
        guiGraphics.fill(targetX-ar,targetY-ar,targetX+ar,targetY-ar+thickness,color);
        guiGraphics.fill(targetX-ar,targetY+ar-thickness,targetX+ar,targetY+ar,color);
        guiGraphics.fill(targetX-ar,targetY-ar,targetX-ar+thickness,targetY+ar,color);
        guiGraphics.fill(targetX+ar-thickness,targetY-ar,targetX+ar,targetY+ar,color);
        guiGraphics.drawCenteredString(font,direction.symbol+"  "+direction.keyName,targetX,targetY-4,0xFFFFFF);
    }

    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(null); }
    @Override public boolean isPauseScreen() { return false; }

    private enum HitGrade { PERFECT, GREAT, GOOD }
    private enum Direction {
        UP(GLFW.GLFW_KEY_W,"W","^"), LEFT(GLFW.GLFW_KEY_A,"A","<"), DOWN(GLFW.GLFW_KEY_S,"S","v"), RIGHT(GLFW.GLFW_KEY_D,"D",">");
        private final int keyCode; private final String keyName; private final String symbol;
        Direction(int keyCode,String keyName,String symbol){this.keyCode=keyCode;this.keyName=keyName;this.symbol=symbol;}
        private static Direction fromKey(int keyCode){for(Direction direction:values())if(direction.keyCode==keyCode)return direction;return null;}
    }
}
