package com.example.examplemod.skill.combat;

import static com.example.examplemod.skill.active.ActiveSkillState.ready;
import static com.example.examplemod.skill.active.ActiveSkillState.startCooldown;
import static com.example.examplemod.skill.active.ActiveSkillSupport.damageEquipment;
import static com.example.examplemod.skill.active.ActiveSkillSupport.findLookTarget;
import static com.example.examplemod.skill.active.ActiveSkillSupport.tierIndex;

import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedSkillConfig;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Active attacks and projectiles; invoked by the central skill dispatcher. */
public final class ActiveCombatSkills {
    private ActiveCombatSkills() {}

    public static void fireball(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.fireball(tier);
        if (!ready(tool, player, "FireballShoot", cooldown)) return;

        // Spawn slightly in front of the player's eyes so the projectile does not collide
        // with the caster immediately. Give it an explicit forward velocity for reliable firing.
        Vec3 look = player.getLookAngle().normalize();
        SmallFireball fireball = new SmallFireball(player.level(), player, look);
        Vec3 spawn = player.getEyePosition().add(look.scale(0.8D));
        fireball.setPos(spawn.x, spawn.y - 0.10D, spawn.z);
        fireball.setDeltaMovement(look.scale(1.35D));
        fireball.hurtMarked = true;
        fireball.getPersistentData().putBoolean("ForgedSkillProjectile", true);
        player.level().addFreshEntity(fireball);

        // Double Trigger: a second projectile is a free bonus cast.
        // It does not start another cooldown and does not consume durability.
        if (DoubleTriggerRuntime.rollActive(player, tool)) {
            Vec3 side = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (side.lengthSqr() > 0.0001D) side = side.normalize().scale(0.18D);
            SmallFireball second = new SmallFireball(player.level(), player, look);
            Vec3 secondSpawn = spawn.add(side);
            second.setPos(secondSpawn.x, secondSpawn.y - 0.10D, secondSpawn.z);
            second.setDeltaMovement(look.scale(1.35D));
            second.hurtMarked = true;
            second.getPersistentData().putBoolean("ForgedSkillProjectile", true);
            player.level().addFreshEntity(second);
        }

        startCooldown(tool, player, "FireballShoot", cooldown);
        damageEquipment(player, 3);
    }

    public static void harpoonPull(Player player, ItemStack tool, EffectTier tier) {
        LivingEntity target = findLookTarget(player, 25.0D);
        if (target == null) return;

        long cooldown = ForgedSkillConfig.harpoon(tier);
        if (!ready(tool, player, "HarpoonPull", cooldown)) return;

        Vec3 pull = player.position().subtract(target.position());
        if (pull.lengthSqr() < 0.01D) return;

        double pullPower = 1.0D + tierIndex(tier) * 0.25D;
        if (DoubleTriggerRuntime.rollActive(player, tool)) pullPower *= 2.0D;
        Vec3 velocity = pull.normalize().scale(pullPower);
        target.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.15D), velocity.z);
        target.hurtMarked = true;
        startCooldown(tool, player, "HarpoonPull", cooldown);
        damageEquipment(player, 2);
    }

    public static void airSlashRupture(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.airSlash(tier);
        if (!ready(tool, player, "AirSlashRupture", cooldown)) return;

        boolean doubled = DoubleTriggerRuntime.rollActive(player, tool);
        double damage = doubled ? 12.0D : 6.0D; // bonus cast deals the same damage again.

        AABB area = player.getBoundingBox().inflate(6.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive())) {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(player.damageSources().playerAttack(player), (float) damage);
            } finally {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
            Vec3 away = target.position().subtract(player.position()).normalize();
            double push = doubled ? 1.0D : 0.5D;
            target.setDeltaMovement(target.getDeltaMovement().add(away.x * push, doubled ? 0.50D : 0.25D, away.z * push));
            target.hurtMarked = true;
        }

        startCooldown(tool, player, "AirSlashRupture", cooldown);
        damageEquipment(player, 4);
    }

    public static void lavaWave(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.lava(tier);
        if (!ready(tool, player, "LavaWave", cooldown)) return;

        boolean doubled = DoubleTriggerRuntime.rollActive(player, tool);
        Vec3 view = player.getLookAngle();
        Vec3 look = new Vec3(view.x, 0.0D, view.z);
        if (look.lengthSqr() < 0.0001D) look = new Vec3(0.0D, 0.0D, 1.0D);
        look = look.normalize();
        Vec3 feet = new Vec3(player.getX(), player.getY(), player.getZ());
        for (int i = 1; i <= 5; i++) {
            Vec3 pos = feet.add(look.scale(i * 1.5D));

            // Orange/lava visual wave without placing real lava blocks, so the caster
            // can never be burned by their own Lava Wave.
            if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        pos.x, pos.y + 0.25D, pos.z, 14, 0.9D, 0.25D, 0.9D, 0.025D);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                        pos.x, pos.y + 0.15D, pos.z, 5, 0.8D, 0.12D, 0.8D, 0.0D);
            }
            AABB area = new AABB(pos.x - 1.2D, pos.y - 1.0D, pos.z - 1.2D,
                    pos.x + 1.2D, pos.y + 1.5D, pos.z + 1.2D);
            for (LivingEntity target : player.level().getEntitiesOfClass(
                    LivingEntity.class, area, e -> e != player && e.isAlive())) {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    float waveDamage = tier == EffectTier.I ? 3.0F : tier == EffectTier.II ? 5.0F : 7.0F;
                    target.hurt(player.damageSources().playerAttack(player), doubled ? waveDamage * 2.0F : waveDamage);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }
                int burnSeconds = tier == EffectTier.I ? 3 : tier == EffectTier.II ? 5 : 7;
                if (doubled) burnSeconds *= 2;
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), burnSeconds * 20));
                double push = doubled ? 0.70D : 0.35D;
                target.setDeltaMovement(target.getDeltaMovement().add(look.x * push, doubled ? 0.40D : 0.20D, look.z * push));
                target.hurtMarked = true;
            }
        }
        startCooldown(tool, player, "LavaWave", cooldown);
        damageEquipment(player, 6);
    }

    public static void divineBeaconLaser(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.divine(tier); // Defaults: 7.5 / 5 / 3 sec.
        if (!ready(tool, player, "DivineBeaconLight", cooldown)) return;

        // Keep the beam active for 5 seconds. Damage is handled by tickWorldEffects
        // every 6 ticks (0.3 sec), so the player can keep aiming during the beam.
        long now = player.level().getGameTime();
        player.getPersistentData().putLong("ForgedDivineBeaconUntil", now + 100L);
        player.getPersistentData().putLong("ForgedDivineBeaconNextHit", now);
        player.getPersistentData().putBoolean("ForgedDivineBeaconDouble",
                DoubleTriggerRuntime.rollActive(player, tool));

        startCooldown(tool, player, "DivineBeaconLight", cooldown);
        damageEquipment(player, 6);
    }
}
