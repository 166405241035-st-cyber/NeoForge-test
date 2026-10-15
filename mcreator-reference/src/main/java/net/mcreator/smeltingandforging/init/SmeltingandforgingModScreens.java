/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.smeltingandforging.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.smeltingandforging.client.gui.SmithingTableGUIScreen;
import net.mcreator.smeltingandforging.client.gui.ForgerGUIScreen;
import net.mcreator.smeltingandforging.client.gui.ForgerGUIMiniGameScreen;

@EventBusSubscriber(Dist.CLIENT)
public class SmeltingandforgingModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(SmeltingandforgingModMenus.SMITHING_TABLE_GUI.get(), SmithingTableGUIScreen::new);
		event.register(SmeltingandforgingModMenus.FORGER_GUI.get(), ForgerGUIScreen::new);
		event.register(SmeltingandforgingModMenus.FORGER_GUI_MINI_GAME.get(), ForgerGUIMiniGameScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}