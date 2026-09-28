package com.example.examplemod.skill.curse;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Vampire Blade:
 * - Hitting with the cursed weapon heals 20% of the damage dealt.
 * - While any Vampire Blade is anywhere in the player's inventory, food/natural healing is halved.
 */
@EventBusSubscriber(modid = "examplemod")
public final class VampireBladeEvents {
    private static final float LIFE_STEAL_RATIO = 0.20F;

    private VampireBladeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (hasVampireBladeInInventory(player)) {
            player.addEffect(new MobEffectInstance(ExampleMod.VAMPIRE_BLADE, 100, 0, false, false, true));
        } else {
            player.removeEffect(ExampleMod.VAMPIRE_BLADE);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        ItemStack weapon = player.getMainHandItem();
        if (!ForgedCurseRuntime.has(weapon, ForgedCurse.VAMPIRE_BLADE)) return;

        float heal = event.getAmount() * LIFE_STEAL_RATIO;
        if (heal > 0.0F) player.heal(heal);
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (!hasVampireBladeInInventory(player)) return;

        // Natural food regeneration occurs while the hunger bar is high.
        // Limit the penalty to that state so potion/command healing is not normally affected.
        if (player.getFoodData().getFoodLevel() >= 18) {
            event.setAmount(event.getAmount() * 0.50F);
        }
    }

    private static boolean hasVampireBladeInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.VAMPIRE_BLADE)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.VAMPIRE_BLADE)) return true;
        }
        return false;
    }
}
