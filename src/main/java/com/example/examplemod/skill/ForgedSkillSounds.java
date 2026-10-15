package com.example.examplemod.skill;

import com.example.examplemod.MonsterMaterial;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/** Short vanilla sound cues for successful forged-skill triggers. Server playback reaches nearby players. */
public final class ForgedSkillSounds {
    private static final int MIN_INTERVAL_TICKS = 10;
    private ForgedSkillSounds() {}

    public static void play(Player player, ForgingEffect effect) {
        if (player.level().isClientSide()) return;
        long now = player.level().getGameTime();
        String key = "ForgedSound_" + effect.name();
        if (player.getPersistentData().contains(key)
                && now - player.getPersistentData().getLong(key) < MIN_INTERVAL_TICKS) return;
        player.getPersistentData().putLong(key, now);

        SoundEvent sound = soundFor(effect.material());
        EffectTier tier = ForgedEffectRuntime.tier(player.getMainHandItem(), effect);
        float pitch = tier == null ? 1.0F : switch (tier) {
            case I -> 0.88F;
            case II -> 1.0F;
            case III -> 1.13F;
        };
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 0.48F, pitch);
    }

    private static SoundEvent soundFor(MonsterMaterial material) {
        return switch (material) {
            case ROTTEN_FLESH, BONE, PHANTOM -> SoundEvents.NOTE_BLOCK_BASS.value();
            case STRING, GHAST_TEAR, NETHER_STAR -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case GUNPOWDER, BLAZE_ROD -> SoundEvents.BLAZE_SHOOT;
            case SLIME -> SoundEvents.SLIME_BLOCK_PLACE;
            case ENDER, DRAGON_BREATH -> SoundEvents.PLAYER_TELEPORT;
            case WITHER -> SoundEvents.WITHER_SHOOT;
            case SHULKER -> SoundEvents.ANVIL_USE;
        };
    }
}
