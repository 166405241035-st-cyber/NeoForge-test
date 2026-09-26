package com.example.examplemod.skill;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The single source of truth for Moisture Retain.
 * Every farmland position tilled by a forged hoe with the effect is stored in world data.
 * A server-level tick restores those plots to moisture 7 independently of players/tools.
 */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgedMoistureData extends SavedData {
    private static final String NAME = "forged_moisture_retain";
    private final Set<Long> plots = new HashSet<>();

    public static ForgedMoistureData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ForgedMoistureData::new, ForgedMoistureData::load, null), NAME);
    }

    public void add(BlockPos pos) {
        if (plots.add(pos.asLong())) setDirty();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getGameTime() % 10L != 0L) return;
        get(level).refresh(level);
    }

    private void refresh(ServerLevel level) {
        Iterator<Long> it = plots.iterator();
        while (it.hasNext()) {
            BlockPos pos = BlockPos.of(it.next());

            // Do not touch unloaded chunks just to maintain farmland.
            if (!level.hasChunkAt(pos)) continue;

            var state = level.getBlockState(pos);
            if (!state.is(Blocks.FARMLAND)) {
                it.remove();
                setDirty();
                continue;
            }

            if (state.getValue(FarmBlock.MOISTURE) != 7) {
                level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, 7), 3);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (long pos : plots) list.add(LongTag.valueOf(pos));
        tag.put("Plots", list);
        return tag;
    }

    private static ForgedMoistureData load(CompoundTag tag, HolderLookup.Provider registries) {
        ForgedMoistureData data = new ForgedMoistureData();
        ListTag list = tag.getList("Plots", Tag.TAG_LONG);
        for (int i = 0; i < list.size(); i++) {
            data.plots.add(((LongTag) list.get(i)).getAsLong());
        }
        return data;
    }
}
