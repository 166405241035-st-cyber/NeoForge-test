package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative menu for the prototype equipment effect tester. */
public class EquipmentTestMenu extends AbstractContainerMenu {
    private static final int EFFECT_COUNT = ForgingEffect.values().length;
    private static final int EFFECT_SLOTS = 3;

    public EquipmentTestMenu(int containerId, Inventory playerInventory) {
        super(ExampleMod.EQUIPMENT_TEST_MENU.get(), containerId);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 35 + col * 18, 142 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 35 + col * 18, 200));
        }
    }

    public static int encodeSelection(HeadBlueprintType blueprint, ForgingMetal metal,
            ForgingEffect[] effects, EffectTier[] tiers) {
        int value = blueprint.ordinal();
        value = value * ForgingMetal.values().length + metal.ordinal();
        for (int i = 0; i < EFFECT_SLOTS; i++) value = value * EFFECT_COUNT + effects[i].ordinal();
        for (int i = 0; i < EFFECT_SLOTS; i++) value = value * EffectTier.values().length + tiers[i].ordinal();
        return value;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || player.level().isClientSide()) return false;

        try {
            int value = id;
            EffectTier[] tiers = new EffectTier[EFFECT_SLOTS];
            ForgingEffect[] effects = new ForgingEffect[EFFECT_SLOTS];

            for (int i = EFFECT_SLOTS - 1; i >= 0; i--) {
                tiers[i] = EffectTier.values()[value % EffectTier.values().length];
                value /= EffectTier.values().length;
            }
            for (int i = EFFECT_SLOTS - 1; i >= 0; i--) {
                effects[i] = ForgingEffect.values()[value % EFFECT_COUNT];
                value /= EFFECT_COUNT;
            }
            ForgingMetal metal = ForgingMetal.values()[value % ForgingMetal.values().length];
            value /= ForgingMetal.values().length;
            HeadBlueprintType blueprint = HeadBlueprintType.values()[value];

            List<AnvilAssemblyResult.FinalEffect> finalEffects = List.of(
                    new AnvilAssemblyResult.FinalEffect(effects[0], tiers[0]),
                    new AnvilAssemblyResult.FinalEffect(effects[1], tiers[1]),
                    new AnvilAssemblyResult.FinalEffect(effects[2], tiers[2]));
            AnvilAssemblyResult assembly = new AnvilAssemblyResult(
                    blueprint, metal, metal, metal,
                    effects[0].material(), effects[1].material(), effects[2].material(),
                    finalEffects);
            ItemStack result = ExampleMod.FORGED_EQUIPMENT_ITEM.get().createStack(assembly);
            if (!player.getInventory().add(result)) player.drop(result, false);
            player.displayClientMessage(Component.literal("Created test " + blueprint.name().toLowerCase()
                    + " with 3 selected effects."), true);
            return true;
        } catch (RuntimeException invalidSelection) {
            ExampleMod.LOGGER.warn("Rejected invalid equipment tester selection id {}", id, invalidSelection);
            return false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
