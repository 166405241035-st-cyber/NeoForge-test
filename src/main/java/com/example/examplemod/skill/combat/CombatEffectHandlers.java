package com.example.examplemod.skill.combat;

import static com.example.examplemod.skill.passive.PassiveEffectSupport.canUseTimedTrigger;
import static com.example.examplemod.skill.passive.PassiveEffectSupport.tierValue;
import static com.example.examplemod.skill.passive.PlayerEffectTicker.applySlimeTrail;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedActiveSkills;
import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Combat and death effects; event subscription stays in ForgedEffectEvents. */
public final class CombatEffectHandlers {
    private CombatEffectHandlers() {}

    private static final double[] CRIPPLING_CHANCE = {0.10D, 0.18D, 0.25D};

    private static final double[] ZOMBIE_MINION_CHANCE = {0.15D, 0.30D, 0.60D};

    private static final double[] VAMPIRIC_CHANCE = {0.60D, 0.60D, 0.60D};

    private static final int[] LEVITATION_DURATION = {40, 80, 120};

    private static final double[] SPINE_SPIKE_CHANCE = {0.10D, 0.18D, 0.25D};

    private static final int[] GRAVE_GRASP_DURATION = {10, 20, 30}; // 0.5 / 1 / 1.5 sec

    private static final double[] RIFT_TELEPORT_CHANCE = {0.80D, 0.80D, 0.80D};

    private static final int[] WEB_TRAP_DURATION = {30, 50, 80}; // 1.5 / 2.5 / 4 sec

    private static final double[] UNSTOPPABLE_KNOCKBACK_POWER = {1.5D, 2.0D, 3.0D};

    private static final double[] SLIME_TRAIL_CHANCE = {0.15D, 0.25D, 0.40D};

    private static final float[] WITHER_DRAIN_HEAL = {1.0F, 2.0F, 3.0F};

    private static final double[] CRITICAL_BLAST_CHANCE = {0.15D, 0.25D, 0.40D};

    private static final double[] VELOCITY_STRIKE_MAX_BONUS = {0.30D, 0.60D, 1.00D};

    private static final int[] POISON_GAS_DURATION = {60, 100, 160}; // 3 / 5 / 8 sec

    private static final float[] COMBO_DETONATION_DAMAGE = {2.5F, 4.0F, 6.0F};

    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        // Aegis Shield only reduces Projectile and Explosive damage.
        // It does not reduce melee, magic, fall, fire, or other damage types.
        if (target instanceof Player protectedPlayer && !protectedPlayer.level().isClientSide()
                && ForgedActiveSkills.isAegisActive(protectedPlayer)
                && (event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                    || event.getSource().is(DamageTypeTags.IS_EXPLOSION))) {
            event.setAmount(event.getAmount() * 0.10F); // 90% reduction
        }

