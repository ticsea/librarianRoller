// Copyright 2020-2026 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

//? if FABRIC {
package me.afk.librarianRoller.entrypoints;


import com.mojang.blaze3d.platform.InputConstants;
import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.Roller;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
		//?}
import org.lwjgl.glfw.GLFW;

import static me.afk.librarianRoller.config.ModConfigManager.registerConfig;

@SuppressWarnings("unused")
public class FabricClientModInitializer implements ClientModInitializer {

	//? if >= 1.21.11 {
	KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(LibrarianRoller.MOD_ID, "roller")
	);
	//?} else {
	/*String CATEGORY = "roller";
	*///?}

	KeyMapping sendToChatKey = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.librarianroller.roller", // The translation key for the key mapping.
					InputConstants.Type.KEYSYM, // // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
					GLFW.GLFW_KEY_J, // The GLFW keycode of the key.
					CATEGORY // The category of the mapping.
			));

	private void key() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (sendToChatKey.consumeClick()) {
				Roller.start();
			}
		});
	}

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		registerConfig();
		Roller.fabricEvent();
		key();
	}
}
//?}
