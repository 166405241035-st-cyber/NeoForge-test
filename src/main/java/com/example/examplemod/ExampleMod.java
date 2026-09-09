package com.example.examplemod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> FORGING_BLOCK = BLOCKS.register(
            "forging_block", registryName -> new ForgingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F)));
    public static final DeferredItem<BlockItem> FORGING_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("forging_block", FORGING_BLOCK);

    public static final DeferredBlock<Block> FORGING_ANVIL = BLOCKS.register(
            "forging_anvil", registryName -> new ForgingAnvilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F)));
    public static final DeferredItem<BlockItem> FORGING_ANVIL_ITEM = ITEMS.registerSimpleBlockItem("forging_anvil", FORGING_ANVIL);

    public static final DeferredHolder<MenuType<?>, MenuType<ForgeMenu>> FORGE_MENU = MENUS.register(
            "forge_menu", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new ForgeMenu(windowId, inventory)));

    // Blueprint items migrated from the MCreator reference project.
    public static final DeferredItem<Item> CORE_BLUEPRINT = ITEMS.registerSimpleItem("coreblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> ROD_BLUEPRINT = ITEMS.registerSimpleItem("rodeblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> PICKAXE_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("pickaxeheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> AXE_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("axeheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SWORD_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("swordheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> HOE_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("hoeheadblueprint", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SHOVEL_HEAD_BLUEPRINT = ITEMS.registerSimpleItem("shovelheadblueprint", new Item.Properties().stacksTo(16));

    public static final DeferredItem<ForgedHeadItem> FORGED_HEAD_ITEM = ITEMS.register(
            "forged_head", registryName -> new ForgedHeadItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ForgedCoreItem> FORGED_CORE_ITEM = ITEMS.register(
            "forged_core", registryName -> new ForgedCoreItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Smelting & Forging"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> FORGING_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FORGING_BLOCK_ITEM.get());
                output.accept(FORGING_ANVIL_ITEM.get());
                output.accept(SWORD_HEAD_BLUEPRINT.get());
                output.accept(AXE_HEAD_BLUEPRINT.get());
                output.accept(PICKAXE_HEAD_BLUEPRINT.get());
                output.accept(SHOVEL_HEAD_BLUEPRINT.get());
                output.accept(HOE_HEAD_BLUEPRINT.get());
                output.accept(CORE_BLUEPRINT.get());
                output.accept(ROD_BLUEPRINT.get());
                output.accept(FORGED_HEAD_ITEM.get());
                output.accept(FORGED_CORE_ITEM.get());
            }).build());

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
