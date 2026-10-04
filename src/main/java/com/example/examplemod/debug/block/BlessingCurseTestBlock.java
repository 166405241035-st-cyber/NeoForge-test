package com.example.examplemod.debug.block;

import com.example.examplemod.debug.menu.BlessingCurseTestMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlessingCurseTestBlock extends Block {
    public BlessingCurseTestBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, openingPlayer) -> new BlessingCurseTestMenu(id, inventory),
                    Component.literal("Blessing & Curse Test Forge")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
