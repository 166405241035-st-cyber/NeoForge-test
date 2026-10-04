package com.example.examplemod.forging.result;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.forging.blueprint.HeadBlueprintType;
import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.forging.material.MonsterMaterial;
import com.example.examplemod.item.ForgedEquipmentItem;
import com.example.examplemod.skill.EffectPool;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgingEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.item.ItemStack;

/** Final effect roll from Head + Core + Rod. Duplicate effects add tiers, capped at III. */
public record AnvilAssemblyResult(
        HeadBlueprintType blueprint,
        ForgingMetal headMetal,
        ForgingMetal coreMetal,
        ForgingMetal rodMetal,
        MonsterMaterial headMaterial,
        MonsterMaterial coreMaterial,
        MonsterMaterial rodMaterial,
        List<FinalEffect> effects) {
    public record FinalEffect(ForgingEffect effect, EffectTier tier) {}

    /** Reconstructs the server's rolled assembly for the client's rhythm/result screens. */
    public static AnvilAssemblyResult fromStack(ItemStack stack) {
        if (!stack.is(ExampleMod.FORGED_EQUIPMENT_ITEM.get())) return null;
        HeadBlueprintType blueprint = ForgedEquipmentItem.readBlueprint(stack);
        ForgingMetal head = ForgedEquipmentItem.readHeadMetal(stack);
        ForgingMetal core = ForgedEquipmentItem.readCoreMetal(stack);
        ForgingMetal rod = ForgedEquipmentItem.readRodMetal(stack);
        MonsterMaterial headMaterial = ForgedEquipmentItem.readHeadMaterial(stack);
        MonsterMaterial coreMaterial = ForgedEquipmentItem.readCoreMaterial(stack);
        MonsterMaterial rodMaterial = ForgedEquipmentItem.readRodMaterial(stack);
        int count = ForgedEquipmentItem.effectCount(stack);
        if (blueprint == null || head == null || core == null || rod == null
                || headMaterial == null || coreMaterial == null || rodMaterial == null
                || count < 1 || count > 3) return null;
        List<FinalEffect> effects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FinalEffect effect = ForgedEquipmentItem.readEffect(stack, i);
            if (effect == null) return null;
            effects.add(effect);
        }
        return new AnvilAssemblyResult(blueprint, head, core, rod,
                headMaterial, coreMaterial, rodMaterial, List.copyOf(effects));
    }

    public static AnvilAssemblyResult roll(ForgedHeadResult head, ForgedCoreResult core, ForgedRodResult rod, Random random) {
        Map<ForgingEffect, Integer> combined = new LinkedHashMap<>();
        add(combined, head.effect(), head.tier().level());
        add(combined, EffectPool.randomEffect(core.monsterMaterial(), head.blueprint(), random), core.tier().level());
        add(combined, EffectPool.randomEffect(rod.monsterMaterial(), head.blueprint(), random), rod.tier().level());

        List<FinalEffect> finalEffects = new ArrayList<>();
        combined.forEach((effect, level) -> finalEffects.add(new FinalEffect(effect, tierOf(Math.min(3, level)))));
        return new AnvilAssemblyResult(
                head.blueprint(), head.metal(), core.metal(), rod.metal(),
                head.monsterMaterial(), core.monsterMaterial(), rod.monsterMaterial(),
                List.copyOf(finalEffects));
    }

    /** All three parts contribute equally; keep fractional difficulty for mixed materials. */
    public float rhythmDifficulty() {
        return (headMetal.difficulty() + coreMetal.difficulty() + rodMetal.difficulty()) / 3.0F;
    }

    private static void add(Map<ForgingEffect, Integer> effects, ForgingEffect effect, int tier) {
        effects.merge(effect, tier, (a, b) -> Math.min(3, a + b));
    }

    private static EffectTier tierOf(int level) {
        return level <= 1 ? EffectTier.I : level == 2 ? EffectTier.II : EffectTier.III;
    }
}
