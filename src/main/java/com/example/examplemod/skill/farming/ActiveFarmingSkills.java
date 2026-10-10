package com.example.examplemod.skill.farming;

import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Active crop growth blessing. */
public final class ActiveFarmingSkills {
    private ActiveFarmingSkills() {}

    public static void natureGodBless(Player player, ItemStack tool, EffectTier tier) {
        // Active Nature God Bless is a harvest buff, separate from the passive
        // plot growth and Golden Apple reward. R grants Fortune-III-like crop drops
        // for 15 seconds, then this exact forged tool has a 60-second cooldown.
        final long cooldown = 1200L; // 60 sec
        if (!ready(tool, player, "NatureGodBless", cooldown)) return;

        long now = player.level().getGameTime();
        long fortuneUntil = now + 300L; // 15 sec
        player.getPersistentData().putLong("ForgedNatureGodBlessFortuneUntil", fortuneUntil);
        if (DoubleTriggerRuntime.rollActive(player, tool)) {
            player.getPersistentData().putLong("ForgedNatureGodBlessDoubleUntil", fortuneUntil);
        } else {
            player.getPersistentData().remove("ForgedNatureGodBlessDoubleUntil");
        }
        startCooldown(tool, player, "NatureGodBless", cooldown);

        // Show the vanilla potion-style HUD icon and swirling status particles
        // while the Fortune harvest blessing is active.
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.LUCK,
                300, 2, false, true, true));

        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Nature God Bless: Fortune III harvest active for 15s"), true);

    }
}
