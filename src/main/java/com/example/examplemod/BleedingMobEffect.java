package com.example.examplemod;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Red damage-over-time status used by Spine Spike.
 * Damage cadence mirrors vanilla Poison: faster at higher amplifiers and it will
 * not reduce a living target below 1 HP.
 */
public final class BleedingMobEffect extends MobEffect {
    public BleedingMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0F) {
            entity.hurt(entity.damageSources().magic(), 1.0F);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 25 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }
}
