package com.example.examplemod.skill.blessing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Runtime behavior for Blessings awarded by the Rhythm Forging minigame. */
@EventBusSubscriber(modid = "examplemod")
public final class ForgedBlessingEvents {
    private static final float POWER_STRIKE_MULTIPLIER = 1.20F;
    private static final double LIFE_STEAL_CHANCE = 0.20D;
    private static final float LIFE_STEAL_HEAL = 2.0F;
    private static final double DIVINE_EXECUTION_CHANCE = 0.05D;
    private static final int VEIN_BREAKER_LIMIT = 8;
    private static final ThreadLocal<Boolean> VEIN_BREAKING = ThreadLocal.withInitial(() -> false);

    private ForgedBlessingEvents() {}

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        ItemStack weapon = player.getMainHandItem();

        if (ForgedBlessingRuntime.has(weapon, ForgedBlessing.POWER_STRIKE)) {
            event.setAmount(event.getAmount() * POWER_STRIKE_MULTIPLIER);
        }

        if (ForgedBlessingRuntime.has(weapon, ForgedBlessing.LIFE_STEAL)
                && player.getRandom().nextDouble() < LIFE_STEAL_CHANCE) {
            player.heal(LIFE_STEAL_HEAL);
        }

        if (ForgedBlessingRuntime.has(weapon, ForgedBlessing.DIVINE_EXECUTION)
                && event.getEntity() instanceof net.minecraft.world.entity.monster.Monster
                && player.getRandom().nextDouble() < DIVINE_EXECUTION_CHANCE) {
            event.setAmount(Math.max(event.getAmount(), event.getEntity().getHealth() + event.getEntity().getAbsorptionAmount()));
        }
    }

    /** Hunter's Fortune duplicates the actual monster drops, preserving modded loot/NBT. */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        if (!ForgedBlessingRuntime.has(player.getMainHandItem(), ForgedBlessing.HUNTERS_FORTUNE)) return;

        java.util.List<ItemEntity> copies = new java.util.ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack copy = drop.getItem().copy();
            if (!copy.isEmpty()) {
                copies.add(new ItemEntity(drop.level(), drop.getX(), drop.getY(), drop.getZ(), copy));
            }
        }
        event.getDrops().addAll(copies);
    }

    /** Experience Boost increases mob XP by 60%. */
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null || player.level().isClientSide()) return;
        if (!ForgedBlessingRuntime.has(player.getMainHandItem(), ForgedBlessing.EXPERIENCE_BOOST)) return;
        event.setDroppedExperience(Math.max(0, Math.round(event.getDroppedExperience() * 1.60F)));
    }

    /** Mining Haste is a permanent +50% break-speed bonus while using the blessed tool. */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (ForgedBlessingRuntime.has(event.getEntity().getMainHandItem(), ForgedBlessing.MINING_HASTE)) {
            event.setNewSpeed(event.getNewSpeed() * 1.50F);
        }
    }

    /** Break up to 8 connected blocks of the same ore type. */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (VEIN_BREAKING.get()) return;
        if (!(event.getPlayer() instanceof Player player) || player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();
        if (!ForgedBlessingRuntime.has(tool, ForgedBlessing.VEIN_BREAKER)) return;

        BlockState origin = event.getState();
        if (!origin.is(BlockTags.MINEABLE_WITH_PICKAXE) || !isOre(origin)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(event.getPos());
        queue.add(event.getPos());

        VEIN_BREAKING.set(true);
        try {
            int broken = 0;
            while (!queue.isEmpty() && broken < VEIN_BREAKER_LIMIT) {
                BlockPos current = queue.removeFirst();
                for (BlockPos next : new BlockPos[]{current.above(), current.below(), current.north(), current.south(), current.east(), current.west()}) {
                    if (!visited.add(next)) continue;
                    BlockState state = level.getBlockState(next);
                    if (!state.is(origin.getBlock())) continue;
                    queue.addLast(next);
                    if (level.destroyBlock(next, true, player)) broken++;
                    if (broken >= VEIN_BREAKER_LIMIT) break;
                }
            }
        } finally {
            VEIN_BREAKING.set(false);
        }
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES);
    }
}
