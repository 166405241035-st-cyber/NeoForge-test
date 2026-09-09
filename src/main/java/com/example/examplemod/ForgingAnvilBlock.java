package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Separate block reserved for Head + Core + Rod assembly and Minigame 2. */
public class ForgingAnvilBlock extends Block {
    public ForgingAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) ForgingClientHooks.openAnvilScreen();
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
