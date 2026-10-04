package com.example.examplemod.skill;

import com.example.examplemod.forging.result.AnvilAssemblyResult;
import com.example.examplemod.item.ForgedEquipmentItem;
import com.example.examplemod.skill.curse.ForgedCurse;
import com.example.examplemod.skill.curse.ForgedCurseRuntime;

import net.minecraft.world.item.ItemStack;

/** Shared helpers used by server-side forged-effect triggers. */
public final class ForgedEffectRuntime {
    private ForgedEffectRuntime() {}

    public static boolean isForgedEquipment(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ForgedEquipmentItem;
    }

    public static EffectTier tier(ItemStack stack, ForgingEffect wanted) {
        // Power Erasure normally suppresses every main Effect earned from the first
        // minigame. Wither Curse Power is the one intentional secret synergy:
        // its damage bonus survives while its self-Wither drawback is erased.
        if (ForgedCurseRuntime.has(stack, ForgedCurse.POWER_ERASURE)
                && wanted != ForgingEffect.WITHER_CURSE_POWER) {
            return null;
        }

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
