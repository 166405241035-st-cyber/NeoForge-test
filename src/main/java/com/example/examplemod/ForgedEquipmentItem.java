package com.example.examplemod;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/** Prototype final equipment item produced after the anvil Rhythm minigame. */
public class ForgedEquipmentItem extends Item {
    public ForgedEquipmentItem(Properties properties) { super(properties); }

    public ItemStack createStack(AnvilAssemblyResult assembly) {
        MonsterMaterial headMaterial = assembly.headMaterial();
        MonsterMaterial coreMaterial = assembly.coreMaterial();
        MonsterMaterial rodMaterial = assembly.rodMaterial();
        ItemStack stack = new ItemStack(this);

        // Keep every important forging value on the final item so later systems
        // can read the metal, equipment type, source materials and effects.
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString("blueprint", assembly.blueprint().name());
            tag.putString("metal", assembly.metal().name());
            tag.putString("headMaterial", headMaterial.name());
            tag.putString("coreMaterial", coreMaterial.name());
            tag.putString("rodMaterial", rodMaterial.name());
            tag.putInt("effectCount", assembly.effects().size());
            for (int i = 0; i < assembly.effects().size(); i++) {
                AnvilAssemblyResult.FinalEffect effect = assembly.effects().get(i);
                tag.putString("effect" + i, effect.effect().name());
                tag.putString("tier" + i, effect.tier().name());
            }
        });

        stack.set(DataComponents.CUSTOM_NAME, Component.literal(equipmentName(assembly.blueprint()) + " "
                + shortName(headMaterial) + "+" + shortName(coreMaterial) + "+" + shortName(rodMaterial)));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        var tag = data.copyTag();

        ForgingMetal metal = readMetal(tag.getString("metal"));
        HeadBlueprintType blueprint = readBlueprint(tag.getString("blueprint"));
        MonsterMaterial head = readMaterial(tag.getString("headMaterial"));
        MonsterMaterial core = readMaterial(tag.getString("coreMaterial"));
        MonsterMaterial rod = readMaterial(tag.getString("rodMaterial"));

        if (blueprint != null) {
            tooltip.add(Component.literal("Type: " + equipmentName(blueprint)).withStyle(ChatFormatting.GRAY));
        }
        if (metal != null) {
            tooltip.add(Component.literal("Metal: " + metal.displayName()).withStyle(ChatFormatting.GRAY));
        }
        if (head != null && core != null && rod != null) {
            tooltip.add(Component.literal("Materials: " + displayName(head) + " + " + displayName(core) + " + " + displayName(rod))
                    .withStyle(ChatFormatting.GRAY));
        }

        int count = tag.getInt("effectCount");
        if (count > 0) tooltip.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
        for (int i = 0; i < count; i++) {
            try {
                ForgingEffect effect = ForgingEffect.valueOf(tag.getString("effect" + i));
                EffectTier tier = EffectTier.valueOf(tag.getString("tier" + i));
                tooltip.add(Component.literal("  " + effect.displayName() + " " + tier.name()).withStyle(ChatFormatting.GREEN));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /** Read the equipment type stored on a forged final item. */
    public static HeadBlueprintType readBlueprint(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : readBlueprint(data.copyTag().getString("blueprint"));
    }

    /** Read the metal stored on a forged final item. */
    public static ForgingMetal readMetal(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : readMetal(data.copyTag().getString("metal"));
    }

    /** Number of final effects stored on the item. */
    public static int effectCount(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : Math.max(0, data.copyTag().getInt("effectCount"));
    }

    /** Read one final effect. Returns null when the slot is invalid. */
    public static AnvilAssemblyResult.FinalEffect readEffect(ItemStack stack, int index) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || index < 0) return null;
        var tag = data.copyTag();
        if (index >= tag.getInt("effectCount")) return null;
        try {
            return new AnvilAssemblyResult.FinalEffect(
                    ForgingEffect.valueOf(tag.getString("effect" + index)),
                    EffectTier.valueOf(tag.getString("tier" + index)));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static MonsterMaterial readMaterial(String value) { try { return MonsterMaterial.valueOf(value); } catch (IllegalArgumentException ex) { return null; } }
    private static ForgingMetal readMetal(String value) { try { return ForgingMetal.valueOf(value); } catch (IllegalArgumentException ex) { return null; } }
    private static HeadBlueprintType readBlueprint(String value) { try { return HeadBlueprintType.valueOf(value); } catch (IllegalArgumentException ex) { return null; } }
    private static String equipmentName(HeadBlueprintType type) { return switch (type) { case SWORD -> "Sword"; case AXE -> "Axe"; case PICKAXE -> "Pickaxe"; case SHOVEL -> "Shovel"; case HOE -> "Hoe"; }; }
    private static String shortName(MonsterMaterial material) { return switch (material) { case ROTTEN_FLESH -> "Ro"; case BONE -> "Bo"; case STRING -> "St"; case GUNPOWDER -> "Gu"; case SLIME -> "Sl"; case ENDER -> "En"; case BLAZE_ROD -> "Bl"; case GHAST_TEAR -> "Gh"; case WITHER -> "Wi"; case PHANTOM -> "Ph"; case DRAGON_BREATH -> "Dr"; case SHULKER -> "Sh"; case NETHER_STAR -> "Ne"; }; }
    private static String displayName(MonsterMaterial material) { String[] words=material.name().toLowerCase().split("_");StringBuilder result=new StringBuilder();for(String word:words){if(!result.isEmpty())result.append(' ');result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}return result.toString(); }
}
