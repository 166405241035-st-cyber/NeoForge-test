package com.example.examplemod.item;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

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


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (ForgedEffectRuntime.tier(stack, ForgingEffect.BOOMERANG_WEAPON) == null)
            return super.use(level, player, hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return ForgedEffectRuntime.tier(stack, ForgingEffect.BOOMERANG_WEAPON) != null ? 72000 : super.getUseDuration(stack, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return ForgedEffectRuntime.tier(stack, ForgingEffect.BOOMERANG_WEAPON) != null ? UseAnim.SPEAR : super.getUseAnimation(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        EffectTier tier = ForgedEffectRuntime.tier(stack, ForgingEffect.BOOMERANG_WEAPON);
        if (tier == null || !(living instanceof Player player)) {
            super.releaseUsing(stack, level, living, timeLeft);
            return;
        }

        int charged = getUseDuration(stack, living) - timeLeft;
        if (charged < 10 || level.isClientSide()) return;

        ItemStack thrownStack = stack.copy();
        thrownStack.setCount(1);
        double multiplier = switch (tier) {
            case I -> 1.0D;
            case II -> 1.35D;
            case III -> 1.75D;
        };
        double throwDamage = readAttackDamage(stack) * multiplier;
        ForgedBoomerangEntity projectile = new ForgedBoomerangEntity(level, player, thrownStack, throwDamage);
        Vec3 direction = player.getLookAngle().normalize();
        projectile.setDeltaMovement(direction.scale(2.5D));
        level.addFreshEntity(projectile);

        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    /** Expose the same NeoForge ItemAbilities as the vanilla tool selected by the Head. */
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        HeadBlueprintType type = readBlueprint(stack);
        if (type == null) return false;
        return switch (type) {
            case AXE -> ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability);
            case SHOVEL -> ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability);
            case HOE -> ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability);
            case PICKAXE -> ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability);
            case SWORD -> ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(ability);
        };
    }

    /**
     * Perform right-click block modification directly through NeoForge's block
     * ItemAbility API. Do NOT construct AxeItem/ShovelItem/HoeItem here: creating
     * new Item instances after registries freeze crashes the running game.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        HeadBlueprintType type = readBlueprint(stack);
        if (type == null) return InteractionResult.PASS;

        ItemAbility[] actions = switch (type) {
            case AXE -> new ItemAbility[] { ItemAbilities.AXE_STRIP, ItemAbilities.AXE_SCRAPE, ItemAbilities.AXE_WAX_OFF };
            case SHOVEL -> new ItemAbility[] { ItemAbilities.SHOVEL_FLATTEN, ItemAbilities.SHOVEL_DOUSE };
            case HOE -> new ItemAbility[] { ItemAbilities.HOE_TILL };
            default -> new ItemAbility[0];
        };

        BlockState original = context.getLevel().getBlockState(context.getClickedPos());
        for (ItemAbility action : actions) {
            BlockState modified = original.getToolModifiedState(context, action, false);
            if (modified != null) {
                context.getLevel().setBlock(context.getClickedPos(), modified, 11);
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                    stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + 1));
                }
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

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
        EffectTier witherCurseTier = ForgedEffectRuntime.tier(stack, ForgingEffect.WITHER_CURSE_POWER);
        if (witherCurseTier != null) {
            double multiplier = switch (witherCurseTier) {
                case I -> 1.5D;
                case II -> 2.0D;
                case III -> 3.0D;
            };
            double cursedDamage = attackDamage * multiplier;
            tooltip.add(Component.literal("Attack Damage: " + formatDamage(cursedDamage)
                    + " (Base " + formatDamage(attackDamage) + " x" + formatDamage(multiplier) + ")")
                    .withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.literal("Attack Damage: " + formatDamage(attackDamage)).withStyle(ChatFormatting.GRAY));
        }

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
