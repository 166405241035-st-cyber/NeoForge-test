package com.example.examplemod;

/** Temporary head-forging result used by the prototype UI. */
public record ForgedHeadResult(
        ForgingMetal metal,
        HeadBlueprintType blueprint,
        MonsterMaterial monsterMaterial,
        ForgingEffect effect,
        EffectTier tier) {
}
