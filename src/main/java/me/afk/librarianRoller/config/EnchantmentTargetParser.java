package me.afk.librarianRoller.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure, stateless parser that converts the config's enchantment entry list
 * (e.g. "minecraft:mending 1") into a {@code Map<enchantmentName, requiredLevel>}.
 * Kept separate from the config holder so it can be unit-tested without Minecraft.
 */
public final class EnchantmentTargetParser {
    private EnchantmentTargetParser() {
    }

    /**
     * Parses the raw entry list into a lookup map.
     * Entries without a level, or with level "0", are skipped.
     *
     * @param rawEntries the config's {@code entry} list
     * @return a mutable map of enchantment name (lowercased) -> required level
     */
    public static Map<String, Integer> parse(List<String> rawEntries) {
        Map<String, Integer> result = new HashMap<>();
        if (rawEntries == null) return result;

        for (String it : rawEntries) {
            if (it == null) continue;

            // Split from the end - the last part is the level.
            int lastSpaceIndex = it.lastIndexOf(' ');
            if (lastSpaceIndex == -1) continue;

            String enchantmentName = it.substring(0, lastSpaceIndex).trim().toLowerCase();
            String levelStr = it.substring(lastSpaceIndex + 1).trim();

            if (levelStr.equals("0")) continue;

            try {
                result.put(enchantmentName, Integer.valueOf(levelStr));
            } catch (NumberFormatException e) {
                // Invalid level - skip this entry.
            }
        }

        return result;
    }
}