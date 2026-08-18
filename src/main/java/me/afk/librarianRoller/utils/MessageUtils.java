package me.afk.librarianRoller.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class MessageUtils {

    public static void throwError(String translatableKey, Component... component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        //? if >= 26.1 {
        /*player.sendSystemMessage(Component.translatable(translatableKey, (Object[]) component).withStyle(ChatFormatting.RED));
        *///?} elif >=1.21.11 {
        player.displayClientMessage(Component.translatable(translatableKey, (Object[]) component).withStyle(ChatFormatting.RED), false);
        //?} elif >= 1.20.1 {
        /*player.sendSystemMessage(Component.translatable(translatableKey, (Object[]) component).withStyle(ChatFormatting.RED));
        *///?}

    }

    public static void print(String translatableKey, Component... component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        //? if >= 26.1 {
        /*player.sendSystemMessage(Component.translatable(translatableKey, (Object[]) component));
        *///?} elif >=1.21.11 {
        player.displayClientMessage(Component.translatable(translatableKey, (Object[]) component), false);
        //?} elif >= 1.20.1 {
        /*player.sendSystemMessage(Component.translatable(translatableKey, (Object[]) component));
        *///?}
    }

    public static void printReward(String translationKey, int level, int cost) {

        ChatFormatting costColor;
        if (cost <= 16) {
            costColor = ChatFormatting.GREEN;
        } else if (cost <= 32) {
            costColor = ChatFormatting.YELLOW;
        } else {
            costColor = ChatFormatting.RED;
        }
        print("afk.enchant_roller.info.obtained_enchantment", Component.literal(translationKey).withStyle(ChatFormatting.GREEN), Component.literal(String.valueOf(level)).withStyle(ChatFormatting.GOLD), Component.literal(String.valueOf(cost)).withStyle(costColor));
    }
}
