package com.example.examplemod.skill.curse;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Gambler's Strike: 50% double damage, 50% zero damage. */
@EventBusSubscriber(modid = "examplemod")
public final class GamblersStrikeEvents {
    private GamblersStrikeEvents() {}

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        ItemStack weapon = player.getMainHandItem();
        if (!ForgedCurseRuntime.has(weapon, ForgedCurse.GAMBLERS_STRIKE)) return;

        if (player.getRandom().nextBoolean()) {
            event.setAmount(event.getAmount() * 2.0F);
        } else {
            event.setAmount(0.0F);
        }
    }
}
