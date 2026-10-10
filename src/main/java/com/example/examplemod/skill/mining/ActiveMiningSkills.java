package com.example.examplemod.skill.mining;

import static com.example.examplemod.skill.active.ActiveSkillState.cooldownKey;
import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;
import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedSkillConfig;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Active mining volumes, laser mining and mined-drop collection. */
public final class ActiveMiningSkills {
    private ActiveMiningSkills() {}

    public static void ultimateLaser(Player player, ItemStack tool, EffectTier tier) {
        // Fixed design values: I 300s, II 210s, III 120s.
        // Keep these authoritative here so old generated config files cannot retain stale cooldowns.
        long cooldown = switch (tier) {
            case I -> 6000L;
            case II -> 4200L;
            case III -> 2400L;
        };
        if (!ready(tool, player, "UltimateLaserBreaker", cooldown)) return;
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        boolean doubled = DoubleTriggerRuntime.rollActive(player, tool);
        int laserDepth = doubled ? 32 : 16;

        // Active R laser: normally 3 x 3 x 16; Double Trigger extends the same cast to 3 x 3 x 32.
        // Bedrock is deliberately protected from the laser; the normal mining ability
        // of the Ultimate tool is handled separately.
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        Direction depthDirection;
        double ax = Math.abs(look.x), ay = Math.abs(look.y), az = Math.abs(look.z);
        if (ay >= ax && ay >= az) depthDirection = look.y >= 0.0D ? Direction.UP : Direction.DOWN;
        else if (ax >= az) depthDirection = look.x >= 0.0D ? Direction.EAST : Direction.WEST;
        else depthDirection = look.z >= 0.0D ? Direction.SOUTH : Direction.NORTH;

        Direction right = depthDirection.getAxis() == Direction.Axis.Y ? Direction.EAST
                : depthDirection.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        Direction up = depthDirection.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;

        BlockPos origin = BlockPos.containing(start.add(look.scale(1.0D)));
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        for (int depth = 0; depth < laserDepth; depth++) {
            BlockPos center = origin.relative(depthDirection, depth);
            for (int width = -1; width <= 1; width++) {
                for (int height = -1; height <= 1; height++) {
                    targets.add(center.relative(right, width).relative(up, height));
                }
            }
        }

        // Render the laser as a visible 3x3 beam instead of a single thin line.
        // The particle cross-section follows the same right/up axes as the 3x3 mining volume.
        Vec3 rightVec = new Vec3(right.getStepX(), right.getStepY(), right.getStepZ());
        Vec3 upVec = new Vec3(up.getStepX(), up.getStepY(), up.getStepZ());
        for (double d = 0.5D; d <= laserDepth; d += 0.35D) {
            Vec3 centerPoint = start.add(look.scale(d));
            for (int px = -1; px <= 1; px++) {
                for (int py = -1; py <= 1; py++) {
                    Vec3 point = centerPoint.add(rightVec.scale(px * 0.65D)).add(upVec.scale(py * 0.65D));
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            point.x, point.y, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
                }
            }
        }

        int broken = 0;
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                var state = level.getBlockState(pos);
                if (state.isAir() || state.is(Blocks.BEDROCK)) continue;
                if (level.destroyBlock(pos, true, player)) broken++;
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }

