package com.example.examplemod;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/** Generic forged rod. Like Core, the named effect is intentionally not revealed or stored yet. */
public class ForgedRodItem extends Item {
    private static final String KEY_METAL = "metal";
    private static final String KEY_MONSTER = "monster_material";
    private static final String KEY_TIER = "tier";

    public ForgedRodItem(Properties properties) { super(properties); }

    public static ItemStack create(ForgedRodResult result) {
        ItemStack stack = new ItemStack(ExampleMod.FORGED_ROD_ITEM.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_METAL, result.metal().name());
        tag.putString(KEY_MONSTER, result.monsterMaterial().name());
        tag.putString(KEY_TIER, result.tier().name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        if (tag == null || !tag.contains(KEY_METAL)) return Component.literal("Forged Rod");
        return Component.literal(formatName(tag.getString(KEY_METAL)) + " Forged Rod");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag tag = getTag(stack);
        if (tag != null) {
            if (tag.contains(KEY_METAL)) tooltipComponents.add(Component.literal("Metal: " + formatName(tag.getString(KEY_METAL))).withStyle(ChatFormatting.GRAY));
            if (tag.contains(KEY_MONSTER)) tooltipComponents.add(Component.literal("Monster Material: " + formatName(tag.getString(KEY_MONSTER))).withStyle(ChatFormatting.DARK_GRAY));
            if (tag.contains(KEY_TIER)) tooltipComponents.add(Component.literal("Effect " + tag.getString(KEY_TIER)).withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static ForgedRodResult readResult(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        if (tag == null) return null;
        try {
            return new ForgedRodResult(ForgingMetal.valueOf(tag.getString(KEY_METAL)), MonsterMaterial.valueOf(tag.getString(KEY_MONSTER)), EffectTier.valueOf(tag.getString(KEY_TIER)));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static CompoundTag getTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.copyTag();
    }

    private static String formatName(String value) {
        String[] words = value.toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }
}
