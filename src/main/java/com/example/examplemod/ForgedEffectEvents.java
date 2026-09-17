package com.example.examplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        ItemStack weapon = player.getMainHandItem();

        EffectTier crippling = ForgedEffectRuntime.tier(weapon, ForgingEffect.CRIPPLING_STRIKE);
        if (crippling != null && player.getRandom().nextDouble() < tierValue(crippling, CRIPPLING_CHANCE))
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        EffectTier vampiric = ForgedEffectRuntime.tier(weapon, ForgingEffect.VAMPIRIC_VITALITY);
        if (vampiric != null && player.getRandom().nextDouble() < tierValue(vampiric, VAMPIRIC_CHANCE))
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));

        EffectTier levitation = ForgedEffectRuntime.tier(weapon, ForgingEffect.LEVITATION_BLOW);
        if (levitation != null)
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.LEVITATION, tierValue(levitation, LEVITATION_DURATION), 0));
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
            // Equal-weight pool requested by design: Bone / Rotten Flesh /
            // Iron Nugget / Gold Nugget. Tier changes proc chance only.
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
            // Any successfully broken block can proc this effect. The bonus Raw Ore
            // is selected independently of the block that was mined.
            Item rawOre = switch (player.getRandom().nextInt(3)) {
                case 0 -> Items.RAW_IRON;
                case 1 -> Items.RAW_COPPER;
                default -> Items.RAW_GOLD;
            };
            Block.popResource(player.level(), event.getPos(), new ItemStack(rawOre));
        }
    }

    private static double tierValue(EffectTier tier, double[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }

    private static int tierValue(EffectTier tier, int[] values) {
        return switch (tier) { case I -> values[0]; case II -> values[1]; case III -> values[2]; };
    }
}
