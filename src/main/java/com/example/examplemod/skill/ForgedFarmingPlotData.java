package com.example.examplemod.skill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistent metadata for farming effects that belong to the tilled plot.
 * This lets a crop remember the effect/tier even when the original forged hoe
 * is no longer held.
 */
public final class ForgedFarmingPlotData extends SavedData {
    private static final String NAME = "forged_farming_plots";
    private final Map<Long, PlotEffect> plots = new HashMap<>();

    public record PlotEffect(ForgingEffect effect, EffectTier tier) {}

    public static ForgedFarmingPlotData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ForgedFarmingPlotData::new, ForgedFarmingPlotData::load, null), NAME);
    }

    public void set(BlockPos pos, ForgingEffect effect, EffectTier tier) {
        plots.put(pos.asLong(), new PlotEffect(effect, tier));
        setDirty();
    }

    public EffectTier tier(BlockPos pos, ForgingEffect effect) {
        PlotEffect plot = plots.get(pos.asLong());
        return plot != null && plot.effect() == effect ? plot.tier() : null;
    }

    public void remove(BlockPos pos) {
        if (plots.remove(pos.asLong()) != null) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, PlotEffect> entry : plots.entrySet()) {
            CompoundTag plot = new CompoundTag();
            plot.putLong("Pos", entry.getKey());
            plot.putString("Effect", entry.getValue().effect().name());
            plot.putString("Tier", entry.getValue().tier().name());
            list.add(plot);
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
                data.plots.put(plot.getLong("Pos"), new PlotEffect(
                        ForgingEffect.valueOf(plot.getString("Effect")),
                        EffectTier.valueOf(plot.getString("Tier"))));
            } catch (IllegalArgumentException ignored) {
                // Ignore data from effects/tier names that no longer exist.
            }
        }
        return data;
    }
}