        // Iron Fortress Guard reduces all incoming damage by 90% while its timed guard is active.
        if (target instanceof Player guardedPlayer && !guardedPlayer.level().isClientSide()
                && guardedPlayer.getPersistentData().getLong("ForgedIronFortressUntil") > guardedPlayer.level().getGameTime()) {
            event.setAmount(event.getAmount() * 0.10F);
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        if (player.getPersistentData().getBoolean("ForgedEffectDamageGuard")) return;

        // Projectiles spawned by forged active skills (for example Fireball Shoot)
        // must not be treated as a fresh melee hit. Otherwise one R cast could
        // accidentally trigger Crippling, Combo, Web Trap, Double Trigger, etc.
        Entity direct = event.getSource().getDirectEntity();
        if (direct != null && direct.getPersistentData().getBoolean("ForgedSkillProjectile")) return;

        ItemStack weapon = player.getMainHandItem();

        // Remember whether THIS melee hit was fully charged. Death-triggered abilities
        // (notably Zombie Minion Calling) can then obey the same full-charge rule even
        // though the vanilla attack meter may already be reset by LivingDeathEvent.
        target.getPersistentData().putUUID("ForgedLastMeleeOwner", player.getUUID());
        target.getPersistentData().putBoolean("ForgedLastMeleeDoubleEligible",
                DoubleTriggerRuntime.isFullChargeAttack(player, weapon));

        EffectTier witherCurse = ForgedEffectRuntime.tier(weapon, ForgingEffect.WITHER_CURSE_POWER);
        if (witherCurse != null) {
            double multiplier = switch (witherCurse) {
                case I -> 1.5D;
                case II -> 2.0D;
                case III -> 3.0D;
            };
            event.setAmount((float)(event.getAmount() * multiplier));
            ForgedSkillSounds.play(player, ForgingEffect.WITHER_CURSE_POWER);
        }

        EffectTier gravitationalSlam = ForgedEffectRuntime.tier(weapon, ForgingEffect.GRAVATIONAL_SLAM);
        if (gravitationalSlam != null && isCriticalHit(player)
                && canUseTimedTrigger(player, "GravitationalSlam", 100L)) {
            double slamDamage = switch (gravitationalSlam) {
                case I -> 6.0D;
                case II -> 9.0D;
                case III -> 12.0D;
            };
            boolean doubled = DoubleTriggerRuntime.rollAttack(player, weapon);
            if (doubled) slamDamage *= 2.0D;
            double radius = 5.0D; // Fixed AoE; Tier only changes power.
            Vec3 center = target.position();
            AABB slamArea = new AABB(center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius);
            for (Monster mob : player.level().getEntitiesOfClass(Monster.class, slamArea,
                    e -> e.isAlive() && e.distanceToSqr(target) <= radius * radius)) {
                Vec3 pull = center.subtract(mob.position());
                if (pull.lengthSqr() > 0.01D) {
                    double pullPower = doubled ? 2.2D : 1.1D;
                    Vec3 velocity = pull.normalize().scale(pullPower);
                    mob.setDeltaMovement(velocity.x, Math.max(doubled ? 0.40D : 0.20D, velocity.y), velocity.z);
                    mob.hurtMarked = true;
                }
                if (mob != target) {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                    try {
                        mob.hurt(player.damageSources().playerAttack(player), (float) slamDamage);
                    } finally {
                        player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                    }
                }
            }
            int explosions = doubled ? 2 : 1;
            for (int i = 0; i < explosions; i++) {
                safeForgedExplosion(player, target.getX(), target.getY(), target.getZ(), 2.0F);
            }
            // Double Trigger is a free replay: durability is paid once.
            ForgedBlessingRuntime.damage(weapon, 3);
            ForgedSkillSounds.play(player, ForgingEffect.GRAVATIONAL_SLAM);
        }

        EffectTier crippling = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRIPPLING_STRIKE);
        if (crippling != null && player.getRandom().nextDouble() < tierValue(crippling, CRIPPLING_CHANCE)) {
            int duration = DoubleTriggerRuntime.rollAttack(player, weapon) ? 120 : 60;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
            ForgedSkillSounds.play(player, ForgingEffect.CRIPPLING_STRIKE);
        }

