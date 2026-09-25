package com.example.examplemod;

import java.util.Random;

/**
 * Converts forging minigame accuracy into the agreed random Effect tier.
 *
 * 0-59%   -> I 100%
 * 60-89%  -> I 80%, II 20%
 * 90-100% -> I 60%, II 30%, III 10%
 */
public final class EffectTierRoller {
    private EffectTierRoller() {
    }

    public static EffectTier roll(double accuracy, Random random) {
        double clampedAccuracy = Math.max(0.0D, Math.min(100.0D, accuracy));
        int roll = random.nextInt(100); // 0-99

        if (clampedAccuracy < 60.0D) {
            return EffectTier.I;
        }

        if (clampedAccuracy < 90.0D) {
            return roll < 80 ? EffectTier.I : EffectTier.II;
        }

        if (roll < 60) {
            return EffectTier.I;
        }
        if (roll < 90) {
            return EffectTier.II;
        }
        return EffectTier.III;
    }
}
