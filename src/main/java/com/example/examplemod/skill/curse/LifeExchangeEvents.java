package com.example.examplemod.skill.curse;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Life Exchange: 5% chance on weapon hit to swap the attacker's and target's current health.
 * Works on normal mobs and bosses, but never on another player.
 */
@EventBusSubscriber(modid = "examplemod")
public final class LifeExchangeEvents {
    private static final double EXCHANGE_CHANCE = 0.05D;

    private LifeExchangeEvents() {}

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity target) || target instanceof Player) return;

        ItemStack weapon = player.getMainHandItem();
        if (!ForgedCurseRuntime.has(weapon, ForgedCurse.LIFE_EXCHANGE)) return;
        if (player.getRandom().nextDouble() >= EXCHANGE_CHANCE) return;

        float playerHealth = player.getHealth();
        float targetHealth = target.getHealth();

        player.setHealth(Math.min(player.getMaxHealth(), targetHealth));
        target.setHealth(Math.min(target.getMaxHealth(), playerHealth));
    }
}
