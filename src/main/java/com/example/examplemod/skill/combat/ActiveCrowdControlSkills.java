package com.example.examplemod.skill.combat;

import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.setItemCooldownReadyAt;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;
import static com.example.examplemod.skill.active.ActiveSkillState.syncHeldEquipmentHud;
import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedEffectNetwork;
import com.example.examplemod.skill.ForgedSkillConfig;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Time stop and charged gravitational slam, including its delayed release. */
public final class ActiveCrowdControlSkills {
    private ActiveCrowdControlSkills() {}

    public static void stunTimeStop(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.timeStop(tier);
        if (!ready(tool, player, "StunTimeStop", cooldown)) return;

        int duration = switch (tier) {
            case I -> 100;   // 5 sec
            case II -> 200;  // 10 sec
            case III -> 300; // 15 sec
        };
        if (DoubleTriggerRuntime.rollActive(player, tool)) duration *= 2;
        double radius = 8.0D; // Fixed radius for every Tier.
        AABB area = player.getBoundingBox().inflate(radius);
        long frozenUntil = player.level().getGameTime() + duration;

        // Stun every living entity in range except the player who cast the skill.
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area,
                e -> e != player && e.isAlive() && e.distanceToSqr(player) <= radius * radius)) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            target.getPersistentData().putLong("ForgedTimeStopUntil", frozenUntil);

            // Remember pre-existing glowing so Time Stop never removes glow from another source.
            if (target.isCurrentlyGlowing()) {
                target.getPersistentData().putBoolean("ForgedTimeStopHadGlow", true);
            }
            target.setGlowingTag(true);

            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 255, false, false));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, duration, 128, false, false));
        }

        // Visible fixed 8-block boundary.
        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (int i = 0; i < 72; i++) {
                double angle = Math.PI * 2.0D * i / 72.0D;
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + 0.15D,
                        player.getZ() + Math.sin(angle) * radius,
                        1, 0.03D, 0.03D, 0.03D, 0.0D);
            }
        }

        // Strong, short camera shake instead of a charge.
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ForgedEffectNetwork.sendTimeStopShake(serverPlayer, 12); // ~0.6 sec
        }

        startCooldown(tool, player, "StunTimeStop", cooldown);
        damageEquipment(player, 8);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Stun Time Stop!"), true);
    }

    public static void gravitationalSlam(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.gravitationalSlam(tier);
        if (!ready(tool, player, "GravitationalSlam", cooldown)) return;
        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong("ForgedGravitationalSlamUntil") > now) return;

        // Fixed 5-second charge. Tier changes cooldown only.
        player.getPersistentData().putLong("ForgedGravitationalSlamUntil", now + 100L);
        player.getPersistentData().putDouble("ForgedGravitationalSlamX", player.getX());
        player.getPersistentData().putDouble("ForgedGravitationalSlamY", player.getY());
        player.getPersistentData().putDouble("ForgedGravitationalSlamZ", player.getZ());
        player.getPersistentData().putBoolean("ForgedGravitationalSlamOldInvulnerable", player.isInvulnerable());
        player.setInvulnerable(true);
        player.getPersistentData().putLong("ForgedGravitationalSlamBaseCooldown", cooldown);
        player.getPersistentData().putBoolean("ForgedGravitationalSlamDouble",
                DoubleTriggerRuntime.rollActive(player, tool));
        // Bind this charge to the exact forged equipment stack that started it.
        // The token is stored on the item, so hotbar switching cannot move the
        // resulting cooldown/durability to a different weapon.
        long chargeToken = now ^ player.getUUID().getLeastSignificantBits();
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                tool, tag -> tag.putLong("forgedGravitationalSlamCharge", chargeToken));
        player.getPersistentData().putLong("ForgedGravitationalSlamChargeToken", chargeToken);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Gravitational Slam: Charging..."), true);
        ForgedSkillSounds.play(player, ForgingEffect.GRAVATIONAL_SLAM);
    }

    private static ItemStack findGravitationalSlamTool(Player player, ItemStack currentTool) {
        long token = player.getPersistentData().getLong("ForgedGravitationalSlamChargeToken");
        if (token == 0L) return currentTool;

        for (ItemStack stack : player.getInventory().items) {
            net.minecraft.world.item.component.CustomData data =
                    stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (data != null && data.copyTag().getLong("forgedGravitationalSlamCharge") == token)
                return stack;
        }
        if (!player.getOffhandItem().isEmpty()) {
            net.minecraft.world.item.component.CustomData data =
                    player.getOffhandItem().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (data != null && data.copyTag().getLong("forgedGravitationalSlamCharge") == token)
                return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    public static void releaseGravitationalSlam(Player player, ItemStack currentTool) {
        ItemStack tool = findGravitationalSlamTool(player, currentTool);
        double x = player.getPersistentData().getDouble("ForgedGravitationalSlamX");
        double y = player.getPersistentData().getDouble("ForgedGravitationalSlamY");
        double z = player.getPersistentData().getDouble("ForgedGravitationalSlamZ");
        Vec3 center = new Vec3(x, y, z);

        // 18-block spherical blast radius. Double Trigger doubles the skill result,
        // but still spends only the original cooldown and durability.
        boolean doubled = player.getPersistentData().getBoolean("ForgedGravitationalSlamDouble");
        float slamDamage = doubled ? 240.0F : 120.0F;
        int killedBySlam = 0;
        double blastRadius = 18.0D;
        AABB blast = new AABB(x - blastRadius, y - blastRadius, z - blastRadius,
                x + blastRadius, y + blastRadius, z + blastRadius);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, blast,
                e -> e != player && e.isAlive() && e.distanceToSqr(center) <= blastRadius * blastRadius)) {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(player.damageSources().playerAttack(player), slamDamage);
            } finally {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
            if (!target.isAlive()) killedBySlam++;
        }

        // Cooldown begins only after the explosion has finished and the kill refund is known.
        long baseCooldown = player.getPersistentData().getLong("ForgedGravitationalSlamBaseCooldown");
        long finalCooldown = Math.max(0L, baseCooldown - killedBySlam * 200L);
        if (!tool.isEmpty()) {
            setItemCooldownReadyAt(tool, "GravitationalSlam", player.level().getGameTime() + finalCooldown);
            net.minecraft.world.item.component.CustomData.update(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    tool, tag -> tag.remove("forgedGravitationalSlamCharge"));
        }
        player.getPersistentData().remove("ForgedGravitationalSlamBaseCooldown");
        player.getPersistentData().remove("ForgedGravitationalSlamChargeToken");
        player.getPersistentData().remove("ForgedGravitationalSlamDouble");
        if (killedBySlam > 0) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Gravitational Slam cooldown reduced by " + (killedBySlam * 10) + "s!"), true);
        }

        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    x, y + 0.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y + 0.5D, z, 24, 1.2D, 0.4D, 1.2D, 0.04D);
        }

        player.setInvulnerable(player.getPersistentData().getBoolean("ForgedGravitationalSlamOldInvulnerable"));
        player.getPersistentData().remove("ForgedGravitationalSlamOldInvulnerable");
        player.getPersistentData().remove("ForgedGravitationalSlamUntil");
        if (!tool.isEmpty()) {
            ForgedBlessingRuntime.damage(tool, 12);
        }
        syncHeldEquipmentHud(player);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Gravitational Slam!"), true);
        ForgedSkillSounds.play(player, ForgingEffect.GRAVATIONAL_SLAM);
    }
}
