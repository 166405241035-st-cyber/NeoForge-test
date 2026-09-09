package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Real Forge container screen with clickable Minecraft inventory slots. */
public class ForgingScreen extends AbstractContainerScreen<ForgeMenu> {
    private Button forgeButton;
    private String status = "Insert ingredients";

    public ForgingScreen(ForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 208;
        this.inventoryLabelY = 115;
    }

    @Override
    protected void init() {
        super.init();
        forgeButton = addRenderableWidget(Button.builder(Component.literal("FORGE"), button -> startForge())
                .bounds(leftPos + 55, topPos + 99, 66, 20).build());
    }

    private void startForge() {
        if (!menu.hasValidRecipe()) {
            status = "Need blueprint + monster + 5 matching metals + coal";
            return;
        }

        ForgingBlueprintType blueprint = menu.selectedBlueprint();
        ForgingMetal metal = menu.selectedMetal();
        MonsterMaterial monster = menu.selectedMonster();
        menu.consumeRecipe();

        if (minecraft == null) return;
        if (blueprint == ForgingBlueprintType.CORE) {
            minecraft.setScreen(new CoreTimingBarScreen(metal, monster));
        } else if (blueprint != null && blueprint.headType() != null) {
            minecraft.setScreen(new TimingBarScreen(metal, blueprint.headType(), monster));
        } else {
            status = "Rod forging will be connected after its rules are locked";
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xEE4B4B4B);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFF5A5A5A);

        drawSlotFrame(graphics, x + 26, y + 44, 0xFF3978C5);
        drawSlotFrame(graphics, x + 85, y + 56, 0xFFE0B82F);
        drawSlotFrame(graphics, x + 85, y + 29, 0xFF9A9A9A);
        drawSlotFrame(graphics, x + 58, y + 45, 0xFF9A9A9A);
        drawSlotFrame(graphics, x + 112, y + 45, 0xFF9A9A9A);
        drawSlotFrame(graphics, x + 68, y + 75, 0xFF9A9A9A);
        drawSlotFrame(graphics, x + 102, y + 75, 0xFF9A9A9A);
        drawSlotFrame(graphics, x + 144, y + 85, 0xFF777777);

        graphics.fill(x + 148, y + 24, x + 160, y + 78, 0xFF202020);
        boolean hasFuel = !menu.stackAt(ForgeMenu.FUEL_SLOT).isEmpty();
        if (hasFuel) graphics.fill(x + 151, y + 50, x + 157, y + 75, 0xFFFF8A22);

        graphics.fill(x + 85, y + 82, x + 91, y + 94, 0xFF181818);
        graphics.fill(x + 81, y + 91, x + 95, y + 95, 0xFF181818);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) drawSlotFrame(graphics, x + 7 + col * 18, y + 125 + row * 18, 0xFF777777);
        }
        for (int col = 0; col < 9; col++) drawSlotFrame(graphics, x + 7 + col * 18, y + 183, 0xFF777777);
    }

    private void drawSlotFrame(GuiGraphics graphics, int x, int y, int accent) {
        graphics.fill(x, y, x + 20, y + 20, 0xFF202020);
        graphics.fill(x + 1, y + 1, x + 19, y + 19, accent);
        graphics.fill(x + 3, y + 3, x + 17, y + 17, 0xFF555555);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "FORGE", 8, 7, 0xFFFFFF, false);
        graphics.drawString(font, "Blueprint", 8, 70, 0xDDEEFF, false);
        graphics.drawString(font, "FUEL", 143, 12, 0xFFFFFF, false);
        graphics.drawString(font, status, 8, 104, menu.hasValidRecipe() ? 0x77FF77 : 0xFFCC66, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
