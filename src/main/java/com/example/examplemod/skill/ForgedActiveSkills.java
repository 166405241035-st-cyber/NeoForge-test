package com.example.examplemod.skill;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ForgedActiveSkills {
    private static final String SELECTED_INDEX = "forgedSelectedActiveSkill";

    private static final int[] AEGIS_DRAIN = {8, 5, 3};

    private ForgedActiveSkills() {}

    public static void handle(Player player, int action) {
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        List<ForgingEffect> active = getActiveEffects(tool);
        if (active.isEmpty()) return;

        int selected = normalizeSelected(tool, active.size());

        if (action == 0) {
            selected = (selected + 1) % active.size();
            setSelectedIndex(tool, selected);
            syncHud(player, active.get(selected), selected);
            return;
        }

        if (action == 1) {
            ForgingEffect effect = active.get(selected);
            use(player, tool, effect);
            syncHud(player, effect, selected);
        }
    }

    private static void use(Player player, ItemStack tool, ForgingEffect effect) {
        EffectTier tier = ForgedEffectRuntime.tier(tool, effect);
        if (tier == null) return;

        switch (effect) {
            case FIREBALL_SHOOT -> fireball(player, tool, tier);
            case FRONT_DASH -> frontDash(player, tool, tier);
            case AEGIS_SHIELD -> toggleAegis(player, tool, tier);
            case HARPOON_PULL -> harpoonPull(player, tool, tier);
            case MOB_SWAP -> mobSwap(player, tool, tier);
            case AIR_SLASH_RUPTURE -> airSlashRupture(player, tool, tier);
            case LAVA_WAVE -> lavaWave(player, tool, tier);
            case STUN_TIME_STOP -> stunTimeStop(player, tool, tier);
            case GRAVATIONAL_SLAM -> gravitationalSlam(player, tool, tier);
            case IRON_FORTRESS_GUARD -> ironFortress(player, tool, tier);
            case DIVINE_BEACON_LIGHT -> divineBeaconLaser(player, tool, tier);
            case ULTIMATE_LASER_BREAKER -> ultimateLaser(player, tool, tier);
            case BLOCK_LEVITATION -> blockLevitation(player, tool, tier);
            case MAGNETIC_CLUMPING -> magneticClumping(player, tool, tier);
            case ROUGH_CLEAVE_3X3 -> miningSweep(player, tool, tier, effect, 3, 3, 1, 160L, 120L, 80L);
            case TUNNEL_CHARGE_3X1 -> miningSweep(player, tool, tier, effect, 3, 1, 1, 160L, 120L, 80L);
            case LINEAR_BLAST_1X5 -> miningSweep(player, tool, tier, effect, 1, 1, 5, 200L, 140L, 100L);
            case WIDE_EXCAVATION_4X4 -> miningSweep(player, tool, tier, effect, 4, 4, 1, 200L, 140L, 80L);
            case LINEAR_PENETRATION_3X15 -> linearPenetration(player, tool, tier);
            case OBSIDIAN_BREAKER -> obsidianBreaker(player, tool, tier);
            case NATURE_GOD_BLESS -> natureGodBless(player, tool, tier);
            case LINE_BUILDER -> lineBuilder(player, tool, tier);
            case EARTHY_WALL_RISE -> earthyWallRise(player, tool, tier);
            case SKY_BRIDGE_WALK -> toggleSkyBridge(player, tier);
            case POCKET_DIMENSION, INTERNAL_STORAGE -> openStorage(player, tool);
            default -> {
                // Other active effects are added to this same dispatcher in later batches.
            }
        }
    }

    private static void fireball(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.fireball(tier);
        if (!ready(tool, player, "FireballShoot", cooldown)) return;

        // Spawn slightly in front of the player's eyes so the projectile does not collide
        // with the caster immediately. Give it an explicit forward velocity for reliable firing.
        Vec3 look = player.getLookAngle().normalize();
        SmallFireball fireball = new SmallFireball(player.level(), player, look);
        Vec3 spawn = player.getEyePosition().add(look.scale(0.8D));
        fireball.setPos(spawn.x, spawn.y - 0.10D, spawn.z);
        fireball.setDeltaMovement(look.scale(1.35D));
        fireball.hurtMarked = true;
        player.level().addFreshEntity(fireball);
        startCooldown(tool, player, "FireballShoot", cooldown);
        damageEquipment(player, 3);
    }

    private static void frontDash(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.dash(tier);
        if (!ready(tool, player, "FrontDash", cooldown)) return;

        Vec3 look = player.getLookAngle().normalize();
        double power = switch (tier) {
            case I -> 1.25D;
            case II -> 1.55D;
            case III -> 1.90D;
        };

        player.setDeltaMovement(
                look.x * power,
                Math.max(player.getDeltaMovement().y, 0.20D),
                look.z * power
        );
        player.hurtMarked = true;
        startCooldown(tool, player, "FrontDash", cooldown);
        damageEquipment(player, 2);
    }

    private static void harpoonPull(Player player, ItemStack tool, EffectTier tier) {
        LivingEntity target = findLookTarget(player, 25.0D);
        if (target == null) return;

        long cooldown = ForgedSkillConfig.harpoon(tier);
        if (!ready(tool, player, "HarpoonPull", cooldown)) return;

        Vec3 pull = player.position().subtract(target.position());
        if (pull.lengthSqr() < 0.01D) return;

        Vec3 velocity = pull.normalize().scale(1.0D + tierIndex(tier) * 0.25D);
        target.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.15D), velocity.z);
        target.hurtMarked = true;
        startCooldown(tool, player, "HarpoonPull", cooldown);
        damageEquipment(player, 2);
    }

    private static void mobSwap(Player player, ItemStack tool, EffectTier tier) {
        // Look farther than the usable range so we can distinguish "missed" from
        // "you are aiming at a mob, but it is too far away".
        LivingEntity target = findLookTarget(player, 64.0D);
        if (target != null && player.getEyePosition().distanceTo(target.getBoundingBox().getCenter()) > 32.0D) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Target is out of range!"), true);
            return;
        }
        if (target == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Swap: Aim at a living target."), true);
            return;
        }

        long cooldown = ForgedSkillConfig.swap(tier);
        if (!ready(tool, player, "MobSwap", cooldown)) return;

        Vec3 playerPos = player.position();
        float playerYaw = player.getYRot();
        float playerPitch = player.getXRot();

        player.teleportTo(target.getX(), target.getY(), target.getZ());
        target.teleportTo(playerPos.x, playerPos.y, playerPos.z);

        player.setYRot(target.getYRot());
        player.setXRot(target.getXRot());
        target.setYRot(playerYaw);
        target.setXRot(playerPitch);

        startCooldown(tool, player, "MobSwap", cooldown);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Swap: สำเร็จ"), true);
        damageEquipment(player, 3);
    }

    private static void airSlashRupture(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.airSlash(tier);
        if (!ready(tool, player, "AirSlashRupture", cooldown)) return;

        double damage = 6.0D; // 3 hearts for every Tier.

        AABB area = player.getBoundingBox().inflate(6.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive())) {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(player.damageSources().playerAttack(player), (float) damage);
            } finally {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
            Vec3 away = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(away.x * 0.5D, 0.25D, away.z * 0.5D));
            target.hurtMarked = true;
        }

        startCooldown(tool, player, "AirSlashRupture", cooldown);
        damageEquipment(player, 4);
    }

    private static void lavaWave(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.lava(tier);
        if (!ready(tool, player, "LavaWave", cooldown)) return;

        Vec3 view = player.getLookAngle();
        Vec3 look = new Vec3(view.x, 0.0D, view.z);
        if (look.lengthSqr() < 0.0001D) look = new Vec3(0.0D, 0.0D, 1.0D);
        look = look.normalize();
        Vec3 feet = new Vec3(player.getX(), player.getY(), player.getZ());
        for (int i = 1; i <= 5; i++) {
            Vec3 pos = feet.add(look.scale(i * 1.5D));

            // Orange/lava visual wave without placing real lava blocks, so the caster
            // can never be burned by their own Lava Wave.
            if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        pos.x, pos.y + 0.25D, pos.z, 14, 0.9D, 0.25D, 0.9D, 0.025D);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                        pos.x, pos.y + 0.15D, pos.z, 5, 0.8D, 0.12D, 0.8D, 0.0D);
            }
            AABB area = new AABB(pos.x - 1.2D, pos.y - 1.0D, pos.z - 1.2D,
                    pos.x + 1.2D, pos.y + 1.5D, pos.z + 1.2D);
            for (LivingEntity target : player.level().getEntitiesOfClass(
                    LivingEntity.class, area, e -> e != player && e.isAlive())) {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    target.hurt(player.damageSources().playerAttack(player), tier == EffectTier.I ? 3.0F : tier == EffectTier.II ? 5.0F : 7.0F);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (tier == EffectTier.I ? 3 : tier == EffectTier.II ? 5 : 7) * 20));
                target.setDeltaMovement(target.getDeltaMovement().add(look.x * 0.35D, 0.20D, look.z * 0.35D));
                target.hurtMarked = true;
            }
        }
        startCooldown(tool, player, "LavaWave", cooldown);
        damageEquipment(player, 6);
    }

    private static void stunTimeStop(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.timeStop(tier);
        if (!ready(tool, player, "StunTimeStop", cooldown)) return;

        int duration = switch (tier) {
            case I -> 100;   // 5 sec
            case II -> 200;  // 10 sec
            case III -> 300; // 15 sec
        };
        double radius = 8.0D; // Fixed radius for every Tier.
        AABB area = player.getBoundingBox().inflate(radius);
        long frozenUntil = player.level().getGameTime() + duration;

        // Stun every living entity in range except the player who cast the skill.
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area,
                e -> e != player && e.isAlive() && e.distanceToSqr(player) <= radius * radius)) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            target.getPersistentData().putLong("ForgedTimeStopUntil", frozenUntil);

            // Remember pre-existing glowing so Time Stop never removes glow from another source.
            if (target.isCurrentlyGlowing()) {
                target.getPersistentData().putBoolean("ForgedTimeStopHadGlow", true);
            }
            target.setGlowingTag(true);

            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 255, false, false));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, duration, 128, false, false));
        }

        // Visible fixed 8-block boundary.
        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (int i = 0; i < 72; i++) {
                double angle = Math.PI * 2.0D * i / 72.0D;
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + 0.15D,
                        player.getZ() + Math.sin(angle) * radius,
                        1, 0.03D, 0.03D, 0.03D, 0.0D);
            }
        }

        // Strong, short camera shake instead of a charge.
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ForgedEffectNetwork.sendTimeStopShake(serverPlayer, 12); // ~0.6 sec
        }

        startCooldown(tool, player, "StunTimeStop", cooldown);
        damageEquipment(player, 8);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Stun Time Stop!"), true);
    }

    private static void gravitationalSlam(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.gravitationalSlam(tier);
        if (!ready(tool, player, "GravitationalSlam", cooldown)) return;
        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong("ForgedGravitationalSlamUntil") > now) return;

        // Fixed 5-second charge. Tier changes cooldown only.
        player.getPersistentData().putLong("ForgedGravitationalSlamUntil", now + 100L);
        player.getPersistentData().putDouble("ForgedGravitationalSlamX", player.getX());
        player.getPersistentData().putDouble("ForgedGravitationalSlamY", player.getY());
        player.getPersistentData().putDouble("ForgedGravitationalSlamZ", player.getZ());
        player.getPersistentData().putBoolean("ForgedGravitationalSlamOldInvulnerable", player.isInvulnerable());
        player.setInvulnerable(true);
        player.getPersistentData().putLong("ForgedGravitationalSlamBaseCooldown", cooldown);
        // Bind this charge to the exact forged equipment stack that started it.
        // The token is stored on the item, so hotbar switching cannot move the
        // resulting cooldown/durability to a different weapon.
        long chargeToken = now ^ player.getUUID().getLeastSignificantBits();
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                tool, tag -> tag.putLong("forgedGravitationalSlamCharge", chargeToken));
        player.getPersistentData().putLong("ForgedGravitationalSlamChargeToken", chargeToken);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Gravitational Slam: Charging..."), true);
    }

    private static ItemStack findGravitationalSlamTool(Player player, ItemStack currentTool) {
        long token = player.getPersistentData().getLong("ForgedGravitationalSlamChargeToken");
        if (token == 0L) return currentTool;

        for (ItemStack stack : player.getInventory().items) {
            net.minecraft.world.item.component.CustomData data =
                    stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (data != null && data.copyTag().getLong("forgedGravitationalSlamCharge") == token)
                return stack;
        }
        if (!player.getOffhandItem().isEmpty()) {
            net.minecraft.world.item.component.CustomData data =
                    player.getOffhandItem().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (data != null && data.copyTag().getLong("forgedGravitationalSlamCharge") == token)
                return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static void releaseGravitationalSlam(Player player, ItemStack currentTool) {
        ItemStack tool = findGravitationalSlamTool(player, currentTool);
        double x = player.getPersistentData().getDouble("ForgedGravitationalSlamX");
        double y = player.getPersistentData().getDouble("ForgedGravitationalSlamY");
        double z = player.getPersistentData().getDouble("ForgedGravitationalSlamZ");
        Vec3 center = new Vec3(x, y, z);

        // 18-block spherical blast radius, 120 damage, no block damage.
        int killedBySlam = 0;
        double blastRadius = 18.0D;
        AABB blast = new AABB(x - blastRadius, y - blastRadius, z - blastRadius,
                x + blastRadius, y + blastRadius, z + blastRadius);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, blast,
                e -> e != player && e.isAlive() && e.distanceToSqr(center) <= blastRadius * blastRadius)) {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(player.damageSources().playerAttack(player), 120.0F);
            } finally {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
            if (!target.isAlive()) killedBySlam++;
        }

        // Cooldown begins only after the explosion has finished and the kill refund is known.
        long baseCooldown = player.getPersistentData().getLong("ForgedGravitationalSlamBaseCooldown");
        long finalCooldown = Math.max(0L, baseCooldown - killedBySlam * 200L);
        if (!tool.isEmpty()) {
            setItemCooldownReadyAt(tool, "GravitationalSlam", player.level().getGameTime() + finalCooldown);
            net.minecraft.world.item.component.CustomData.update(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    tool, tag -> tag.remove("forgedGravitationalSlamCharge"));
        }
        player.getPersistentData().remove("ForgedGravitationalSlamBaseCooldown");
        player.getPersistentData().remove("ForgedGravitationalSlamChargeToken");
        if (killedBySlam > 0) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Gravitational Slam cooldown reduced by " + (killedBySlam * 10) + "s!"), true);
        }

        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    x, y + 0.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y + 0.5D, z, 24, 1.2D, 0.4D, 1.2D, 0.04D);
        }

        player.setInvulnerable(player.getPersistentData().getBoolean("ForgedGravitationalSlamOldInvulnerable"));
        player.getPersistentData().remove("ForgedGravitationalSlamOldInvulnerable");
        player.getPersistentData().remove("ForgedGravitationalSlamUntil");
        if (!tool.isEmpty()) {
            tool.hurtAndBreak(12, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        syncHeldEquipmentHud(player);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Gravitational Slam!"), true);
    }

    private static void ironFortress(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.fortress(tier);
        if (!ready(tool, player, "IronFortressGuard", cooldown)) return;

        long duration = switch (tier) {
            case I -> 40L;
            case II -> 80L;
            case III -> 120L;
        };
        player.getPersistentData().putLong("ForgedIronFortressUntil", player.level().getGameTime() + duration);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Iron Fortress Guard: ON"), true);
        startCooldown(tool, player, "IronFortressGuard", cooldown + duration);
        damageEquipment(player, 8);
    }

    private static void divineBeaconLaser(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.divine(tier); // Defaults: 7.5 / 5 / 3 sec.
        if (!ready(tool, player, "DivineBeaconLight", cooldown)) return;

        // Keep the beam active for 5 seconds. Damage is handled by tickWorldEffects
        // every 6 ticks (0.3 sec), so the player can keep aiming during the beam.
        long now = player.level().getGameTime();
        player.getPersistentData().putLong("ForgedDivineBeaconUntil", now + 100L);
        player.getPersistentData().putLong("ForgedDivineBeaconNextHit", now);

        startCooldown(tool, player, "DivineBeaconLight", cooldown);
        damageEquipment(player, 6);
    }

    private static void ultimateLaser(Player player, ItemStack tool, EffectTier tier) {
        // Fixed design values: I 300s, II 210s, III 120s.
        // Keep these authoritative here so old generated config files cannot retain stale cooldowns.
        long cooldown = switch (tier) {
            case I -> 6000L;
            case II -> 4200L;
            case III -> 2400L;
        };
        if (!ready(tool, player, "UltimateLaserBreaker", cooldown)) return;
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        // Active R laser: fixed 3 x 3 x 16 volume following the player's look direction.
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
        for (int depth = 0; depth < 16; depth++) {
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
        for (double d = 0.5D; d <= 16.0D; d += 0.35D) {
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

    private static BlockPos lookedBlock(Player player, double range) {
        net.minecraft.world.phys.HitResult hit = player.pick(range, 0.0F, false);
        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return null;
        return ((net.minecraft.world.phys.BlockHitResult) hit).getBlockPos();
    }

    /**
     * Shared active R implementation for the four area-mining skills.
     * A bright END_ROD trail sweeps from the player's view into the selected block volume,
     * giving the moving-energy / Silver-Surfer-style visual requested for this family.
     */
    private static void miningSweep(Player player, ItemStack tool, EffectTier tier, ForgingEffect effect,
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

        int w0 = -(width / 2);
        int h0 = -(height / 2);
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        for (int d = 0; d < depth; d++) {
            BlockPos center = origin.relative(depthDirection, d);
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    targets.add(center.relative(right, w0 + w).relative(up, h0 + h));
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
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                var state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) continue;
                if (level.destroyBlock(pos, true, player)) broken++;
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }

        if (broken > 0) {
            startCooldown(tool, player, key, cooldown);
            int extraCost = (broken + 1) / 2;
            if (tool.isDamageableItem())
                tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + extraCost));
        }
    }

    private static void linearPenetration(Player player, ItemStack tool, EffectTier tier) {
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

        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        for (int depth = 0; depth < 15; depth++) {
            BlockPos center = origin.relative(depthDirection, depth);
            for (int width = -1; width <= 1; width++) targets.add(center.relative(right, width));
        }

        // Visible laser from the player's eyes to the target/maximum range.
        Vec3 hitPoint = blockHit.getLocation();
        double laserLength = Math.min(15.0D, startPos.distanceTo(hitPoint) + 14.0D);
        for (double d = 0.5D; d <= laserLength; d += 0.35D) {
            Vec3 point = startPos.add(look.scale(d));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    point.x, point.y, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
        }

        int broken = 0;
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                var state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) continue;
                if (level.destroyBlock(pos, true, player)) broken++;
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }

        if (broken > 0) {
            startCooldown(tool, player, "LinearPenetration3x15", cooldown);
            int extraCost = (broken + 1) / 2;
            if (tool.isDamageableItem())
                tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + extraCost));
        }
    }

    private static void blockLevitation(Player player, ItemStack tool, EffectTier tier) {
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
        damageEquipment(player, cost);
    }

    private static void magneticClumping(Player player, ItemStack tool, EffectTier tier) {
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
                new AABB(centerPos).inflate(4.0D))) {
            drop.setPos(center.x, center.y, center.z);
            drop.setDeltaMovement(Vec3.ZERO);
        }

        startCooldown(tool, player, "MagneticClumping", cooldown);
    }

    private static void obsidianBreaker(Player player, ItemStack tool, EffectTier tier) {
        BlockPos pos = lookedBlock(player, 6.0D);
        if (pos == null) return;
        var state = player.level().getBlockState(pos);
        if (state.isAir()) return;

        int cost = switch (tier) { case I -> 10; case II -> 6; case III -> 3; };
        if (tool.isDamageableItem() && tool.getDamageValue() + cost >= tool.getMaxDamage()) return;

        boolean broken = player.level().destroyBlock(pos, true, player);
        if (!broken) player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        damageEquipment(player, cost);
    }

    private static void natureGodBless(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.nature(tier);
        if (!ready(tool, player, "NatureGodBless", cooldown)) return;

        int chance = tier == EffectTier.I ? 5 : tier == EffectTier.II ? 10 : 20;
        if (player.getRandom().nextInt(100) < chance) {
            player.getInventory().add(new ItemStack(
                    player.getRandom().nextDouble() < 0.1D ? net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE
                            : net.minecraft.world.item.Items.GOLDEN_APPLE));
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Nature God Bless: ได้รับพรจากธรรมชาติ"), true);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Nature God Bless: ไม่ได้รับรางวัล"), true);
        }
        startCooldown(tool, player, "NatureGodBless", cooldown);
        damageEquipment(player, 8);
    }

    private static void openStorage(Player player, ItemStack tool) {
        int size = ForgedStorageMenu.storageSize(tool);
        if (size <= 0) return;
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inv, p) -> new ForgedStorageMenu(id, inv, tool),
                net.minecraft.network.chat.Component.literal("Forged Storage")));
    }

    private static void lineBuilder(Player player, ItemStack tool, EffectTier tier) {
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
        BlockPos start = feet.relative(forward, 2);

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

    private static void earthyWallRise(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = switch (tier) { case I -> 160L; case II -> 100L; case III -> 60L; };
        if (!ready(tool, player, "EarthyWallRise", cooldown)) return;
        net.minecraft.core.Direction forward = player.getDirection();
        net.minecraft.core.Direction side = forward.getClockWise();
        BlockPos center = player.blockPosition().relative(forward, 2);
        int placed = 0;
        for (int i = -1; i <= 1; i++) {
            BlockPos pos = center.relative(side, i);
            if (player.level().getBlockState(pos).canBeReplaced()) {
                player.level().setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                placed++;
            }
        }
        if (placed == 0) return;
        startCooldown(tool, player, "EarthyWallRise", cooldown);
        damageEquipment(player, 4);
    }

    private static void toggleSkyBridge(Player player, EffectTier tier) {
        boolean active = !player.getPersistentData().getBoolean("ForgedSkyBridgeActive");
        player.getPersistentData().putBoolean("ForgedSkyBridgeActive", active);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Sky Bridge Walk: " + (active ? "ON" : "OFF")), true);
    }

    private static void toggleAegis(Player player, ItemStack tool, EffectTier tier) {
        boolean active = player.getPersistentData().getBoolean("ForgedAegisActive");
        if (active) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF"), true);
            return;
        }

        player.getPersistentData().putBoolean("ForgedAegisActive", true);
        player.getPersistentData().putLong("ForgedAegisNextDrain", player.level().getGameTime() + 100L);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: ON"), true);
    }

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
                            target.hurt(player.damageSources().playerAttack(player), 12.0F);
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
                BlockPos below = player.blockPosition().below();
                if (player.level().getBlockState(below).canBeReplaced()) {
                    int cost = switch (sky) { case I -> 3; case II -> 2; case III -> 1; };
                    if (!tool.isDamageableItem() || tool.getDamageValue() + cost < tool.getMaxDamage()) {
                        player.level().setBlockAndUpdate(below, Blocks.COBBLESTONE.defaultBlockState());
                        damageEquipment(player, cost);
                    } else {
                        player.getPersistentData().putBoolean("ForgedSkyBridgeActive", false);
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

    public static void tickAegis(Player player, ItemStack tool) {
        if (player.level().isClientSide()) return;
        if (!player.getPersistentData().getBoolean("ForgedAegisActive")) return;

        EffectTier tier = ForgedEffectRuntime.tier(tool, ForgingEffect.AEGIS_SHIELD);
        if (tier == null) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF (effect missing)"), true);
            return;
        }

        long now = player.level().getGameTime();
        long nextDrain = player.getPersistentData().getLong("ForgedAegisNextDrain");
        if (now < nextDrain) return;

        int cost = tierValue(tier, AEGIS_DRAIN);
        if (tool.getDamageValue() + cost >= tool.getMaxDamage()) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF (durability)"), true);
            return;
        }

        damageEquipment(player, cost);
        player.getPersistentData().putLong("ForgedAegisNextDrain", now + 100L);
    }

    public static boolean isAegisActive(Player player) {
        return player.getPersistentData().getBoolean("ForgedAegisActive");
    }

    public static List<ForgingEffect> getActiveEffects(ItemStack tool) {
        List<ForgingEffect> result = new ArrayList<>();
        int count = ForgedEquipmentItem.effectCount(tool);
        for (int i = 0; i < count; i++) {
            AnvilAssemblyResult.FinalEffect effect = ForgedEquipmentItem.readEffect(tool, i);
            if (effect != null && isActive(effect.effect())) {
                result.add(effect.effect());
            }
        }
        return result;
    }

    private static boolean isActive(ForgingEffect effect) {
        return switch (effect) {
            case FIREBALL_SHOOT, FRONT_DASH, AEGIS_SHIELD,
                 HARPOON_PULL, MOB_SWAP, AIR_SLASH_RUPTURE, LAVA_WAVE,
                 STUN_TIME_STOP, GRAVATIONAL_SLAM, IRON_FORTRESS_GUARD, DIVINE_BEACON_LIGHT,
                 ULTIMATE_LASER_BREAKER, BLOCK_LEVITATION, MAGNETIC_CLUMPING,
                 ROUGH_CLEAVE_3X3, TUNNEL_CHARGE_3X1, LINEAR_BLAST_1X5, WIDE_EXCAVATION_4X4,
                 LINEAR_PENETRATION_3X15, OBSIDIAN_BREAKER,
                 NATURE_GOD_BLESS, LINE_BUILDER, EARTHY_WALL_RISE,
                 SKY_BRIDGE_WALK, POCKET_DIMENSION, INTERNAL_STORAGE -> true;
            default -> false;
        };
    }

    public static int normalizeSelected(ItemStack tool, int size) {
        if (tool.isEmpty() || size <= 0) return 0;
        net.minecraft.world.item.component.CustomData data =
                tool.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        int selected = data == null ? 0 : data.copyTag().getInt(SELECTED_INDEX);
        if (selected < 0 || selected >= size) selected = 0;
        setSelectedIndex(tool, selected);
        return selected;
    }

    private static void setSelectedIndex(ItemStack tool, int selected) {
        if (tool.isEmpty()) return;
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                tool,
                tag -> tag.putInt(SELECTED_INDEX, selected));
    }

    /** Refresh the client HUD from the equipment currently held in the main hand. */
    public static void syncHeldEquipmentHud(Player player) {
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();
        List<ForgingEffect> active = getActiveEffects(tool);
        if (active.isEmpty()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                ForgedEffectNetwork.sendHudState(serverPlayer, 0, "", 0L, 0L, false);
            return;
        }
        int selected = normalizeSelected(tool, active.size());
        syncHud(player, active.get(selected), selected);
    }

    public static String cooldownKey(ForgingEffect effect) {
        return switch (effect) {
            case FIREBALL_SHOOT -> "FireballShoot";
            case FRONT_DASH -> "FrontDash";
            case HARPOON_PULL -> "HarpoonPull";
            case MOB_SWAP -> "MobSwap";
            case AIR_SLASH_RUPTURE -> "AirSlashRupture";
            case LAVA_WAVE -> "LavaWave";
            case STUN_TIME_STOP -> "StunTimeStop";
            case GRAVATIONAL_SLAM -> "GravitationalSlam";
            case IRON_FORTRESS_GUARD -> "IronFortressGuard";
            case DIVINE_BEACON_LIGHT -> "DivineBeaconLight";
            case ULTIMATE_LASER_BREAKER -> "UltimateLaserBreaker";
            case MAGNETIC_CLUMPING -> "MagneticClumping";
            case ROUGH_CLEAVE_3X3 -> "RoughCleave3x3";
            case TUNNEL_CHARGE_3X1 -> "TunnelCharge3x1";
            case LINEAR_BLAST_1X5 -> "LinearBlast1x5";
            case WIDE_EXCAVATION_4X4 -> "WideExcavation4x4";
            case LINEAR_PENETRATION_3X15 -> "LinearPenetration3x15";
            case NATURE_GOD_BLESS -> "NatureGodBless";
            case LINE_BUILDER -> "LineBuilder";
            case EARTHY_WALL_RISE -> "EarthyWallRise";
            default -> null;
        };
    }

    private static void syncHud(Player player, ForgingEffect effect, int selectedIndex) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;
        ItemStack tool = player.getMainHandItem();
        StringBuilder cooldownStates = new StringBuilder();
        for (ForgingEffect activeEffect : getActiveEffects(tool)) {
            String activeKey = cooldownKey(activeEffect);
            long readyAt = activeKey == null ? 0L : itemCooldownReadyAt(tool, activeKey);
            if (!cooldownStates.isEmpty()) cooldownStates.append(';');
            cooldownStates.append(activeEffect.name()).append('=').append(readyAt);
        }
        ForgedEffectNetwork.sendHudState(serverPlayer, selectedIndex, cooldownStates.toString(), 0L,
                player.getPersistentData().getLong("ForgedGravitationalSlamUntil"),
                player.getPersistentData().getBoolean("ForgedAegisActive"));
    }

    public static long cooldownRemaining(Player player, ItemStack tool, ForgingEffect effect) {
        String key = cooldownKey(effect);
        if (key == null || tool.isEmpty()) return 0L;
        return Math.max(0L, itemCooldownReadyAt(tool, key) - player.level().getGameTime());
    }

    private static String itemCooldownKey(String key) {
        return "forgedCooldown_" + key;
    }

    private static long itemCooldownReadyAt(ItemStack tool, String key) {
        net.minecraft.world.item.component.CustomData data =
                tool.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data == null ? 0L : data.copyTag().getLong(itemCooldownKey(key));
    }

    private static void setItemCooldownReadyAt(ItemStack tool, String key, long readyAt) {
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                tool,
                tag -> tag.putLong(itemCooldownKey(key), readyAt));
    }

    private static boolean ready(ItemStack tool, Player player, String key, long cooldownTicks) {
        long now = player.level().getGameTime();
        return now >= itemCooldownReadyAt(tool, key);
    }

    private static void startCooldown(ItemStack tool, Player player, String key, long cooldownTicks) {
        setItemCooldownReadyAt(tool, key, player.level().getGameTime() + cooldownTicks);
    }

    private static void damageEquipment(Player player, int amount) {
        ItemStack tool = player.getMainHandItem();
        tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + amount));
    }

    private static LivingEntity findLookTarget(Player player, double range) {
        // True crosshair ray: choose the first living entity whose hitbox is actually
        // intersected by the player's view ray instead of using a wide aim cone.
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);

        LivingEntity best = null;
        double bestDistance = range * range;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, search, e -> e != player && e.isAlive())) {
            AABB hitbox = entity.getBoundingBox().inflate(0.30D);
            java.util.Optional<Vec3> hit = hitbox.clip(eye, end);
            if (hit.isEmpty()) continue;

            double distance = eye.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                best = entity;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static int tierIndex(EffectTier tier) {
        return switch (tier) {
            case I -> 0;
            case II -> 1;
            case III -> 2;
        };
    }

    private static long tierValue(EffectTier tier, long[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }

    private static int tierValue(EffectTier tier, int[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }
}
