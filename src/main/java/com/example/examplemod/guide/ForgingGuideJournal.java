package com.example.examplemod.guide;

import com.example.examplemod.AnvilAssemblyResult;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.ForgedHeadResult;
import com.example.examplemod.item.ForgedEquipmentItem;
import com.example.examplemod.item.ForgedHeadItem;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import com.example.examplemod.skill.curse.ForgedCurse;
import com.example.examplemod.skill.curse.ForgedCurseRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Per-player discoveries, stored in persistent player NBT. Each bit records one seen Tier. */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgingGuideJournal {
    private static final String KEY = "examplemod_forging_guide";
    private ForgingGuideJournal() {}

    public static boolean hasCompletedAnvil(Player player) {
        return player.getPersistentData().getCompound(KEY).getBoolean("completedAnvil");
    }

    public static void markAnvilCompleted(Player player) {
        CompoundTag data = player.getPersistentData().getCompound(KEY);
        data.putBoolean("completedAnvil", true);
        player.getPersistentData().put(KEY, data);
    }

    public static void record(Player player, ItemStack reward) {
        if (!(player instanceof ServerPlayer)) return;
        CompoundTag data = player.getPersistentData().getCompound(KEY);
        int[] tiers = data.getIntArray("tierMasks");
        if (tiers.length != ForgingEffect.values().length) tiers = new int[ForgingEffect.values().length];
        if (reward.is(ExampleMod.FORGED_HEAD_ITEM.get())) {
            ForgedHeadResult head = ForgedHeadItem.readResult(reward);
            if (head != null) tiers[head.effect().ordinal()] |= 1 << (head.tier().level() - 1);
        } else if (reward.is(ExampleMod.FORGED_EQUIPMENT_ITEM.get())) {
            for (int i = 0; i < Math.min(3, ForgedEquipmentItem.effectCount(reward)); i++) {
                AnvilAssemblyResult.FinalEffect effect = ForgedEquipmentItem.readEffect(reward, i);
                if (effect != null) tiers[effect.effect().ordinal()] |= 1 << (effect.tier().level() - 1);
            }
            ForgedBlessing blessing = ForgedBlessingRuntime.get(reward);
            ForgedCurse curse = ForgedCurseRuntime.get(reward);
            if (blessing != null) data.putInt("blessings", data.getInt("blessings") | 1 << blessing.ordinal());
            if (curse != null) data.putInt("curses", data.getInt("curses") | 1 << curse.ordinal());
        }
        data.putIntArray("tierMasks", tiers);
        player.getPersistentData().put(KEY, data);
    }

    public static String snapshot(Player player) {
        CompoundTag data = player.getPersistentData().getCompound(KEY);
        int[] tiers = data.getIntArray("tierMasks");
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < ForgingEffect.values().length; i++) {
            if (i > 0) value.append(',');
            value.append(i < tiers.length ? tiers[i] & 7 : 0);
        }
        return value.append(';').append(data.getInt("blessings")).append(';').append(data.getInt("curses")).toString();
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        CompoundTag old = event.getOriginal().getPersistentData();
        if (old.contains(KEY)) event.getEntity().getPersistentData().put(KEY, old.getCompound(KEY).copy());
    }
}
