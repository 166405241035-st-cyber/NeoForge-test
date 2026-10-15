package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.ForgeIngredientResolver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stores persistent Forge Energy for one Monster Forge block. */
public class ForgingBlockEntity extends BlockEntity {
    private int energy;

    public ForgingBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.FORGING_BLOCK_ENTITY.get(), pos, state);
    }

    public int energy() {
        return energy;
    }

    public void setEnergy(int value) {
        int clamped = Math.max(0, Math.min(ForgeIngredientResolver.MAX_FUEL, value));
        if (clamped != energy) {
            energy = clamped;
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = Math.max(0, Math.min(ForgeIngredientResolver.MAX_FUEL, tag.getInt("ForgeEnergy")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ForgeEnergy", energy);
    }
}
