package com.example.examplemod;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Prototype final equipment item produced after the anvil Rhythm minigame. */
public class ForgedEquipmentItem extends Item {
    private static final ResourceLocation FORGED_ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "forged_attack_damage");

    public ForgedEquipmentItem(Properties properties) { super(properties); }

    public ItemStack createStack(AnvilAssemblyResult assembly) {
        MonsterMaterial headMaterial = assembly.headMaterial();
        MonsterMaterial coreMaterial = assembly.coreMaterial();
        MonsterMaterial rodMaterial = assembly.rodMaterial();
        int durability = calculateDurability(assembly.blueprint(), assembly.headMetal(), assembly.coreMetal(), assembly.rodMetal());
        double attackDamage = calculateAttackDamage(assembly.blueprint(), assembly.headMetal(), assembly.coreMetal(), assembly.rodMetal());
        ItemStack stack = new ItemStack(this);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString("blueprint", assembly.blueprint().name());
            tag.putString("headMetal", assembly.headMetal().name());
            tag.putString("coreMetal", assembly.coreMetal().name());
            tag.putString("rodMetal", assembly.rodMetal().name());
            tag.putInt("forgedDurability", durability);
            tag.putDouble("forgedAttackDamage", attackDamage);
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

        stack.set(DataComponents.MAX_DAMAGE, durability);
        stack.set(DataComponents.DAMAGE, 0);
        configureMiningTool(stack, assembly.blueprint(), assembly.headMetal());

        double modifierDamage = Math.max(0.0D, attackDamage - 1.0D);
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(FORGED_ATTACK_DAMAGE_ID, modifierDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);

        stack.set(DataComponents.CUSTOM_NAME, Component.literal(equipmentName(assembly.blueprint()) + " "
                + shortName(headMaterial) + "+" + shortName(coreMaterial) + "+" + shortName(rodMaterial)));
        return stack;
    }

    /** Gives the generic forged item real mining behavior based on its Head. */
    private static void configureMiningTool(ItemStack stack, HeadBlueprintType type, ForgingMetal headMetal) {
        TagKey<Block> mineableTag = switch (type) {
            case PICKAXE -> BlockTags.MINEABLE_WITH_PICKAXE;
            case AXE -> BlockTags.MINEABLE_WITH_AXE;
            case SHOVEL -> BlockTags.MINEABLE_WITH_SHOVEL;
            case HOE -> BlockTags.MINEABLE_WITH_HOE;
            case SWORD -> null;
        };
        if (mineableTag != null) stack.set(DataComponents.TOOL, vanillaTier(headMetal).createToolProperties(mineableTag));
    }

    /**
     * Vanilla-style right-click actions are routed by the forged Head Blueprint.
     * We intentionally reuse vanilla Axe/Shovel/Hoe implementations so stripped
     * logs, dirt paths, farmland, campfire extinguishing and other supported
     * vanilla interactions stay consistent with Minecraft.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        HeadBlueprintType type = readBlueprint(context.getItemInHand());
        ForgingMetal metal = readHeadMetal(context.getItemInHand());
        if (type == null || metal == null) return InteractionResult.PASS;

        Tier tier = vanillaTier(metal);
        Item vanillaTool = switch (type) {
            case AXE -> new AxeItem(tier, new Item.Properties());
            case SHOVEL -> new ShovelItem(tier, new Item.Properties());
            case HOE -> new HoeItem(tier, new Item.Properties());
            default -> null;
        };
        if (vanillaTool == null) return InteractionResult.PASS;

        // Vanilla code damages context.getItemInHand(), so the forged item itself
        // loses durability exactly like the corresponding vanilla tool action.
        return vanillaTool.useOn(context);
    }

    /** Mining strength follows the metal used for the Head. */
    private static Tier vanillaTier(ForgingMetal metal) {
        return switch (metal) {
            case GOLD -> Tiers.GOLD;
            case IRON -> Tiers.IRON;
            case DIAMOND -> Tiers.DIAMOND;
            case NETHERITE -> Tiers.NETHERITE;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        var tag = data.copyTag();
        MonsterMaterial head = readMaterial(tag.getString("headMaterial"));
        MonsterMaterial core = readMaterial(tag.getString("coreMaterial"));
        MonsterMaterial rod = readMaterial(tag.getString("rodMaterial"));

        int durability = tag.getInt("forgedDurability");
        if (durability > 0) {
            int remaining = Math.max(0, durability - stack.getDamageValue());
            tooltip.add(Component.literal("Durability: " + remaining + " / " + durability).withStyle(ChatFormatting.GRAY));
        }
        double attackDamage = tag.getDouble("forgedAttackDamage");
        tooltip.add(Component.literal("Attack Damage: " + formatDamage(attackDamage)).withStyle(ChatFormatting.GRAY));

        if (head != null && core != null && rod != null) {
            tooltip.add(Component.literal("Materials: " + displayName(head) + " + " + displayName(core) + " + " + displayName(rod)).withStyle(ChatFormatting.GRAY));
        }

        int count = tag.getInt("effectCount");
        if (count > 0) tooltip.add(Component.literal("Effects:").withStyle(ChatFormatting.GOLD));
        for (int i = 0; i < count; i++) {
            try {
                ForgingEffect effect = ForgingEffect.valueOf(tag.getString("effect" + i));
                EffectTier effectTier = EffectTier.valueOf(tag.getString("tier" + i));
                tooltip.add(Component.literal("  " + effect.displayName() + " " + effectTier.name()).withStyle(ChatFormatting.GREEN));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public static int calculateDurability(HeadBlueprintType type, ForgingMetal head, ForgingMetal core, ForgingMetal rod) {
        double total = vanillaDurability(type, head) / 3.0D + vanillaDurability(type, core) / 3.0D + vanillaDurability(type, rod) / 3.0D;
        return Math.max(1, (int)Math.round(total));
    }

    private static int vanillaDurability(HeadBlueprintType type, ForgingMetal metal) {
        return switch (metal) { case GOLD -> 32; case IRON -> 250; case DIAMOND -> 1561; case NETHERITE -> 2031; };
    }

    public static double calculateAttackDamage(HeadBlueprintType type, ForgingMetal head, ForgingMetal core, ForgingMetal rod) {
        return vanillaAttackDamage(type, head) / 3.0D + vanillaAttackDamage(type, core) / 3.0D + vanillaAttackDamage(type, rod) / 3.0D;
    }

    private static double vanillaAttackDamage(HeadBlueprintType type, ForgingMetal metal) {
        return switch (type) {
            case SWORD -> switch (metal) { case GOLD -> 4.0D; case IRON -> 6.0D; case DIAMOND -> 7.0D; case NETHERITE -> 8.0D; };
            case AXE -> switch (metal) { case GOLD -> 7.0D; case IRON -> 9.0D; case DIAMOND -> 9.0D; case NETHERITE -> 10.0D; };
            case PICKAXE -> switch (metal) { case GOLD -> 2.0D; case IRON -> 4.0D; case DIAMOND -> 5.0D; case NETHERITE -> 6.0D; };
            case SHOVEL -> switch (metal) { case GOLD -> 2.5D; case IRON -> 4.5D; case DIAMOND -> 5.5D; case NETHERITE -> 6.5D; };
            case HOE -> 1.0D;
        };
    }

    public static double readAttackDamage(ItemStack stack) { CustomData data=stack.get(DataComponents.CUSTOM_DATA);return data==null?0.0D:data.copyTag().getDouble("forgedAttackDamage"); }
    public static HeadBlueprintType readBlueprint(ItemStack stack) { CustomData data=stack.get(DataComponents.CUSTOM_DATA);return data==null?null:readBlueprint(data.copyTag().getString("blueprint")); }
    public static ForgingMetal readHeadMetal(ItemStack stack) { CustomData data=stack.get(DataComponents.CUSTOM_DATA);return data==null?null:readMetal(data.copyTag().getString("headMetal")); }
    public static ForgingMetal readCoreMetal(ItemStack stack) { CustomData data=stack.get(DataComponents.CUSTOM_DATA);return data==null?null:readMetal(data.copyTag().getString("coreMetal")); }
    public static ForgingMetal readRodMetal(ItemStack stack) { CustomData data=stack.get(DataComponents.CUSTOM_DATA);return data==null?null:readMetal(data.copyTag().getString("rodMetal")); }
    public static int effectCount(ItemStack stack) { CustomData data=stack.get(DataComponents.CUSTOM_DATA);return data==null?0:Math.max(0,data.copyTag().getInt("effectCount")); }
    public static AnvilAssemblyResult.FinalEffect readEffect(ItemStack stack,int index){CustomData data=stack.get(DataComponents.CUSTOM_DATA);if(data==null||index<0)return null;var tag=data.copyTag();if(index>=tag.getInt("effectCount"))return null;try{return new AnvilAssemblyResult.FinalEffect(ForgingEffect.valueOf(tag.getString("effect"+index)),EffectTier.valueOf(tag.getString("tier"+index)));}catch(IllegalArgumentException ex){return null;}}

    private static MonsterMaterial readMaterial(String value){try{return MonsterMaterial.valueOf(value);}catch(IllegalArgumentException ex){return null;}}
    private static ForgingMetal readMetal(String value){try{return ForgingMetal.valueOf(value);}catch(IllegalArgumentException ex){return null;}}
    private static HeadBlueprintType readBlueprint(String value){try{return HeadBlueprintType.valueOf(value);}catch(IllegalArgumentException ex){return null;}}
    private static String equipmentName(HeadBlueprintType type){return switch(type){case SWORD->"Sword";case AXE->"Axe";case PICKAXE->"Pickaxe";case SHOVEL->"Shovel";case HOE->"Hoe";};}
    private static String shortName(MonsterMaterial material){return switch(material){case ROTTEN_FLESH->"Ro";case BONE->"Bo";case STRING->"St";case GUNPOWDER->"Gu";case SLIME->"Sl";case ENDER->"En";case BLAZE_ROD->"Bl";case GHAST_TEAR->"Gh";case WITHER->"Wi";case PHANTOM->"Ph";case DRAGON_BREATH->"Dr";case SHULKER->"Sh";case NETHER_STAR->"Ne";};}
    private static String displayName(MonsterMaterial material){String[] words=material.name().toLowerCase().split("_");StringBuilder result=new StringBuilder();for(String word:words){if(!result.isEmpty())result.append(' ');result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}return result.toString();}
    private static String formatDamage(double value){return Math.abs(value-Math.rint(value))<0.0001D?Integer.toString((int)Math.rint(value)):String.format("%.2f",value);}
}
