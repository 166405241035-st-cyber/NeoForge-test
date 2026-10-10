package com.example.examplemod.skill.blessing;

import com.example.examplemod.skill.ForgingEffect;

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

    /**
     * Authoritative behavior category for every current forged main effect.
     * Keeping this in one place prevents individual event handlers from inventing
     * incompatible Double Trigger rules.
     */
    public static DoubleTriggerType type(ForgingEffect effect) {
        return switch (effect) {
            // Active / repeatable actions.
            case HARPOON_PULL, COMBO_DETONATION, CRITICAL_BLAST, RIFT_TELEPORT_ATTACK,
                 FIREBALL_SHOOT, LAVA_WAVE, BOOMERANG_WEAPON,
                 GRAVATIONAL_SLAM, AIR_SLASH_RUPTURE, FRONT_DASH,
                 ROUGH_CLEAVE_3X3, TUNNEL_CHARGE_3X1, LINEAR_BLAST_1X5,
                 WIDE_EXCAVATION_4X4, LINEAR_PENETRATION_3X15,
                 EARTHY_WALL_RISE, EXPLOSIVE_TILLING -> DoubleTriggerType.DOUBLE_SKILL;

            // Produced items / blocks / rewards.
            case ZOMBIE_MINION_CALLING, SLIME_TRAIL_STRIKE, SCAVENGER_DIG,
                 BONE_DUST_EXTRACT, SOUL_SAND_EXTRACTION, AUTO_SMELT_MINING,
                 SELF_REPAIRING, LINE_BUILDER, ROTTEN_COMPOST,
                 UNREFINED_ORE_DISCOVERY, HEALING_HARVEST,
                 NETHER_MUTATION -> DoubleTriggerType.DOUBLE_RESULT;

            // Timed/status/damage effects where replay is represented by doubled
            // duration, damage, healing, force, or another equivalent result.
            case CRIPPLING_STRIKE, SPINE_SPIKE, GRAVE_GRASP, WEB_TRAP,
                 UNSTOPPABLE_KNOCKBACK, VAMPIRIC_VITALITY, POISON_GAS_CLOUD,
                 WITHER_DRAIN, STUN_TIME_STOP, IRON_FORTRESS_GUARD,
                 LEVITATION_BLOW, STATIC_HOVER_DROP, MAGNETIC_CLUMPING,
                 EARTHY_SHOCKWAVE, ORGANIC_CATALYST -> DoubleTriggerType.DOUBLE_EFFECT;

            // Main effect is passive/continuous/toggle/storage or already permanent.
            case WITHER_CURSE_POWER, VELOCITY_STRIKE, AEGIS_SHIELD, FRENZY_DIGGING,
                 AIRBORNE_MINING, OBSIDIAN_BREAKER, VOID_VACUUM_PICK,
                 BLOCK_LEVITATION, INTERNAL_STORAGE, POCKET_DIMENSION,
                 SKY_BRIDGE_WALK, MOISTURE_RETAIN, FLORA_AEGIS,
                 THERMAL_CROP_BARRIER, EXTENDED_REACH_TILLING,
                 HYPER_GROWTH_SOIL, AUTO_CHEST_TRANSPORT -> DoubleTriggerType.NO_DOUBLE;

            // Passive portion stays normal; active portion has custom Double Trigger handling.
            case DIVINE_BEACON_LIGHT, ULTIMATE_LASER_BREAKER,
                 NATURE_GOD_BLESS -> DoubleTriggerType.HYBRID;

            // Swapping twice undoes itself, so Mob Swap deliberately receives no replay.
            case MOB_SWAP -> DoubleTriggerType.SPECIAL;
        };
    }

    public static boolean supports(ForgingEffect effect) {
        return type(effect) != DoubleTriggerType.NO_DOUBLE
                && type(effect) != DoubleTriggerType.SPECIAL;
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
