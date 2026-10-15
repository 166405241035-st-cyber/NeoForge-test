package com.example.examplemod.skill.blessing;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ForgedBlessingRuntime {
    private static final String KEY = "forgedBlessing";
    private ForgedBlessingRuntime() {}

    public static ForgedBlessing get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String name = data.copyTag().getString(KEY);
        if (name.isEmpty()) return null;
        try { return ForgedBlessing.valueOf(name); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public static boolean has(ItemStack stack, ForgedBlessing blessing) { return get(stack) == blessing; }

    /** All forged durability costs should pass here so Durability Guard also protects skill costs. */
    public static boolean damage(ItemStack stack, int amount) {
        if (stack == null || stack.isEmpty() || amount <= 0) return false;
        if (has(stack, ForgedBlessing.DURABILITY_GUARD)
                && java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.25D) {
            return false;
        }
        stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + amount));
        return true;
    }

    public static void set(ItemStack stack, ForgedBlessing blessing) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove("forgedCurse");
            if (blessing == null) tag.remove(KEY);
            else tag.putString(KEY, blessing.name());
        });
    }
}
