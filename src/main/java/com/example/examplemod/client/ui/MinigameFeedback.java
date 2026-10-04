package com.example.examplemod.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

/** Short vanilla sound cues shared by the two forging minigames. */
public final class MinigameFeedback {
    private MinigameFeedback() {}

    public static void hit(int grade) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        minecraft.player.playSound(SoundEvents.ANVIL_USE, 0.35F, grade == 0 ? 1.35F : grade == 1 ? 1.1F : 0.85F);
        if (grade == 0) minecraft.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.28F, 1.55F);
    }

    public static void miss() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.35F, 0.7F);
    }

    public static void complete() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.45F, 1.2F);
    }
}