        EffectTier vampiric = ForgedEffectRuntime.tier(weapon, ForgingEffect.VAMPIRIC_VITALITY);
        if (vampiric != null && player.getRandom().nextDouble() < tierValue(vampiric, VAMPIRIC_CHANCE)) {
            // Instant healing: Tier I = 2 hearts, II = 3 hearts, III = 4 hearts.
            float heal = switch (vampiric) {
                case I -> 4.0F;
                case II -> 6.0F;
                case III -> 8.0F;
            };
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) heal *= 2.0F;
            player.heal(heal);
            ForgedSkillSounds.play(player, ForgingEffect.VAMPIRIC_VITALITY);
        }

        EffectTier levitation = ForgedEffectRuntime.tier(weapon, ForgingEffect.LEVITATION_BLOW);
        if (levitation != null && canUseTimedTrigger(player, "LevitationBlow", 40L)) {
            int duration = tierValue(levitation, LEVITATION_DURATION);
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) duration *= 2;
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0));
            ForgedSkillSounds.play(player, ForgingEffect.LEVITATION_BLOW);
        }

        EffectTier spineSpike = ForgedEffectRuntime.tier(weapon, ForgingEffect.SPINE_SPIKE);
        if (spineSpike != null && player.getRandom().nextDouble() < tierValue(spineSpike, SPINE_SPIKE_CHANCE)) {
            // Bleeding uses poison-like non-lethal damage-over-time, but is a separate red status.
            // Tier changes proc chance only.
            int duration = DoubleTriggerRuntime.rollAttack(player, weapon) ? 200 : 100;
            target.addEffect(new MobEffectInstance(ExampleMod.BLEEDING, duration, 0));
            ForgedSkillSounds.play(player, ForgingEffect.SPINE_SPIKE);
        }

        EffectTier graveGrasp = ForgedEffectRuntime.tier(weapon, ForgingEffect.GRAVE_GRASP);
        if (graveGrasp != null && isCriticalHit(player)) {
            int duration = tierValue(graveGrasp, GRAVE_GRASP_DURATION);
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) duration *= 2;
            // Stun: stop movement and suppress movement/jump during the short stun window.
            target.setDeltaMovement(Vec3.ZERO);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 255));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 128));
            ForgedSkillSounds.play(player, ForgingEffect.GRAVE_GRASP);
        }

        EffectTier riftTeleport = ForgedEffectRuntime.tier(weapon, ForgingEffect.RIFT_TELEPORT_ATTACK);
        if (riftTeleport != null && player.getRandom().nextDouble() < tierValue(riftTeleport, RIFT_TELEPORT_CHANCE)) {
            double distance = switch (riftTeleport) { case I -> 4.0D; case II -> 8.0D; case III -> 15.0D; };
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) distance *= 2.0D;
            teleportTargetAway(player, target, distance);
            ForgedSkillSounds.play(player, ForgingEffect.RIFT_TELEPORT_ATTACK);
        }

        EffectTier webTrap = ForgedEffectRuntime.tier(weapon, ForgingEffect.WEB_TRAP);
        if (webTrap != null && canUseTimedTrigger(player, "WebTrap", 60L)) {
            // Real cobweb restraint: movement is slowed by the block itself, but the mob keeps its AI
            // and can still attack the player when in reach.
            BlockPos webPos = target.blockPosition();
            if (player.level().getBlockState(webPos).canBeReplaced()) {
                player.level().setBlockAndUpdate(webPos, Blocks.COBWEB.defaultBlockState());
                int webDuration = tierValue(webTrap, WEB_TRAP_DURATION);
                if (DoubleTriggerRuntime.rollAttack(player, weapon)) webDuration *= 2;
                target.getPersistentData().putLong("ForgedWebTrapUntil",
                        player.level().getGameTime() + webDuration);
                target.getPersistentData().putInt("ForgedWebTrapX", webPos.getX());
                target.getPersistentData().putInt("ForgedWebTrapY", webPos.getY());
                target.getPersistentData().putInt("ForgedWebTrapZ", webPos.getZ());
                ForgedSkillSounds.play(player, ForgingEffect.WEB_TRAP);
            }
        }

        EffectTier knockback = ForgedEffectRuntime.tier(weapon, ForgingEffect.UNSTOPPABLE_KNOCKBACK);
        if (knockback != null) {
            // Direct velocity is used so the forged effect is not reduced by vanilla knockback resistance.
            Vec3 away = target.position().subtract(player.position());
            if (away.lengthSqr() < 0.001D) away = player.getLookAngle();
            double knockbackPower = tierValue(knockback, UNSTOPPABLE_KNOCKBACK_POWER);
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) knockbackPower *= 2.0D;
            away = away.normalize().scale(knockbackPower);
            target.setDeltaMovement(target.getDeltaMovement().add(away.x, 0.25D, away.z));
            target.hurtMarked = true;
            ForgedSkillSounds.play(player, ForgingEffect.UNSTOPPABLE_KNOCKBACK);
        }

        EffectTier slimeTrail = ForgedEffectRuntime.tier(weapon, ForgingEffect.SLIME_TRAIL_STRIKE);
        if (slimeTrail != null && player.getRandom().nextDouble() < tierValue(slimeTrail, SLIME_TRAIL_CHANCE)) {
            BlockPos floor = target.blockPosition().below();
            applySlimeTrail(player, floor, slimeTrail);
            ForgedSkillSounds.play(player, ForgingEffect.SLIME_TRAIL_STRIKE);
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) {
                // A second patch is shifted forward so the bonus result is visible
                // instead of trying to replace the exact same blocks twice.
                applySlimeTrail(player, floor.relative(player.getDirection(), 2), slimeTrail);
            }
        }

        EffectTier witherDrain = ForgedEffectRuntime.tier(weapon, ForgingEffect.WITHER_DRAIN);
        if (witherDrain != null && canUseTimedTrigger(player, "WitherDrain", 20L)) {
            // Fixed Wither; Tier changes only the amount of health stolen.
            boolean doubled = DoubleTriggerRuntime.rollAttack(player, weapon);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, doubled ? 160 : 80, 0));
            float heal = tierValue(witherDrain, WITHER_DRAIN_HEAL);
            player.heal(doubled ? heal * 2.0F : heal);
            ForgedSkillSounds.play(player, ForgingEffect.WITHER_DRAIN);
        }

        EffectTier comboDetonation = ForgedEffectRuntime.tier(weapon, ForgingEffect.COMBO_DETONATION);
        if (comboDetonation != null) {
            String targetKey = "ForgedComboTarget";
            String countKey = "ForgedComboCount";
            String currentTarget = target.getUUID().toString();
            String previousTarget = player.getPersistentData().getString(targetKey);
            int combo = currentTarget.equals(previousTarget)
                    ? player.getPersistentData().getInt(countKey) + 1 : 1;
            player.getPersistentData().putString(targetKey, currentTarget);
            player.getPersistentData().putInt(countKey, combo);

            if (combo >= 3) {
                player.getPersistentData().putInt(countKey, 0);
                float comboDamage = tierValue(comboDetonation, COMBO_DETONATION_DAMAGE);
                boolean doubled = DoubleTriggerRuntime.rollAttack(player, weapon);
                int activations = doubled ? 2 : 1;

                // Double Trigger replays the actual Combo Detonation result instead of
                // merely multiplying one number: each activation deals its bonus hit
                // and creates its own non-block-breaking explosion.
                // Apply the bonus hit as one combined damage packet. Two immediate
                // hurt() calls can be swallowed by Minecraft's hurt-resistance window,
                // which would make Double Trigger look like it failed.
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    target.hurt(player.damageSources().playerAttack(player), comboDamage * activations);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }
                for (int i = 0; i < activations; i++) {
                    safeForgedExplosion(player, target.getX(), target.getY(), target.getZ(), 1.25F);
                }
                ForgedSkillSounds.play(player, ForgingEffect.COMBO_DETONATION);
            }
        }

        EffectTier criticalBlast = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRITICAL_BLAST);
        if (criticalBlast != null && isCriticalHit(player)
                && player.getRandom().nextDouble() < tierValue(criticalBlast, CRITICAL_BLAST_CHANCE)) {
            // Small non-block-breaking blast so the proc does not destroy terrain.
            int blasts = DoubleTriggerRuntime.rollAttack(player, weapon) ? 2 : 1;
            for (int i = 0; i < blasts; i++) {
                safeForgedExplosion(player, target.getX(), target.getY(), target.getZ(), 1.5F);
            }
            ForgedSkillSounds.play(player, ForgingEffect.CRITICAL_BLAST);
        }

        EffectTier poisonGas = ForgedEffectRuntime.tier(weapon, ForgingEffect.POISON_GAS_CLOUD);
        if (poisonGas != null && canUseTimedTrigger(player, "PoisonGasCloud", 100L)) {
            int duration = tierValue(poisonGas, POISON_GAS_DURATION);
            if (DoubleTriggerRuntime.rollAttack(player, weapon)) duration *= 2;

            // Real lingering-style cloud. Radius is fixed at 5 blocks for every Tier;
            // Tier changes only how long the cloud remains.
            AreaEffectCloud cloud = new AreaEffectCloud(player.level(), target.getX(), target.getY(), target.getZ());
            cloud.setOwner(player);
            cloud.setRadius(5.0F);
            cloud.setDuration(duration);
            cloud.setWaitTime(0);
            cloud.setRadiusPerTick(0.0F);
            cloud.setRadiusOnUse(0.0F);
            cloud.setPotionContents(new PotionContents(java.util.Optional.empty(),
                    java.util.Optional.of(0x4E9331),
                    java.util.List.of(new MobEffectInstance(MobEffects.POISON, 40, 0))));
            cloud.getPersistentData().putUUID("ForgedPoisonGasOwner", player.getUUID());
            player.level().addFreshEntity(cloud);
            ForgedSkillSounds.play(player, ForgingEffect.POISON_GAS_CLOUD);
        }

        EffectTier velocityStrike = ForgedEffectRuntime.tier(weapon, ForgingEffect.VELOCITY_STRIKE);
        if (velocityStrike != null) {
            double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
            double speedFactor = Math.min(1.0D, horizontalSpeed / 0.20D);
            float bonus = (float)(event.getAmount() * tierValue(velocityStrike, VELOCITY_STRIKE_MAX_BONUS) * speedFactor);
            if (bonus > 0.0F) {
                event.setAmount(event.getAmount() + bonus);
                ForgedSkillSounds.play(player, ForgingEffect.VELOCITY_STRIKE);
            }
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || !(player.level() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Monster)) return;

        ItemStack weapon = player.getMainHandItem();
        EffectTier tier = ForgedEffectRuntime.tier(weapon, ForgingEffect.ZOMBIE_MINION_CALLING);
        if (tier == null || player.getRandom().nextDouble() >= tierValue(tier, ZOMBIE_MINION_CHANCE)) return;

        boolean fullCharge = event.getEntity().getPersistentData().hasUUID("ForgedLastMeleeOwner")
                && event.getEntity().getPersistentData().getUUID("ForgedLastMeleeOwner").equals(player.getUUID())
                && event.getEntity().getPersistentData().getBoolean("ForgedLastMeleeDoubleEligible");
        int minionCount = fullCharge && DoubleTriggerRuntime.has(weapon)
                && player.getRandom().nextDouble() < DoubleTriggerRuntime.CHANCE ? 2 : 1;

        for (int i = 0; i < minionCount; i++) {
            Zombie minion = new Zombie(level);
            minion.moveTo(event.getEntity().getX() + i * 0.6D, event.getEntity().getY(),
                    event.getEntity().getZ(), player.getYRot(), 0.0F);
            minion.setCustomName(net.minecraft.network.chat.Component.literal("Zombie Minion"));
            minion.setCustomNameVisible(true);
            minion.setPersistenceRequired();
            minion.getPersistentData().putUUID("ForgingMinionOwner", player.getUUID());
            level.addFreshEntity(minion);
        }
        ForgedSkillSounds.play(player, ForgingEffect.ZOMBIE_MINION_CALLING);
    }

    private static boolean isCriticalHit(Player player) {
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isPassenger();
    }

    /**
     * Forged explosions can damage several entities and fire LivingIncomingDamageEvent again.
     * Guard the entire explosion so Combo/Critical/other hit effects cannot recursively
     * trigger themselves from their own bonus explosion.
     */
    private static void safeForgedExplosion(Player player, double x, double y, double z, float radius) {
        boolean previous = player.getPersistentData().getBoolean("ForgedEffectDamageGuard");
        player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
        try {
            player.level().explode(player, x, y, z, radius,
                    net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        } finally {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", previous);
        }
    }

    private static void teleportTargetAway(Player player, LivingEntity target, double distance) {
        Vec3 away = target.position().subtract(player.position());
        if (away.lengthSqr() < 0.001D) away = player.getLookAngle().scale(-1.0D);
        away = away.normalize();

        // Tier controls the exact displacement distance: 4 / 8 / 15 blocks.
        Vec3 destination = target.position().add(away.scale(distance));
        target.teleportTo(destination.x, destination.y, destination.z);
    }
}
