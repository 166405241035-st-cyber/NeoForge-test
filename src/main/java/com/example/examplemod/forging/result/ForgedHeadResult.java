package com.example.examplemod.forging.result;

import com.example.examplemod.forging.blueprint.HeadBlueprintType;
import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.forging.material.MonsterMaterial;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgingEffect;

/** Temporary head-forging result used by the prototype UI. */
public record ForgedHeadResult(
        ForgingMetal metal,
        HeadBlueprintType blueprint,
        MonsterMaterial monsterMaterial,
        ForgingEffect effect,
        EffectTier tier) {
}
