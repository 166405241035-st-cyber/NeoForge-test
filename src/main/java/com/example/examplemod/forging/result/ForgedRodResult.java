package com.example.examplemod.forging.result;

import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.forging.material.MonsterMaterial;
import com.example.examplemod.skill.EffectTier;

/** Result data for a forged Rod. Like Core, the named effect stays hidden until assembly. */
public record ForgedRodResult(ForgingMetal metal, MonsterMaterial monsterMaterial, EffectTier tier) {
}
