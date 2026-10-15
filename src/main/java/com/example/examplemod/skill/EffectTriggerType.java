package com.example.examplemod.skill;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

/**
 * Runtime trigger families for forged equipment effects.
 *
 * Keeping trigger identity separate from effect behavior lets the event engine
 * route combat, mining, farming and active-skill events without a giant chain
 * of unrelated checks.
 */
public enum EffectTriggerType {
    PASSIVE,
    ON_HIT,
    ON_KILL,
    ACTIVE,
    MINING,
    FARMING,
    HARVEST,
    LANDING
}
