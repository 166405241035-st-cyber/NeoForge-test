package com.example.examplemod.guide;

import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedSkillConfig;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;

/** Player-facing details for discovered skills, blessings, and curses. */
public final class ForgingGuideDescriptions {
    private ForgingGuideDescriptions() {}

    /** Values below mirror the server skill handlers. Unspecified tiers retain the plain effect description. */
    public static String tierEffect(ForgingEffect effect, int tier) {
        int t = Math.max(1, Math.min(3, tier)) - 1;
        return switch (effect) {
            case ZOMBIE_MINION_CALLING -> pick(t, "15%", "30%", "60%") + " summon chance on a kill";
            case CRIPPLING_STRIKE -> pick(t, "10%", "18%", "25%") + " slow chance";
            case SCAVENGER_DIG -> pick(t, "5%", "10%", "15%") + " extra drop chance";
            case ROTTEN_COMPOST -> pick(t, "15%", "25%", "35%") + " compost chance";
            case UNREFINED_ORE_DISCOVERY -> pick(t, "4%", "8%", "12%") + " raw ore chance";
            case SPINE_SPIKE -> pick(t, "10%", "18%", "25%") + " spike chance";
            case GRAVE_GRASP -> pick(t, "0.5", "1", "1.5") + "s restraint";
            case ROUGH_CLEAVE_3X3 -> "3x3; " + pick(t, "8", "6", "4") + "s cooldown";
            case BONE_DUST_EXTRACT -> pick(t, "10%", "20%", "30%") + " bone meal chance";
            case ORGANIC_CATALYST -> "3x3 crop area; " + pick(t, "10", "7", "5") + "s cooldown";
            case WEB_TRAP -> pick(t, "1.5", "2.5", "4") + "s web duration";
            case FLORA_AEGIS -> "Marks tilled soil to protect nearby crops";
            case HARPOON_PULL -> "Pull target; " + cooldown(ForgedSkillConfig.harpoon(level(t)));
            case STATIC_HOVER_DROP -> pick(t, "5", "10", "20") + "s hover";
            case BLOCK_LEVITATION -> "Support block; " + pick(t, "5", "3", "1") + " durability";
            case COMBO_DETONATION -> pick(t, "2.5", "4", "6") + " explosion damage";
            case CRITICAL_BLAST -> pick(t, "15%", "25%", "40%") + " blast chance";
            case TUNNEL_CHARGE_3X1 -> "3x1; " + pick(t, "8", "6", "4") + "s cooldown";
            case LINEAR_BLAST_1X5 -> "1x5; " + pick(t, "10", "7", "5") + "s cooldown";
            case EXPLOSIVE_TILLING -> "3x3 soil patch; " + pick(t, "6", "4", "2") + "s cooldown";
            case UNSTOPPABLE_KNOCKBACK -> pick(t, "1.5x", "2x", "3x") + " knockback";
            case SLIME_TRAIL_STRIKE -> pick(t, "15%", "25%", "40%") + " slime trail chance";
            case MAGNETIC_CLUMPING -> "Pull drops; " + cooldown(ForgedSkillConfig.magneticClumping(level(t)));
            case EARTHY_SHOCKWAVE -> pick(t, "3", "5", "7") + " shockwave damage";
            case MOISTURE_RETAIN -> "Keeps marked farmland hydrated";
            case MOB_SWAP -> "Swap with a targeted mob; no cooldown; 3 durability";
            case RIFT_TELEPORT_ATTACK -> pick(t, "4", "8", "15") + " block teleport range";
            case VOID_VACUUM_PICK -> "Send drops to inventory; " + pick(t, "5", "3", "1") + " durability";
            case LINE_BUILDER -> pick(t, "5", "9", "14") + " blocks; 2s cooldown";
            case POCKET_DIMENSION -> pick(t, "4", "8", "15") + " storage slots";
            case FIREBALL_SHOOT -> "Fireball; " + cooldown(ForgedSkillConfig.fireball(level(t)));
            case LAVA_WAVE -> "Lava wave; " + cooldown(ForgedSkillConfig.lava(level(t)));
            case FRENZY_DIGGING -> "+" + pick(t, "15%", "30%", "50%") + " mining speed after 5 blocks";
            case THERMAL_CROP_BARRIER -> pick(t, "3", "5", "8") + "s burn duration";
            case AUTO_SMELT_MINING -> pick(t, "1", "1-2", "2") + " smelted ingots per ore";
            case AEGIS_SHIELD -> "90% projectile/blast protection; " + pick(t, "8", "5", "3") + " durability/5s";
            case VAMPIRIC_VITALITY -> "60% chance to heal " + pick(t, "4", "6", "8") + " health";
            case AIR_SLASH_RUPTURE -> "Air slash; " + cooldown(ForgedSkillConfig.airSlash(level(t)));
            case SELF_REPAIRING -> "Restores " + pick(t, "2", "5", "10") + " durability";
            case HEALING_HARVEST -> pick(t, "5%", "10%", "18%") + " healing chance";
            case WITHER_DRAIN -> "Heals " + pick(t, "1", "2", "3") + " health";
            case WITHER_CURSE_POWER -> "+" + pick(t, "50%", "100%", "200%") + " attack damage";
            case OBSIDIAN_BREAKER -> "Break target block; " + pick(t, "10", "6", "3") + " durability";
            case SOUL_SAND_EXTRACTION -> pick(t, "10%", "20%", "35%") + " soul sand chance";
            case NETHER_MUTATION -> pick(t, "5%", "10%", "20%") + " crop mutation chance";
            case VELOCITY_STRIKE -> "Up to +" + pick(t, "30%", "60%", "100%") + " damage while moving";
            case BOOMERANG_WEAPON -> "Throw and retrieve the weapon";
            case AIRBORNE_MINING -> "+" + pick(t, "25%", "45%", "70%") + " speed while airborne";
            case FRONT_DASH -> "Dash forward; " + cooldown(ForgedSkillConfig.dash(level(t)));
            case EXTENDED_REACH_TILLING -> "Till soil from farther away";
            case POISON_GAS_CLOUD -> pick(t, "3", "5", "8") + "s poison duration";
            case STUN_TIME_STOP -> "8 block radius; " + pick(t, "5", "10", "15") + "s stun; " + cooldown(ForgedSkillConfig.timeStop(level(t)));
            case WIDE_EXCAVATION_4X4 -> "4x4; " + pick(t, "10", "7", "4") + "s cooldown";
            case LINEAR_PENETRATION_3X15 -> "3x15; " + pick(t, "20", "14", "9") + "s cooldown";
            case HYPER_GROWTH_SOIL -> pick(t, "3x", "4x", "5x") + " crop growth";
            case IRON_FORTRESS_GUARD -> "90% damage reduction for " + pick(t, "2", "4", "6") + "s";
            case LEVITATION_BLOW -> pick(t, "2", "4", "6") + "s target levitation";
            case INTERNAL_STORAGE -> pick(t, "18", "27", "36") + " storage slots";
            case AUTO_CHEST_TRANSPORT -> pick(t, "8", "16", "32") + " block chest range";
            case EARTHY_WALL_RISE -> "3x3 " + pick(t, "stone", "deepslate", "obsidian") + " wall; 15s cooldown";
            case DIVINE_BEACON_LIGHT -> "5s beam; " + cooldown(ForgedSkillConfig.divine(level(t)));
            case GRAVATIONAL_SLAM -> "5s charge; " + cooldown(ForgedSkillConfig.gravitationalSlam(level(t)));
            case ULTIMATE_LASER_BREAKER -> "Mining beam; " + pick(t, "300", "210", "120") + "s cooldown";
            case SKY_BRIDGE_WALK -> "Bridge; " + pick(t, "3", "2", "1") + " durability per block";
            case NATURE_GOD_BLESS -> pick(t, "1", "2", "3") + " nearby crop growth attempts";
            default -> effect(effect, tier);
        };
    }

