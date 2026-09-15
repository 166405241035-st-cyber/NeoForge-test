package com.example.examplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Server-side entry point for forged equipment effects.
 *
 * First implementation slice:
 * - Crippling Strike: ON_HIT
 * - Zombie Minion Calling: ON_KILL
 * - Bone Dust Extract: MINING
 *
 * Effect-generated damage must eventually be marked before more damaging effects
 * are added so it cannot recursively trigger other forged effects.
 */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgedEffectEvents {
    private ForgedEffectEvents() {}

    // Alpha balance values agreed for the first three trigger tests.
    private static final double[] CRIPPLING_CHANCE = {0.10D, 0.18D, 0.25D};
    private static final double[] ZOMBIE_MINION_CHANCE = {0.05D, 0.10D, 0.15D};
    private static final double[] BONE_DUST_CHANCE = {0.10D, 0.20D, 0.30D};

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        ItemStack weapon = player.getMainHandItem();
        EffectTier tier = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRIPPLING_STRIKE);
        if (tier == null) return;

        double chance = tierValue(tier, CRIPPLING_CHANCE);
        if (player.getRandom().nextDouble() < chance) {
            // Tier changes proc chance only. Slow strength/duration stay fixed.
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || !(player.level() instanceof ServerLevel level)) return;

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
        EffectTier tier = ForgedEffectRuntime.tier(tool, ForgingEffect.BONE_DUST_EXTRACT);
        if (tier == null || player.getRandom().nextDouble() >= tierValue(tier, BONE_DUST_CHANCE)) return;

        // One bonus Bone Meal per successful proc. Tier changes chance only.
        Block.popResource(player.level(), event.getPos(), new ItemStack(Items.BONE_MEAL));
    }

    private static double tierValue(EffectTier tier, double[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }
}
