package com.example.examplemod;

import java.util.EnumSet;
import java.util.Set;

/**
 * Head blueprint tags.
 * The blueprint does not choose a named effect; it only controls which effect
 * categories are allowed when the monster-material pool is filtered.
 */
public enum HeadBlueprintType {
    SWORD(EnumSet.of(EffectCategory.ATTACK)),
    AXE(EnumSet.of(EffectCategory.ATTACK, EffectCategory.MINING)),
    PICKAXE(EnumSet.of(EffectCategory.MINING)),
    SHOVEL(EnumSet.of(EffectCategory.MINING)),
    HOE(EnumSet.of(EffectCategory.MINING, EffectCategory.FARMING));

    private final Set<EffectCategory> allowedCategories;

    HeadBlueprintType(Set<EffectCategory> allowedCategories) {
        this.allowedCategories = Set.copyOf(allowedCategories);
    }

    public Set<EffectCategory> allowedCategories() {
        return allowedCategories;
    }

    public boolean allows(EffectCategory category) {
        return allowedCategories.contains(category);
    }
}
