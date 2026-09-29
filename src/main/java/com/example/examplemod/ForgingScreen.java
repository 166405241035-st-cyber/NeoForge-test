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

/** Larger, cleaner pre-Miniganme 1 forging screen. */
public class ForgingScreen extends AbstractContainerScreen<ForgeMenu> {
    private Button forgeButton;
    private String status = "Place the required components";

    private static final int IRON_DARK = 0xFF15181C;
    private static final int IRON = 0xFF242A31;
    private static final int IRON_LIGHT = 0xFF353C45;
    private static final int COPPER = 0xFFD07A35;
    private static final int COPPER_DARK = 0xFF6E3A1C;
    private static final int TEXT = 0xFFF1E9DC;
    private static final int MUTED = 0xFFADB4BC;
    private static final int READY = 0xFF76D95B;
    private static final int WARN = 0xFFFFB454;

    public ForgingScreen(ForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 250;
        this.imageHeight = 236;
        this.inventoryLabelX = 37;
        this.inventoryLabelY = 140;
    }

    @Override
    protected void init() {
        super.init();
        forgeButton = addRenderableWidget(Button.builder(
                Component.literal("START FORGING"), button -> startForge())
                .bounds(leftPos + 74, topPos + 116, 102, 20).build());
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
        if (blueprint == ForgingBlueprintType.CORE) minecraft.setScreen(new CoreTimingBarScreen(metal, monster));
        else if (blueprint == ForgingBlueprintType.ROD) minecraft.setScreen(new RodTimingBarScreen(metal, monster));
        else if (blueprint != null && blueprint.headType() != null) minecraft.setScreen(new TimingBarScreen(metal, blueprint.headType(), monster));
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
        int x = leftPos, y = topPos;

        g.fill(x, y, x + imageWidth, y + imageHeight, IRON_DARK);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, COPPER_DARK);
        g.fill(x + 5, y + 5, x + imageWidth - 5, y + imageHeight - 5, IRON);

        // Header
        g.fill(x + 9, y + 9, x + imageWidth - 9, y + 31, 0xFF101317);
        g.fill(x + 9, y + 31, x + imageWidth - 9, y + 34, COPPER);

        // Main recipe panel
        g.fill(x + 12, y + 39, x + imageWidth - 12, y + 109, 0xFF1B2026);
        g.fill(x + 15, y + 42, x + imageWidth - 15, y + 106, 0xFF2A3038);

        // Group cards
        drawGroup(g, x + 19, y + 49, 36, 50, 0xFF386FA8);
        drawGroup(g, x + 65, y + 49, 36, 50, 0xFF7F4BAA);
        drawGroup(g, x + 105, y + 42, 72, 64, COPPER_DARK);

        // Actual slot frames line up with ForgeMenu.
        drawSlotFrame(g, x + 25, y + 57, 0xFF4B8FD8);  // Blueprint slot at 26,58
        drawSlotFrame(g, x + 71, y + 57, 0xFFA45AD6);  // Monster slot at 72,58
        drawSlotFrame(g, x + 131, y + 39, COPPER);
        drawSlotFrame(g, x + 111, y + 63, COPPER);
        drawSlotFrame(g, x + 151, y + 63, COPPER);
        drawSlotFrame(g, x + 119, y + 87, COPPER);
        drawSlotFrame(g, x + 143, y + 87, COPPER);

        // Reserved fuel panel, visually separated from recipe.
        g.fill(x + 186, y + 43, x + 225, y + 99, 0xFF181C21);
        g.fill(x + 188, y + 45, x + 223, y + 97, 0xFF252B32);
        drawSlotFrame(g, x + 197, y + 57, 0xFF656A70);

        // Start/status panel
        g.fill(x + 12, y + 112, x + imageWidth - 12, y + 140, 0xFF15191E);
        g.fill(x + 12, y + 139, x + imageWidth - 12, y + 142, COPPER_DARK);

        // Inventory region
        g.fill(x + 25, y + 145, x + imageWidth - 25, y + imageHeight - 8, 0xFF1D2228);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            drawInventorySlot(g, x + 36 + col * 18, y + 150 + row * 18);
        for (int col = 0; col < 9; col++)
            drawInventorySlot(g, x + 36 + col * 18, y + 210);
    }

    private void drawGroup(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x, y, x + w, y + h, 0xFF111419);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, accent);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFF252B32);
    }

    private void drawSlotFrame(GuiGraphics g, int x, int y, int accent) {
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
        g.drawCenteredString(font, "MONSTER FORGE", imageWidth / 2, 13, TEXT);
        g.drawCenteredString(font, "Prepare components for Minigame 1", imageWidth / 2, 24, MUTED);

        g.drawCenteredString(font, "BLUEPRINT", 37, 44, 0xFF9BCFFF);
        g.drawCenteredString(font, "MONSTER", 83, 44, 0xFFE1B5FF);
        g.drawCenteredString(font, "METAL x5", 141, 36, 0xFFFFC27A);
        g.drawCenteredString(font, "FUEL", 205, 48, 0xFF8C929A);
        g.drawCenteredString(font, "reserved", 205, 84, 0xFF686E75);

        boolean ready = menu.hasValidRecipe();
        String liveStatus = ready ? "READY TO FORGE" : status;
        g.drawCenteredString(font, liveStatus, imageWidth / 2, 105, ready ? READY : WARN);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
