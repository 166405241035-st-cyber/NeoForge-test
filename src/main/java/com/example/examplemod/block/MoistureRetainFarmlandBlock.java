package com.example.examplemod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Farmland created by Moisture Retain.
 * It inherits vanilla farmland planting/growth behavior, but its moisture
 * never ticks down because the random-tick drying logic is intentionally disabled.
 */
public class MoistureRetainFarmlandBlock extends FarmBlock {
    public static final MapCodec<FarmBlock> CODEC =
            simpleCodec(properties -> new MoistureRetainFarmlandBlock(properties));

    public MoistureRetainFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MOISTURE, 7));
    }

    @Override
    public MapCodec<FarmBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(net.minecraft.world.level.block.state.BlockState state) {
        return false;
    }
}
