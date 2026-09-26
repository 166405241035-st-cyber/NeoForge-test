package com.example.examplemod.skill;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

/** Server-side entry point for forged equipment effects. */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgedEffectEvents {
    private ForgedEffectEvents() {}

    private static final double[] CRIPPLING_CHANCE = {0.10D, 0.18D, 0.25D};
    private static final double[] ZOMBIE_MINION_CHANCE = {0.15D, 0.30D, 0.60D};
    private static final double[] BONE_DUST_CHANCE = {0.10D, 0.20D, 0.30D};
    private static final double[] VAMPIRIC_CHANCE = {0.60D, 0.60D, 0.60D};
    private static final int[] LEVITATION_DURATION = {40, 80, 120};
    private static final double[] SOUL_SAND_CHANCE = {0.10D, 0.20D, 0.35D};
    private static final double[] SCAVENGER_CHANCE = {0.05D, 0.10D, 0.15D};
    private static final double[] UNREFINED_ORE_CHANCE = {0.04D, 0.08D, 0.12D};

    // Combat batch.
    private static final double[] SPINE_SPIKE_CHANCE = {0.10D, 0.18D, 0.25D};
    private static final int[] GRAVE_GRASP_DURATION = {10, 20, 30}; // 0.5 / 1 / 1.5 sec
    private static final double[] RIFT_TELEPORT_CHANCE = {0.80D, 0.80D, 0.80D};

    // Additional combat effects from the project skill list (pages 16-19).
    private static final int[] WEB_TRAP_DURATION = {30, 50, 80}; // 1.5 / 2.5 / 4 sec
    private static final double[] UNSTOPPABLE_KNOCKBACK_POWER = {1.5D, 2.0D, 3.0D};
    private static final double[] SLIME_TRAIL_CHANCE = {0.15D, 0.25D, 0.40D};
    private static final float[] WITHER_DRAIN_HEAL = {1.0F, 2.0F, 3.0F};
    private static final double[] CRITICAL_BLAST_CHANCE = {0.15D, 0.25D, 0.40D};
    private static final double[] VELOCITY_STRIKE_MAX_BONUS = {0.30D, 0.60D, 1.00D};
    private static final double[] AIRBORNE_MINING_SPEED = {0.25D, 0.45D, 0.70D};
    private static final int[] SELF_REPAIR_AMOUNT = {2, 5, 10};
    private static final double[] HEALING_HARVEST_CHANCE = {0.05D, 0.10D, 0.18D};
    private static final double[] ROTTEN_COMPOST_CHANCE = {0.15D, 0.25D, 0.35D};
    private static final double[] ORGANIC_CATALYST_COOLDOWN = {200.0D, 140.0D, 100.0D};
    private static final double[] NETHER_MUTATION_CHANCE = {0.05D, 0.10D, 0.20D};
    private static final int[] POISON_GAS_DURATION = {60, 100, 160}; // 3 / 5 / 8 sec
    private static final float[] EARTHY_SHOCKWAVE_DAMAGE = {3.0F, 5.0F, 7.0F};
    private static final float[] COMBO_DETONATION_DAMAGE = {2.5F, 4.0F, 6.0F};

    @SubscribeEvent
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

        ItemStack weapon = player.getMainHandItem();

        EffectTier witherCurse = ForgedEffectRuntime.tier(weapon, ForgingEffect.WITHER_CURSE_POWER);
        if (witherCurse != null) {
            double multiplier = switch (witherCurse) {
                case I -> 1.5D;
                case II -> 2.0D;
                case III -> 3.0D;
            };
            event.setAmount((float)(event.getAmount() * multiplier));
        }

        EffectTier gravitationalSlam = ForgedEffectRuntime.tier(weapon, ForgingEffect.GRAVATIONAL_SLAM);
        if (gravitationalSlam != null && isCriticalHit(player)
                && canUseTimedTrigger(player, "GravitationalSlam", 100L)) {
            double slamDamage = switch (gravitationalSlam) {
                case I -> 6.0D;
                case II -> 9.0D;
                case III -> 12.0D;
            };
            double radius = 5.0D; // Fixed AoE; Tier only changes power.
            Vec3 center = target.position();
            AABB slamArea = new AABB(center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius);
            for (Monster mob : player.level().getEntitiesOfClass(Monster.class, slamArea,
                    e -> e.isAlive() && e.distanceToSqr(target) <= radius * radius)) {
                Vec3 pull = center.subtract(mob.position());
                if (pull.lengthSqr() > 0.01D) {
                    Vec3 velocity = pull.normalize().scale(1.1D);
                    mob.setDeltaMovement(velocity.x, Math.max(0.20D, velocity.y), velocity.z);
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
            player.level().explode(player, target.getX(), target.getY(), target.getZ(),
                    2.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
            weapon.setDamageValue(Math.min(weapon.getMaxDamage(), weapon.getDamageValue() + 3));
        }

        EffectTier crippling = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRIPPLING_STRIKE);
        if (crippling != null && player.getRandom().nextDouble() < tierValue(crippling, CRIPPLING_CHANCE))
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        EffectTier vampiric = ForgedEffectRuntime.tier(weapon, ForgingEffect.VAMPIRIC_VITALITY);
        if (vampiric != null && player.getRandom().nextDouble() < tierValue(vampiric, VAMPIRIC_CHANCE)) {
            // Instant healing: Tier I = 2 hearts, II = 3 hearts, III = 4 hearts.
            player.heal(switch (vampiric) {
                case I -> 4.0F;
                case II -> 6.0F;
                case III -> 8.0F;
            });
        }

        EffectTier levitation = ForgedEffectRuntime.tier(weapon, ForgingEffect.LEVITATION_BLOW);
        if (levitation != null && canUseTimedTrigger(player, "LevitationBlow", 40L))
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, tierValue(levitation, LEVITATION_DURATION), 0));

        EffectTier spineSpike = ForgedEffectRuntime.tier(weapon, ForgingEffect.SPINE_SPIKE);
        if (spineSpike != null && player.getRandom().nextDouble() < tierValue(spineSpike, SPINE_SPIKE_CHANCE)) {
            // Bleeding uses poison-like non-lethal damage-over-time, but is a separate red status.
            // Tier changes proc chance only.
            target.addEffect(new MobEffectInstance(ExampleMod.BLEEDING, 100, 0));
        }

        EffectTier graveGrasp = ForgedEffectRuntime.tier(weapon, ForgingEffect.GRAVE_GRASP);
        if (graveGrasp != null && isCriticalHit(player)) {
            int duration = tierValue(graveGrasp, GRAVE_GRASP_DURATION);
            // Stun: stop movement and suppress movement/jump during the short stun window.
            target.setDeltaMovement(Vec3.ZERO);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 255));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 128));
        }

        EffectTier riftTeleport = ForgedEffectRuntime.tier(weapon, ForgingEffect.RIFT_TELEPORT_ATTACK);
        if (riftTeleport != null && player.getRandom().nextDouble() < tierValue(riftTeleport, RIFT_TELEPORT_CHANCE)) {
            teleportTargetAway(player, target, switch (riftTeleport) { case I -> 4.0D; case II -> 8.0D; case III -> 15.0D; });
        }

        EffectTier webTrap = ForgedEffectRuntime.tier(weapon, ForgingEffect.WEB_TRAP);
        if (webTrap != null && canUseTimedTrigger(player, "WebTrap", 60L)) {
            // Real cobweb restraint: movement is slowed by the block itself, but the mob keeps its AI
            // and can still attack the player when in reach.
            BlockPos webPos = target.blockPosition();
            if (player.level().getBlockState(webPos).canBeReplaced()) {
                player.level().setBlockAndUpdate(webPos, Blocks.COBWEB.defaultBlockState());
                target.getPersistentData().putLong("ForgedWebTrapUntil",
                        player.level().getGameTime() + tierValue(webTrap, WEB_TRAP_DURATION));
                target.getPersistentData().putInt("ForgedWebTrapX", webPos.getX());
                target.getPersistentData().putInt("ForgedWebTrapY", webPos.getY());
                target.getPersistentData().putInt("ForgedWebTrapZ", webPos.getZ());
            }
        }

        EffectTier knockback = ForgedEffectRuntime.tier(weapon, ForgingEffect.UNSTOPPABLE_KNOCKBACK);
        if (knockback != null) {
            // Direct velocity is used so the forged effect is not reduced by vanilla knockback resistance.
            Vec3 away = target.position().subtract(player.position());
            if (away.lengthSqr() < 0.001D) away = player.getLookAngle();
            away = away.normalize().scale(tierValue(knockback, UNSTOPPABLE_KNOCKBACK_POWER));
            target.setDeltaMovement(target.getDeltaMovement().add(away.x, 0.25D, away.z));
            target.hurtMarked = true;
        }

        EffectTier slimeTrail = ForgedEffectRuntime.tier(weapon, ForgingEffect.SLIME_TRAIL_STRIKE);
        if (slimeTrail != null && player.getRandom().nextDouble() < tierValue(slimeTrail, SLIME_TRAIL_CHANCE)) {
            BlockPos floor = target.blockPosition().below();
            if (slimeTrail == EffectTier.I) {
                replaceFloorWithSlime(player, floor);
            } else if (slimeTrail == EffectTier.II) {
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

        EffectTier witherDrain = ForgedEffectRuntime.tier(weapon, ForgingEffect.WITHER_DRAIN);
        if (witherDrain != null && canUseTimedTrigger(player, "WitherDrain", 20L)) {
            // Fixed Wither; Tier changes only the amount of health stolen.
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            player.heal(tierValue(witherDrain, WITHER_DRAIN_HEAL));
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
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    target.hurt(player.damageSources().playerAttack(player), comboDamage);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }
                player.level().explode(player, target.getX(), target.getY(), target.getZ(),
                        1.25F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
            }
        }

        EffectTier criticalBlast = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRITICAL_BLAST);
        if (criticalBlast != null && isCriticalHit(player)
                && player.getRandom().nextDouble() < tierValue(criticalBlast, CRITICAL_BLAST_CHANCE)) {
            // Small non-block-breaking blast so the proc does not destroy terrain.
            player.level().explode(player, target.getX(), target.getY(), target.getZ(),
                    1.5F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }

        EffectTier poisonGas = ForgedEffectRuntime.tier(weapon, ForgingEffect.POISON_GAS_CLOUD);
        if (poisonGas != null && canUseTimedTrigger(player, "PoisonGasCloud", 100L)) {
            int duration = tierValue(poisonGas, POISON_GAS_DURATION);

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
        }

        EffectTier velocityStrike = ForgedEffectRuntime.tier(weapon, ForgingEffect.VELOCITY_STRIKE);
        if (velocityStrike != null) {
            double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
            double speedFactor = Math.min(1.0D, horizontalSpeed / 0.20D);
            float bonus = (float)(event.getAmount() * tierValue(velocityStrike, VELOCITY_STRIKE_MAX_BONUS) * speedFactor);
            if (bonus > 0.0F) event.setAmount(event.getAmount() + bonus);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();
        long now = player.level().getGameTime();

        // Refresh every Moisture Retain plot from world saved data. Do it once per
        // second; this is far faster than vanilla farmland can visibly dry out.
        if (player.level() instanceof ServerLevel moistureLevel && now % 20L == 0L) {
            ForgedMoistureData.get(moistureLevel).refresh(moistureLevel);
        }

        // Keep the stamped Moisture Retain plot fully hydrated after the immediate tilling trigger.
        long moisturePos = player.getPersistentData().getLong("ForgedPermanentMoisturePos");
        if (moisturePos != 0L) {
            BlockPos pos = BlockPos.of(moisturePos);
            net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(pos);
            if (state.is(Blocks.FARMLAND)
                    && state.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE) != 7) {
                player.level().setBlockAndUpdate(pos,
                        state.setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
            }
        }

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
            float damage = tierValue(shockwave, EARTHY_SHOCKWAVE_DAMAGE);
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
                    mob.setDeltaMovement(mob.getDeltaMovement().add(away.x * 0.85D, 0.42D, away.z * 0.85D));
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
                tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + 2));
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
            tool.setDamageValue(Math.max(0, tool.getDamageValue() - repair));
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
            // Do not reset Wither's internal damage timer every player tick.
            // Refresh Wither I only when it is close to expiring, so vanilla Wither damage can tick normally.
            MobEffectInstance currentWither = player.getEffect(MobEffects.WITHER);
            if (currentWither == null || currentWither.getDuration() <= 20)
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0, false, false));
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

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        float speed = event.getNewSpeed();

        EffectTier airborne = ForgedEffectRuntime.tier(tool, ForgingEffect.AIRBORNE_MINING);
        if (airborne != null && !player.onGround()) {
            double bonus = tierValue(airborne, AIRBORNE_MINING_SPEED);
            speed = (float)(speed * 5.0D * (1.0D + bonus));
        }

        EffectTier frenzy = ForgedEffectRuntime.tier(tool, ForgingEffect.FRENZY_DIGGING);
        if (frenzy != null
                && player.getPersistentData().getInt("ForgedFrenzyChain") >= 5
                && player.level().getGameTime() - player.getPersistentData().getLong("ForgedFrenzyLastMine") <= 100L) {
            double bonus = switch (frenzy) {
                case I -> 0.15D;
                case II -> 0.30D;
                case III -> 0.50D;
            };
            speed = (float)(speed * (1.0D + bonus));
        }
        event.setNewSpeed(speed);
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) return;
        ItemStack tool = event.getTool();

        // Ultimate Laser Breaker behaves as Fortune V for normal mining.
        // Rebuild this block's vanilla loot with a temporary Fortune V copy of the forged tool,
        // so every block uses Minecraft's own Fortune loot table instead of our old 0..5 bonus.
        EffectTier ultimate = ForgedEffectRuntime.tier(tool, ForgingEffect.ULTIMATE_LASER_BREAKER);
        if (ultimate != null && event.getLevel() instanceof ServerLevel serverLevel
                && !event.getState().is(Blocks.BEDROCK)) {
            ItemStack fortuneTool = tool.copy();
            Holder<Enchantment> fortune = serverLevel.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.FORTUNE);
            fortuneTool.enchant(fortune, 5);

            java.util.List<ItemStack> fortuneDrops = Block.getDrops(
                    event.getState(), serverLevel, event.getPos(), event.getBlockEntity(), player, fortuneTool);
            event.getDrops().clear();
            for (ItemStack dropStack : fortuneDrops) {
                if (!dropStack.isEmpty()) {
                    BlockPos pos = event.getPos();
                    event.getDrops().add(new ItemEntity(serverLevel,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, dropStack));
                }
            }
        }

        EffectTier autoSmelt = ForgedEffectRuntime.tier(tool, ForgingEffect.AUTO_SMELT_MINING);
        if (autoSmelt != null) {
            Item smelted = null;
            if (event.getState().is(Blocks.IRON_ORE) || event.getState().is(Blocks.DEEPSLATE_IRON_ORE)) smelted = Items.IRON_INGOT;
            else if (event.getState().is(Blocks.GOLD_ORE) || event.getState().is(Blocks.DEEPSLATE_GOLD_ORE)
                    || event.getState().is(Blocks.NETHER_GOLD_ORE)) smelted = Items.GOLD_INGOT;
            else if (event.getState().is(Blocks.COPPER_ORE) || event.getState().is(Blocks.DEEPSLATE_COPPER_ORE)) smelted = Items.COPPER_INGOT;

            if (smelted != null) {
                int output = switch (autoSmelt) {
                    case I -> 1;
                    case II -> 1 + (player.getRandom().nextBoolean() ? 1 : 0);
                    case III -> 2;
                };

                event.getDrops().clear();
                BlockPos pos = event.getPos();
                event.getDrops().add(new ItemEntity(event.getLevel(),
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        new ItemStack(smelted, output)));
            }
        }

        // Static Hover Drop: freeze only the item entities produced by THIS block.
        // Tier controls hover duration only: I = 5s, II = 10s, III = 20s.
        EffectTier staticHover = ForgedEffectRuntime.tier(tool, ForgingEffect.STATIC_HOVER_DROP);
        if (staticHover != null && !event.getDrops().isEmpty()) {
            int duration = switch (staticHover) { case I -> 100; case II -> 200; case III -> 400; };
            long hoverUntil = player.level().getGameTime() + duration;
            for (ItemEntity drop : event.getDrops()) {
                drop.setNoGravity(true);
                drop.setDeltaMovement(Vec3.ZERO);
                drop.getPersistentData().putLong("ForgedStaticHoverUntil", hoverUntil);
            }
        }

        // BlockDropsEvent already contains the drops from THIS block. Moving these
        // entities here makes Void Vacuum work on the same mining action instead of
        // waiting until the next block break.
        EffectTier vacuum = ForgedEffectRuntime.tier(tool, ForgingEffect.VOID_VACUUM_PICK);
        if (vacuum != null && !event.getDrops().isEmpty()) {
            for (ItemEntity drop : event.getDrops()) {
                drop.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
                drop.setDeltaMovement(Vec3.ZERO);
            }

            if (tool.isDamageableItem()) {
                int cost = switch (vacuum) { case I -> 5; case II -> 3; case III -> 1; };
                tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + cost));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldData = event.getOriginal().getPersistentData();
        if (oldData.contains("forgedPocketDimension", net.minecraft.nbt.Tag.TAG_LIST)) {
            event.getEntity().getPersistentData().put(
                    "forgedPocketDimension",
                    oldData.getList("forgedPocketDimension", net.minecraft.nbt.Tag.TAG_COMPOUND).copy());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || !(player.level() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Monster)) return;

        ItemStack weapon = player.getMainHandItem();
        EffectTier tier = ForgedEffectRuntime.tier(weapon, ForgingEffect.ZOMBIE_MINION_CALLING);
        if (tier == null || player.getRandom().nextDouble() >= tierValue(tier, ZOMBIE_MINION_CHANCE)) return;

        Zombie minion = new Zombie(level);
        minion.moveTo(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), player.getYRot(), 0.0F);
        minion.setCustomName(net.minecraft.network.chat.Component.literal("Zombie Minion"));
        minion.setCustomNameVisible(true);
        minion.setPersistenceRequired();
        minion.getPersistentData().putUUID("ForgingMinionOwner", player.getUUID());
        level.addFreshEntity(minion);
    }

    @SubscribeEvent
    public static void onUltimateBedrockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        if (ForgedEffectRuntime.tier(tool, ForgingEffect.ULTIMATE_LASER_BREAKER) == null) return;

        BlockPos pos = event.getPos();
        if (!player.level().getBlockState(pos).is(Blocks.BEDROCK)) return;

        // Vanilla never reaches BreakEvent for Bedrock in Survival because hardness is -1.
        // Ultimate therefore handles the mining action at left-click, removes the block,
        // and explicitly drops the collectible Bedrock item.
        event.setCanceled(true);
        player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        Block.popResource(player.level(), pos, new ItemStack(Blocks.BEDROCK));

        if (!player.getAbilities().instabuild && tool.isDamageableItem()) {
            tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + 1));
        }
    }

    @SubscribeEvent
    public static void onUltimateBedrockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        ItemStack tool = player.getMainHandItem();
        if (ForgedEffectRuntime.tier(tool, ForgingEffect.ULTIMATE_LASER_BREAKER) == null) return;
        if (!event.getState().is(Blocks.BEDROCK)) return;

        // Bedrock normally has destroy speed -1 and no loot table. Ultimate normal mining
        // explicitly turns it into a collectible Bedrock item. The R laser still skips it.
        event.setCanceled(true);
        if (!player.level().isClientSide()) {
            player.level().setBlockAndUpdate(event.getPos(), Blocks.AIR.defaultBlockState());
            Block.popResource(player.level(), event.getPos(), new ItemStack(Blocks.BEDROCK));
            if (!player.getAbilities().instabuild && tool.isDamageableItem()) {
                tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + 1));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();

        // Remember the latest mined block for the active Magnetic Clumping skill.
        player.getPersistentData().putInt("ForgedLastMinedX", event.getPos().getX());
        player.getPersistentData().putInt("ForgedLastMinedY", event.getPos().getY());
        player.getPersistentData().putInt("ForgedLastMinedZ", event.getPos().getZ());

        if (ForgedEffectRuntime.tier(tool, ForgingEffect.FRENZY_DIGGING) != null) {
            long now = player.level().getGameTime();
            long last = player.getPersistentData().getLong("ForgedFrenzyLastMine");
            int chain = (last > 0L && now - last <= 100L)
                    ? player.getPersistentData().getInt("ForgedFrenzyChain") + 1 : 1;
            player.getPersistentData().putInt("ForgedFrenzyChain", Math.min(chain, 5));
            player.getPersistentData().putLong("ForgedFrenzyLastMine", now);
        }

        // Multi-block mining skills. Generated block breaks are guarded so they do not
        // recursively trigger another forged mining skill.
        if (!player.getPersistentData().getBoolean("ForgedMultiBreakGuard")) {
            Direction face = directionFromLook(player);

            // Rough Cleave, Tunnel Charge, Linear Blast and Wide Excavation are active R skills.
            // Their old automatic-on-break triggers were removed to prevent double activation.

        }

        EffectTier staticHover = ForgedEffectRuntime.tier(tool, ForgingEffect.STATIC_HOVER_DROP);
        if (staticHover != null) {
            int duration = switch (staticHover) { case I -> 100; case II -> 200; case III -> 400; };
            player.getPersistentData().putLong("ForgedStaticHoverUntil", player.level().getGameTime() + duration);
            player.getPersistentData().putLong("ForgedStaticHoverX", event.getPos().getX());
            player.getPersistentData().putLong("ForgedStaticHoverY", event.getPos().getY());
            player.getPersistentData().putLong("ForgedStaticHoverZ", event.getPos().getZ());
        }


        // Bone Dust Extract: each successfully mined block can create one bonus Bone Meal.
        EffectTier boneDust = ForgedEffectRuntime.tier(tool, ForgingEffect.BONE_DUST_EXTRACT);
        if (boneDust != null && player.getRandom().nextDouble() < tierValue(boneDust, BONE_DUST_CHANCE)) {
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.BONE_MEAL));
        }

        // Soul Sand Extraction: each successfully mined block can create one bonus Soul Sand.
        EffectTier soulSand = ForgedEffectRuntime.tier(tool, ForgingEffect.SOUL_SAND_EXTRACTION);
        if (soulSand != null && player.getRandom().nextDouble() < tierValue(soulSand, SOUL_SAND_CHANCE)) {
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.SOUL_SAND));
        }

        EffectTier scavenger = ForgedEffectRuntime.tier(tool, ForgingEffect.SCAVENGER_DIG);
        if (scavenger != null && player.getRandom().nextDouble() < tierValue(scavenger, SCAVENGER_CHANCE)) {
            Item bonus = switch (player.getRandom().nextInt(4)) {
                case 0 -> Items.BONE;
                case 1 -> Items.ROTTEN_FLESH;
                case 2 -> Items.IRON_NUGGET;
                default -> Items.GOLD_NUGGET;
            };
            Block.popResource(player.level(), event.getPos(), new ItemStack(bonus));
        }

        // Unrefined Ore Discovery: bonus ore comes out as nuggets, not raw ore.
        EffectTier unrefined = ForgedEffectRuntime.tier(tool, ForgingEffect.UNREFINED_ORE_DISCOVERY);
        if (unrefined != null && player.getRandom().nextDouble() < tierValue(unrefined, UNREFINED_ORE_CHANCE)) {
            Item nugget = player.getRandom().nextBoolean() ? Items.IRON_NUGGET : Items.GOLD_NUGGET;
            Block.popResource(player.level(), event.getPos(), new ItemStack(nugget));
        }

        EffectTier autoChest = ForgedEffectRuntime.tier(tool, ForgingEffect.AUTO_CHEST_TRANSPORT);
        if (autoChest != null && isCrop(event.getState()) && player.level() instanceof ServerLevel serverLevel) {
            int range = switch (autoChest) { case I -> 8; case II -> 16; case III -> 32; };
            net.minecraft.world.Container destination = null;
            double best = Double.MAX_VALUE;
            BlockPos center = event.getPos();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -4, -range), center.offset(range, 4, range))) {
                net.minecraft.world.level.block.entity.BlockEntity be = serverLevel.getBlockEntity(pos);
                if (be instanceof net.minecraft.world.Container container) {
                    double dist = pos.distSqr(center);
                    if (dist < best) { best = dist; destination = container; }
                }
            }
            if (destination != null) {
                for (ItemEntity drop : serverLevel.getEntitiesOfClass(ItemEntity.class, new AABB(center).inflate(3.0D))) {
                    ItemStack stack = drop.getItem();
                    for (int slot = 0; slot < destination.getContainerSize() && !stack.isEmpty(); slot++) {
                        ItemStack existing = destination.getItem(slot);
                        if (existing.isEmpty()) {
                            destination.setItem(slot, stack.copy());
                            stack.setCount(0);
                        } else if (ItemStack.isSameItemSameComponents(existing, stack)
                                && existing.getCount() < existing.getMaxStackSize()) {
                            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                            existing.grow(move); stack.shrink(move);
                            destination.setItem(slot, existing);
                        }
                    }
                    if (stack.isEmpty()) drop.discard(); else drop.setItem(stack);
                }
                if (tool.isDamageableItem())
                    tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + 1));
            }
        }

        EffectTier natureBless = ForgedEffectRuntime.tier(tool, ForgingEffect.NATURE_GOD_BLESS);
        if (natureBless != null && isCrop(event.getState())) {
            double rewardChance = switch (natureBless) {
                case I -> 0.05D;
                case II -> 0.10D;
                case III -> 0.20D;
            };
            if (player.getRandom().nextDouble() < rewardChance) {
                ItemStack reward = new ItemStack(player.getRandom().nextDouble() < 0.10D
                        ? Items.ENCHANTED_GOLDEN_APPLE : Items.GOLDEN_APPLE);
                Block.popResource(player.level(), event.getPos(), reward);
            }
        }

        EffectTier healingHarvest = ForgedEffectRuntime.tier(tool, ForgingEffect.HEALING_HARVEST);
        if (healingHarvest != null && isCrop(event.getState())
                && player.getRandom().nextDouble() < tierValue(healingHarvest, HEALING_HARVEST_CHANCE)) {
            Block.popResource(player.level(), event.getPos(), PotionContents.createItemStack(Items.POTION, net.minecraft.core.registries.BuiltInRegistries.POTION.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.POTION, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "healing")))));
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        BlockPos clicked = event.getPos();

        EffectTier extendedReach = ForgedEffectRuntime.tier(tool, ForgingEffect.EXTENDED_REACH_TILLING);
        if (extendedReach != null) {
            int extraReach = switch (extendedReach) { case I -> 2; case II -> 4; case III -> 6; };
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            for (int d = 1; d <= extraReach; d++) {
                BlockPos pos = BlockPos.containing(eye.add(look.scale(4.5D + d)));
                net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(pos);
                if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH))
                        && player.level().getBlockState(pos.above()).isAir()) {
                    player.level().setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                    break;
                }
            }
        }

        EffectTier explosiveTilling = ForgedEffectRuntime.tier(tool, ForgingEffect.EXPLOSIVE_TILLING);
        if (explosiveTilling != null) {
            long cd = switch (explosiveTilling) { case I -> 120L; case II -> 80L; case III -> 40L; };
            if (canUseTimedTrigger(player, "ExplosiveTilling", cd)) {
                int changed = 0;
                // Fixed 3x3 area centered on the soil that was tilled.
                for (BlockPos pos : BlockPos.betweenClosed(clicked.offset(-1, 0, -1), clicked.offset(1, 0, 1))) {
                    net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(pos);
                    if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH)
                            || state.is(Blocks.FARMLAND))
                            && player.level().getBlockState(pos.above()).isAir()) {
                        if (!state.is(Blocks.FARMLAND)) changed++;
                        player.level().setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                    }
                }
                // Durability is charged only for blocks actually tilled.
                if (changed > 0 && tool.isDamageableItem())
                    tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + changed));
            }
        }

        EffectTier hyperGrowth = ForgedEffectRuntime.tier(tool, ForgingEffect.HYPER_GROWTH_SOIL);
        if (hyperGrowth != null && player.level() instanceof ServerLevel serverLevel) {
            int attempts = switch (hyperGrowth) { case I -> 1; case II -> 2; case III -> 3; };
            boolean grew = false;
            // Fixed 3x3 farming area for every tier. Tier changes growth speed only.
            BlockPos center = clicked.above();
            java.util.List<BlockPos> crops = new java.util.ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 1, 1))) {
                if (serverLevel.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.CropBlock)
                    crops.add(pos.immutable());
            }
            for (int i = 0; i < attempts && !crops.isEmpty(); i++) {
                BlockPos pos = crops.get(player.getRandom().nextInt(crops.size()));
                net.minecraft.world.level.block.state.BlockState state = serverLevel.getBlockState(pos);
                if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(state)) {
                    BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), serverLevel, pos);
                    grew = true;
                }
            }
            if (grew && tool.isDamageableItem())
                tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + 2));
        }

        boolean tillableSoil = player.level().getBlockState(clicked).is(Blocks.DIRT)
                || player.level().getBlockState(clicked).is(Blocks.GRASS_BLOCK)
                || player.level().getBlockState(clicked).is(Blocks.DIRT_PATH)
                || player.level().getBlockState(clicked).is(Blocks.FARMLAND);

        // Farming effects trigger on the same hoe click that tills the soil.
        EffectTier moisture = ForgedEffectRuntime.tier(tool, ForgingEffect.MOISTURE_RETAIN);
        if (moisture != null && tillableSoil) {
            player.level().setBlockAndUpdate(clicked, Blocks.FARMLAND.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
            player.getPersistentData().putLong("ForgedPermanentMoisturePos", clicked.asLong());
        }

        // Rotten Compost no longer hydrates farmland. It only rolls a bonus soil-block drop on tilling.
        EffectTier rottenCompost = ForgedEffectRuntime.tier(tool, ForgingEffect.ROTTEN_COMPOST);
        if (rottenCompost != null && tillableSoil
                && player.getRandom().nextDouble() < tierValue(rottenCompost, ROTTEN_COMPOST_CHANCE)) {
            ItemStack soilDrop = switch (player.getRandom().nextInt(6)) {
                case 0 -> new ItemStack(Blocks.DIRT);
                case 1 -> new ItemStack(Blocks.COARSE_DIRT);
                case 2 -> new ItemStack(Blocks.ROOTED_DIRT);
                case 3 -> new ItemStack(Blocks.MUD);
                case 4 -> new ItemStack(Blocks.CLAY);
                default -> new ItemStack(Blocks.MYCELIUM);
            };
            Block.popResource(player.level(), clicked, soilDrop);
        }

        EffectTier floraAegis = ForgedEffectRuntime.tier(tool, ForgingEffect.FLORA_AEGIS);
        if (floraAegis != null && tillableSoil) {
            player.getPersistentData().putLong("ForgedProtectedFarmlandPos", clicked.asLong());
        }

        EffectTier organic = ForgedEffectRuntime.tier(tool, ForgingEffect.ORGANIC_CATALYST);
        if (organic != null && canUseTimedTrigger(player, "OrganicCatalyst", Math.round(tierValue(organic, ORGANIC_CATALYST_COOLDOWN)))) {
            BlockPos center = clicked.above();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
                if (isCrop(player.level().getBlockState(pos))) {
                    BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), player.level(), pos, player);
                }
            }
        }

    }


    @SubscribeEvent
    public static void onFarmlandTrample(net.neoforged.neoforge.event.level.BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel().isClientSide()) return;
        long trampled = event.getPos().asLong();

        // Flora Aegis is stamped when the plot is tilled. The owner does not need to
        // hold the tool or stand beside the crop afterward.
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            for (net.minecraft.server.level.ServerPlayer owner : serverLevel.getServer().getPlayerList().getPlayers()) {
                if (owner.level() == serverLevel
                        && owner.getPersistentData().getLong("ForgedProtectedFarmlandPos") == trampled) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityPlace(EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        // Nether Mutation: trigger only after a plant/crop is actually placed.
        // Tier scales the mutation chance: I = 5%, II = 10%, III = 20%.
        EffectTier mutation = ForgedEffectRuntime.tier(player.getMainHandItem(), ForgingEffect.NETHER_MUTATION);
        if (mutation == null || !isPlantableCrop(event.getPlacedBlock())) return;
        if (player.getRandom().nextDouble() >= tierValue(mutation, NETHER_MUTATION_CHANCE)) return;

        // The planted block mutates into either Nether Wart or a Wither Rose.
        Block mutatedBlock = player.getRandom().nextBoolean() ? Blocks.NETHER_WART : Blocks.WITHER_ROSE;
        player.level().setBlockAndUpdate(event.getPos(), mutatedBlock.defaultBlockState());
    }

    /**
     * Mirrors the important vanilla melee-critical conditions closely enough for
     * the forged trigger: falling, not grounded, not climbing/in water, and not a passenger.
     */
    private static Direction directionFromLook(Player player) {
        Vec3 look = player.getLookAngle();
        double ax = Math.abs(look.x), ay = Math.abs(look.y), az = Math.abs(look.z);
        if (ay >= ax && ay >= az) return look.y > 0 ? Direction.UP : Direction.DOWN;
        if (ax >= az) return look.x > 0 ? Direction.EAST : Direction.WEST;
        return look.z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    /**
     * Breaks a fixed plane/depth volume without increasing its dimensions by Tier.
     * width/height describe the face plane; depth extends forward from the broken block.
     */
    private static void breakPlane(Player player, BlockPos origin, Direction face, int width, int height, int depth) {
        if (!(player.level() instanceof ServerLevel level)) return;
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        Direction right = (face.getAxis() == Direction.Axis.Y) ? Direction.EAST
                : (face.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST);
        Direction up = (face.getAxis() == Direction.Axis.Y) ? Direction.SOUTH : Direction.UP;
        int w0 = -(width / 2), h0 = -(height / 2);

        for (int d = 0; d < depth; d++) {
            BlockPos center = origin.relative(face, d);
            for (int w = 0; w < width; w++) for (int h = 0; h < height; h++)
                targets.add(center.relative(right, w0 + w).relative(up, h0 + h));
        }
        breakTargets(player, level, origin, targets);
    }

    private static void breakLine(Player player, BlockPos origin, Direction face, int length) {
        if (!(player.level() instanceof ServerLevel level)) return;
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        for (int i = 0; i < length; i++) targets.add(origin.relative(face, i));
        breakTargets(player, level, origin, targets);
    }

    private static void breakTargets(Player player, ServerLevel level, BlockPos origin,
                                     java.util.Set<BlockPos> targets) {
        ItemStack tool = player.getMainHandItem();
        int extraBroken = 0;
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                if (pos.equals(origin)) continue;
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) continue;
                if (!tool.isCorrectToolForDrops(state)) continue;
                if (level.destroyBlock(pos, true, player)) extraBroken++;
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }
        // Project rule: extra blocks cost 50% durability, rounded up.
        int extraCost = (extraBroken + 1) / 2;
        if (extraCost > 0 && tool.isDamageableItem())
            tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + extraCost));
    }

    private static boolean isCrop(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS) || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN);
    }

    private static boolean isPlantableCrop(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS) || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.MELON_STEM);
    }

    private static boolean isCriticalHit(Player player) {
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isPassenger();
    }

    private static void replaceFloorWithSlime(Player player, BlockPos pos) {
        if (!player.level().getBlockState(pos).isAir())
            player.level().setBlockAndUpdate(pos, Blocks.SLIME_BLOCK.defaultBlockState());
    }

    private static void teleportTargetAway(Player player, LivingEntity target, double distance) {
        Vec3 away = target.position().subtract(player.position());
        if (away.lengthSqr() < 0.001D) away = player.getLookAngle().scale(-1.0D);
        away = away.normalize();

        // Tier controls the exact displacement distance: 4 / 8 / 15 blocks.
        Vec3 destination = target.position().add(away.scale(distance));
        target.teleportTo(destination.x, destination.y, destination.z);
    }

    private static boolean canUseTimedTrigger(Player player, String key, long cooldownTicks) {
        String dataKey = "ForgingCooldown_" + key;
        long now = player.level().getGameTime();
        long readyAt = player.getPersistentData().getLong(dataKey);
        if (now < readyAt) return false;
        player.getPersistentData().putLong(dataKey, now + cooldownTicks);
        return true;
    }

    private static float tierValue(EffectTier tier, float[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }

    private static double tierValue(EffectTier tier, double[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }

    private static int tierValue(EffectTier tier, int[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }
}
