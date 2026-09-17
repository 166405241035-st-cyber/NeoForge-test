package com.example.examplemod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ForgeIngredientResolver {
    public static final int FUEL_PER_COAL = 8;
    public static final int MAX_FUEL = 100;

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

    public static boolean isFuel(ItemStack stack) {
        return fuelValue(stack) > 0;
    }

    /** Coal and charcoal both add 8 units to the forge fuel tank. */
    public static int fuelValue(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) ? FUEL_PER_COAL : 0;
    }

    public static boolean sameItem(ItemStack first, ItemStack second) {
        Item a = first.getItem();
        Item b = second.getItem();
        return a == b;
    }
}
