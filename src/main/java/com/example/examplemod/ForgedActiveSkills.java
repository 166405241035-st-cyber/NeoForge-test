package com.example.examplemod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ForgedActiveSkills {
    private static final String SELECTED_INDEX = "ForgedActiveSkillIndex";

    private static final long[] FIREBALL_COOLDOWN = {160L, 100L, 60L};
    private static final long[] FRONT_DASH_COOLDOWN = {140L, 90L, 60L};
    private static final long[] WITHER_CURSE_COOLDOWN = {400L, 400L, 400L};
    private static final long[] HARPOON_COOLDOWN = {120L, 80L, 50L};
    private static final long[] MOB_SWAP_COOLDOWN = {240L, 160L, 100L};
    private static final long[] AIR_SLASH_COOLDOWN = {200L, 140L, 80L};
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
            case HARPOON_PULL -> harpoonPull(player, tier);
            case MOB_SWAP -> mobSwap(player, tier);
            case AIR_SLASH_RUPTURE -> airSlashRupture(player, tier);
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
        startCooldown(player, "FireballShoot", cooldown);
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
        startCooldown(player, "FrontDash", cooldown);
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
        startCooldown(player, "WitherCursePower", cooldown);
        damageEquipment(player, 4);
    }

    private static void harpoonPull(Player player, EffectTier tier) {
        LivingEntity target = findLookTarget(player, 12.0D);
        if (target == null) return;

        long cooldown = tierValue(tier, HARPOON_COOLDOWN);
        if (!ready(player, "HarpoonPull", cooldown)) return;

        Vec3 pull = player.position().subtract(target.position());
        if (pull.lengthSqr() < 0.01D) return;

        Vec3 velocity = pull.normalize().scale(1.0D + tierIndex(tier) * 0.25D);
        target.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.15D), velocity.z);
        target.hurtMarked = true;
        startCooldown(player, "HarpoonPull", cooldown);
        damageEquipment(player, 2);
    }

    private static void mobSwap(Player player, EffectTier tier) {
        LivingEntity target = findLookTarget(player, 16.0D);
        if (!(target instanceof Monster)) return;

        long cooldown = tierValue(tier, MOB_SWAP_COOLDOWN);
        if (!ready(player, "MobSwap", cooldown)) return;

        Vec3 playerPos = player.position();
        float playerYaw = player.getYRot();
        float playerPitch = player.getXRot();

        player.teleportTo(target.getX(), target.getY(), target.getZ());
        target.teleportTo(playerPos.x, playerPos.y, playerPos.z);

        player.setYRot(target.getYRot());
        player.setXRot(target.getXRot());
        target.setYRot(playerYaw);
        target.setXRot(playerPitch);

        startCooldown(player, "MobSwap", cooldown);
        damageEquipment(player, 3);
    }

    private static void airSlashRupture(Player player, EffectTier tier) {
        long cooldown = tierValue(tier, AIR_SLASH_COOLDOWN);
        if (!ready(player, "AirSlashRupture", cooldown)) return;

        double damage = switch (tier) {
            case I -> 4.0D;
            case II -> 6.0D;
            case III -> 8.0D;
        };

        AABB area = player.getBoundingBox().inflate(3.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive())) {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(player.damageSources().playerAttack(player), (float) damage);
            } finally {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
            Vec3 away = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(away.x * 0.5D, 0.25D, away.z * 0.5D));
            target.hurtMarked = true;
        }

        startCooldown(player, "AirSlashRupture", cooldown);
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

        EffectTier tier = ForgedEffectRuntime.tier(tool, ForgingEffect.AEGIS_SHIELD);
        if (tier == null) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            return;
        }

        long now = player.level().getGameTime();
        long nextDrain = player.getPersistentData().getLong("ForgedAegisNextDrain");
        if (now < nextDrain) return;

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
            case FIREBALL_SHOOT, FRONT_DASH, WITHER_CURSE_POWER, AEGIS_SHIELD,
                 HARPOON_PULL, MOB_SWAP, AIR_SLASH_RUPTURE -> true;
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
        return now >= readyAt;
    }

    private static void startCooldown(Player player, String key, long cooldownTicks) {
        player.getPersistentData().putLong(
                "ForgedActiveCooldown_" + key,
                player.level().getGameTime() + cooldownTicks
        );
    }

    private static void damageEquipment(Player player, int amount) {
        ItemStack tool = player.getMainHandItem();
        tool.setDamageValue(Math.min(tool.getMaxDamage(), tool.getDamageValue() + amount));
    }

    private static LivingEntity findLookTarget(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D);

        LivingEntity best = null;
        double bestDistance = range * range;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, search, e -> e != player && e.isAlive())) {
            Vec3 toTarget = entity.getBoundingBox().getCenter().subtract(eye);
            double distance = toTarget.length();
            if (distance > range) continue;

            double dot = look.dot(toTarget.normalize());
            if (dot < 0.94D) continue;

            if (distance * distance < bestDistance) {
                best = entity;
                bestDistance = distance * distance;
            }
        }
        return best;
    }

    private static int tierIndex(EffectTier tier) {
        return switch (tier) {
            case I -> 0;
            case II -> 1;
            case III -> 2;
        };
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
