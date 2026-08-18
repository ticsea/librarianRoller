// Copyright 2020-2026 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

//? if FABRIC {
package me.afk.librarianRoller.entrypoints;


import com.mojang.blaze3d.platform.InputConstants;
import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.RollerContext;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.utils.MessageUtils;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
		//? if >= 26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.resources.Identifier;
		*///?} else if >= 1.21.11 {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.resources.Identifier;
		//?} elif >= 1.20.1 {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
public class FabricClientModInitializer implements ClientModInitializer {
    private RollerContext rollercontext;
	private ModConfigManager modConfigManager;


	//? if >= 1.21.11 {
	KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(LibrarianRoller.MOD_ID, "roller")
	);
	//?} else {
	/*String CATEGORY = "Librarian Roller";
	*///?}

	KeyMapping toggleKey = keymapping(
			"key.librarianroller.toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_J,
			CATEGORY
	);
	KeyMapping addHandEnchantmentKey = keymapping(
			"key.librarianroller.add_enchant",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_K,
			CATEGORY
	);
	KeyMapping addInventoryEnchantmentKey = keymapping(
			"key.librarianroller.add_inventory_enchantment",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_KP_5,
			CATEGORY
	);
	KeyMapping openConfigKey = keymapping(
			"key.librarianroller.open_config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_L,
			CATEGORY
	);


	private KeyMapping keymapping(
			final String name,
			final InputConstants.Type type,
			final int value,
			//~ if >= 1.21.11 'String' -> 'KeyMapping.Category' {
			final KeyMapping.Category category) {
		//~}
		//? if >= 26.1 {
		/*return KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						name,
						type,
						value,
						category
				)
		);
		*///?} else {
		return KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						name,
						type,
						value,
						category
				)
		);
		//?}
	}

	private void key() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				rollercontext.toggle();
			}
			while (addHandEnchantmentKey.consumeClick()) {
				int i = LibrarianRoller.getConfigService().addHandToEntry();
				MessageUtils.print("afk.enchant_roller.info.added_entry", Component.literal(String.valueOf(i)));
			}
			while (addInventoryEnchantmentKey.consumeClick()) {
				int i = LibrarianRoller.getConfigService().addAllToEntry();
				MessageUtils.print("afk.enchant_roller.info.added_entry", Component.literal(String.valueOf(i)));
			}
			while (openConfigKey.consumeClick()) {
				//~ if >= 26.2 'Minecraft.getInstance().screen' -> 'Minecraft.getInstance().gui.screen()' {
				Screen screen = Minecraft.getInstance().screen;
				//~}

				//~ if >= 26.2 'setScreen' -> 'setScreenAndShow' {
				Minecraft.getInstance().setScreen(modConfigManager.getConfigScreen(screen));
				//~}
			}
		});
	}

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		this.rollercontext = LibrarianRoller.getRollerContext();
		this.modConfigManager = LibrarianRoller.getModConfigManager();
		modConfigManager.registerConfig();

		key();
		doAction();
	}

	private void doAction() {
		ClientTickEvents.END_CLIENT_TICK.register( minecraft -> {
			rollercontext.doAction();
		});
	}
}
//?}
