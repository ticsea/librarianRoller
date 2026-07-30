package me.afk.librarianRoller.utils;

//? if >= 1.21.1 {
/*import me.afk.librarianRoller.dataModel.Enchantment;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
        *///?} else if >= 1.20.1 {

import me.afk.librarianRoller.dataModel.Enchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
        //?}

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class EnchantedBookUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("EnchantedBookUtils");

    // region 通用可复用工具函数

    //? if >= 1.21.1 {
    /*/^*
     * 新版（1.20.2+ DataComponent）：从附魔书读取所有 <翻译名称,等级>
     ^/
    public static List<Enchantment> readStoredEnchantments(ItemStack itemStack) {
        List<Enchantment> list = new ArrayList<>();
        var components = DataComponents.ENCHANTMENTS;
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            components = DataComponents.STORED_ENCHANTMENTS;
        }
        var storedEnchs = itemStack.get(components);
        if (storedEnchs == null || storedEnchs.isEmpty()) return list;

        for (var entry : storedEnchs.entrySet()) {
            String fullId = entry.getKey().getRegisteredName();
            int lvl = entry.getIntValue();

            list.add(new Enchantment(fullId, lvl));
        }
        return list;
    }
    *///?} else {
    /**
     * 1.20.1 getTag() 版本：从附魔书读取所有 <翻译名称,等级>
     * 只传入 ENCHANTED_BOOK，外部先判断 item
     */
    public static List<Enchantment> readStoredEnchantments(ItemStack itemStack) {
        List<Enchantment> list = new ArrayList<>();
//        if (!itemStack.is(Items.ENCHANTED_BOOK)) return list;
        var tag = itemStack.getTag();
        var components = "Enchantments";
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            components = "StoredEnchantments";
        }
        if (tag == null || !tag.contains(components, Tag.TAG_LIST)) return list;

        ListTag storedEnchantments = tag.getList(components, Tag.TAG_COMPOUND);

        for (var tagObj : storedEnchantments) {
            if (!(tagObj instanceof CompoundTag enchTag)) continue;

            String fullId = enchTag.getString("id");
            int lvl = enchTag.getShort("lvl");

            list.add(new Enchantment(fullId, lvl));
        }
        return list;
    }
    //?}
}