package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

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
            case LAVA_WAVE -> lavaWave(player, tier);
            case STUN_TIME_STOP -> stunTimeStop(player, tier);
            case IRON_FORTRESS_GUARD -> ironFortress(player, tier);
            case BOOMERANG_WEAPON -> boomerangWeapon(player, tool, tier);
            case DIVINE_BEACON_LIGHT -> divineBeaconLaser(player, tier);
            case ULTIMATE_LASER_BREAKER -> ultimateLaser(player, tier);
            case NATURE_GOD_BLESS -> natureGodBless(player, tier);
            case LINE_BUILDER -> lineBuilder(player, tier);
            case EARTHY_WALL_RISE -> earthyWallRise(player, tier);
            case SKY_BRIDGE_WALK -> toggleSkyBridge(player, tier);
            case POCKET_DIMENSION, INTERNAL_STORAGE -> openStorage(player, tool);
            default -> {
                // Other active effects are added to this same dispatcher in later batches.
            }
        }
    }

    private static void fireball(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.fireball(tier);
        if (!ready(player, "FireballShoot", cooldown)) return;

        SmallFireball fireball = new SmallFireball(player.level(), player, player.getLookAngle());
        player.level().addFreshEntity(fireball);
        startCooldown(player, "FireballShoot", cooldown);
        damageEquipment(player, 3);
    }

    private static void frontDash(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.dash(tier);
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
        long cooldown = ForgedSkillConfig.wither(tier);
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

        long cooldown = ForgedSkillConfig.harpoon(tier);
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
        if (!(target instanceof Monster)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Swap: ต้องเล็งมอนสเตอร์"), true);
            return;
        }

        long cooldown = ForgedSkillConfig.swap(tier);
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
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Swap: สำเร็จ"), true);
        damageEquipment(player, 3);
    }

    private static void airSlashRupture(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.airSlash(tier);
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

    private static void lavaWave(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.lava(tier);
        if (!ready(player, "LavaWave", cooldown)) return;

        Vec3 look = player.getLookAngle().normalize();
        for (int i = 1; i <= 5; i++) {
            Vec3 pos = player.position().add(look.scale(i * 1.5D));
            AABB area = new AABB(pos.x - 1.2D, pos.y - 1.0D, pos.z - 1.2D,
                    pos.x + 1.2D, pos.y + 1.5D, pos.z + 1.2D);
            for (LivingEntity target : player.level().getEntitiesOfClass(
                    LivingEntity.class, area, e -> e != player && e.isAlive())) {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    target.hurt(player.damageSources().playerAttack(player), tier == EffectTier.I ? 3.0F : tier == EffectTier.II ? 5.0F : 7.0F);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (tier == EffectTier.I ? 3 : tier == EffectTier.II ? 5 : 7) * 20));
                target.setDeltaMovement(target.getDeltaMovement().add(look.x * 0.35D, 0.20D, look.z * 0.35D));
                target.hurtMarked = true;
            }
        }
        startCooldown(player, "LavaWave", cooldown);
        damageEquipment(player, 6);
    }

    private static void stunTimeStop(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.timeStop(tier);
        if (!ready(player, "StunTimeStop", cooldown)) return;

        int duration = switch (tier) {
            case I -> 30;
            case II -> 50;
            case III -> 80;
        };
        double radius = 5.0D; // Fixed area for every tier; Tier only changes duration.
        AABB area = player.getBoundingBox().inflate(radius);
        long frozenUntil = player.level().getGameTime() + duration;
        for (Monster target : player.level().getEntitiesOfClass(
                Monster.class, area, e -> e.isAlive() && e.distanceToSqr(player) <= radius * radius)) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            target.getPersistentData().putLong("ForgedTimeStopUntil", frozenUntil);
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 255, false, false));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, duration, 128, false, false));
        }
        startCooldown(player, "StunTimeStop", cooldown);
        damageEquipment(player, 8);
    }

    private static void ironFortress(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.fortress(tier);
        if (!ready(player, "IronFortressGuard", cooldown)) return;

        long duration = switch (tier) {
            case I -> 40L;
            case II -> 80L;
            case III -> 120L;
        };
        player.getPersistentData().putLong("ForgedIronFortressUntil", player.level().getGameTime() + duration);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Iron Fortress Guard: ON"), true);
        startCooldown(player, "IronFortressGuard", cooldown + duration);
        damageEquipment(player, 8);
    }

    private static void boomerangWeapon(Player player, ItemStack tool, EffectTier tier) {
        long cooldown = ForgedSkillConfig.boomerang(tier); // Configurable; defaults to fixed 6 sec.
        if (!ready(player, "BoomerangWeapon", cooldown)) return;

        LivingEntity target = findLookTarget(player, 18.0D);
        if (target == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Boomerang Weapon: ต้องเล็งเป้าหมาย"), true);
            return;
        }

        double multiplier = switch (tier) { case I -> 1.0D; case II -> 1.35D; case III -> 1.75D; };
        float baseDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
        try {
            target.hurt(player.damageSources().playerAttack(player), (float)(baseDamage * multiplier));
        } finally {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
        }

        // Visual outbound/return path. The forged item stays in hand so it cannot be lost/desynced.
        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            Vec3 from = player.getEyePosition();
            Vec3 to = target.getEyePosition();
            Vec3 delta = to.subtract(from);
            for (int i = 0; i <= 12; i++) {
                double t = i / 12.0D;
                Vec3 p = from.add(delta.scale(t));
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                        p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        }
        startCooldown(player, "BoomerangWeapon", cooldown);
        damageEquipment(player, 3);
    }

    private static void divineBeaconLaser(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.divine(tier); // Configurable; defaults 7.5/5/3 sec.
        if (!ready(player, "DivineBeaconLight", cooldown)) return;

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity target = findLookTarget(player, 24.0D);
        if (target != null) {
            player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(player.damageSources().playerAttack(player), 12.0F);
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
            } finally {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
        }

        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (int i = 1; i <= 24; i++) {
                Vec3 p = start.add(look.scale(i));
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        startCooldown(player, "DivineBeaconLight", cooldown);
        damageEquipment(player, 6);
    }

    private static void ultimateLaser(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.laser(tier);
        if (!ready(player, "UltimateLaserBreaker", cooldown)) return;

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double damage = tier == EffectTier.I ? 8.0D : tier == EffectTier.II ? 12.0D : 16.0D;

        for (int i = 1; i <= 16; i++) {
            Vec3 point = start.add(look.scale(i));
            AABB area = new AABB(point.x - 0.8D, point.y - 0.8D, point.z - 0.8D,
                    point.x + 0.8D, point.y + 0.8D, point.z + 0.8D);
            for (LivingEntity target : player.level().getEntitiesOfClass(
                    LivingEntity.class, area, e -> e != player && e.isAlive())) {
                player.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
                try {
                    target.hurt(player.damageSources().playerAttack(player), (float) damage);
                } finally {
                    player.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
                }
            }
        }
        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                start.x, start.y, start.z, look.x, look.y, look.z);
        startCooldown(player, "UltimateLaserBreaker", cooldown);
        damageEquipment(player, 12);
    }

    private static void natureGodBless(Player player, EffectTier tier) {
        long cooldown = ForgedSkillConfig.nature(tier);
        if (!ready(player, "NatureGodBless", cooldown)) return;

        int chance = tier == EffectTier.I ? 5 : tier == EffectTier.II ? 10 : 20;
        if (player.getRandom().nextInt(100) < chance) {
            player.getInventory().add(new ItemStack(
                    player.getRandom().nextDouble() < 0.1D ? net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE
                            : net.minecraft.world.item.Items.GOLDEN_APPLE));
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Nature God Bless: ได้รับพรจากธรรมชาติ"), true);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Nature God Bless: ไม่ได้รับรางวัล"), true);
        }
        startCooldown(player, "NatureGodBless", cooldown);
        damageEquipment(player, 8);
    }

    private static void openStorage(Player player, ItemStack tool) {
        int size = ForgedStorageMenu.storageSize(tool);
        if (size <= 0) return;
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inv, p) -> new ForgedStorageMenu(id, inv, tool),
                net.minecraft.network.chat.Component.literal("Forged Storage")));
    }

    private static void lineBuilder(Player player, EffectTier tier) {
        long cooldown = 40L; // fixed 2 sec
        if (!ready(player, "LineBuilder", cooldown)) return;
        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) || offhand.isEmpty()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Line Builder: ถือบล็อกไว้มือซ้าย"), true);
            return;
        }
        net.minecraft.core.Direction dir = player.getDirection();
        BlockPos start = player.blockPosition().relative(dir);
        int placed = 0;
        for (int i = 0; i < 5 && !offhand.isEmpty(); i++) {
            BlockPos pos = start.relative(dir, i);
            if (!player.level().getBlockState(pos).canBeReplaced()) continue;
            player.level().setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
            offhand.shrink(1);
            placed++;
        }
        if (placed == 0) return;
        startCooldown(player, "LineBuilder", cooldown);
        damageEquipment(player, switch (tier) { case I -> 5; case II -> 3; case III -> 1; });
    }

    private static void earthyWallRise(Player player, EffectTier tier) {
        long cooldown = switch (tier) { case I -> 160L; case II -> 100L; case III -> 60L; };
        if (!ready(player, "EarthyWallRise", cooldown)) return;
        net.minecraft.core.Direction forward = player.getDirection();
        net.minecraft.core.Direction side = forward.getClockWise();
        BlockPos center = player.blockPosition().relative(forward, 2);
        int placed = 0;
        for (int i = -1; i <= 1; i++) {
            BlockPos pos = center.relative(side, i);
            if (player.level().getBlockState(pos).canBeReplaced()) {
                player.level().setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                placed++;
            }
        }
        if (placed == 0) return;
        startCooldown(player, "EarthyWallRise", cooldown);
        damageEquipment(player, 4);
    }

    private static void toggleSkyBridge(Player player, EffectTier tier) {
        boolean active = !player.getPersistentData().getBoolean("ForgedSkyBridgeActive");
        player.getPersistentData().putBoolean("ForgedSkyBridgeActive", active);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Sky Bridge Walk: " + (active ? "ON" : "OFF")), true);
    }

    private static void toggleAegis(Player player, ItemStack tool, EffectTier tier) {
        boolean active = player.getPersistentData().getBoolean("ForgedAegisActive");
        if (active) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF"), true);
            return;
        }

        player.getPersistentData().putBoolean("ForgedAegisActive", true);
        player.getPersistentData().putLong("ForgedAegisNextDrain", player.level().getGameTime() + 100L);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: ON"), true);
    }

    public static void tickWorldEffects(Player player, ItemStack tool) {
        if (player.level().isClientSide()) return;
        long now = player.level().getGameTime();

        // Keep Time Stop targets fully immobilized for the whole duration.
        AABB freezeArea = player.getBoundingBox().inflate(12.0D);
        for (Monster target : player.level().getEntitiesOfClass(Monster.class, freezeArea,
                e -> e.getPersistentData().getLong("ForgedTimeStopUntil") > now)) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            target.setTarget(null);
        }

        if (player.getPersistentData().getBoolean("ForgedSkyBridgeActive")) {
            EffectTier sky = ForgedEffectRuntime.tier(tool, ForgingEffect.SKY_BRIDGE_WALK);
            if (sky == null) {
                player.getPersistentData().putBoolean("ForgedSkyBridgeActive", false);
            } else {
                BlockPos below = player.blockPosition().below();
                if (player.level().getBlockState(below).canBeReplaced()) {
                    int cost = switch (sky) { case I -> 3; case II -> 2; case III -> 1; };
                    if (!tool.isDamageableItem() || tool.getDamageValue() + cost < tool.getMaxDamage()) {
                        player.level().setBlockAndUpdate(below, Blocks.COBBLESTONE.defaultBlockState());
                        damageEquipment(player, cost);
                    } else {
                        player.getPersistentData().putBoolean("ForgedSkyBridgeActive", false);
                    }
                }
            }
        }

        // Nature God Bless passive: while the forged tool is held, nearby crops receive
        // extra random growth ticks. Tier does NOT increase the area.
        EffectTier nature = ForgedEffectRuntime.tier(tool, ForgingEffect.NATURE_GOD_BLESS);
        if (nature != null && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && now % 10L == 0L) {
            int attempts = switch (nature) { case I -> 1; case II -> 2; case III -> 3; };
            net.minecraft.core.BlockPos center = player.blockPosition();
            java.util.List<net.minecraft.core.BlockPos> crops = new java.util.ArrayList<>();
            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                    center.offset(-4, -1, -4), center.offset(4, 2, 4))) {
                if (serverLevel.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.CropBlock) {
                    crops.add(pos.immutable());
                }
            }
            for (int i = 0; i < attempts && !crops.isEmpty(); i++) {
                net.minecraft.core.BlockPos pos = crops.get(player.getRandom().nextInt(crops.size()));
                net.minecraft.world.level.block.state.BlockState state = serverLevel.getBlockState(pos);
                if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(state)) {
                    net.minecraft.world.item.BoneMealItem.growCrop(
                            new ItemStack(net.minecraft.world.item.Items.BONE_MEAL),
                            serverLevel, pos);
                }
            }
        }
    }

    public static void tickAegis(Player player, ItemStack tool) {
        if (player.level().isClientSide()) return;
        if (!player.getPersistentData().getBoolean("ForgedAegisActive")) return;

        EffectTier tier = ForgedEffectRuntime.tier(tool, ForgingEffect.AEGIS_SHIELD);
        if (tier == null) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF (effect missing)"), true);
            return;
        }

        long now = player.level().getGameTime();
        long nextDrain = player.getPersistentData().getLong("ForgedAegisNextDrain");
        if (now < nextDrain) return;

        int cost = tierValue(tier, AEGIS_DRAIN);
        if (tool.getDamageValue() + cost >= tool.getMaxDamage()) {
            player.getPersistentData().putBoolean("ForgedAegisActive", false);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Aegis Shield: OFF (durability)"), true);
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
                 HARPOON_PULL, MOB_SWAP, AIR_SLASH_RUPTURE, LAVA_WAVE,
                 STUN_TIME_STOP, IRON_FORTRESS_GUARD, BOOMERANG_WEAPON, DIVINE_BEACON_LIGHT,
                 ULTIMATE_LASER_BREAKER, NATURE_GOD_BLESS, LINE_BUILDER,
                 EARTHY_WALL_RISE, SKY_BRIDGE_WALK, POCKET_DIMENSION, INTERNAL_STORAGE -> true;
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
            if (dot < 0.80D) continue;

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
