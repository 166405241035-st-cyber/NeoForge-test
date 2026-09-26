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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Creative prototype utility for producing forged equipment with chosen effects. */
public class EquipmentTestBlock extends Block {
    public EquipmentTestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            MenuProvider provider = new SimpleMenuProvider(
                    (containerId, inventory, openingPlayer) -> new EquipmentTestMenu(containerId, inventory),
                    Component.literal("Equipment Effect Tester"));
            player.openMenu(provider);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
