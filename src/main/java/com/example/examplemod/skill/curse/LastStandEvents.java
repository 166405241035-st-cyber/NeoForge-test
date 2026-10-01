package com.example.examplemod.skill.curse;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Last Stand:
 * - Carry the cursed item anywhere in inventory.
 * - At 1 heart (2 HP) or less, arm Last Stand if the 120 second cooldown is ready.
 * - The next attack deals x3 damage and starts the cooldown.
 * - Removing the item cancels READY but never clears the cooldown.
 */
@EventBusSubscriber(modid = "examplemod")
public final class LastStandEvents {
    private static final float READY_HEALTH = 2.0F;
    private static final float DAMAGE_MULTIPLIER = 3.0F;
    private static final long COOLDOWN_TICKS = 120L * 20L;
    private static final String READY_KEY = "LastStandReady";
    private static final String COOLDOWN_UNTIL_KEY = "LastStandCooldownUntil";

    private LastStandEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        var data = player.getPersistentData();
        long now = player.level().getGameTime();
        boolean hasCurse = hasLastStandInInventory(player);

        if (!hasCurse) {
            data.putBoolean(READY_KEY, false);
            player.removeEffect(ExampleMod.LAST_STAND);
            return;
        }

        boolean ready = data.getBoolean(READY_KEY);
        long cooldownUntil = data.getLong(COOLDOWN_UNTIL_KEY);

        if (!ready && player.getHealth() > 0.0F && player.getHealth() <= READY_HEALTH && now >= cooldownUntil) {
            ready = true;
            data.putBoolean(READY_KEY, true);
        }

        if (ready) {
            player.addEffect(new MobEffectInstance(ExampleMod.LAST_STAND, 100, 0, false, false, true));
        } else {
            player.removeEffect(ExampleMod.LAST_STAND);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        var data = player.getPersistentData();
        if (!data.getBoolean(READY_KEY)) return;
        if (!hasLastStandInInventory(player)) {
            data.putBoolean(READY_KEY, false);
            player.removeEffect(ExampleMod.LAST_STAND);
            return;
        }

        event.setAmount(event.getAmount() * DAMAGE_MULTIPLIER);
        data.putBoolean(READY_KEY, false);
        data.putLong(COOLDOWN_UNTIL_KEY, player.level().getGameTime() + COOLDOWN_TICKS);
        player.removeEffect(ExampleMod.LAST_STAND);
    }

    private static boolean hasLastStandInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.LAST_STAND)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.LAST_STAND)) return true;
        }
        return false;
    }
}
