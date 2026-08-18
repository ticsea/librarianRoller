// Copyright 2020-2026 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

//? if FORGE {
/*package me.afk.librarianRoller.entrypoints;

import com.mojang.blaze3d.platform.InputConstants;
import me.afk.librarianRoller.LibrarianRoller;
import me.afk.librarianRoller.RollerContext;
import me.afk.librarianRoller.config.ModConfigManager;
import me.afk.librarianRoller.utils.MessageUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.*;
//? if >=1.18.0 {
import net.minecraftforge.client.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
//?} else {
/^import net.minecraftforge.fmlclient.*;
^///?}

@Mod(LibrarianRoller.MOD_ID)
@SuppressWarnings("unused")
public class ForgeInitializer {
	RollerContext rollercontext;
	ModConfigManager modConfigManager;

	public ForgeInitializer(FMLJavaModLoadingContext context) {
		rollercontext = LibrarianRoller.getRollerContext();
		modConfigManager = LibrarianRoller.getModConfigManager();
		modConfigManager.registerConfig();
		//? if >=1.19.0 {
		context.registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((mc, parentScreen) -> modConfigManager.getConfigScreen(parentScreen))
		);

		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		IEventBus modBus = context.getModEventBus();
		modBus.addListener(this::onRegisterKeyMappings);
		eventBus.addListener(this::onClientTick);
		eventBus.addListener(this::doAction);
		//?} else {
		/^ModLoadingContext.get().registerExtensionPoint(
			ConfigGuiHandler.ConfigGuiFactory.class,
			() -> new ConfigGuiHandler.ConfigGuiFactory((mc, parentScreen) -> ConfigScreen.getConfigScreen(parentScreen))
		);
		^///?}
	}

	private void doAction(TickEvent.ClientTickEvent event) {
		rollercontext.doAction();
	}

	public static final String KEY_CATEGORY = "Librarian Roller";
	public final Lazy<KeyMapping> TOGGLEKEY = Lazy.of(() ->
			new KeyMapping(
					"key.librarianroller.toggle",            // 按键的显示名称（本地化键名）
					KeyConflictContext.IN_GAME,      // 仅在游戏中生效，而非GUI界面
					KeyModifier.NONE,                // 默认无组合键
					InputConstants.Type.KEYSYM,      // 输入类型为键盘
					GLFW.GLFW_KEY_J,                 // 默认按键为 O
					KEY_CATEGORY             // 在控制菜单中所属的分类b
			)
	);
	public final Lazy<KeyMapping> ADDHANDENCHANTMENTKEY = Lazy.of(() ->
			new KeyMapping(
					"key.librarianroller.add_enchant",            // 按键的显示名称（本地化键名）
					KeyConflictContext.IN_GAME,      // 仅在游戏中生效，而非GUI界面
					KeyModifier.NONE,                // 默认无组合键
					InputConstants.Type.KEYSYM,      // 输入类型为键盘
					GLFW.GLFW_KEY_K,                 // 默认按键为 O
					KEY_CATEGORY             // 在控制菜单中所属的分类b
			)
	);
	public final Lazy<KeyMapping> ADDINVENTORYENCHANTMENTKEY = Lazy.of(() ->
			new KeyMapping(
					"key.librarianroller.add_inventory_enchantment",            // 按键的显示名称（本地化键名）
					KeyConflictContext.IN_GAME,      // 仅在游戏中生效，而非GUI界面
					KeyModifier.NONE,                // 默认无组合键
					InputConstants.Type.KEYSYM,      // 输入类型为键盘
					GLFW.GLFW_KEY_L,                 // 默认按键为 O
					KEY_CATEGORY             // 在控制菜单中所属的分类b
			)
	);
	public final Lazy<KeyMapping> OPENCONFIGKEY = Lazy.of(() ->
			new KeyMapping(
					"key.librarianroller.open_config",            // 按键的显示名称（本地化键名）
					KeyConflictContext.IN_GAME,      // 仅在游戏中生效，而非GUI界面
					KeyModifier.NONE,                // 默认无组合键
					InputConstants.Type.KEYSYM,      // 输入类型为键盘
					GLFW.GLFW_KEY_KP_5,                 // 默认按键为 O
					KEY_CATEGORY             // 在控制菜单中所属的分类b
			)
	);

	public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		// 将你的按键注册到游戏中
		event.register(TOGGLEKEY.get());
		event.register(ADDHANDENCHANTMENTKEY.get());
		event.register(ADDINVENTORYENCHANTMENTKEY.get());
		event.register(OPENCONFIGKEY.get());
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		// 使用 consumeClick() 检查按键是否被按下并消费掉本次点击事件
		while (TOGGLEKEY.get().consumeClick()) {
			// 执行你的逻辑，例如向玩家发送一条消息
			LibrarianRoller.getRollerContext().toggle();
		}
		while (ADDHANDENCHANTMENTKEY.get().consumeClick()) {
			int i = LibrarianRoller.getConfigService().addHandToEntry();
			MessageUtils.print("afk.enchant_roller.info.added_entry", Component.literal(String.valueOf(i)));
		}
		while (ADDINVENTORYENCHANTMENTKEY.get().consumeClick()) {
			int i = LibrarianRoller.getConfigService().addAllToEntry();
			MessageUtils.print("afk.enchant_roller.info.added_entry", Component.literal(String.valueOf(i)));
		}
		while (OPENCONFIGKEY.get().consumeClick()) {
			Screen screen = Minecraft.getInstance().screen;
			Minecraft.getInstance().setScreen(modConfigManager.getConfigScreen(screen));
		}
	}
}
*///?}
