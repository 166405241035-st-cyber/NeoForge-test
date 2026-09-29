package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Pre-Miniganme 1 forging screen.
 *
 * Slot positions intentionally match ForgeMenu exactly. This class only changes
 * presentation so the recipe flow is easier to read.
 */
public class ForgingScreen extends AbstractContainerScreen<ForgeMenu> {
    private Button forgeButton;
    private String status = "Place the required components";

    private static final int IRON_DARK = 0xFF17191D;
    private static final int IRON = 0xFF252A30;
    private static final int IRON_LIGHT = 0xFF353B43;
    private static final int COPPER = 0xFFC97832;
    private static final int COPPER_DARK = 0xFF74401E;
    private static final int TEXT = 0xFFF2E9DC;
    private static final int MUTED = 0xFFAEB5BD;
    private static final int READY = 0xFF7ED957;
    private static final int WARN = 0xFFFFB454;

    public ForgingScreen(ForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 208;
        this.inventoryLabelY = 115;
    }

    @Override
    protected void init() {
        super.init();
        forgeButton = addRenderableWidget(Button.builder(
                Component.literal("START FORGING"), button -> startForge())
                .bounds(leftPos + 48, topPos + 99, 80, 20).build());
    }

    private void startForge() {
        if (!menu.hasValidRecipe()) {
            status = missingRecipeMessage();
            return;
        }

        ForgingBlueprintType blueprint = menu.selectedBlueprint();
        ForgingMetal metal = menu.selectedMetal();
        MonsterMaterial monster = menu.selectedMonster();
        menu.consumeRecipe();

        if (minecraft == null) return;
        if (blueprint == ForgingBlueprintType.CORE) {
            minecraft.setScreen(new CoreTimingBarScreen(metal, monster));
        } else if (blueprint == ForgingBlueprintType.ROD) {
            minecraft.setScreen(new RodTimingBarScreen(metal, monster));
        } else if (blueprint != null && blueprint.headType() != null) {
            minecraft.setScreen(new TimingBarScreen(metal, blueprint.headType(), monster));
        }
    }

    private String missingRecipeMessage() {
        if (menu.stackAt(ForgeMenu.BLUEPRINT_SLOT).isEmpty()) return "Missing Blueprint";
        if (menu.stackAt(ForgeMenu.MONSTER_SLOT).isEmpty()) return "Missing Monster Material";
        for (int slot = ForgeMenu.METAL_START; slot < ForgeMenu.METAL_END; slot++) {
            if (menu.stackAt(slot).isEmpty()) return "Need 5 matching Metals";
        }
        return "Metals must all match";
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Outer forged-iron frame.
        g.fill(x, y, x + imageWidth, y + imageHeight, IRON_DARK);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, COPPER_DARK);
        g.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, IRON);

        // Header and molten accent.
        g.fill(x + 6, y + 6, x + imageWidth - 6, y + 25, 0xFF111317);
        g.fill(x + 6, y + 25, x + imageWidth - 6, y + 27, COPPER);

        // Recipe workbench panel.
        g.fill(x + 7, y + 29, x + imageWidth - 7, y + 95, 0xFF1D2126);
        g.fill(x + 9, y + 31, x + imageWidth - 9, y + 93, 0xFF292E35);

        // Visual flow: Blueprint + Monster + five Metals -> forge.
        g.fill(x + 47, y + 54, x + 57, y + 56, COPPER_DARK);
        g.fill(x + 107, y + 65, x + 120, y + 67, COPPER_DARK);
        g.fill(x + 88, y + 81, x + 90, y + 90, COPPER_DARK);

        drawSlotFrame(g, x + 26, y + 44, 0xFF4B8FD8); // blueprint
        drawSlotFrame(g, x + 85, y + 56, 0xFFA45AD6); // monster
        drawSlotFrame(g, x + 85, y + 29, COPPER);
        drawSlotFrame(g, x + 58, y + 45, COPPER);
        drawSlotFrame(g, x + 112, y + 45, COPPER);
        drawSlotFrame(g, x + 68, y + 75, COPPER);
        drawSlotFrame(g, x + 102, y + 75, COPPER);

        // Fuel slot is kept for compatibility, but visually de-emphasized because
        // fuel cost is not yet part of the actual forging recipe.
        drawSlotFrame(g, x + 144, y + 85, 0xFF666A70);

        // Button cradle / status strip.
        g.fill(x + 7, y + 97, x + imageWidth - 7, y + 121, 0xFF171A1E);
        g.fill(x + 7, y + 121, x + imageWidth - 7, y + 123, COPPER_DARK);

        // Inventory panel.
        g.fill(x + 5, y + 123, x + imageWidth - 5, y + imageHeight - 5, 0xFF20242A);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawInventorySlot(g, x + 7 + col * 18, y + 125 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawInventorySlot(g, x + 7 + col * 18, y + 183);
        }
    }

    private void drawSlotFrame(GuiGraphics g, int x, int y, int accent) {
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
        g.drawCenteredString(font, "MONSTER FORGE", imageWidth / 2, 9, TEXT);
        g.drawString(font, "Blueprint", 8, 34, 0xFF8EC5FF, false);
        g.drawString(font, "Monster", 72, 47, 0xFFD9A3FF, false);
        g.drawString(font, "Metal x5", 73, 18, 0xFFFFC27A, false);

        // Fuel is intentionally marked optional/reserved until its gameplay cost is finalized.
        g.drawString(font, "Fuel*", 140, 74, 0xFF8D939A, false);

        boolean ready = menu.hasValidRecipe();
        String liveStatus = ready ? "READY - press START FORGING" : status;
        g.drawCenteredString(font, liveStatus, imageWidth / 2, 90, ready ? READY : WARN);

        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED, false);
        g.drawString(font, "*reserved", 132, 113, 0xFF70767E, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
