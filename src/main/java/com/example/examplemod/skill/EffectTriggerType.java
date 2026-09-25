package com.example.examplemod;

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
