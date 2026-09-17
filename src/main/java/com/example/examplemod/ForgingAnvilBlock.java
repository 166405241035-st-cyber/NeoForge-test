package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Separate block for Head + Core + Rod assembly and the rhythm minigame. */
public class ForgingAnvilBlock extends Block {
    public ForgingAnvilBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            SimpleContainer inventory = new SimpleContainer(AnvilMenu.ANVIL_SLOT_COUNT);
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, openingPlayer) -> new AnvilMenu(containerId, playerInventory, inventory),
                    Component.literal("Forging Anvil")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
