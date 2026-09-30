package com.example.examplemod;

import com.example.examplemod.guide.ForgingGuideJournal;
import com.example.examplemod.skill.ForgedEffectNetwork;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import com.example.examplemod.skill.curse.ForgedCurse;
import com.example.examplemod.skill.curse.ForgedCurseRuntime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-owned assembly and one-time reward for an anvil attempt. */
public final class AnvilRewardSession {
    private static final long TIMEOUT_MS = 10 * 60 * 1000L;
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private record Pending(AnvilAssemblyResult assembly, long createdAt) {}
    private AnvilRewardSession() {}

    public static void begin(ServerPlayer player, ForgedHeadResult head, ForgedCoreResult core, ForgedRodResult rod) {
        long now = System.currentTimeMillis();
        PENDING.entrySet().removeIf(entry -> now - entry.getValue().createdAt() > TIMEOUT_MS);
        AnvilAssemblyResult assembly = AnvilAssemblyResult.roll(head, core, rod,
                new java.util.Random(player.getRandom().nextLong()));
        PENDING.put(player.getUUID(), new Pending(assembly, now));
        PacketDistributor.sendToPlayer(player, new ForgedEffectNetwork.AnvilStartPayload(
                ExampleMod.FORGED_EQUIPMENT_ITEM.get().createStack(assembly)));
    }

    public static void finish(ServerPlayer player, int accuracyHundredths, int maxCombo, int perfectCount, int misses) {
        Pending pending = PENDING.remove(player.getUUID());
        if (pending == null || System.currentTimeMillis() - pending.createdAt() > TIMEOUT_MS) return;
        // Scores come from the local minigame. Bound them to possible values; rewards and RNG stay on the server.
        double accuracy = Math.max(0, Math.min(10000, accuracyHundredths)) / 100.0D;
        maxCombo = Math.max(0, Math.min(10, maxCombo));
        perfectCount = Math.max(0, Math.min(10, perfectCount));
        misses = Math.max(0, Math.min(10, misses));
        String rank;
        double chance;
        if (accuracy >= 90 && maxCombo >= 8) { rank = "S"; chance = .90; }
        else if (accuracy >= 80) { rank = "A"; chance = .75; }
        else if (accuracy >= 65) { rank = "B"; chance = .60; }
        else if (accuracy >= 50) { rank = "C"; chance = .45; }
        else { rank = "D"; chance = .10; }
        chance -= misses * .05;
        if (perfectCount >= 7) chance += .05;
        if (maxCombo >= 10) chance += .10;
        chance = Math.max(.05, Math.min(.95, chance));

        ItemStack equipment = ExampleMod.FORGED_EQUIPMENT_ITEM.get().createStack(pending.assembly());
        if (player.getRandom().nextDouble() < chance) {
            boolean supportsDouble = pending.assembly().effects().stream()
                    .anyMatch(effect -> DoubleTriggerRuntime.supports(effect.effect()));
            ArrayList<ForgedBlessing> pool = new ArrayList<>();
            for (ForgedBlessing blessing : ForgedBlessing.values()) {
                if (blessing != ForgedBlessing.DOUBLE_TRIGGER || supportsDouble) pool.add(blessing);
            }
            ForgedBlessingRuntime.set(equipment, pool.get(player.getRandom().nextInt(pool.size())));
        } else {
            ForgedCurse[] pool = ForgedCurse.values();
            ForgedCurseRuntime.set(equipment, pool[player.getRandom().nextInt(pool.length)]);
        }
        ItemStack reward = equipment.copy();
        if (!player.getInventory().add(reward)) player.drop(reward, false);
        ForgingGuideJournal.record(player, equipment);
        PacketDistributor.sendToPlayer(player, new ForgedEffectNetwork.AnvilResultPayload(
                equipment, rank, (int) Math.round(chance * 1000)));
    }
}
