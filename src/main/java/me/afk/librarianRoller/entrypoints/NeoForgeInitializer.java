// Copyright 2020-2026 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

//? if NEOFORGE {
/*package me.afk.librarianRoller.entrypoints;

import me.afk.librarianRoller.KeyRegister;
import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.Roller;
import me.afk.librarianRoller.config.ModConfigManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.gui.*;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LibrarianRoller.MOD_ID)
@SuppressWarnings("unused")
public class NeoForgeInitializer {
	public NeoForgeInitializer() {
		ModConfigManager.registerConfig();

		IEventBus eventBus = NeoForge.EVENT_BUS;
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		IEventBus modBus = modLoadingContext.getActiveContainer().getEventBus();

		modLoadingContext.registerExtensionPoint(IConfigScreenFactory.class, () -> (mc, p) -> ModConfigManager.getConfigScreen(p));
//		eventBus.addListener(KeyRegister::onRegisterKeyMappings);
		modBus.addListener(KeyRegister::onRegisterKeyMappings);
		eventBus.addListener(KeyRegister::onClientTick);
		eventBus.addListener(Roller::tick);
	}
}
*///?}
