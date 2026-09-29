package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class ForgeIngredientResolver {
    public static final int MAX_FUEL = 150;
    private static final int BURN_TICKS_PER_ENERGY = 80;

    private ForgeIngredientResolver() {
    }

    public static ForgingBlueprintType blueprint(ItemStack stack) {
        if (stack.is(ExampleMod.SWORD_HEAD_BLUEPRINT.get())) return ForgingBlueprintType.SWORD_HEAD;
        if (stack.is(ExampleMod.AXE_HEAD_BLUEPRINT.get())) return ForgingBlueprintType.AXE_HEAD;
        if (stack.is(ExampleMod.PICKAXE_HEAD_BLUEPRINT.get())) return ForgingBlueprintType.PICKAXE_HEAD;
        if (stack.is(ExampleMod.SHOVEL_HEAD_BLUEPRINT.get())) return ForgingBlueprintType.SHOVEL_HEAD;
        if (stack.is(ExampleMod.HOE_HEAD_BLUEPRINT.get())) return ForgingBlueprintType.HOE_HEAD;
        if (stack.is(ExampleMod.CORE_BLUEPRINT.get())) return ForgingBlueprintType.CORE;
        if (stack.is(ExampleMod.ROD_BLUEPRINT.get())) return ForgingBlueprintType.ROD;
        return null;
    }

    public static ForgingMetal metal(ItemStack stack) {
        if (stack.is(Items.IRON_INGOT)) return ForgingMetal.IRON;
        if (stack.is(Items.GOLD_INGOT)) return ForgingMetal.GOLD;
        if (stack.is(Items.DIAMOND)) return ForgingMetal.DIAMOND;
        if (stack.is(Items.NETHERITE_INGOT)) return ForgingMetal.NETHERITE;
        return null;
    }

    /** All 13 monster materials used by the forging effect system. */
    public static MonsterMaterial monsterMaterial(ItemStack stack) {
        if (stack.is(Items.ROTTEN_FLESH)) return MonsterMaterial.ROTTEN_FLESH;
        if (stack.is(Items.BONE)) return MonsterMaterial.BONE;
        if (stack.is(Items.STRING)) return MonsterMaterial.STRING;
        if (stack.is(Items.GUNPOWDER)) return MonsterMaterial.GUNPOWDER;
        if (stack.is(Items.SLIME_BALL)) return MonsterMaterial.SLIME;
        if (stack.is(Items.ENDER_PEARL)) return MonsterMaterial.ENDER;
        if (stack.is(Items.BLAZE_ROD)) return MonsterMaterial.BLAZE_ROD;
        if (stack.is(Items.GHAST_TEAR)) return MonsterMaterial.GHAST_TEAR;
        if (stack.is(Items.WITHER_SKELETON_SKULL)) return MonsterMaterial.WITHER;
        if (stack.is(Items.PHANTOM_MEMBRANE)) return MonsterMaterial.PHANTOM;
        if (stack.is(Items.DRAGON_BREATH)) return MonsterMaterial.DRAGON_BREATH;
        if (stack.is(Items.SHULKER_SHELL)) return MonsterMaterial.SHULKER;
        if (stack.is(Items.NETHER_STAR)) return MonsterMaterial.NETHER_STAR;
        return null;
    }

    public static boolean isFuel(ItemStack stack, Level level) {
        return fuelValue(stack, level) > 0;
    }

    /**
     * Converts normal furnace burn time into Forge Energy.
     * Coal = 1600 ticks -> 20 energy. Coal blocks and lava buckets cap at 150.
     * Any vanilla/modded item accepted by the furnace fuel system is supported.
     */
    public static int fuelValue(ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty() || level == null) return 0;
        int burnTicks = stack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
        if (burnTicks <= 0) return 0;
        int energy = (burnTicks + BURN_TICKS_PER_ENERGY - 1) / BURN_TICKS_PER_ENERGY;
        return Math.max(1, Math.min(MAX_FUEL, energy));
    }

    public static int forgeCost(ForgingMetal metal) {
        if (metal == null) return 0;
        return switch (metal.difficulty()) {
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 45;
            case 4 -> 65;
            default -> 90;
        };
    }

    public static boolean sameItem(ItemStack first, ItemStack second) {
        Item a = first.getItem();
        Item b = second.getItem();
        return a == b;
    }
}
