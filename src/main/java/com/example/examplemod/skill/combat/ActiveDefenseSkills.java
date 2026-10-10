package com.example.examplemod.skill.combat;

import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;
import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;
import static com.example.examplemod.skill.active.ActiveSkillSupport.tierValue;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgedSkillConfig;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Active defensive buffs and Aegis durability upkeep. */
public final class ActiveDefenseSkills {
    private ActiveDefenseSkills() {}

    private static final int[] AEGIS_DRAIN = {8, 5, 3};

    public static void ironFortress(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.fortress(tier);
        if (!ready(tool, player, "IronFortressGuard", cooldown)) return;

        long baseDuration = switch (tier) {
            case I -> 40L;
            case II -> 80L;
            case III -> 120L;
        };
        long duration = DoubleTriggerRuntime.rollActive(player, tool) ? baseDuration * 2L : baseDuration;
        player.getPersistentData().putLong("ForgedIronFortressUntil", player.level().getGameTime() + duration);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Iron Fortress Guard: ON"), true);

        // Double Trigger extends only the effect. Cooldown remains exactly the same
        // as a normal activation, per the blessing rule.
        startCooldown(tool, player, "IronFortressGuard", cooldown + baseDuration);
        damageEquipment(player, 8);
    }

    public static void toggleAegis(Player player, ItemStack tool, EffectTier tier) {
        boolean active = player.getPersistentData().getBoolean("ForgedAegisActive");
        if (active) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF"), true);
            ForgedSkillSounds.play(player, ForgingEffect.AEGIS_SHIELD);
            return;
        }

        player.getPersistentData().putBoolean("ForgedAegisActive", true);
        player.getPersistentData().putLong("ForgedAegisNextDrain", player.level().getGameTime() + 100L);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: ON"), true);
        ForgedSkillSounds.play(player, ForgingEffect.AEGIS_SHIELD);
    }

    public static void tickAegis(Player player, ItemStack tool) {
        if (player.level().isClientSide()) return;
        if (!player.getPersistentData().getBoolean("ForgedAegisActive")) return;

        EffectTier tier = ForgedEffectRuntime.tier(tool, ForgingEffect.AEGIS_SHIELD);
        if (tier == null) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF (effect missing)"), true);
            return;
        }

        long now = player.level().getGameTime();
        long nextDrain = player.getPersistentData().getLong("ForgedAegisNextDrain");
        if (now < nextDrain) return;

        int cost = tierValue(tier, AEGIS_DRAIN);
        if (tool.getDamageValue() + cost >= tool.getMaxDamage()) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF (durability)"), true);
            return;
        }

        damageEquipment(player, cost);
        player.getPersistentData().putLong("ForgedAegisNextDrain", now + 100L);
    }

    public static boolean isAegisActive(Player player) {
        return player.getPersistentData().getBoolean("ForgedAegisActive");
    }
}
