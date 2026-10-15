/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.smeltingandforging.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.mcreator.smeltingandforging.block.ForgerBlock;
import net.mcreator.smeltingandforging.SmeltingandforgingMod;

public class SmeltingandforgingModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(SmeltingandforgingMod.MODID);
	public static final DeferredBlock<Block> FORGER;
	static {
		FORGER = REGISTRY.register("forger", ForgerBlock::new);
	}
	// Start of user code block custom blocks
	// End of user code block custom blocks
}