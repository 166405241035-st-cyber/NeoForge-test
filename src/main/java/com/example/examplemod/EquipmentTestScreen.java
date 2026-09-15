package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Selection GUI for generating a complete forged item without playing the minigames. */
public class EquipmentTestScreen extends AbstractContainerScreen<EquipmentTestMenu> {
    private HeadBlueprintType blueprint = HeadBlueprintType.SWORD;
    private ForgingMetal metal = ForgingMetal.IRON;
    private final ForgingEffect[] effects = {
            ForgingEffect.ZOMBIE_MINION_CALLING,
            ForgingEffect.CRIPPLING_STRIKE,
            ForgingEffect.SCAVENGER_DIG
    };
    private final EffectTier[] tiers = {EffectTier.I, EffectTier.I, EffectTier.I};
    private Button blueprintButton;
    private Button metalButton;
    private final Button[] effectButtons = new Button[3];
    private final Button[] tierButtons = new Button[3];
    private String status = "Choose all values, then create";

    public EquipmentTestScreen(EquipmentTestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 224;
        inventoryLabelY = 130;
    }

    @Override
    protected void init() {
        super.init();
        blueprintButton = addRenderableWidget(Button.builder(blueprintText(), button -> cycleBlueprint())
                .bounds(leftPos + 10, topPos + 28, 100, 20).build());
        metalButton = addRenderableWidget(Button.builder(metalText(), button -> cycleMetal())
                .bounds(leftPos + 120, topPos + 28, 100, 20).build());

        for (int i = 0; i < 3; i++) {
            final int slot = i;
            effectButtons[i] = addRenderableWidget(Button.builder(effectText(i), button -> cycleEffect(slot))
                    .bounds(leftPos + 10, topPos + 56 + i * 24, 164, 20).build());
            tierButtons[i] = addRenderableWidget(Button.builder(tierText(i), button -> cycleTier(slot))
                    .bounds(leftPos + 180, topPos + 56 + i * 24, 40, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("CREATE ITEM"), button -> createItem())
                .bounds(leftPos + 65, topPos + 108, 100, 20).build());
    }

    private void cycleBlueprint() {
        blueprint = next(HeadBlueprintType.values(), blueprint.ordinal());
        blueprintButton.setMessage(blueprintText());
    }

    private void cycleMetal() {
        metal = next(ForgingMetal.values(), metal.ordinal());
        metalButton.setMessage(metalText());
    }

    private void cycleEffect(int slot) {
        effects[slot] = next(ForgingEffect.values(), effects[slot].ordinal());
        effectButtons[slot].setMessage(effectText(slot));
    }

    private void cycleTier(int slot) {
        tiers[slot] = next(EffectTier.values(), tiers[slot].ordinal());
        tierButtons[slot].setMessage(tierText(slot));
    }

    private void createItem() {
        if (minecraft == null || minecraft.gameMode == null) return;
        int selection = EquipmentTestMenu.encodeSelection(blueprint, metal, effects, tiers);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selection);
        status = "Item created in your inventory";
    }

    private Component blueprintText() { return Component.literal("Type: " + pretty(blueprint.name())); }
    private Component metalText() { return Component.literal("Metal x3: " + metal.displayName()); }
    private Component effectText(int slot) { return Component.literal((slot + 1) + ". " + effects[slot].displayName()); }
    private Component tierText(int slot) { return Component.literal(tiers[slot].name()); }

    private static <T> T next(T[] values, int ordinal) {
        return values[(ordinal + 1) % values.length];
    }

    private static String pretty(String value) {
        String lower = value.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFE0E0E0);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC8C8C8);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) drawSlot(graphics, x + 34 + col * 18, y + 141 + row * 18);
        }
        for (int col = 0; col < 9; col++) drawSlot(graphics, x + 34 + col * 18, y + 199);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 20, y + 20, 0xFF666666);
        graphics.fill(x + 1, y + 1, x + 19, y + 19, 0xFFEEEEEE);
        graphics.fill(x + 3, y + 3, x + 17, y + 17, 0xFF999999);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, "EQUIPMENT EFFECT TESTER", imageWidth / 2, 9, 0xFF333333);
        graphics.drawCenteredString(font, status, imageWidth / 2, 132, 0xFF555555);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
