package com.example.examplemod.guide;

import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;

/** Player-facing details for discovered skills, blessings, and curses. */
public final class ForgingGuideDescriptions {
    private ForgingGuideDescriptions() {}

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
