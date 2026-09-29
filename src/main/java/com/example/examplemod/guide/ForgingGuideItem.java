package com.example.examplemod.guide;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ForgingGuideItem extends Item {
    public ForgingGuideItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        if (level.isClientSide()) ForgingGuideScreen.open();
        return InteractionResultHolder.sidedSuccess(book, level.isClientSide());
    }
}