    private static String pick(int index, String first, String second, String third) {
        return index == 0 ? first : index == 1 ? second : third;
    }

    private static EffectTier level(int index) {
        return index == 0 ? EffectTier.I : index == 1 ? EffectTier.II : EffectTier.III;
    }

    private static String cooldown(long ticks) {
        return (ticks / 20.0D) + "s cooldown";
    }

    public static String effect(ForgingEffect effect, int tier) {
        if (effect == ForgingEffect.FRENZY_DIGGING) {
            int bonus = tier == 1 ? 15 : tier == 2 ? 30 : 50;
            return "Break 5 blocks in a row for +" + bonus + "% mining speed. The chain resets after 5 seconds idle.";
        }
        return switch (effect) {
            case ZOMBIE_MINION_CALLING -> "Attacks may summon a zombie ally.";
            case CRIPPLING_STRIKE -> "Attacks may slow the target.";
            case SCAVENGER_DIG -> "Mining may uncover extra monster remains.";
            case ROTTEN_COMPOST -> "Farming may produce compost from rotten flesh.";
            case UNREFINED_ORE_DISCOVERY -> "Harvesting may reveal raw ore.";
            case SPINE_SPIKE -> "Attacks send a bone spike at the target.";
            case GRAVE_GRASP -> "Briefly restrains the target.";
            case ROUGH_CLEAVE_3X3 -> "Active skill: break a 3x3 area ahead.";
            case BONE_DUST_EXTRACT -> "Mining may yield bone meal.";
            case ORGANIC_CATALYST -> "Speeds up nearby crop growth.";
            case WEB_TRAP -> "Attacks can trap enemies in webs.";
            case HARPOON_PULL -> "Active skill: pull a target closer.";
            case STATIC_HOVER_DROP -> "Briefly suspends your fall.";
            case BLOCK_LEVITATION -> "Active skill: raise a temporary barrier.";
            case FLORA_AEGIS -> "Protects crops around you.";
            case COMBO_DETONATION -> "Consecutive hits build up an explosion.";
            case CRITICAL_BLAST -> "Critical hits may cause an explosion.";
            case TUNNEL_CHARGE_3X1 -> "Active skill: carve a 3x1 tunnel.";
            case LINEAR_BLAST_1X5 -> "Active skill: break a 1x5 line.";
            case EXPLOSIVE_TILLING -> "Tills a line of soil with a blast.";
            case UNSTOPPABLE_KNOCKBACK -> "Attacks push enemies farther away.";
            case SLIME_TRAIL_STRIKE -> "Attacks leave a slime trail.";
            case MAGNETIC_CLUMPING -> "Draws dropped items toward you.";
            case EARTHY_SHOCKWAVE -> "Sends out a ground shockwave.";
            case MOISTURE_RETAIN -> "Helps farmland stay hydrated.";
            case MOB_SWAP -> "Active skill: swap places with a target.";
            case RIFT_TELEPORT_ATTACK -> "Attacks may teleport through a rift.";
            case VOID_VACUUM_PICK -> "Sends mined drops into your inventory.";
            case LINE_BUILDER -> "Active skill: place blocks in a line.";
            case POCKET_DIMENSION -> "Opens a personal storage space.";
            case FIREBALL_SHOOT -> "Active skill: shoot a fireball.";
            case LAVA_WAVE -> "Sends a wave of lava forward.";
            case THERMAL_CROP_BARRIER -> "Guards farmland with heat.";
            case AUTO_SMELT_MINING -> "Smelts mined drops automatically.";
            case AEGIS_SHIELD -> "Active skill: raise a protective shield.";
            case VAMPIRIC_VITALITY -> "Attacks may restore your health.";
            case AIR_SLASH_RUPTURE -> "Releases a cutting air wave.";
            case SELF_REPAIRING -> "Restores tool durability.";
            case HEALING_HARVEST -> "Harvesting may restore health.";
            case WITHER_DRAIN -> "Drains health from a Withered enemy.";
            case WITHER_CURSE_POWER -> "Boosts damage at the cost of self-Wither.";
            case OBSIDIAN_BREAKER -> "Active skill: destroy a tough block ahead.";
            case SOUL_SAND_EXTRACTION -> "Mining may yield soul sand.";
            case NETHER_MUTATION -> "Changes crops through Nether energy.";
            case VELOCITY_STRIKE -> "Movement speed increases attack power.";
            case BOOMERANG_WEAPON -> "Throw the weapon and catch it on return.";
            case AIRBORNE_MINING -> "Mine faster while airborne.";
            case FRONT_DASH -> "Active skill: dash forward.";
            case EXTENDED_REACH_TILLING -> "Till farmland from farther away.";
            case POISON_GAS_CLOUD -> "Creates a cloud of poison gas.";
            case STUN_TIME_STOP -> "Briefly stops an enemy's movement.";
            case WIDE_EXCAVATION_4X4 -> "Active skill: excavate a 4x4 area.";
            case LINEAR_PENETRATION_3X15 -> "Active skill: bore through a 3x15 line.";
            case HYPER_GROWTH_SOIL -> "Accelerates growth on farmland.";
            case IRON_FORTRESS_GUARD -> "Adds protection when taking a hit.";
            case LEVITATION_BLOW -> "Attacks lift the target into the air.";
            case INTERNAL_STORAGE -> "Opens storage inside the tool.";
            case EARTHY_WALL_RISE -> "Raises a wall from the ground.";
            case AUTO_CHEST_TRANSPORT -> "Sends harvested items to a chest.";
            case DIVINE_BEACON_LIGHT -> "A holy light aids the wielder.";
            case GRAVATIONAL_SLAM -> "Slams the ground with gravity.";
            case ULTIMATE_LASER_BREAKER -> "Fires a mining beam ahead.";
            case SKY_BRIDGE_WALK -> "Creates a path as you move.";
            case NATURE_GOD_BLESS -> "Enhances farming results.";
            case FRENZY_DIGGING -> throw new IllegalStateException("Handled above");
        };
    }

