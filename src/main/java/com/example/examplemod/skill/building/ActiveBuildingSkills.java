package com.example.examplemod.skill.building;

import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;
import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;
import static com.example.examplemod.skill.mining.ActiveMiningSkills.lookedBlock;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Line, wall and temporary support placement skills. */
public final class ActiveBuildingSkills {
    private ActiveBuildingSkills() {}

    public static void blockLevitation(Player player, ItemStack tool, EffectTier tier) {
        BlockPos pos = lookedBlock(player, 6.0D);
        if (pos == null) return;
        var state = player.level().getBlockState(pos);
        if (state.isAir() || state.is(ExampleMod.INVISIBLE_SUPPORT_BLOCK.get())) return;
        if (state.getDestroySpeed(player.level(), pos) < 0.0F) return;

        int cost = switch (tier) { case I -> 5; case II -> 3; case III -> 1; };
        if (tool.isDamageableItem() && tool.getDamageValue() + cost >= tool.getMaxDamage()) return;

        if (!player.level().destroyBlock(pos, true, player)) return;
        // The invisible support is permanent. It only disappears when a player
        // deliberately breaks it; Tier affects durability cost only.
        player.level().setBlockAndUpdate(pos, ExampleMod.INVISIBLE_SUPPORT_BLOCK.get().defaultBlockState());
        ForgedSkillSounds.play(player, ForgingEffect.BLOCK_LEVITATION);
        damageEquipment(player, cost);
    }

    public static void lineBuilder(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = 40L; // fixed 2 sec
        if (!ready(tool, player, "LineBuilder", cooldown)) return;

        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) || offhand.isEmpty()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Line Builder: ถือบล็อกไว้มือซ้าย"), true);
            return;
        }

        // Tier controls the maximum line length.
        int maxBlocks = switch (tier) {
            case I -> 5;
            case II -> 9;
            case III -> 14;
        };
        if (DoubleTriggerRuntime.rollActive(player, tool)) maxBlocks *= 2;

        Vec3 look = player.getLookAngle();
        boolean vertical = Math.abs(look.y) >= 0.65D;
        net.minecraft.core.Direction forward = player.getDirection();
        net.minecraft.core.Direction buildDir = vertical
                ? (look.y >= 0.0D ? net.minecraft.core.Direction.UP : net.minecraft.core.Direction.DOWN)
                : forward;

        BlockPos feet = player.blockPosition();

        // Vertical columns are offset TWO blocks forward. One block forward can still
        // intersect the player's bounding box near a block edge and Minecraft rejects/blocks placement.
        // Keep both modes consistent: the first block always starts two blocks
        // in front of the player's feet.
        // Both modes begin one block in front of the player's feet.
        // Looking up builds upward. Looking down builds downward only when there is
        // empty space below the front anchor (for example at an edge or over a gap).
        BlockPos start = feet.relative(forward, 1);
        if (vertical) {
            if (look.y < 0.0D) {
                BlockPos belowStart = start.below();
                buildDir = player.level().getBlockState(belowStart).canBeReplaced()
                        ? net.minecraft.core.Direction.DOWN
                        : net.minecraft.core.Direction.UP;
                if (buildDir == net.minecraft.core.Direction.DOWN) {
                    start = belowStart;
                }
            } else {
                buildDir = net.minecraft.core.Direction.UP;
            }
        }

        int placed = 0;
        for (int i = 0; i < maxBlocks && (!offhand.isEmpty() || player.getAbilities().instabuild); i++) {
            BlockPos pos = start.relative(buildDir, i);
            if (!player.level().getBlockState(pos).canBeReplaced()) break;

            player.level().setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
            if (!player.getAbilities().instabuild) offhand.shrink(1);
            placed++;

            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                Vec3 point = Vec3.atCenterOf(pos);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        point.x, point.y, point.z, 4, 0.22D, 0.22D, 0.22D, 0.04D);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                        point.x, point.y, point.z, 2, 0.18D, 0.12D, 0.18D, 0.015D);
            }
        }

        if (placed == 0) return;
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Line Builder: " + (vertical ? "VERTICAL" : "HORIZONTAL")
                        + " (" + placed + "/" + maxBlocks + ")"), true);
        startCooldown(tool, player, "LineBuilder", cooldown);

        // One activation costs one durability; Tier strength is represented by line length.
        damageEquipment(player, 1);
    }

    public static void earthyWallRise(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = 300L; // fixed 15 sec for every tier
        if (!ready(tool, player, "EarthyWallRise", cooldown)) return;

        net.minecraft.world.level.block.Block wallBlock = switch (tier) {
            case I -> Blocks.STONE;
            case II -> Blocks.DEEPSLATE;
            case III -> Blocks.OBSIDIAN;
        };

        net.minecraft.core.Direction forward = player.getDirection();
        net.minecraft.core.Direction side = forward.getClockWise();
        BlockPos feet = player.blockPosition();
        BlockPos center = feet.relative(forward, 1);

        // Raise a 3x3x1 wall from the ground in front of the player.
        // If the player is standing above a gap, search downward a short distance
        // so the wall still rises from the nearest ground surface.
        int baseY = center.getY();
        for (int d = 0; d <= 4; d++) {
            BlockPos probe = center.below(d);
            if (!player.level().getBlockState(probe.below()).canBeReplaced()) {
                baseY = probe.getY();
                break;
            }
        }

        int placed = 0;
        int thickness = DoubleTriggerRuntime.rollActive(player, tool) ? 2 : 1;
        // Normal cast is 3x3x1. Double Trigger adds one free layer behind it.
        for (int depth = 0; depth < thickness; depth++) {
            BlockPos depthCenter = center.relative(forward, depth);
            for (int y = 0; y < 3; y++) {
                for (int i = -1; i <= 1; i++) {
                BlockPos sidePos = depthCenter.relative(side, i);
                BlockPos pos = new BlockPos(sidePos.getX(), baseY + y, sidePos.getZ());
                // Break any block occupying the wall area first, then raise the wall.
                // destroyBlock(..., true, player) makes the original block drop normally.
                if (!player.level().getBlockState(pos).isAir()) {
                    player.level().destroyBlock(pos, true, player);
                }
                player.level().setBlockAndUpdate(pos, wallBlock.defaultBlockState());
                placed++;

                if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    Vec3 point = Vec3.atCenterOf(pos);
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                            point.x, point.y, point.z, 5, 0.30D, 0.15D, 0.30D, 0.03D);
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                            point.x, point.y + 0.15D, point.z, 2, 0.18D, 0.22D, 0.18D, 0.02D);
                }
            }
        }
        }

        if (placed == 0) return;
        startCooldown(tool, player, "EarthyWallRise", cooldown);
        damageEquipment(player, 4);
    }

    public static void toggleSkyBridge(Player player, EffectTier tier) {
        boolean active = !player.getPersistentData().getBoolean("ForgedSkyBridgeActive");
        player.getPersistentData().putBoolean("ForgedSkyBridgeActive", active);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Sky Bridge Walk: " + (active ? "ON" : "OFF")), true);
        ForgedSkillSounds.play(player, ForgingEffect.SKY_BRIDGE_WALK);
    }
}
