package com.example.examplemod.skill.curse;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Berserker curse.
 *
 * The curse is active from anywhere in the player's inventory.
 * Below 50% health:
 *  - +50% outgoing attack damage
 *  - +30% attack speed
 *  - +30% incoming damage
 */
@EventBusSubscriber(modid = "examplemod")
public final class BerserkerEvents {
    private static final float HEALTH_THRESHOLD = 0.50F;
    private static final float OUTGOING_DAMAGE_MULTIPLIER = 1.50F;
    private static final float INCOMING_DAMAGE_MULTIPLIER = 1.30F;
    private static final double ATTACK_SPEED_BONUS = 0.30D;

    private static final net.minecraft.resources.ResourceLocation ATTACK_SPEED_ID =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("examplemod", "berserker_attack_speed");

    private BerserkerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        boolean active = hasBerserkerInInventory(player)
                && player.getHealth() > 0.0F
                && player.getHealth() < player.getMaxHealth() * HEALTH_THRESHOLD;

        // Keep a short hidden-duration refresh so the HUD behaves like a potion effect
        // but disappears almost immediately when Berserker is no longer active.
        if (active) {
            player.addEffect(new MobEffectInstance(ExampleMod.BERSERKER, 100, 0, false, false, true));
        } else {
            player.removeEffect(ExampleMod.BERSERKER);
        }

        var attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) return;

        AttributeModifier current = attackSpeed.getModifier(ATTACK_SPEED_ID);
        if (active && current == null) {
            attackSpeed.addTransientModifier(new AttributeModifier(
                    ATTACK_SPEED_ID,
                    ATTACK_SPEED_BONUS,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        } else if (!active && current != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        // Player attacks while Berserker is active: +50% damage.
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player
                && !player.level().isClientSide()
                && isBerserkerActive(player)) {
            event.setAmount(event.getAmount() * OUTGOING_DAMAGE_MULTIPLIER);
        }

        // A Berserker player below half health also takes 30% more damage.
        if (event.getEntity() instanceof Player player
                && !player.level().isClientSide()
                && isBerserkerActive(player)) {
            event.setAmount(event.getAmount() * INCOMING_DAMAGE_MULTIPLIER);
        }
    }

    private static boolean isBerserkerActive(Player player) {
        return hasBerserkerInInventory(player)
                && player.getHealth() > 0.0F
                && player.getHealth() < player.getMaxHealth() * HEALTH_THRESHOLD;
    }

    private static boolean hasBerserkerInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.BERSERKER)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.BERSERKER)) return true;
        }
        return false;
    }
}
