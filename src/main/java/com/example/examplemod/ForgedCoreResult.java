package com.example.examplemod;

/** Result data for a forged core. The named effect is intentionally not decided here. */
public record ForgedCoreResult(
        ForgingMetal metal,
        MonsterMaterial monsterMaterial,
        EffectTier tier) {
}
