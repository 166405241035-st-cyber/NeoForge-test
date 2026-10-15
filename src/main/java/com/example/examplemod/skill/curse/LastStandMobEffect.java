package com.example.examplemod.skill.curse;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible potion-style indicator while Last Stand is armed and waiting for the next attack. */
public final class LastStandMobEffect extends MobEffect {
    public LastStandMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
