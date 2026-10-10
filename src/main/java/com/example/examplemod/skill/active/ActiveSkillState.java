package com.example.examplemod.skill.active;

import com.example.examplemod.forging.result.AnvilAssemblyResult;
import com.example.examplemod.item.ForgedEquipmentItem;
import com.example.examplemod.skill.ForgedEffectNetwork;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Shared skill selection, item cooldown NBT and server-to-client HUD synchronization. */
public final class ActiveSkillState {
    private ActiveSkillState() {}

    private static final String SELECTED_INDEX = "forgedSelectedActiveSkill";

    public static List<ForgingEffect> getActiveEffects(ItemStack tool) {
        List<ForgingEffect> result = new ArrayList<>();
        int count = ForgedEquipmentItem.effectCount(tool);
        for (int i = 0; i < count; i++) {
            AnvilAssemblyResult.FinalEffect effect = ForgedEquipmentItem.readEffect(tool, i);
            if (effect != null && isActive(effect.effect())) {
                result.add(effect.effect());
            }
        }
        return result;
    }

    private static boolean isActive(ForgingEffect effect) {
        return switch (effect) {
            case FIREBALL_SHOOT, FRONT_DASH, AEGIS_SHIELD,
                 HARPOON_PULL, MOB_SWAP, AIR_SLASH_RUPTURE, LAVA_WAVE,
                 STUN_TIME_STOP, GRAVATIONAL_SLAM, IRON_FORTRESS_GUARD, DIVINE_BEACON_LIGHT,
                 ULTIMATE_LASER_BREAKER, BLOCK_LEVITATION, MAGNETIC_CLUMPING,
                 ROUGH_CLEAVE_3X3, TUNNEL_CHARGE_3X1, LINEAR_BLAST_1X5, WIDE_EXCAVATION_4X4,
                 LINEAR_PENETRATION_3X15, OBSIDIAN_BREAKER,
                 NATURE_GOD_BLESS, LINE_BUILDER, EARTHY_WALL_RISE,
                 SKY_BRIDGE_WALK, POCKET_DIMENSION, INTERNAL_STORAGE -> true;
            default -> false;
        };
    }

    public static int normalizeSelected(ItemStack tool, int size) {
        if (tool.isEmpty() || size <= 0) return 0;
        net.minecraft.world.item.component.CustomData data =
                tool.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        int selected = data == null ? 0 : data.copyTag().getInt(SELECTED_INDEX);
        if (selected < 0 || selected >= size) selected = 0;
        setSelectedIndex(tool, selected);
        return selected;
    }

    public static void setSelectedIndex(ItemStack tool, int selected) {
        if (tool.isEmpty()) return;
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                tool,
                tag -> tag.putInt(SELECTED_INDEX, selected));
    }

    /** Refresh the client HUD from the equipment currently held in the main hand. */
    public static void syncHeldEquipmentHud(Player player) {
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();
        List<ForgingEffect> active = getActiveEffects(tool);
        if (active.isEmpty()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                ForgedEffectNetwork.sendHudState(serverPlayer, 0, "", 0L, 0L, false);
            return;
        }
        int selected = normalizeSelected(tool, active.size());
        syncHud(player, active.get(selected), selected);
    }

    public static String cooldownKey(ForgingEffect effect) {
        return switch (effect) {
            case FIREBALL_SHOOT -> "FireballShoot";
            case FRONT_DASH -> "FrontDash";
            case HARPOON_PULL -> "HarpoonPull";

            case AIR_SLASH_RUPTURE -> "AirSlashRupture";
            case LAVA_WAVE -> "LavaWave";
            case STUN_TIME_STOP -> "StunTimeStop";
            case GRAVATIONAL_SLAM -> "GravitationalSlam";
            case IRON_FORTRESS_GUARD -> "IronFortressGuard";
            case DIVINE_BEACON_LIGHT -> "DivineBeaconLight";
            case ULTIMATE_LASER_BREAKER -> "UltimateLaserBreaker";
            case MAGNETIC_CLUMPING -> "MagneticClumping";
            case ROUGH_CLEAVE_3X3 -> "RoughCleave3x3";
            case TUNNEL_CHARGE_3X1 -> "TunnelCharge3x1";
            case LINEAR_BLAST_1X5 -> "LinearBlast1x5";
            case WIDE_EXCAVATION_4X4 -> "WideExcavation4x4";
            case LINEAR_PENETRATION_3X15 -> "LinearPenetration3x15";
            case NATURE_GOD_BLESS -> "NatureGodBless";
            case LINE_BUILDER -> "LineBuilder";
            case EARTHY_WALL_RISE -> "EarthyWallRise";
            default -> null;
        };
    }

    public static void syncHud(Player player, ForgingEffect effect, int selectedIndex) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;
        ItemStack tool = player.getMainHandItem();
        StringBuilder cooldownStates = new StringBuilder();
        for (ForgingEffect activeEffect : getActiveEffects(tool)) {
            String activeKey = cooldownKey(activeEffect);
            long readyAt = activeKey == null ? 0L : itemCooldownReadyAt(tool, activeKey);
            if (!cooldownStates.isEmpty()) cooldownStates.append(';');
            cooldownStates.append(activeEffect.name()).append('=').append(readyAt);
        }
        ForgedEffectNetwork.sendHudState(serverPlayer, selectedIndex, cooldownStates.toString(), 0L,
                player.getPersistentData().getLong("ForgedGravitationalSlamUntil"),
                player.getPersistentData().getBoolean("ForgedAegisActive"));
    }

    public static long cooldownRemaining(Player player, ItemStack tool, ForgingEffect effect) {
        String key = cooldownKey(effect);
        if (key == null || tool.isEmpty()) return 0L;
        return Math.max(0L, itemCooldownReadyAt(tool, key) - player.level().getGameTime());
    }

    private static String itemCooldownKey(String key) {
        return "forgedCooldown_" + key;
    }

    private static long itemCooldownReadyAt(ItemStack tool, String key) {
        net.minecraft.world.item.component.CustomData data =
                tool.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data == null ? 0L : data.copyTag().getLong(itemCooldownKey(key));
    }

    public static void setItemCooldownReadyAt(ItemStack tool, String key, long readyAt) {
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                tool,
                tag -> tag.putLong(itemCooldownKey(key), readyAt));
    }

    public static boolean ready(ItemStack tool, Player player, String key, long cooldownTicks) {
        long now = player.level().getGameTime();
        return now >= itemCooldownReadyAt(tool, key);
    }

    public static void startCooldown(ItemStack tool, Player player, String key, long cooldownTicks) {
        setItemCooldownReadyAt(tool, key, player.level().getGameTime() + cooldownTicks);
        for (ForgingEffect effect : ForgingEffect.values()) {
            if (key.equals(cooldownKey(effect))) {
                ForgedSkillSounds.play(player, effect);
                break;
            }
        }
    }
}
