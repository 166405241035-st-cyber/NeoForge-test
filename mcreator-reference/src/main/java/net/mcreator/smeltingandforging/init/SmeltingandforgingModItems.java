/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.smeltingandforging.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.smeltingandforging.item.*;
import net.mcreator.smeltingandforging.SmeltingandforgingMod;

public class SmeltingandforgingModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SmeltingandforgingMod.MODID);
	public static final DeferredItem<Item> FORGER;
	public static final DeferredItem<Item> COREBLUEPRINT;
	public static final DeferredItem<Item> RODEBLUEPRINT;
	public static final DeferredItem<Item> PICKAXEHEADBLUEPRINT;
	public static final DeferredItem<Item> AXEHEADBLUEPRINT;
	public static final DeferredItem<Item> SWORDHEADBLUEPRINT;
	public static final DeferredItem<Item> HOEHEADBLUEPRINT;
	public static final DeferredItem<Item> SHOVELHEADBLUEPRINT;
	static {
		FORGER = block(SmeltingandforgingModBlocks.FORGER);
		COREBLUEPRINT = REGISTRY.register("coreblueprint", CoreblueprintItem::new);
		RODEBLUEPRINT = REGISTRY.register("rodeblueprint", RodeblueprintItem::new);
		PICKAXEHEADBLUEPRINT = REGISTRY.register("pickaxeheadblueprint", PickaxeheadblueprintItem::new);
		AXEHEADBLUEPRINT = REGISTRY.register("axeheadblueprint", AxeheadblueprintItem::new);
		SWORDHEADBLUEPRINT = REGISTRY.register("swordheadblueprint", SwordheadblueprintItem::new);
		HOEHEADBLUEPRINT = REGISTRY.register("hoeheadblueprint", HoeheadblueprintItem::new);
		SHOVELHEADBLUEPRINT = REGISTRY.register("shovelheadblueprint", ShovelheadblueprintItem::new);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}