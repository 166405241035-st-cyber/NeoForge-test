package com.example.examplemod.skill.active;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Shared targeting, durability and Tier helpers for active skills. */
public final class ActiveSkillSupport {
    private ActiveSkillSupport() {}

    public static void damageEquipment(Player player, int amount) {
        ItemStack tool = player.getMainHandItem();
        ForgedBlessingRuntime.damage(tool, amount);
    }

    public static LivingEntity findLookTarget(Player player, double range) {
        // True crosshair ray: choose the first living entity whose hitbox is actually
        // intersected by the player's view ray instead of using a wide aim cone.
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);

        LivingEntity best = null;
        double bestDistance = range * range;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class, search, e -> e != player && e.isAlive())) {
            AABB hitbox = entity.getBoundingBox().inflate(0.30D);
            java.util.Optional<Vec3> hit = hitbox.clip(eye, end);
            if (hit.isEmpty()) continue;

            double distance = eye.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                best = entity;
                bestDistance = distance;
            }
        }
        return best;
    }

    public static int tierIndex(EffectTier tier) {
        return switch (tier) {
            case I -> 0;
            case II -> 1;
            case III -> 2;
        };
    }

    public static long tierValue(EffectTier tier, long[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }

    public static int tierValue(EffectTier tier, int[] values) {
        return switch (tier) {
            case I -> values[0];
            case II -> values[1];
            case III -> values[2];
        };
    }
}