        if (broken > 0) startCooldown(tool, player, "UltimateLaserBreaker", cooldown);
    }

    public static BlockPos lookedBlock(Player player, double range) {
        net.minecraft.world.phys.HitResult hit = player.pick(range, 0.0F, false);
        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return null;
        return ((net.minecraft.world.phys.BlockHitResult) hit).getBlockPos();
    }

    /**
     * Shared active R implementation for the four area-mining skills.
     * A bright END_ROD trail sweeps from the player's view into the selected block volume,
     * giving the moving-energy / Silver-Surfer-style visual requested for this family.
     */
    public static void miningSweep(Player player, ItemStack tool, EffectTier tier, ForgingEffect effect,
                                    int width, int height, int depth,
                                    long cooldownI, long cooldownII, long cooldownIII) {
        String key = cooldownKey(effect);
        long cooldown = switch (tier) { case I -> cooldownI; case II -> cooldownII; case III -> cooldownIII; };
        if (key == null || !ready(tool, player, key, cooldown)) return;
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        net.minecraft.world.phys.HitResult rawHit = player.pick(8.0D, 0.0F, false);
        if (!(rawHit instanceof net.minecraft.world.phys.BlockHitResult hit)) return;

        BlockPos origin = hit.getBlockPos();
        Direction depthDirection = hit.getDirection().getOpposite();
        Direction right = depthDirection.getAxis() == Direction.Axis.Y ? Direction.EAST
                : (depthDirection.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST);
        Direction up = depthDirection.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;

        boolean doubled = DoubleTriggerRuntime.rollActive(player, tool);
        int effectiveDepth = doubled ? depth * 2 : depth;
        int w0 = -(width / 2);
        int h0 = -(height / 2);
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<BlockPos> baseTargets = new java.util.LinkedHashSet<>();
        for (int d = 0; d < effectiveDepth; d++) {
            BlockPos center = origin.relative(depthDirection, d);
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    BlockPos targetPos = center.relative(right, w0 + w).relative(up, h0 + h);
                    targets.add(targetPos);
                    if (d < depth) baseTargets.add(targetPos);
                }
            }
        }

        // Mining-wave visual: no white laser. Each affected block emits a short
        // impact/sweep burst, so the particle shape follows the actual mining area.
        for (BlockPos pos : targets) {
            Vec3 point = Vec3.atCenterOf(pos);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    point.x, point.y, point.z, 5, 0.34D, 0.34D, 0.34D, 0.08D);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    point.x, point.y, point.z, 3, 0.26D, 0.26D, 0.26D, 0.025D);
        }

        int broken = 0;
        int baseBroken = 0;
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                var state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) continue;
                if (level.destroyBlock(pos, true, player)) {
                    broken++;
                    if (baseTargets.contains(pos)) baseBroken++;
                }
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }

        if (broken > 0) {
            startCooldown(tool, player, key, cooldown);
            // Bonus blocks from Double Trigger are free; durability is based on the normal cast only.
            int extraCost = (baseBroken + 1) / 2;
            if (tool.isDamageableItem())
                ForgedBlessingRuntime.damage(tool, extraCost);
        }
    }

    public static void linearPenetration(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = switch (tier) { case I -> 400L; case II -> 280L; case III -> 180L; };
        if (!ready(tool, player, "LinearPenetration3x15", cooldown)) return;
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        // Fire the mining laser from the player's view. It can travel up to 15 blocks,
        // so R still gives visible feedback even when the first block is not within melee reach.
        Vec3 startPos = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        net.minecraft.world.phys.HitResult rawHit = player.pick(15.0D, 0.0F, false);
        if (!(rawHit instanceof net.minecraft.world.phys.BlockHitResult blockHit)) {
            // No block was hit: draw the laser anyway, but do not spend cooldown/durability.
            for (double d = 0.5D; d <= 15.0D; d += 0.35D) {
                Vec3 point = startPos.add(look.scale(d));
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        point.x, point.y, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
            }
            return;
        }

        BlockPos origin = blockHit.getBlockPos();

        // The hit face points OUT of the wall. Penetration must travel INTO the wall,
        // so use the opposite direction for depth.
        Direction depthDirection = blockHit.getDirection().getOpposite();
        Direction right = (depthDirection.getAxis() == Direction.Axis.Y) ? Direction.EAST
                : (depthDirection.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST);

        boolean doubled = DoubleTriggerRuntime.rollActive(player, tool);
        int penetrationDepth = doubled ? 30 : 15;
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<BlockPos> baseTargets = new java.util.LinkedHashSet<>();
        for (int depth = 0; depth < penetrationDepth; depth++) {
            BlockPos center = origin.relative(depthDirection, depth);
            for (int width = -1; width <= 1; width++) {
                BlockPos targetPos = center.relative(right, width);
                targets.add(targetPos);
                if (depth < 15) baseTargets.add(targetPos);
            }
        }

        // Visible laser from the player's eyes to the target/maximum range.
        Vec3 hitPoint = blockHit.getLocation();
        double laserLength = Math.min((double) penetrationDepth, startPos.distanceTo(hitPoint) + penetrationDepth - 1.0D);
        for (double d = 0.5D; d <= laserLength; d += 0.35D) {
            Vec3 point = startPos.add(look.scale(d));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    point.x, point.y, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
        }

        int broken = 0;
        int baseBroken = 0;
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                var state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) continue;
                if (level.destroyBlock(pos, true, player)) {
                    broken++;
                    if (baseTargets.contains(pos)) baseBroken++;
                }
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }

        if (broken > 0) {
            startCooldown(tool, player, "LinearPenetration3x15", cooldown);
            int extraCost = (baseBroken + 1) / 2;
            if (tool.isDamageableItem())
                ForgedBlessingRuntime.damage(tool, extraCost);
        }
    }

    public static void magneticClumping(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.magneticClumping(tier);
        if (!ready(tool, player, "MagneticClumping", cooldown)) return;

        var data = player.getPersistentData();
        if (!data.contains("ForgedLastMinedX") || !data.contains("ForgedLastMinedY")
                || !data.contains("ForgedLastMinedZ")) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Magnetic Clumping: ขุดบล็อกก่อนใช้งาน"), true);
            return;
        }

        BlockPos centerPos = new BlockPos(
                data.getInt("ForgedLastMinedX"),
                data.getInt("ForgedLastMinedY"),
                data.getInt("ForgedLastMinedZ"));
        Vec3 center = Vec3.atCenterOf(centerPos);

        // Gather nearby dropped items onto the most recently mined block.
        // Tier changes cooldown only: 30 / 15 / 8 seconds.
        for (net.minecraft.world.entity.item.ItemEntity drop : player.level().getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class,
                new AABB(centerPos).inflate(DoubleTriggerRuntime.rollActive(player, tool) ? 8.0D : 4.0D))) {
            drop.setPos(center.x, center.y, center.z);
            drop.setDeltaMovement(Vec3.ZERO);
        }

        startCooldown(tool, player, "MagneticClumping", cooldown);
    }

    public static void obsidianBreaker(Player player, ItemStack tool, EffectTier tier) {
        BlockPos pos = lookedBlock(player, 6.0D);
        if (pos == null) return;
        var state = player.level().getBlockState(pos);
        if (state.isAir()) return;

        int cost = switch (tier) { case I -> 10; case II -> 6; case III -> 3; };
        if (tool.isDamageableItem() && tool.getDamageValue() + cost >= tool.getMaxDamage()) return;

        boolean broken = player.level().destroyBlock(pos, true, player);
        if (!broken) player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        ForgedSkillSounds.play(player, ForgingEffect.OBSIDIAN_BREAKER);
        damageEquipment(player, cost);
    }
}
