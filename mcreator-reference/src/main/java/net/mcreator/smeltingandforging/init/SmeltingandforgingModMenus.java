/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.smeltingandforging.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import net.mcreator.smeltingandforging.world.inventory.SmithingTableGUIMenu;
import net.mcreator.smeltingandforging.world.inventory.ForgerGUIMiniGameMenu;
import net.mcreator.smeltingandforging.world.inventory.ForgerGUIMenu;
import net.mcreator.smeltingandforging.network.MenuStateUpdateMessage;
import net.mcreator.smeltingandforging.SmeltingandforgingMod;

import java.util.Map;

public class SmeltingandforgingModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, SmeltingandforgingMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<SmithingTableGUIMenu>> SMITHING_TABLE_GUI = REGISTRY.register("smithing_table_gui", () -> IMenuTypeExtension.create(SmithingTableGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<ForgerGUIMenu>> FORGER_GUI = REGISTRY.register("forger_gui", () -> IMenuTypeExtension.create(ForgerGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<ForgerGUIMiniGameMenu>> FORGER_GUI_MINI_GAME = REGISTRY.register("forger_gui_mini_game", () -> IMenuTypeExtension.create(ForgerGUIMiniGameMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide) {
				if (Minecraft.getInstance().screen instanceof SmeltingandforgingModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				PacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}