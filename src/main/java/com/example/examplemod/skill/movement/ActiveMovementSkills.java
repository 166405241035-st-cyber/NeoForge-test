package com.example.examplemod.skill.movement;

import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;
import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;
import static com.example.examplemod.skill.active.ActiveSkillSupport.findLookTarget;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedSkillConfig;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Dash and entity-swap movement skills. */
public final class ActiveMovementSkills {
    private ActiveMovementSkills() {}

    public static void frontDash(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.dash(tier);
        if (!ready(tool, player, "FrontDash", cooldown)) return;

        Vec3 look = player.getLookAngle().normalize();
        double power = switch (tier) {
            case I -> 1.25D;
            case II -> 1.55D;
            case III -> 1.90D;
        };
        // A simultaneous second dash is represented as double travel impulse.
        if (DoubleTriggerRuntime.rollActive(player, tool)) power *= 2.0D;

        player.setDeltaMovement(
                look.x * power,
                Math.max(player.getDeltaMovement().y, 0.20D),
                look.z * power
        );
        player.hurtMarked = true;
        startCooldown(tool, player, "FrontDash", cooldown);
        damageEquipment(player, 2);
    }

    public static void mobSwap(Player player, ItemStack tool, EffectTier tier) {
        // Look farther than the usable range so we can distinguish "missed" from
        // "you are aiming at a mob, but it is too far away".
        LivingEntity target = findLookTarget(player, 64.0D);
        if (target != null && player.getEyePosition().distanceTo(target.getBoundingBox().getCenter()) > 32.0D) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Target is out of range!"), true);
            return;
        }
        if (target == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Swap: Aim at a living target."), true);
            return;
        }

        Vec3 playerPos = player.position();
        float playerYaw = player.getYRot();
        float playerPitch = player.getXRot();

        player.teleportTo(target.getX(), target.getY(), target.getZ());
        target.teleportTo(playerPos.x, playerPos.y, playerPos.z);

        player.setYRot(target.getYRot());
        player.setXRot(target.getXRot());
        target.setYRot(playerYaw);
        target.setXRot(playerPitch);

        // Mob Swap intentionally has no cooldown. It is also excluded from Double Trigger:
        // swapping twice would immediately undo the first swap.
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Swap: สำเร็จ"), true);
        ForgedSkillSounds.play(player, ForgingEffect.MOB_SWAP);
        damageEquipment(player, 3);
    }
}
