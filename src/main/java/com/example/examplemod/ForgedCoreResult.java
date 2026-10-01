package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

/** Result data for a forged core. The named effect is intentionally not decided here. */
public record ForgedCoreResult(
        ForgingMetal metal,
        MonsterMaterial monsterMaterial,
        EffectTier tier) {
}