    public static String blessing(ForgedBlessing blessing) {
        return switch (blessing) {
            case DOUBLE_TRIGGER -> "Supported skills have a 20% chance to trigger twice. No extra cooldown or durability cost. Melee hits must be fully charged.";
            case POWER_STRIKE -> "Attacks with this tool deal 20% more damage.";
            case HUNTERS_FORTUNE -> "Duplicates the monster's actual drops.";
            case LIFE_STEAL -> "Attacks have a 20% chance to restore 2 health.";
            case DURABILITY_GUARD -> "25% chance to avoid one durability cost.";
            case VEIN_BREAKER -> "Breaks up to 8 connected blocks of the same ore.";
            case MINING_HASTE -> "+50% mining speed while holding this tool.";
            case EXPERIENCE_BOOST -> "+60% experience from defeated monsters.";
            case DIVINE_EXECUTION -> "Fully charged attacks have a 5% chance to execute a monster.";
        };
    }

    public static String curse(ForgedCurse curse) {
        return switch (curse) {
            case TALKATIVE_BLADE -> "The blade talks from your inventory and may briefly vanish as a prank.";
            case BERSERKER -> "Below half health: +50% attack damage and +30% attack speed, but +30% incoming damage.";
            case GAMBLERS_STRIKE -> "Each hit either deals double damage or no damage, at equal odds.";
            case POWER_ERASURE -> "Normally suppresses the main skills on this piece of gear.";
            case LIFE_EXCHANGE -> "5% chance on a monster hit to swap your current health with the target's.";
            case VAMPIRE_BLADE -> "Heal for 20% of damage dealt, but natural regeneration is halved.";
            case LAST_STAND -> "At 2 health or less, the next attack deals 3x damage. 120-second cooldown.";
            case CRITICAL_FAILURE -> "Normal attacks deal half damage, while critical attacks gain 75%.";
        };
    }
}
