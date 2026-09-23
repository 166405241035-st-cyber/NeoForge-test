package com.example.examplemod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ForgedActiveSkills {
    private static final String SELECTED_INDEX = "ForgedActiveSkillIndex";

    private static final long[] FIREBALL_COOLDOWN = {160L, 100L, 60L};
    private static final long[] FRONT_DASH_COOLDOWN = {140L, 90L, 60L};
    private static final long[] WITHER_CURSE_COOLDOWN = {400L, 400L, 400L};
    private static final int[] AEGIS_DRAIN = {8, 5, 3};

    private ForgedActiveSkills() {}

    public static void handle(Player player, int action) {
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        List<ForgingEffect> active = getActiveEffects(tool);
        if (active.isEmpty()) return;

        int selected = normalizeSelected(player, active.size());

        if (action == 0) {
            selected = (selected + 1) % active.size();
            player.getPersistentData().putInt(SELECTED_INDEX, selected);
            return;
        }

        if (action == 1) {
            use(player, tool, active.get(selected));
        }
    }

    private static void use(Player player, ItemStack tool, ForgingEffect effect) {
        EffectTier tier = ForgedEffectRuntime.tier(tool, effect);
        if (tier == null) return;

        switch (effect) {
            case FIREBALL_SHOOT -> fireball(player, tier);
            case FRONT_DASH -> frontDash(player, tier);
            case WITHER_CURSE_POWER -> witherCurse(player, tier);
            case AEGIS_SHIELD -> toggleAegis(player, tool, tier);
            default -> {
                // Other active effects are added to this same dispatcher in later batches.
            }
        }
    }

    private static void fireball(Player player, EffectTier tier) {
        long cooldown = tierValue(tier, FIREBALL_COOLDOWN);
        if (!ready(player, "FireballShoot", cooldown)) return;

        SmallFireball fireball = new SmallFireball(player.level(), player, player.getLookAngle());
        player.level().addFreshEntity(fireball);
        damageEquipment(player, 3);
    }

    private static void frontDash(Player player, EffectTier tier) {
        long cooldown = tierValue(tier, FRONT_DASH_COOLDOWN);
        if (!ready(player, "FrontDash", cooldown)) return;

        Vec3 look = player.getLookAngle().normalize();
        double power = switch (tier) {
            case I -> 1.25D;
            case II -> 1.55D;
            case III -> 1.90D;
        };

        player.setDeltaMovement(
                look.x * power,
                Math.max(player.getDeltaMovement().y, 0.20D),
                look.z * power
        );
        player.hurtMarked = true;
        damageEquipment(player, 2);
    }

    private static void witherCurse(Player player, EffectTier tier) {
        long cooldown = tierValue(tier, WITHER_CURSE_COOLDOWN);
        if (!ready(player, "WitherCursePower", cooldown)) return;

        long until = player.level().getGameTime() + 200L;
        double bonus = switch (tier) {
            case I -> 0.30D;
            case II -> 0.50D;
            case III -> 0.75D;
        };

        player.getPersistentData().putLong("ForgedWitherCurseUntil", until);
        player.getPersistentData().putDouble("ForgedWitherCurseBonus", bonus);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WITHER, 100, 0, false, false
        ));
        damageEquipment(player, 4);
    }

    private static void toggleAegis(Player player, ItemStack tool, EffectTier tier) {
        boolean active = player.getPersistentData().getBoolean("ForgedAegisActive");
        if (active) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            return;
        }

        player.getPersistentData().putBoolean("ForgedAegisActive", true);
        player.getPersistentData().putLong("ForgedAegisNextDrain", player.level().getGameTime() + 100L);
    }

    public static void tickAegis(Player player, ItemStack tool) {
        if (player.level().isClientSide()) return;
        if (!player.getPersistentData().getBoolean("ForgedAegisActive")) return;

        if (ForgedEffectRuntime.tier(tool, ForgingEffect.AEGIS_SHIELD) == null) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            return;
        }

        long now = player.level().getGameTime();
        long nextDrain = player.getPersistentData().getLong("ForgedAegisNextDrain");
        if (now < nextDrain) return;

        EffectTier tier = ForgedEffectRuntime.tier(tool, ForgingEffect.AEGIS_SHIELD);
        int cost = tierValue(tier, AEGIS_DRAIN);
        if (tool.getDamageValue() + cost >= tool.getMaxDamage()) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            return;
        }

        damageEquipment(player, cost);
        player.getPersistentData().putLong("ForgedAegisNextDrain", now + 100L);
    }

    public static boolean isAegisActive(Player player) {
        return player.getPersistentData().getBoolean("ForgedAegisActive");
    }

    private static List<ForgingEffect> getActiveEffects(ItemStack tool) {
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
            case FIREBALL_SHOOT, FRONT_DASH, WITHER_CURSE_POWER, AEGIS_SHIELD -> true;
            default -> false;
        };
    }

    private static int normalizeSelected(Player player, int size) {
        int selected = player.getPersistentData().getInt(SELECTED_INDEX);
        if (selected < 0 || selected >= size) selected = 0;
        player.getPersistentData().putInt(SELECTED_INDEX, selected);
        return selected;
    }

    private static boolean ready(Player player, String key, long cooldownTicks) {
        String dataKey = "ForgedActiveCooldown_" + key;
        long now = player.level().getGameTime();
        long readyAt = player.getPersistentData().getLong(dataKey);
        if (now < readyAt) return false;
        player.getPersistentData().putLong(dataKey, now + cooldownTicks);
        return true;
    }

    private static void damageEquipment(Player player, int amount) {
        ItemStack tool = player.getMainHandItem();
        tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + amount));
    }

    private static long tierValue(EffectTier tier, long[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }

    private static int tierValue(EffectTier tier, int[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }
}
