package com.example.examplemod.skill.curse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Reads/writes the single Curse attached to forged equipment. */
public final class ForgedCurseRuntime {
    private static final String CURSE_KEY = "forgedCurse";

    private ForgedCurseRuntime() {}

    public static ForgedCurse get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String name = data.copyTag().getString(CURSE_KEY);
        if (name.isEmpty()) return null;
        try {
            return ForgedCurse.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static boolean has(ItemStack stack, ForgedCurse curse) {
        return get(stack) == curse;
    }

    /** Used later by the Rhythm Forging reward resolver. */
    public static void set(ItemStack stack, ForgedCurse curse) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (curse == null) tag.remove(CURSE_KEY);
            else tag.putString(CURSE_KEY, curse.name());
        });
    }
}
