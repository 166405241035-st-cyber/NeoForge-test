package com.example.examplemod.guide;

import com.example.examplemod.ExampleMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Delivers the guide after the player's inventory has loaded into the world. */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgingGuideStarterEvents {
    private static final String JOURNAL_KEY = "examplemod_forging_guide";
    private static final String DELIVERED_KEY = "starterBookDeliveredV2";
    private ForgingGuideStarterEvents() {}

    @SubscribeEvent
    public static void afterPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        CompoundTag journal = player.getPersistentData().getCompound(JOURNAL_KEY);
        if (journal.getBoolean(DELIVERED_KEY)) return;

        // This also recognizes a book already supplied by the earlier login handler.
        boolean delivered = player.getInventory().items.stream().anyMatch(stack -> stack.is(ExampleMod.FORGING_GUIDE.get()))
                || player.getInventory().offhand.stream().anyMatch(stack -> stack.is(ExampleMod.FORGING_GUIDE.get()));
        if (!delivered) {
            ItemStack book = new ItemStack(ExampleMod.FORGING_GUIDE.get());
            delivered = player.getInventory().add(book);
            if (!delivered) delivered = player.drop(book, false) != null;
        }
        if (delivered) {
            journal.putBoolean(DELIVERED_KEY, true);
            player.getPersistentData().put(JOURNAL_KEY, journal);
        }
    }
}
