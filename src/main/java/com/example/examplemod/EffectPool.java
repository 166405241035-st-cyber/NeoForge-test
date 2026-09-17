package com.example.examplemod;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Filters a monster material's effects through the selected head blueprint tags. */
public final class EffectPool {
    private EffectPool() {}

    public static List<ForgingEffect> getAllowedEffects(MonsterMaterial material, HeadBlueprintType blueprint) {
        return Arrays.stream(ForgingEffect.values())
                .filter(effect -> effect.material() == material)
                .filter(effect -> blueprint.allows(effect.category()))
                .toList();
    }

    public static ForgingEffect randomEffect(MonsterMaterial material, HeadBlueprintType blueprint, Random random) {
        List<ForgingEffect> allowed = getAllowedEffects(material, blueprint);
        if (allowed.isEmpty()) {
            throw new IllegalStateException("No effect available for " + material + " with " + blueprint);
        }
        return allowed.get(random.nextInt(allowed.size()));
    }
}
