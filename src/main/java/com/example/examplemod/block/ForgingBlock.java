package com.example.examplemod.block;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ForgingBlock extends Block implements EntityBlock {

    public ForgingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForgingBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            SimpleContainer inventory = new SimpleContainer(ForgeMenu.FORGE_SLOT_COUNT);
            ForgingBlockEntity forge = (ForgingBlockEntity) level.getBlockEntity(pos);
            if (forge == null) return InteractionResult.PASS;
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, openingPlayer) -> new ForgeMenu(
                            containerId,
                            playerInventory,
                            inventory,
                            forge.energy(),
                            forge::setEnergy),
                    Component.literal("Forge")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
