package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Pre-Miniganme 2 assembly screen.
 *
 * Head/Core/Rod and player inventory slot coordinates are unchanged from AnvilMenu.
 */
public class ForgingAnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private String status = "Place Head + Core + Rod";

    private static final int IRON_DARK = 0xFF17191D;
    private static final int IRON = 0xFF252A30;
    private static final int IRON_LIGHT = 0xFF353B43;
    private static final int COPPER = 0xFFC97832;
    private static final int COPPER_DARK = 0xFF74401E;
    private static final int TEXT = 0xFFF2E9DC;
    private static final int MUTED = 0xFFAEB5BD;
    private static final int READY = 0xFF7ED957;
    private static final int WARN = 0xFFFFB454;

    public ForgingAnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 178;
        inventoryLabelY = 85;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("FINAL FORGE"), b -> startForge())
                .bounds(leftPos + 53, topPos + 66, 70, 20).build());
    }

    private void startForge() {
        if (!menu.hasValidAssembly()) {
            status = missingAssemblyMessage();
            return;
        }

        ForgedHeadResult head = menu.headResult();
        ForgedCoreResult core = menu.coreResult();
        ForgedRodResult rod = menu.rodResult();
        if (head == null || core == null || rod == null) return;

        AnvilAssemblyResult assembly = AnvilAssemblyResult.roll(head, core, rod, new Random());
        if (minecraft == null || minecraft.gameMode == null) return;

        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        minecraft.setScreen(new AnvilRhythmForgingScreen(head.metal(), assembly));
    }

    private String missingAssemblyMessage() {
        if (menu.stackAt(AnvilMenu.HEAD_SLOT).isEmpty()) return "Missing Head";
        if (menu.stackAt(AnvilMenu.CORE_SLOT).isEmpty()) return "Missing Core";
        if (menu.stackAt(AnvilMenu.ROD_SLOT).isEmpty()) return "Missing Rod";
        return "Use forged Head + Core + Rod";
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Outer forged-iron frame.
        g.fill(x, y, x + imageWidth, y + imageHeight, IRON_DARK);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, COPPER_DARK);
        g.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, IRON);

        // Header.
        g.fill(x + 6, y + 6, x + imageWidth - 6, y + 25, 0xFF111317);
        g.fill(x + 6, y + 25, x + imageWidth - 6, y + 27, COPPER);

        // Assembly workbench panel.
        g.fill(x + 8, y + 29, x + imageWidth - 8, y + 63, 0xFF1D2126);
        g.fill(x + 10, y + 31, x + imageWidth - 10, y + 61, 0xFF292E35);

        drawPartSlot(g, x + 49, y + 41, 0xFFD58A45); // Head
        drawPartSlot(g, x + 87, y + 41, 0xFF68A7D8); // Core
        drawPartSlot(g, x + 125, y + 41, 0xFFA477D4); // Rod

        // Assembly connectors, purely visual.
        g.fill(x + 69, y + 50, x + 83, y + 52, COPPER_DARK);
        g.fill(x + 107, y + 50, x + 121, y + 52, COPPER_DARK);
        g.drawCenteredString(font, "+", x + 78, y + 46, 0xFFFFC27A);
        g.drawCenteredString(font, "+", x + 116, y + 46, 0xFFFFC27A);

        // Button / readiness strip.
        g.fill(x + 8, y + 64, x + imageWidth - 8, y + 89, 0xFF171A1E);
        g.fill(x + 8, y + 89, x + imageWidth - 8, y + 91, COPPER_DARK);

        // Inventory panel.
        g.fill(x + 5, y + 91, x + imageWidth - 5, y + imageHeight - 5, 0xFF20242A);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawInventorySlot(g, x + 7 + col * 18, y + 95 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawInventorySlot(g, x + 7 + col * 18, y + 153);
        }
    }

    private void drawPartSlot(GuiGraphics g, int x, int y, int accent) {
        g.fill(x, y, x + 20, y + 20, 0xFF0E1013);
        g.fill(x + 1, y + 1, x + 19, y + 19, accent);
        g.fill(x + 3, y + 3, x + 17, y + 17, 0xFF3A4048);
        g.fill(x + 4, y + 4, x + 16, y + 16, 0xFF252A30);
    }

    private void drawInventorySlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 20, y + 20, 0xFF101216);
        g.fill(x + 1, y + 1, x + 19, y + 19, IRON_LIGHT);
        g.fill(x + 3, y + 3, x + 17, y + 17, 0xFF272C32);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawCenteredString(font, "FINAL ASSEMBLY", imageWidth / 2, 9, TEXT);

        g.drawCenteredString(font, "HEAD", 59, 30, 0xFFFFB56B);
        g.drawCenteredString(font, "CORE", 97, 30, 0xFF8CCBFF);
        g.drawCenteredString(font, "ROD", 135, 30, 0xFFD0A4FF);

        boolean ready = menu.hasValidAssembly();
        String liveStatus = ready ? "READY FOR FINAL FORGE" : status;
        g.drawCenteredString(font, liveStatus, imageWidth / 2, 57, ready ? READY : WARN);

        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
