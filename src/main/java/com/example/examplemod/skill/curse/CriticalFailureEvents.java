package com.example.examplemod.skill.curse;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Critical Failure:
 * - Critical hit damage +75%
 * - Non-critical hit damage -25%
 */
@EventBusSubscriber(modid = "examplemod")
public final class CriticalFailureEvents {
    private static final float CRITICAL_MULTIPLIER = 1.75F;
    private static final float NORMAL_MULTIPLIER = 0.75F;

    private CriticalFailureEvents() {}

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        ItemStack weapon = player.getMainHandItem();
        if (!ForgedCurseRuntime.has(weapon, ForgedCurse.CRITICAL_FAILURE)) return;

        event.setAmount(event.getAmount() * (isCriticalAttack(player) ? CRITICAL_MULTIPLIER : NORMAL_MULTIPLIER));
    }

    private static boolean isCriticalAttack(Player player) {
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger()
                && !player.isSprinting();
    }
}
