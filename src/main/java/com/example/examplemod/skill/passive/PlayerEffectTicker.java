package com.example.examplemod.skill.passive;

import static com.example.examplemod.skill.passive.PassiveEffectSupport.canUseTimedTrigger;
import static com.example.examplemod.skill.passive.PassiveEffectSupport.tierValue;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedActiveSkills;
import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import com.example.examplemod.skill.curse.ForgedCurse;
import com.example.examplemod.skill.curse.ForgedCurseRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Held-equipment effects and allied minion upkeep during player ticks. */
public final class PlayerEffectTicker {
    private PlayerEffectTicker() {}

    private static final int[] SELF_REPAIR_AMOUNT = {2, 5, 10};

    private static final float[] EARTHY_SHOCKWAVE_DAMAGE = {3.0F, 5.0F, 7.0F};

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();
        long now = player.level().getGameTime();

        // Refresh HUD when the actual held ItemStack changes. Each forged item owns
        // its selected active skill and its cooldown timestamps.
        int heldSlot = player.getInventory().selected;
        int lastSlot = player.getPersistentData().getInt("ForgedHudLastHeldSlot");
        int currentHash = System.identityHashCode(tool);
        int lastHash = player.getPersistentData().getInt("ForgedHudLastHeldHash");
        if (heldSlot != lastSlot || currentHash != lastHash) {
            player.getPersistentData().putInt("ForgedHudLastHeldSlot", heldSlot);
            player.getPersistentData().putInt("ForgedHudLastHeldHash", currentHash);
            ForgedActiveSkills.syncHeldEquipmentHud(player);
        }

        // Poison Gas Cloud owner immunity. The cloud itself remains a normal
        // lingering-style AreaEffectCloud for every other living entity.
        AABB gasCheck = player.getBoundingBox().inflate(5.5D);
        boolean insideOwnGas = !player.level().getEntitiesOfClass(AreaEffectCloud.class, gasCheck,
                cloud -> cloud.isAlive()
                        && cloud.getPersistentData().hasUUID("ForgedPoisonGasOwner")
                        && cloud.getPersistentData().getUUID("ForgedPoisonGasOwner").equals(player.getUUID())
                        && player.distanceToSqr(cloud) <= (double) cloud.getRadius() * cloud.getRadius()).isEmpty();
        if (insideOwnGas && player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }

