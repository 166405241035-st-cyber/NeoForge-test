package com.example.examplemod.skill;

import static com.example.examplemod.skill.active.ActiveSkillState.setSelectedIndex;
import static com.example.examplemod.skill.active.ActiveSkillState.syncHud;
import static com.example.examplemod.skill.building.ActiveBuildingSkills.blockLevitation;
import static com.example.examplemod.skill.building.ActiveBuildingSkills.earthyWallRise;
import static com.example.examplemod.skill.building.ActiveBuildingSkills.lineBuilder;
import static com.example.examplemod.skill.building.ActiveBuildingSkills.toggleSkyBridge;
import static com.example.examplemod.skill.combat.ActiveCombatSkills.airSlashRupture;
import static com.example.examplemod.skill.combat.ActiveCombatSkills.divineBeaconLaser;
import static com.example.examplemod.skill.combat.ActiveCombatSkills.fireball;
import static com.example.examplemod.skill.combat.ActiveCombatSkills.harpoonPull;
import static com.example.examplemod.skill.combat.ActiveCombatSkills.lavaWave;
import static com.example.examplemod.skill.combat.ActiveCrowdControlSkills.gravitationalSlam;
import static com.example.examplemod.skill.combat.ActiveCrowdControlSkills.stunTimeStop;
import static com.example.examplemod.skill.combat.ActiveDefenseSkills.ironFortress;
import static com.example.examplemod.skill.combat.ActiveDefenseSkills.toggleAegis;
import static com.example.examplemod.skill.farming.ActiveFarmingSkills.natureGodBless;
import static com.example.examplemod.skill.mining.ActiveMiningSkills.linearPenetration;
import static com.example.examplemod.skill.mining.ActiveMiningSkills.magneticClumping;
import static com.example.examplemod.skill.mining.ActiveMiningSkills.miningSweep;
import static com.example.examplemod.skill.mining.ActiveMiningSkills.obsidianBreaker;
import static com.example.examplemod.skill.mining.ActiveMiningSkills.ultimateLaser;
import static com.example.examplemod.skill.movement.ActiveMovementSkills.frontDash;
import static com.example.examplemod.skill.movement.ActiveMovementSkills.mobSwap;
import static com.example.examplemod.skill.storage.ForgedStorageSkills.openStorage;

import com.example.examplemod.skill.active.ActiveSkillState;
import com.example.examplemod.skill.active.ActiveWorldEffects;
import com.example.examplemod.skill.combat.ActiveDefenseSkills;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Central entry point delegating forged equipment behavior to focused skill modules. */
public final class ForgedActiveSkills {
    private ForgedActiveSkills() {}

    public static void handle(Player player, int action) {
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        List<ForgingEffect> active = getActiveEffects(tool);
        if (active.isEmpty()) return;

        int selected = normalizeSelected(tool, active.size());

        if (action == 0) {
            selected = (selected + 1) % active.size();
            setSelectedIndex(tool, selected);
            syncHud(player, active.get(selected), selected);
            return;
        }

        if (action == 1) {
            ForgingEffect effect = active.get(selected);
            use(player, tool, effect);
            syncHud(player, effect, selected);
        }
    }

    private static void use(Player player, ItemStack tool, ForgingEffect effect) {
        EffectTier tier = ForgedEffectRuntime.tier(tool, effect);
        if (tier == null) return;

        switch (effect) {
            case FIREBALL_SHOOT -> fireball(player, tool, tier);
            case FRONT_DASH -> frontDash(player, tool, tier);
            case AEGIS_SHIELD -> toggleAegis(player, tool, tier);
            case HARPOON_PULL -> harpoonPull(player, tool, tier);
            case MOB_SWAP -> mobSwap(player, tool, tier);
            case AIR_SLASH_RUPTURE -> airSlashRupture(player, tool, tier);
            case LAVA_WAVE -> lavaWave(player, tool, tier);
            case STUN_TIME_STOP -> stunTimeStop(player, tool, tier);
            case GRAVATIONAL_SLAM -> gravitationalSlam(player, tool, tier);
            case IRON_FORTRESS_GUARD -> ironFortress(player, tool, tier);
            case DIVINE_BEACON_LIGHT -> divineBeaconLaser(player, tool, tier);
            case ULTIMATE_LASER_BREAKER -> ultimateLaser(player, tool, tier);
            case BLOCK_LEVITATION -> blockLevitation(player, tool, tier);
            case MAGNETIC_CLUMPING -> magneticClumping(player, tool, tier);
            case ROUGH_CLEAVE_3X3 -> miningSweep(player, tool, tier, effect, 3, 3, 1, 160L, 120L, 80L);
            case TUNNEL_CHARGE_3X1 -> miningSweep(player, tool, tier, effect, 3, 1, 1, 160L, 120L, 80L);
            case LINEAR_BLAST_1X5 -> miningSweep(player, tool, tier, effect, 1, 1, 5, 200L, 140L, 100L);
            case WIDE_EXCAVATION_4X4 -> miningSweep(player, tool, tier, effect, 4, 4, 1, 200L, 140L, 80L);
            case LINEAR_PENETRATION_3X15 -> linearPenetration(player, tool, tier);
            case OBSIDIAN_BREAKER -> obsidianBreaker(player, tool, tier);
            case NATURE_GOD_BLESS -> natureGodBless(player, tool, tier);
            case LINE_BUILDER -> lineBuilder(player, tool, tier);
            case EARTHY_WALL_RISE -> earthyWallRise(player, tool, tier);
            case SKY_BRIDGE_WALK -> toggleSkyBridge(player, tier);
            case POCKET_DIMENSION, INTERNAL_STORAGE -> openStorage(player, tool);
            default -> {
                // Other active effects are added to this same dispatcher in later batches.
            }
        }
    }

    public static void tickWorldEffects(Player player, ItemStack tool) {
        ActiveWorldEffects.tickWorldEffects(player, tool);
    }

    public static void tickAegis(Player player, ItemStack tool) {
        ActiveDefenseSkills.tickAegis(player, tool);
    }

    public static boolean isAegisActive(Player player) {
        return ActiveDefenseSkills.isAegisActive(player);
    }

    public static List<ForgingEffect> getActiveEffects(ItemStack tool) {
        return ActiveSkillState.getActiveEffects(tool);
    }

    public static int normalizeSelected(ItemStack tool, int size) {
        return ActiveSkillState.normalizeSelected(tool, size);
    }

    public static void syncHeldEquipmentHud(Player player) {
        ActiveSkillState.syncHeldEquipmentHud(player);
    }

    public static String cooldownKey(ForgingEffect effect) {
        return ActiveSkillState.cooldownKey(effect);
    }

    public static long cooldownRemaining(Player player, ItemStack tool, ForgingEffect effect) {
        return ActiveSkillState.cooldownRemaining(player, tool, effect);
    }
}
