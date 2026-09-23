package com.example.examplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Server-side entry point for forged equipment effects. */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgedEffectEvents {
    private ForgedEffectEvents() {}

    private static final double[] CRIPPLING_CHANCE = {0.10D, 0.18D, 0.25D};
    private static final double[] ZOMBIE_MINION_CHANCE = {0.05D, 0.10D, 0.15D};
    private static final double[] BONE_DUST_CHANCE = {0.10D, 0.20D, 0.30D};
    private static final double[] VAMPIRIC_CHANCE = {0.15D, 0.25D, 0.40D};
    private static final int[] LEVITATION_DURATION = {40, 80, 120};
    private static final double[] SOUL_SAND_CHANCE = {0.10D, 0.20D, 0.35D};
    private static final double[] SCAVENGER_CHANCE = {0.05D, 0.10D, 0.15D};
    private static final double[] UNREFINED_ORE_CHANCE = {0.04D, 0.08D, 0.12D};

    // Combat batch.
    private static final double[] SPINE_SPIKE_CHANCE = {0.10D, 0.18D, 0.25D};
    private static final int[] GRAVE_GRASP_DURATION = {10, 20, 30}; // 0.5 / 1 / 1.5 sec
    private static final double[] RIFT_TELEPORT_CHANCE = {0.15D, 0.25D, 0.40D};

    // Additional combat effects from the project skill list (pages 16-19).
    private static final int[] WEB_TRAP_DURATION = {30, 50, 80}; // 1.5 / 2.5 / 4 sec
    private static final double[] UNSTOPPABLE_KNOCKBACK_POWER = {1.5D, 2.0D, 3.0D};
    private static final double[] SLIME_TRAIL_CHANCE = {0.15D, 0.25D, 0.40D};
    private static final float[] WITHER_DRAIN_HEAL = {1.0F, 2.0F, 3.0F};
    private static final double[] CRITICAL_BLAST_CHANCE = {0.15D, 0.25D, 0.40D};
    private static final double[] VELOCITY_STRIKE_MAX_BONUS = {0.30D, 0.60D, 1.00D};
    private static final double[] AIRBORNE_MINING_SPEED = {0.50D, 1.00D, 1.50D};
    private static final int[] SELF_REPAIR_AMOUNT = {2, 5, 10};
    private static final double[] HEALING_HARVEST_CHANCE = {0.05D, 0.10D, 0.18D};
    private static final double[] MOISTURE_RETAIN_CHANCE = {0.25D, 0.50D, 0.75D};

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        ItemStack weapon = player.getMainHandItem();
        LivingEntity target = event.getEntity();

        EffectTier crippling = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRIPPLING_STRIKE);
        if (crippling != null && player.getRandom().nextDouble() < tierValue(crippling, CRIPPLING_CHANCE))
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        EffectTier vampiric = ForgedEffectRuntime.tier(weapon, ForgingEffect.VAMPIRIC_VITALITY);
        if (vampiric != null && player.getRandom().nextDouble() < tierValue(vampiric, VAMPIRIC_CHANCE))
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));

        EffectTier levitation = ForgedEffectRuntime.tier(weapon, ForgingEffect.LEVITATION_BLOW);
        if (levitation != null)
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, tierValue(levitation, LEVITATION_DURATION), 0));

        EffectTier spineSpike = ForgedEffectRuntime.tier(weapon, ForgingEffect.SPINE_SPIKE);
        if (spineSpike != null && player.getRandom().nextDouble() < tierValue(spineSpike, SPINE_SPIKE_CHANCE)) {
            // Bleeding prototype: Poison provides non-lethal damage-over-time behavior.
            // Tier changes proc chance only.
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
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
            teleportTargetAway(player, target);
        }

        EffectTier webTrap = ForgedEffectRuntime.tier(weapon, ForgingEffect.WEB_TRAP);
        if (webTrap != null && canUseTimedTrigger(player, "WebTrap", 60L)) {
            // Temporary web-like restraint without leaving permanent cobweb blocks.
            target.setDeltaMovement(Vec3.ZERO);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, tierValue(webTrap, WEB_TRAP_DURATION), 6));
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
            if (!player.level().getBlockState(floor).isAir()) {
                player.level().setBlockAndUpdate(floor, Blocks.SLIME_BLOCK.defaultBlockState());
            }
        }

        EffectTier witherDrain = ForgedEffectRuntime.tier(weapon, ForgingEffect.WITHER_DRAIN);
        if (witherDrain != null && canUseTimedTrigger(player, "WitherDrain", 20L)) {
            // Fixed Wither; Tier changes only the amount of health stolen.
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            player.heal(tierValue(witherDrain, WITHER_DRAIN_HEAL));
        }

        EffectTier criticalBlast = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRITICAL_BLAST);
        if (criticalBlast != null && isCriticalHit(player)
                && player.getRandom().nextDouble() < tierValue(criticalBlast, CRITICAL_BLAST_CHANCE)) {
            // Small non-block-breaking blast so the proc does not destroy terrain.
            player.level().explode(player, target.getX(), target.getY(), target.getZ(),
                    1.5F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
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

        EffectTier selfRepair = ForgedEffectRuntime.tier(tool, ForgingEffect.SELF_REPAIRING);
        if (selfRepair != null && tool.isDamaged() && now % 600L == 0L)
            tool.setDamageValue(Math.max(0, tool.getDamageValue() - tierValue(selfRepair, SELF_REPAIR_AMOUNT)));

        EffectTier divine = ForgedEffectRuntime.tier(tool, ForgingEffect.DIVINE_BEACON_LIGHT);
        if (divine != null) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, false));
        }

        EffectTier frenzy = ForgedEffectRuntime.tier(tool, ForgingEffect.FRENZY_DIGGING);
        if (frenzy != null && player.swinging) {
            int amp = switch (frenzy) { case I -> 0; case II -> 1; case III -> 2; };
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30, amp, false, false));
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
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();

        EffectTier boneDust = ForgedEffectRuntime.tier(tool, ForgingEffect.BONE_DUST_EXTRACT);
        if (boneDust != null && player.getRandom().nextDouble() < tierValue(boneDust, BONE_DUST_CHANCE))
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.BONE_MEAL));

        EffectTier soulSand = ForgedEffectRuntime.tier(tool, ForgingEffect.SOUL_SAND_EXTRACTION);
        if (soulSand != null && player.getRandom().nextDouble() < tierValue(soulSand, SOUL_SAND_CHANCE))
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.SOUL_SAND));

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

        EffectTier unrefined = ForgedEffectRuntime.tier(tool, ForgingEffect.UNREFINED_ORE_DISCOVERY);
        if (unrefined != null && player.getRandom().nextDouble() < tierValue(unrefined, UNREFINED_ORE_CHANCE)) {
            Item rawOre = switch (player.getRandom().nextInt(3)) {
                case 0 -> Items.RAW_IRON;
                case 1 -> Items.RAW_COPPER;
                default -> Items.RAW_GOLD;
            };
            Block.popResource(player.level(), event.getPos(), new ItemStack(rawOre));
        }

        EffectTier vacuum = ForgedEffectRuntime.tier(tool, ForgingEffect.VOID_VACUUM_PICK);
        if (vacuum != null) {
            // Pull nearby item entities directly to the player immediately after a block break.
            for (ItemEntity drop : player.level().getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(event.getPos()).inflate(3.0D))) {
                drop.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
                drop.setDeltaMovement(Vec3.ZERO);
            }
        }

        EffectTier magnetic = ForgedEffectRuntime.tier(tool, ForgingEffect.MAGNETIC_CLUMPING);
        if (magnetic != null) {
            Vec3 center = Vec3.atCenterOf(event.getPos());
            for (ItemEntity drop : player.level().getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(event.getPos()).inflate(4.0D))) {
                drop.setPos(center.x, center.y, center.z);
                drop.setDeltaMovement(Vec3.ZERO);
            }
        }

        EffectTier airborne = ForgedEffectRuntime.tier(tool, ForgingEffect.AIRBORNE_MINING);
        if (airborne != null && !player.onGround()) {
            // Strong, visible airborne mining boost. This is applied immediately
            // after each airborne break and lasts long enough to affect the next block.
            double bonus = tierValue(airborne, AIRBORNE_MINING_SPEED);
            int amplifier = bonus >= 1.50D ? 4 : bonus >= 1.00D ? 2 : 1;
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 80, amplifier, false, false));
        }


        EffectTier healingHarvest = ForgedEffectRuntime.tier(tool, ForgingEffect.HEALING_HARVEST);
        if (healingHarvest != null && isCrop(event.getState())
                && player.getRandom().nextDouble() < tierValue(healingHarvest, HEALING_HARVEST_CHANCE)) {
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.POTION));
        }

        EffectTier moisture = ForgedEffectRuntime.tier(tool, ForgingEffect.MOISTURE_RETAIN);
        if (moisture != null && event.getState().is(Blocks.FARMLAND)
                && player.getRandom().nextDouble() < tierValue(moisture, MOISTURE_RETAIN_CHANCE)) {
            for (BlockPos pos : BlockPos.betweenClosed(event.getPos().offset(-2, -1, -2), event.getPos().offset(2, 1, 2))) {
                if (player.level().getBlockState(pos).is(Blocks.FARMLAND))
                    player.level().setBlockAndUpdate(pos, player.level().getBlockState(pos)
                            .setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
            }
        }    }

    /**
     * Mirrors the important vanilla melee-critical conditions closely enough for
     * the forged trigger: falling, not grounded, not climbing/in water, and not a passenger.
     */
    private static boolean isCrop(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS) || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN);
    }

    private static boolean isCriticalHit(Player player) {
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isPassenger();
    }

    private static void teleportTargetAway(Player player, LivingEntity target) {
        Vec3 away = target.position().subtract(player.position());
        if (away.lengthSqr() < 0.001D) away = player.getLookAngle().scale(-1.0D);
        away = away.normalize();

        // Fixed distance: Tier changes chance only.
        double distance = 8.0D;
        double side = (player.getRandom().nextDouble() - 0.5D) * 4.0D;
        Vec3 sideways = new Vec3(-away.z, 0.0D, away.x).scale(side);
        Vec3 destination = target.position().add(away.scale(distance)).add(sideways);
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
