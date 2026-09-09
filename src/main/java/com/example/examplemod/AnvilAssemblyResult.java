package com.example.examplemod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Final effect roll from Head + Core + Rod. Duplicate effects add tiers, capped at III. */
public record AnvilAssemblyResult(
        HeadBlueprintType blueprint,
        ForgingMetal metal,
        MonsterMaterial headMaterial,
        MonsterMaterial coreMaterial,
        MonsterMaterial rodMaterial,
        List<FinalEffect> effects) {
    public record FinalEffect(ForgingEffect effect, EffectTier tier) {}

    public static AnvilAssemblyResult roll(ForgedHeadResult head, ForgedCoreResult core, ForgedRodResult rod, Random random) {
        Map<ForgingEffect, Integer> combined = new LinkedHashMap<>();
        add(combined, head.effect(), head.tier().level());
        add(combined, EffectPool.randomEffect(core.monsterMaterial(), head.blueprint(), random), core.tier().level());
        add(combined, EffectPool.randomEffect(rod.monsterMaterial(), head.blueprint(), random), rod.tier().level());

        List<FinalEffect> finalEffects = new ArrayList<>();
        combined.forEach((effect, level) -> finalEffects.add(new FinalEffect(effect, tierOf(Math.min(3, level)))));
        return new AnvilAssemblyResult(
                head.blueprint(), head.metal(), head.monsterMaterial(), core.monsterMaterial(), rod.monsterMaterial(),
                List.copyOf(finalEffects));
    }

    private static void add(Map<ForgingEffect, Integer> effects, ForgingEffect effect, int tier) {
        effects.merge(effect, tier, (a, b) -> Math.min(3, a + b));
    }

    private static EffectTier tierOf(int level) {
        return level <= 1 ? EffectTier.I : level == 2 ? EffectTier.II : EffectTier.III;
    }
}
