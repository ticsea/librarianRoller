package me.afk.librarianRoller.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class MessageUtils {

    public static void throwError(String translatableKey, Component... component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        //? if >=1.21.11 {
        player.displayClientMessage(Component.translatable(translatableKey, (Object[]) component).withStyle(ChatFormatting.RED), false);
        //?} elif >= 1.21.1 {
        /*player.sendSystemMessage(Component.translatable(translatableKey, component).withStyle(ChatFormatting.RED));
        *///?}
    }

    public static void print(String translatableKey, Component... component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        //? if >= 1.21.11 {
        player.displayClientMessage(Component.translatable(translatableKey, (Object[]) component), false);
        //?} elif >= 1.21.1 {
        /*player.sendSystemMessage(Component.translatable(translatableKey, (Object) component));
        *///?}
    }
}
