package com.example.examplemod;

/** Result data for a forged Rod. Like Core, the named effect stays hidden until assembly. */
public record ForgedRodResult(ForgingMetal metal, MonsterMaterial monsterMaterial, EffectTier tier) {
}
