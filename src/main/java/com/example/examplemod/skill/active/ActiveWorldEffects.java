package com.example.examplemod.skill.active;

import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;
import static com.example.examplemod.skill.active.ActiveSkillSupport.findLookTarget;
import static com.example.examplemod.skill.combat.ActiveCrowdControlSkills.releaseGravitationalSlam;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgingEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Per-tick progression of active beams, charge releases and temporary world effects. */
public final class ActiveWorldEffects {
    private ActiveWorldEffects() {}

    public static void tickWorldEffects(Player player, ItemStack tool) {
        if (player.level().isClientSide()) return;
        long now = player.level().getGameTime();

        // Divine Beacon Light: a dense 24-block beam that follows the crosshair for
        // 5 seconds. The same target can be damaged again every 6 ticks (0.3 sec).
        long divineUntil = player.getPersistentData().getLong("ForgedDivineBeaconUntil");
        if (divineUntil > 0L) {
            if (now >= divineUntil) {
                player.getPersistentData().remove("ForgedDivineBeaconUntil");
                player.getPersistentData().remove("ForgedDivineBeaconNextHit");
                player.getPersistentData().remove("ForgedDivineBeaconDouble");
            } else {
                Vec3 beamStart = player.getEyePosition();
                Vec3 beamLook = player.getLookAngle().normalize();

                // Dense END_ROD particles form a bright beacon-like beam.
                if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
                    for (double d = 0.5D; d <= 24.0D; d += 0.5D) {
                        Vec3 p = beamStart.add(beamLook.scale(d));
                        server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                                p.x, p.y, p.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
                    }
                }

                long nextHit = player.getPersistentData().getLong("ForgedDivineBeaconNextHit");
                if (now >= nextHit) {
                    LivingEntity target = findLookTarget(player, 24.0D);
                    if (target != null) {
                        player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                        try {
                            float beamDamage = player.getPersistentData().getBoolean("ForgedDivineBeaconDouble")
                                    ? 24.0F : 12.0F;
                            target.hurt(player.damageSources().playerAttack(player), beamDamage);
                            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
                        } finally {
                            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                        }
                    }
                    player.getPersistentData().putLong("ForgedDivineBeaconNextHit", now + 6L);
                }
            }
        }

