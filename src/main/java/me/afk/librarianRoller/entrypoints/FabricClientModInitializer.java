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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
//? if >= 1.21.11 {
/*import net.minecraft.resources.Identifier;
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
	/*KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(LibrarianRoller.MOD_ID, "roller")
	);
	*///?} else {
	String CATEGORY = "Librarian Roller";
	//?}

	KeyMapping toggleKey = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.librarianroller.toggle", // The translation key for the key mapping.
					InputConstants.Type.KEYSYM, // // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
					GLFW.GLFW_KEY_J, // The GLFW keycode of the key.
					CATEGORY // The category of the mapping.
			));
	KeyMapping addHandEnchantmentKey = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.librarianroller.add_enchant", // The translation key for the key mapping.
					InputConstants.Type.KEYSYM, // // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
					GLFW.GLFW_KEY_K, // The GLFW keycode of the key.
					CATEGORY // The category of the mapping.
			));
	KeyMapping addInventoryEnchantmentKey = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.librarianroller.add_inventory_enchantment", // The translation key for the key mapping.
					InputConstants.Type.KEYSYM, // // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
					GLFW.GLFW_KEY_KP_5, // The GLFW keycode of the key.
					CATEGORY // The category of the mapping.
			));
	KeyMapping openConfigKey = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.librarianroller.open_config", // The translation key for the key mapping.
					InputConstants.Type.KEYSYM, // // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
					GLFW.GLFW_KEY_L, // The GLFW keycode of the key.
					CATEGORY // The category of the mapping.
			));


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
				Screen screen = Minecraft.getInstance().screen;
				Minecraft.getInstance().setScreen(modConfigManager.getConfigScreen(screen));
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
		ClientTickEvents.END_WORLD_TICK.register( minecraft -> {
			rollercontext.doAction();
		});
	}
}
//?}
