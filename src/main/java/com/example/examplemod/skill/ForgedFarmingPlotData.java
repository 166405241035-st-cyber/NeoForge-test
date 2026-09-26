package com.example.examplemod.skill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Persistent metadata for farming effects that belong to the tilled plot.
 * This lets a crop remember the effect/tier even when the original forged hoe
 * is no longer held.
 */
public final class ForgedFarmingPlotData extends SavedData {
    private static final String NAME = "forged_farming_plots";
    private final Map<Long, EnumMap<ForgingEffect, EffectTier>> plots = new HashMap<>();

    public static ForgedFarmingPlotData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ForgedFarmingPlotData::new, ForgedFarmingPlotData::load, null), NAME);
    }

    public void set(BlockPos pos, ForgingEffect effect, EffectTier tier) {
        plots.computeIfAbsent(pos.asLong(), key -> new EnumMap<>(ForgingEffect.class))
                .put(effect, tier);
        setDirty();
    }

    public EffectTier tier(BlockPos pos, ForgingEffect effect) {
        Map<ForgingEffect, EffectTier> effects = plots.get(pos.asLong());
        return effects == null ? null : effects.get(effect);
    }

    public void remove(BlockPos pos) {
        if (plots.remove(pos.asLong()) != null) setDirty();
    }

    public java.util.List<BlockPos> positionsWith(ForgingEffect effect) {
        java.util.List<BlockPos> result = new java.util.ArrayList<>();
        for (Map.Entry<Long, EnumMap<ForgingEffect, EffectTier>> entry : plots.entrySet()) {
            if (entry.getValue().containsKey(effect)) {
                result.add(BlockPos.of(entry.getKey()));
            }
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, EnumMap<ForgingEffect, EffectTier>> entry : plots.entrySet()) {
            for (Map.Entry<ForgingEffect, EffectTier> effect : entry.getValue().entrySet()) {
                CompoundTag plot = new CompoundTag();
                plot.putLong("Pos", entry.getKey());
                plot.putString("Effect", effect.getKey().name());
                plot.putString("Tier", effect.getValue().name());
                list.add(plot);
            }
        }
        tag.put("Plots", list);
        return tag;
    }

    private static ForgedFarmingPlotData load(CompoundTag tag, HolderLookup.Provider registries) {
        ForgedFarmingPlotData data = new ForgedFarmingPlotData();
        ListTag list = tag.getList("Plots", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag plot = list.getCompound(i);
            try {
                ForgingEffect effect = ForgingEffect.valueOf(plot.getString("Effect"));
                EffectTier tier = EffectTier.valueOf(plot.getString("Tier"));
                data.plots.computeIfAbsent(plot.getLong("Pos"), key -> new EnumMap<>(ForgingEffect.class))
                        .put(effect, tier);
            } catch (IllegalArgumentException ignored) {
                // Ignore data from effects/tier names that no longer exist.
            }
        }
        return data;
    }
}