        // Gravitational Slam: lock the caster for 5 seconds, continuously pull all
        // living entities within 10 blocks, and show rising smoke once per second.
        long slamUntil = player.getPersistentData().getLong("ForgedGravitationalSlamUntil");
        if (slamUntil > 0L) {
            if (now >= slamUntil) {
                releaseGravitationalSlam(player, tool);
            } else {
                double x = player.getPersistentData().getDouble("ForgedGravitationalSlamX");
                double y = player.getPersistentData().getDouble("ForgedGravitationalSlamY");
                double z = player.getPersistentData().getDouble("ForgedGravitationalSlamZ");
                Vec3 center = new Vec3(x, y, z);

                // Hard-lock movement and position during the charge.
                player.setDeltaMovement(Vec3.ZERO);
                player.teleportTo(x, y, z);
                player.hurtMarked = true;

                double pullRadius = 10.0D;
                AABB pullArea = new AABB(x - pullRadius, y - pullRadius, z - pullRadius,
                        x + pullRadius, y + pullRadius, z + pullRadius);
                for (LivingEntity target : player.level().getEntitiesOfClass(
                        LivingEntity.class, pullArea,
                        e -> e != player && e.isAlive() && e.position().distanceToSqr(center) <= pullRadius * pullRadius)) {
                    Vec3 toward = center.subtract(target.position());
                    if (toward.lengthSqr() > 0.04D) {
                        Vec3 pull = toward.normalize().scale(0.18D);
                        target.setDeltaMovement(target.getDeltaMovement().scale(0.72D).add(pull));
                        target.hurtMarked = true;
                    }
                }

                // Campfire-like rising smoke every 1 second while charging.
                long elapsed = 100L - (slamUntil - now);
                if (elapsed % 20L == 0L && player.level() instanceof net.minecraft.server.level.ServerLevel server) {
                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            x, y + 0.2D, z, 12, 0.7D, 0.15D, 0.7D, 0.035D);
                }
            }
        }

        // Keep every living Time Stop target immobilized. Hostile mobs also lose
        // their attack target while stunned. Remove only the glow added by this skill.
        AABB freezeArea = player.getBoundingBox().inflate(20.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, freezeArea,
                e -> e != player && e.getPersistentData().getLong("ForgedTimeStopUntil") > 0L)) {
            long until = target.getPersistentData().getLong("ForgedTimeStopUntil");
            if (until > now) {
                target.setDeltaMovement(Vec3.ZERO);
                target.hurtMarked = true;
                if (target instanceof Monster monster) {
                    monster.setTarget(null);
                }
            } else {
                target.getPersistentData().remove("ForgedTimeStopUntil");
                boolean hadGlow = target.getPersistentData().getBoolean("ForgedTimeStopHadGlow");
                target.getPersistentData().remove("ForgedTimeStopHadGlow");
                if (!hadGlow) {
                    target.setGlowingTag(false);
                }
            }
        }

        if (player.getPersistentData().getBoolean("ForgedSkyBridgeActive")) {
            EffectTier sky = ForgedEffectRuntime.tier(tool, ForgingEffect.SKY_BRIDGE_WALK);
            if (sky == null) {
                player.getPersistentData().putBoolean("ForgedSkyBridgeActive", false);
            } else {
                // Create the bridge one block below the player's feet while walking over air.
                // Tier changes durability cost per generated bridge block.
                BlockPos below = player.blockPosition().below();
                if (player.level().getBlockState(below).canBeReplaced()) {
                    int cost = switch (sky) { case I -> 3; case II -> 2; case III -> 1; };
                    if (!tool.isDamageableItem() || tool.getDamageValue() + cost < tool.getMaxDamage()) {
                        // Frost-Walker-like temporary bridge: invisible support appears under
                        // the player's feet and schedules itself to disappear shortly afterwards.
                        player.level().setBlockAndUpdate(below, ExampleMod.INVISIBLE_SUPPORT_BLOCK.get().defaultBlockState());
                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.scheduleTick(below, ExampleMod.INVISIBLE_SUPPORT_BLOCK.get(), 120);
                            Vec3 point = Vec3.atCenterOf(below);
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                    point.x, point.y + 0.45D, point.z, 3,
                                    0.18D, 0.03D, 0.18D, 0.01D);
                        }

                        damageEquipment(player, cost);
                    } else {
                        player.getPersistentData().putBoolean("ForgedSkyBridgeActive", false);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                "Sky Bridge Walk: OFF (durability too low)"), true);
                    }
                }
            }
        }

        // Nature God Bless passive: while the forged tool is held, nearby crops receive
        // extra random growth ticks. Tier does NOT increase the area.
        EffectTier nature = ForgedEffectRuntime.tier(tool, ForgingEffect.NATURE_GOD_BLESS);
        if (nature != null && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && now % 10L == 0L) {
            int attempts = switch (nature) { case I -> 1; case II -> 2; case III -> 3; };
            net.minecraft.core.BlockPos center = player.blockPosition();
            java.util.List<net.minecraft.core.BlockPos> crops = new java.util.ArrayList<>();
            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                    center.offset(-4, -1, -4), center.offset(4, 2, 4))) {
                if (serverLevel.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.CropBlock) {
                    crops.add(pos.immutable());
                }
            }
            for (int i = 0; i < attempts && !crops.isEmpty(); i++) {
                net.minecraft.core.BlockPos pos = crops.get(player.getRandom().nextInt(crops.size()));
                net.minecraft.world.level.block.state.BlockState state = serverLevel.getBlockState(pos);
                if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(state)) {
                    net.minecraft.world.item.BoneMealItem.growCrop(
                            new ItemStack(net.minecraft.world.item.Items.BONE_MEAL),
                            serverLevel, pos);
                }
            }
        }
    }
}
