package com.example.examplemod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

public class ForgingBlock extends Block {
    /*
     * Temporary per-block fuel storage until the Forge receives its own BlockEntity.
     * This keeps fuel when the GUI is closed/reopened during the current world session.
     */
    private static final Map<String, Integer> FUEL_BY_FORGE = new ConcurrentHashMap<>();

    public ForgingBlock(Properties properties) {
        super(properties);
    }

    private static String fuelKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.asLong();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            SimpleContainer inventory = new SimpleContainer(ForgeMenu.FORGE_SLOT_COUNT);
            String key = fuelKey(level, pos);
            int storedFuel = FUEL_BY_FORGE.getOrDefault(key, 0);
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, openingPlayer) -> new ForgeMenu(
                            containerId,
                            playerInventory,
                            inventory,
                            storedFuel,
                            value -> FUEL_BY_FORGE.put(key, value)),
                    Component.literal("Forge")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
