package com.example.examplemod.skill.curse;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible status icon for the Berserker curse while its low-health state is active. */
public final class BerserkerMobEffect extends MobEffect {
    public BerserkerMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
