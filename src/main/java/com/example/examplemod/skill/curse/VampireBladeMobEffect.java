package com.example.examplemod.skill.curse;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible potion-style status while a Vampire Blade curse is carried in inventory. */
public final class VampireBladeMobEffect extends MobEffect {
    public VampireBladeMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
