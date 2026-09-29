package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;
import com.example.examplemod.skill.curse.BerserkerMobEffect;
import com.example.examplemod.skill.curse.VampireBladeMobEffect;
import com.example.examplemod.skill.curse.LastStandMobEffect;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> FORGING_BLOCK = BLOCKS.register("forging_block", registryName -> new ForgingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgingBlockEntity>> FORGING_BLOCK_ENTITY = BLOCK_ENTITIES.register("forging_block", () -> BlockEntityType.Builder.of(ForgingBlockEntity::new, FORGING_BLOCK.get()).build(null));
    public static final DeferredItem<BlockItem> FORGING_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("forging_block", FORGING_BLOCK);
    public static final DeferredBlock<Block> FORGING_ANVIL = BLOCKS.register("forging_anvil", registryName -> new ForgingAnvilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).noOcclusion()));
    public static final DeferredItem<BlockItem> FORGING_ANVIL_ITEM = ITEMS.registerSimpleBlockItem("forging_anvil", FORGING_ANVIL);
    public static final DeferredBlock<Block> EQUIPMENT_TEST_BLOCK = BLOCKS.register("equipment_test_block", registryName -> new EquipmentTestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.5F)));
    public static final DeferredBlock<Block> BLESSING_CURSE_TEST_BLOCK = BLOCKS.register("blessing_curse_test_block", registryName -> new BlessingCurseTestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(4.0F)));
    public static final DeferredItem<BlockItem> BLESSING_CURSE_TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("blessing_curse_test_block", BLESSING_CURSE_TEST_BLOCK);
    public static final DeferredBlock<Block> INVISIBLE_SUPPORT_BLOCK = BLOCKS.register("invisible_support", registryName -> new InvisibleSupportBlock(BlockBehaviour.Properties.of().strength(0.2F).noCollission().noOcclusion()));
    public static final DeferredBlock<Block> MOISTURE_RETAIN_FARMLAND = BLOCKS.register("moisture_retain_farmland",
            registryName -> new MoistureRetainFarmlandBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.FARMLAND)));
    public static final DeferredItem<BlockItem> EQUIPMENT_TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("equipment_test_block", EQUIPMENT_TEST_BLOCK);
    public static final DeferredHolder<EntityType<?>, EntityType<ForgedBoomerangEntity>> FORGED_BOOMERANG = ENTITY_TYPES.register("forged_boomerang",
            () -> EntityType.Builder.<ForgedBoomerangEntity>of(ForgedBoomerangEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1).build("forged_boomerang"));
    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING = MOB_EFFECTS.register("bleeding",
            () -> new BleedingMobEffect(MobEffectCategory.HARMFUL, 0xD11A2A));
    public static final DeferredHolder<MobEffect, MobEffect> BERSERKER = MOB_EFFECTS.register("berserker",
            () -> new BerserkerMobEffect(MobEffectCategory.HARMFUL, 0xB32020));
    public static final DeferredHolder<MobEffect, MobEffect> VAMPIRE_BLADE = MOB_EFFECTS.register("vampire_blade",
            () -> new VampireBladeMobEffect(MobEffectCategory.HARMFUL, 0x7A1026));
    public static final DeferredHolder<MobEffect, MobEffect> LAST_STAND = MOB_EFFECTS.register("last_stand",
            () -> new LastStandMobEffect(MobEffectCategory.HARMFUL, 0xD68A18));

    public static final DeferredHolder<MenuType<?>, MenuType<ForgeMenu>> FORGE_MENU = MENUS.register("forge_menu", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new ForgeMenu(windowId, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<AnvilMenu>> ANVIL_MENU = MENUS.register("anvil_menu", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new AnvilMenu(windowId, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<EquipmentTestMenu>> EQUIPMENT_TEST_MENU = MENUS.register("equipment_test_menu", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new EquipmentTestMenu(windowId, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<BlessingCurseTestMenu>> BLESSING_CURSE_TEST_MENU = MENUS.register("blessing_curse_test_menu", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new BlessingCurseTestMenu(windowId, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<ForgedStorageMenu>> FORGED_STORAGE_MENU = MENUS.register("forged_storage", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new ForgedStorageMenu(windowId, inventory)));

    public static final DeferredItem<Item> CORE_BLUEPRINT = ITEMS.registerSimpleItem("coreblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> ROD_BLUEPRINT = ITEMS.registerSimpleItem("rodeblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> PICKAXE_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("pickaxeheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> AXE_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("axeheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SWORD_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("swordheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> HOE_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("hoeheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SHOVEL_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("shovelheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<ForgedHeadItem> FORGED_HEAD_ITEM = ITEMS.register("forged_head", registryName -> new ForgedHeadItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ForgedCoreItem> FORGED_CORE_ITEM = ITEMS.register("forged_core", registryName -> new ForgedCoreItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ForgedRodItem> FORGED_ROD_ITEM = ITEMS.register("forged_rod", registryName -> new ForgedRodItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ForgedEquipmentItem> FORGED_EQUIPMENT_ITEM = ITEMS.register("forged_equipment", registryName -> new ForgedEquipmentItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Smelting & Forging")).withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> FORGING_BLOCK_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
                output.accept(FORGING_BLOCK_ITEM.get()); output.accept(FORGING_ANVIL_ITEM.get()); output.accept(EQUIPMENT_TEST_BLOCK_ITEM.get()); output.accept(BLESSING_CURSE_TEST_BLOCK_ITEM.get());
                output.accept(SWORD_HEAD_BLUEPRINT.get()); output.accept(AXE_HEAD_BLUEPRINT.get()); output.accept(PICKAXE_HEAD_BLUEPRINT.get()); output.accept(SHOVEL_HEAD_BLUEPRINT.get()); output.accept(HOE_HEAD_BLUEPRINT.get());
                output.accept(CORE_BLUEPRINT.get()); output.accept(ROD_BLUEPRINT.get());
                output.accept(FORGED_HEAD_ITEM.get()); output.accept(FORGED_CORE_ITEM.get()); output.accept(FORGED_ROD_ITEM.get()); output.accept(FORGED_EQUIPMENT_ITEM.get());
            }).build());

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus); ITEMS.register(modEventBus); MENUS.register(modEventBus); MOB_EFFECTS.register(modEventBus); ENTITY_TYPES.register(modEventBus); BLOCK_ENTITIES.register(modEventBus); CREATIVE_MODE_TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, ForgedSkillConfig.SPEC, "examplemod-forged-skills.toml");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {}
        @net.neoforged.bus.api.SubscribeEvent
        public static void registerForgedItemVariants(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ResourceLocation variant = ResourceLocation.fromNamespaceAndPath(MODID, "variant");
                ItemProperties.register(FORGED_HEAD_ITEM.get(), variant, (stack, level, entity, seed) -> {
                    ForgedHeadResult result = ForgedHeadItem.readResult(stack);
                    return result == null ? 0 : result.blueprint().ordinal() * MonsterMaterial.values().length + result.monsterMaterial().ordinal() + 1;
                });
                ItemProperties.register(FORGED_CORE_ITEM.get(), variant, (stack, level, entity, seed) -> {
                    ForgedCoreResult result = ForgedCoreItem.readResult(stack);
                    return result == null ? 0 : result.monsterMaterial().ordinal() + 1;
                });
                ItemProperties.register(FORGED_ROD_ITEM.get(), variant, (stack, level, entity, seed) -> {
                    ForgedRodResult result = ForgedRodItem.readResult(stack);
                    return result == null ? 0 : result.monsterMaterial().ordinal() + 1;
                });
                ResourceLocation equipmentVariant = ResourceLocation.fromNamespaceAndPath(MODID, "equipment_variant");
                ItemProperties.register(FORGED_EQUIPMENT_ITEM.get(), equipmentVariant, (stack, level, entity, seed) -> {
                    HeadBlueprintType blueprint = ForgedEquipmentItem.readBlueprint(stack);
                    MonsterMaterial head = ForgedEquipmentItem.readHeadMaterial(stack);
                    MonsterMaterial rod = ForgedEquipmentItem.readRodMaterial(stack);
                    if (blueprint == null || head == null || rod == null) return 0;
                    int materials = MonsterMaterial.values().length;
                    return 1 + (blueprint.ordinal() * materials + head.ordinal()) * materials + rod.ordinal();
                });
            });
        }
        @net.neoforged.bus.api.SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(FORGE_MENU.get(), ForgingScreen::new);
            event.register(ANVIL_MENU.get(), ForgingAnvilScreen::new);
            event.register(EQUIPMENT_TEST_MENU.get(), EquipmentTestScreen::new);
            event.register(BLESSING_CURSE_TEST_MENU.get(), BlessingCurseTestScreen::new);
            event.register(FORGED_STORAGE_MENU.get(), ForgedStorageScreen::new);
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // Temporary invisible renderer for the server-controlled boomerang.
            // A visible custom/item renderer can replace this once mechanics are stable.
            event.registerEntityRenderer(FORGED_BOOMERANG.get(), ForgedBoomerangRenderer::new);
        }
    }
}
