package com.example.examplemod.skill.blessing;

import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgingEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Runtime behavior for Blessings awarded by the Rhythm Forging minigame. */
@EventBusSubscriber(modid = "examplemod")
public final class ForgedBlessingEvents {
    private static final float POWER_STRIKE_MULTIPLIER = 1.20F;

    private ForgedBlessingEvents() {}

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;

        ItemStack weapon = player.getMainHandItem();
        if (!ForgedBlessingRuntime.has(weapon, ForgedBlessing.POWER_STRIKE)) return;

        // Apply Power Strike before Wither Curse Power. ForgedEffectEvents then
        // multiplies this already-boosted amount, so the combination is:
        // base damage x 1.20 x Wither multiplier.
        event.setAmount(event.getAmount() * POWER_STRIKE_MULTIPLIER);
    }
}