        // Allied Zombie Minion AI: protect/follow the summoner and attack hostile monsters.
        if (now % 5L == 0L) {
            AABB minionArea = player.getBoundingBox().inflate(32.0D);
            for (Zombie minion : player.level().getEntitiesOfClass(Zombie.class, minionArea,
                    z -> z.isAlive() && z.getPersistentData().hasUUID("ForgingMinionOwner")
                            && z.getPersistentData().getUUID("ForgingMinionOwner").equals(player.getUUID()))) {
                LivingEntity current = minion.getTarget();
                if (current == player || (current instanceof Zombie allied
                        && allied.getPersistentData().hasUUID("ForgingMinionOwner"))) {
                    minion.setTarget(null);
                    current = null;
                }
                if (current == null || !current.isAlive()) {
                    Monster nearest = null;
                    double bestDistance = 256.0D;
                    for (Monster candidate : player.level().getEntitiesOfClass(Monster.class,
                            minion.getBoundingBox().inflate(16.0D),
                            mob -> mob.isAlive() && mob != minion
                                    && !(mob instanceof Zombie z && z.getPersistentData().hasUUID("ForgingMinionOwner")))) {
                        double distance = minion.distanceToSqr(candidate);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            nearest = candidate;
                        }
                    }
                    if (nearest != null) minion.setTarget(nearest);
                }
                if (minion.getTarget() == null && minion.distanceToSqr(player) > 36.0D) {
                    minion.getNavigation().moveTo(player, 1.15D);
                }
            }
        }

        // Vanilla cobweb has no scheduled self-removal behavior, so remove forged webs explicitly.
        for (LivingEntity trapped : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(32.0D),
                e -> e.getPersistentData().getLong("ForgedWebTrapUntil") > 0L
                        && e.getPersistentData().getLong("ForgedWebTrapUntil") <= now)) {
            BlockPos webPos = new BlockPos(
                    trapped.getPersistentData().getInt("ForgedWebTrapX"),
                    trapped.getPersistentData().getInt("ForgedWebTrapY"),
                    trapped.getPersistentData().getInt("ForgedWebTrapZ"));
            if (player.level().getBlockState(webPos).is(Blocks.COBWEB))
                player.level().removeBlock(webPos, false);
            trapped.getPersistentData().remove("ForgedWebTrapUntil");
            trapped.getPersistentData().remove("ForgedWebTrapX");
            trapped.getPersistentData().remove("ForgedWebTrapY");
            trapped.getPersistentData().remove("ForgedWebTrapZ");
        }

        boolean wasGrounded = player.getPersistentData().getBoolean("ForgedWasGrounded");
        boolean groundedNow = player.onGround();
        EffectTier shockwave = ForgedEffectRuntime.tier(tool, ForgingEffect.EARTHY_SHOCKWAVE);

        // fallDistance can be reset by vanilla on the landing tick, so remember whether
        // the player was genuinely airborne/falling before touching the ground.
        if (!groundedNow && player.fallDistance > 0.0F) {
            player.getPersistentData().putBoolean("ForgedEarthyWasFalling", true);
        }

        boolean landedFromFall = groundedNow && !wasGrounded
                && player.getPersistentData().getBoolean("ForgedEarthyWasFalling");
        if (shockwave != null && landedFromFall
                && canUseTimedTrigger(player, "EarthyShockwave", 60L)) {
            boolean doubled = DoubleTriggerRuntime.rollResult(player, tool);
            float damage = tierValue(shockwave, EARTHY_SHOCKWAVE_DAMAGE);
            if (doubled) damage *= 2.0F;
            double radius = 3.0D; // Fixed AoE; Tier changes damage only.
            AABB area = player.getBoundingBox().inflate(radius, 1.5D, radius);

            for (Monster mob : player.level().getEntitiesOfClass(Monster.class, area,
                    e -> e.isAlive() && e.distanceToSqr(player) <= radius * radius)) {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    mob.hurt(player.damageSources().playerAttack(player), damage);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }

                Vec3 away = mob.position().subtract(player.position());
                if (away.lengthSqr() > 0.01D) {
                    away = away.normalize();
                    double horizontal = doubled ? 1.70D : 0.85D;
                    double vertical = doubled ? 0.84D : 0.42D;
                    mob.setDeltaMovement(mob.getDeltaMovement().add(
                            away.x * horizontal, vertical, away.z * horizontal));
                    mob.hurtMarked = true;
                }
            }

            // Visible ground shockwave: two expanding rings of dust/electric impact
            // around the landing point. This does not break terrain.
            if (player.level() instanceof ServerLevel serverLevel) {
                double y = player.getY() + 0.12D;
                for (double ring : new double[]{1.5D, 3.0D}) {
                    int points = ring < 2.0D ? 20 : 36;
                    for (int i = 0; i < points; i++) {
                        double angle = (Math.PI * 2.0D * i) / points;
                        double x = player.getX() + Math.cos(angle) * ring;
                        double z = player.getZ() + Math.sin(angle) * ring;
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                x, y, z, 1, 0.08D, 0.03D, 0.08D, 0.01D);
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                x, y + 0.05D, z, 1, 0.04D, 0.02D, 0.04D, 0.02D);
                    }
                }
            }

            if (tool.isDamageableItem())
                ForgedBlessingRuntime.damage(tool, 2);
            ForgedSkillSounds.play(player, ForgingEffect.EARTHY_SHOCKWAVE);
        }

        if (groundedNow) player.getPersistentData().putBoolean("ForgedEarthyWasFalling", false);
        player.getPersistentData().putBoolean("ForgedWasGrounded", groundedNow);

        // Restore gravity independently for each Static Hover drop when its own timer expires.
        // This avoids affecting unrelated nearby drops and also works if the player moves away.
        if (now % 2L == 0L && player.level() instanceof ServerLevel serverLevel) {
            for (ItemEntity drop : serverLevel.getEntitiesOfClass(ItemEntity.class,
                    player.getBoundingBox().inflate(64.0D),
                    item -> item.getPersistentData().getLong("ForgedStaticHoverUntil") > 0L)) {
                long until = drop.getPersistentData().getLong("ForgedStaticHoverUntil");
                if (now >= until) {
                    drop.setNoGravity(false);
                    drop.getPersistentData().remove("ForgedStaticHoverUntil");
                } else {
                    drop.setNoGravity(true);
                    drop.setDeltaMovement(Vec3.ZERO);
                }
            }
        }

        EffectTier thermalBarrier = ForgedEffectRuntime.tier(tool, ForgingEffect.THERMAL_CROP_BARRIER);
        if (thermalBarrier != null && now % 10L == 0L) {
            int burnSeconds = switch (thermalBarrier) { case I -> 3; case II -> 5; case III -> 8; };
            double radius = 5.0D; // Fixed protection area; Tier changes burn duration only.
            AABB area = player.getBoundingBox().inflate(radius, 2.0D, radius);
            for (Monster mob : player.level().getEntitiesOfClass(Monster.class, area,
                    e -> e.isAlive() && e.distanceToSqr(player) <= radius * radius)) {
                BlockPos below = mob.blockPosition().below();
                if (player.level().getBlockState(below).is(Blocks.FARMLAND)) {
                    if (mob.getRemainingFireTicks() <= 0)
                        ForgedSkillSounds.play(player, ForgingEffect.THERMAL_CROP_BARRIER);
                    mob.setRemainingFireTicks(Math.max(mob.getRemainingFireTicks(), burnSeconds * 20));
                }
            }
        }

        long thermalUntil = player.getPersistentData().getLong("ForgedThermalBarrierUntil");
        if (thermalUntil > now) {
            BlockPos center = new BlockPos(
                    player.getPersistentData().getInt("ForgedThermalBarrierX"),
                    player.getPersistentData().getInt("ForgedThermalBarrierY"),
                    player.getPersistentData().getInt("ForgedThermalBarrierZ"));
            int burn = player.getPersistentData().getInt("ForgedThermalBarrierBurn");
            AABB protectedArea = new AABB(
                    center.getX() - 1.0D, center.getY(), center.getZ() - 1.0D,
                    center.getX() + 2.0D, center.getY() + 3.0D, center.getZ() + 2.0D);
            for (Monster mob : player.level().getEntitiesOfClass(Monster.class, protectedArea, Entity::isAlive))
                mob.setRemainingFireTicks(Math.max(mob.getRemainingFireTicks(), burn * 20));
            // Keep the fixed 3x3 protected patch as farmland if it was trampled to dirt.
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
                if (player.level().getBlockState(pos).is(Blocks.DIRT))
                    player.level().setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
            }
        }

        // Self-Repairing: every 30 seconds while the forged equipment is held,
        // restore durability according to tier without exceeding full durability.
        EffectTier selfRepair = ForgedEffectRuntime.tier(tool, ForgingEffect.SELF_REPAIRING);
        if (selfRepair != null && tool.isDamaged() && now % 600L == 0L) {
            int repair = tierValue(selfRepair, SELF_REPAIR_AMOUNT);
            if (DoubleTriggerRuntime.rollResult(player, tool)) repair *= 2;
            tool.setDamageValue(Math.max(0, tool.getDamageValue() - repair));
            ForgedSkillSounds.play(player, ForgingEffect.SELF_REPAIRING);
        }

        ForgedActiveSkills.tickAegis(player, tool);
        ForgedActiveSkills.tickWorldEffects(player, tool);

        EffectTier divine = ForgedEffectRuntime.tier(tool, ForgingEffect.DIVINE_BEACON_LIGHT);
        if (divine != null) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, false));
        }

        // Wither Curse Power is passive while held: permanent-feeling Wither I via refresh.
        // Its attack multiplier is applied in onLivingAttack.
        EffectTier witherCurse = ForgedEffectRuntime.tier(tool, ForgingEffect.WITHER_CURSE_POWER);
        if (witherCurse != null) {
            // Secret synergy: Power Erasure removes Wither Curse Power's drawback
            // without erasing its damage multiplier.
            if (ForgedCurseRuntime.has(tool, ForgedCurse.POWER_ERASURE)) {
                player.removeEffect(MobEffects.WITHER);
            } else {
                // Do not reset Wither's internal damage timer every player tick.
                // Refresh Wither I only when it is close to expiring, so vanilla Wither damage can tick normally.
                MobEffectInstance currentWither = player.getEffect(MobEffects.WITHER);
                if (currentWither == null || currentWither.getDuration() <= 20)
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0, false, false));
            }
        }

        EffectTier frenzy = ForgedEffectRuntime.tier(tool, ForgingEffect.FRENZY_DIGGING);
        if (frenzy != null) {
            long lastMine = player.getPersistentData().getLong("ForgedFrenzyLastMine");
            if (lastMine > 0L && now - lastMine > 100L) {
                player.getPersistentData().remove("ForgedFrenzyLastMine");
                player.getPersistentData().remove("ForgedFrenzyChain");
            }
        }
    }

    public static void applySlimeTrail(Player player, BlockPos floor, EffectTier tier) {
        if (tier == EffectTier.I) {
            replaceFloorWithSlime(player, floor);
        } else if (tier == EffectTier.II) {
            replaceFloorWithSlime(player, floor);
            replaceFloorWithSlime(player, floor.north());
            replaceFloorWithSlime(player, floor.south());
            replaceFloorWithSlime(player, floor.east());
            replaceFloorWithSlime(player, floor.west());
        } else {
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    replaceFloorWithSlime(player, floor.offset(x, 0, z));
        }
    }

    private static void replaceFloorWithSlime(Player player, BlockPos pos) {
        if (!player.level().getBlockState(pos).isAir())
            player.level().setBlockAndUpdate(pos, Blocks.SLIME_BLOCK.defaultBlockState());
    }
}
