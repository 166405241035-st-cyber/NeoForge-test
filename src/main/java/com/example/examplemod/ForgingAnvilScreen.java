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

/** Larger, cleaner pre-Miniganme 2 assembly screen. */
public class ForgingAnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private String status = "Place Head + Core + Rod";

    private static final int IRON_DARK = 0xFF15181C;
    private static final int IRON = 0xFF242A31;
    private static final int IRON_LIGHT = 0xFF353C45;
    private static final int COPPER = 0xFFD07A35;
    private static final int COPPER_DARK = 0xFF6E3A1C;
    private static final int TEXT = 0xFFF1E9DC;
    private static final int MUTED = 0xFFADB4BC;
    private static final int READY = 0xFF76D95B;
    private static final int WARN = 0xFFFFB454;

    public ForgingAnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 214;
        inventoryLabelX = 37;
        inventoryLabelY = 120;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("FINAL FORGE"), b -> startForge())
                .bounds(leftPos + 70, topPos + 94, 90, 20).build());
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
        int x = leftPos, y = topPos;

        g.fill(x, y, x + imageWidth, y + imageHeight, IRON_DARK);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, COPPER_DARK);
        g.fill(x + 5, y + 5, x + imageWidth - 5, y + imageHeight - 5, IRON);

        // Header
        g.fill(x + 9, y + 9, x + imageWidth - 9, y + 31, 0xFF101317);
        g.fill(x + 9, y + 31, x + imageWidth - 9, y + 34, COPPER);

        // Assembly panel
        g.fill(x + 20, y + 41, x + imageWidth - 20, y + 87, 0xFF1A1F25);
        g.fill(x + 23, y + 44, x + imageWidth - 23, y + 84, 0xFF2A3038);

        drawPartCard(g, x + 45, y + 48, 38, 33, 0xFFD58A45);
        drawPartCard(g, x + 100, y + 48, 38, 33, 0xFF68A7D8);
        drawPartCard(g, x + 155, y + 48, 38, 33, 0xFFA477D4);

        // Slots align with AnvilMenu at 54/109/164,58.
        drawPartSlot(g, x + 53, y + 57, 0xFFD58A45);
        drawPartSlot(g, x + 108, y + 57, 0xFF68A7D8);
        drawPartSlot(g, x + 163, y + 57, 0xFFA477D4);

        g.drawCenteredString(font, "+", x + 94, y + 62, 0xFFFFC27A);
        g.drawCenteredString(font, "+", x + 149, y + 62, 0xFFFFC27A);

        // Status / button zone
        g.fill(x + 20, y + 90, x + imageWidth - 20, y + 119, 0xFF15191E);
        g.fill(x + 20, y + 118, x + imageWidth - 20, y + 121, COPPER_DARK);

        // Inventory region
        g.fill(x + 25, y + 124, x + imageWidth - 25, y + imageHeight - 8, 0xFF1D2228);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            drawInventorySlot(g, x + 36 + col * 18, y + 130 + row * 18);
        for (int col = 0; col < 9; col++)
            drawInventorySlot(g, x + 36 + col * 18, y + 188);
    }

    private void drawPartCard(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x, y, x + w, y + h, 0xFF111419);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, accent);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFF252B32);
    }

    private void drawPartSlot(GuiGraphics g, int x, int y, int accent) {
        g.fill(x, y, x + 20, y + 20, 0xFF0C0E11);
        g.fill(x + 1, y + 1, x + 19, y + 19, accent);
        g.fill(x + 3, y + 3, x + 17, y + 17, IRON_LIGHT);
        g.fill(x + 4, y + 4, x + 16, y + 16, 0xFF242A30);
    }

    private void drawInventorySlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 20, y + 20, 0xFF0E1115);
        g.fill(x + 1, y + 1, x + 19, y + 19, IRON_LIGHT);
        g.fill(x + 3, y + 3, x + 17, y + 17, 0xFF272D34);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawCenteredString(font, "FINAL ASSEMBLY", imageWidth / 2, 13, TEXT);
        g.drawCenteredString(font, "Prepare equipment for Minigame 2", imageWidth / 2, 24, MUTED);

        g.drawCenteredString(font, "HEAD", 64, 43, 0xFFFFB56B);
        g.drawCenteredString(font, "CORE", 119, 43, 0xFF8CCBFF);
        g.drawCenteredString(font, "ROD", 174, 43, 0xFFD0A4FF);

        boolean ready = menu.hasValidAssembly();
        String liveStatus = ready ? "READY FOR FINAL FORGE" : status;
        g.drawCenteredString(font, liveStatus, imageWidth / 2, 84, ready ? READY : WARN);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
