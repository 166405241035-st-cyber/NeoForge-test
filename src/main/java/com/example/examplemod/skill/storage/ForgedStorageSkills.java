package com.example.examplemod.skill.storage;

import com.example.examplemod.menu.ForgedStorageMenu;
import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Pocket storage activation and preservation when the player is cloned. */
public final class ForgedStorageSkills {
    private ForgedStorageSkills() {}

    public static void openStorage(Player player, ItemStack tool) {
        int size = ForgedStorageMenu.storageSize(tool);
        if (size <= 0) return;
        ForgedSkillSounds.play(player, ForgedEffectRuntime.tier(tool, ForgingEffect.POCKET_DIMENSION) != null
                ? ForgingEffect.POCKET_DIMENSION : ForgingEffect.INTERNAL_STORAGE);
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inv, p) -> new ForgedStorageMenu(id, inv, tool),
                net.minecraft.network.chat.Component.literal("Forged Storage")));
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldData = event.getOriginal().getPersistentData();
        if (oldData.contains("forgedPocketDimension", net.minecraft.nbt.Tag.TAG_LIST)) {
            event.getEntity().getPersistentData().put(
                    "forgedPocketDimension",
                    oldData.getList("forgedPocketDimension", net.minecraft.nbt.Tag.TAG_COMPOUND).copy());
        }
    }
}
