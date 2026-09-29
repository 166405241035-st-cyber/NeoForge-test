package com.example.examplemod.skill.blessing;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Central rules for the Double Trigger blessing.
 *
 * One successful trigger has a 20% chance to produce one extra result.
 * Attack-triggered effects are eligible only when the vanilla attack meter is fully charged.
 * Active R skills and non-attack result/effect triggers do not use the attack meter.
 *
 * Callers must duplicate only the skill result/effect. They must NOT start a second
 * cooldown or charge durability a second time.
 */
public final class DoubleTriggerRuntime {
    public static final double CHANCE = 0.20D;

    private DoubleTriggerRuntime() {}

    public static boolean has(ItemStack stack) {
        return ForgedBlessingRuntime.has(stack, ForgedBlessing.DOUBLE_TRIGGER);
    }

    /** R-key / explicit active skill. */
    public static boolean rollActive(Player player, ItemStack stack) {
        return roll(player, stack);
    }

    /** Mining/farming/passive result that has already successfully triggered. */
    public static boolean rollResult(Player player, ItemStack stack) {
        return roll(player, stack);
    }

    /**
     * Melee-triggered skill. Double Trigger is only allowed on a fully charged attack.
     */
    public static boolean rollAttack(Player player, ItemStack stack) {
        if (player == null || !has(stack)) return false;
        if (player.getAttackStrengthScale(0.5F) < 1.0F) return false;
        return player.getRandom().nextDouble() < CHANCE;
    }

    public static boolean isFullChargeAttack(Player player, ItemStack stack) {
        return player != null && has(stack) && player.getAttackStrengthScale(0.5F) >= 1.0F;
    }

    private static boolean roll(Player player, ItemStack stack) {
        return player != null
                && has(stack)
                && player.getRandom().nextDouble() < CHANCE;
    }
}
