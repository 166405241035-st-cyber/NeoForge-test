package com.example.examplemod;

import net.minecraft.world.item.ItemStack;

/** Shared helpers used by server-side forged-effect triggers. */
public final class ForgedEffectRuntime {
    private ForgedEffectRuntime() {}

    public static boolean isForgedEquipment(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ForgedEquipmentItem;
    }

    public static EffectTier tier(ItemStack stack, ForgingEffect wanted) {
        int count = ForgedEquipmentItem.effectCount(stack);
        for (int i = 0; i < count; i++) {
            AnvilAssemblyResult.FinalEffect effect = ForgedEquipmentItem.readEffect(stack, i);
            if (effect != null && effect.effect() == wanted) return effect.tier();
        }
        return null;
    }

    public static boolean has(ItemStack stack, ForgingEffect wanted) {
        return tier(stack, wanted) != null;
    }

    public static double chance(EffectTier tier, double tierI, double tierII, double tierIII) {
        if (tier == null) return 0.0D;
        return switch (tier) {
            case I -> tierI;
            case II -> tierII;
            case III -> tierIII;
        };
    }
}
