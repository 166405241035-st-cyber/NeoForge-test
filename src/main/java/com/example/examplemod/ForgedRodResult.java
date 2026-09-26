package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

/** Result data for a forged Rod. Like Core, the named effect stays hidden until assembly. */
public record ForgedRodResult(ForgingMetal metal, MonsterMaterial monsterMaterial, EffectTier tier) {
}
