package com.example.examplemod.skill.passive;

import com.example.examplemod.skill.EffectTier;
import net.minecraft.world.entity.player.Player;

/** Shared passive trigger timers and Tier value lookup. */
public final class PassiveEffectSupport {
    private PassiveEffectSupport() {}

    public static boolean canUseTimedTrigger(Player player, String key, long cooldownTicks) {
        String dataKey = "ForgingCooldown_" + key;
        long now = player.level().getGameTime();
        long readyAt = player.getPersistentData().getLong(dataKey);
        if (now < readyAt) return false;
        player.getPersistentData().putLong(dataKey, now + cooldownTicks);
        return true;
    }

    public static float tierValue(EffectTier tier, float[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }

    public static double tierValue(EffectTier tier, double[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }

    public static int tierValue(EffectTier tier, int[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }
}
