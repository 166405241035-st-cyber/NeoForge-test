package com.example.examplemod.forging.result;

import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.forging.material.MonsterMaterial;
import com.example.examplemod.skill.EffectTier;

/** Result data for a forged core. The named effect is intentionally not decided here. */
public record ForgedCoreResult(
        ForgingMetal metal,
        MonsterMaterial monsterMaterial,
        EffectTier tier) {
}
