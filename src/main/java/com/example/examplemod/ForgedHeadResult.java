package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

/** Temporary head-forging result used by the prototype UI. */
public record ForgedHeadResult(
        ForgingMetal metal,
        HeadBlueprintType blueprint,
        MonsterMaterial monsterMaterial,
        ForgingEffect effect,
        EffectTier tier) {
}
