package com.example.examplemod;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimingBarScreen extends Screen {
    private static final int BAR_WIDTH = 300;
    private static final int BAR_HEIGHT = 24;
    private static final int CURSOR_WIDTH = 4;

    private final Random random = new Random();
    private int greenStart;
    private int greenWidth;
    private float cursorPosition;
    private boolean movingRight = true;
    private String resultText = "Press SPACE when the marker is in the green zone";
    private int resultColor = 0xFFFFFF;

    public TimingBarScreen() {
        super(Component.literal("Timing Bar"));
        randomizeGreenZone();
    }

    private void randomizeGreenZone() {
        greenWidth = 45 + random.nextInt(56); // 45-100 pixels
        greenStart = random.nextInt(BAR_WIDTH - greenWidth + 1);
    }

    @Override
    public void tick() {
        float speed = 4.0F;
        cursorPosition += movingRight ? speed : -speed;

        if (cursorPosition >= BAR_WIDTH - CURSOR_WIDTH) {
            cursorPosition = BAR_WIDTH - CURSOR_WIDTH;
            movingRight = false;
        } else if (cursorPosition <= 0) {
            cursorPosition = 0;
            movingRight = true;
        }
    }

    private void attemptHit() {
        float cursorCenter = cursorPosition + CURSOR_WIDTH / 2.0F;
        float greenEnd = greenStart + greenWidth;

        if (cursorCenter >= greenStart && cursorCenter <= greenEnd) {
            float greenCenter = greenStart + greenWidth / 2.0F;
            float distance = Math.abs(cursorCenter - greenCenter);
            float normalizedDistance = distance / (greenWidth / 2.0F);

            if (normalizedDistance <= 0.20F) {
                resultText = "PERFECT!";
                resultColor = 0x55FF55;
            } else if (normalizedDistance <= 0.55F) {
                resultText = "GREAT!";
                resultColor = 0xAAFF55;
            } else {
                resultText = "GOOD!";
                resultColor = 0xFFFF55;
            }
        } else {
            resultText = "MISS!";
            resultColor = 0xFF5555;
        }

        randomizeGreenZone();
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int barX = (this.width - BAR_WIDTH) / 2;
        int barY = this.height / 2 - BAR_HEIGHT / 2;

        // Do not call renderBackground() here. In Minecraft 1.21.1 it applies the
        // menu blur effect, which makes this real-time minigame hard to read.
        // Instead, draw a local translucent panel and keep the world visible.
        int panelPaddingX = 28;
        int panelTop = barY - 78;
        int panelBottom = barY + 72;
        int panelLeft = barX - panelPaddingX;
        int panelRight = barX + BAR_WIDTH + panelPaddingX;

        guiGraphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xB0000000);

        // Simple border around the minigame panel.
        guiGraphics.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0xFFAAAAAA);
        guiGraphics.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0xFFAAAAAA);
        guiGraphics.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0xFFAAAAAA);
        guiGraphics.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0xFFAAAAAA);

        guiGraphics.drawCenteredString(this.font, "TIMING FORGING", this.width / 2, barY - 55, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, resultText, this.width / 2, barY - 32, resultColor);

        // Dark outline behind the timing bar for better contrast.
        guiGraphics.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, 0xFF111111);

        // Red base bar.
        guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFAA2222);

        // Random green target zone.
        guiGraphics.fill(barX + greenStart, barY, barX + greenStart + greenWidth, barY + BAR_HEIGHT, 0xFF22AA44);

        // Moving white marker.
        int cursorX = barX + Math.round(cursorPosition);
        guiGraphics.fill(cursorX, barY - 5, cursorX + CURSOR_WIDTH, barY + BAR_HEIGHT + 5, 0xFFFFFFFF);

        guiGraphics.drawCenteredString(this.font, "SPACE = HIT   |   ESC = BACK", this.width / 2, barY + 45, 0xDDDDDD);

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
