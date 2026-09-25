package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Server-controlled boomerang projectile. It deliberately does not use
 * ThrownTrident collision state: mobs are damaged without stopping the flight,
 * and a block collision switches the projectile into return mode.
 */
public class ForgedBoomerangEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DISPLAY_STACK =
            SynchedEntityData.defineId(ForgedBoomerangEntity.class, EntityDataSerializers.ITEM_STACK);

    private Player owner;
    private ItemStack weapon = ItemStack.EMPTY;
    private double damage;
    private boolean returning;
    private int age;

    public ForgedBoomerangEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public ForgedBoomerangEntity(Level level, Player owner, ItemStack weapon, double damage) {
        this(ExampleMod.FORGED_BOOMERANG.get(), level);
        this.owner = owner;
        this.weapon = weapon.copy();
        this.weapon.setCount(1);
        this.entityData.set(DISPLAY_STACK, this.weapon.copy());
        this.damage = damage;
        setPos(owner.getX(), owner.getEyeY() - 0.15D, owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }

        age++;
        Vec3 oldPos = position();
        Vec3 velocity = getDeltaMovement();

        if (!returning && age >= 20) returning = true;

        if (returning) {
            // Aim above the floor at the owner's upper body, so a ground impact
            // cannot make the boomerang drag horizontally along the ground.
            Vec3 target = owner.position().add(0.0D, owner.getBbHeight() * 0.75D, 0.0D);
            Vec3 home = target.subtract(position());
            if (home.lengthSqr() <= 2.25D) {
                giveBack();
                return;
            }
            velocity = home.normalize().scale(0.95D);
        } else {
            // Detect solid blocks ourselves because noPhysics keeps entity hits from
            // converting this projectile into vanilla trident's embedded state.
            Vec3 next = oldPos.add(velocity);
            var hit = level().clip(new net.minecraft.world.level.ClipContext(
                    oldPos, next,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, this));
            if (hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                returning = true;
                velocity = new Vec3(velocity.x, Math.max(0.35D, Math.abs(velocity.y) + 0.25D), velocity.z)
                        .normalize().scale(0.95D);
            }
        }

        Vec3 nextPos = oldPos.add(velocity);
        AABB path = getBoundingBox().expandTowards(velocity).inflate(0.65D);
        String phase = returning ? "Return_" : "Out_";
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, path,
                e -> e != owner && e.isAlive())) {
            String key = "ForgedBoomerangHit_" + phase + target.getUUID();
            if (getPersistentData().getBoolean(key)) continue;
            getPersistentData().putBoolean(key, true);
            owner.getPersistentData().putBoolean("ForgedEffectDamageGuard", true);
            try {
                target.hurt(owner.damageSources().playerAttack(owner), (float) damage);
            } finally {
                owner.getPersistentData().putBoolean("ForgedEffectDamageGuard", false);
            }
        }

        setDeltaMovement(velocity);
        setPos(nextPos.x, nextPos.y, nextPos.z);
        if (age > 240) giveBack();
    }

    private void giveBack() {
        if (!owner.getAbilities().instabuild && !owner.getInventory().add(weapon.copy()))
            owner.drop(weapon.copy(), false);
        discard();
    }

    public ItemStack getDisplayStack() {
        return this.entityData.get(DISPLAY_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DISPLAY_STACK, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
