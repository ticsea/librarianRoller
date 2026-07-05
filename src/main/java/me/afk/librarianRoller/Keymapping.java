//? if NEOFORGE {
/*package me.afk.librarianRoller;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class Keymapping {

            //? if >= 1.21.11 {
            public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.MISC;
            //?} else {

    /^public static final String KEY_CATEGORY = "key.category.librarianroller";
            ^///?}

    public static final String ROLLER = "key.librarianroller.roller";

    public static final Lazy<KeyMapping> RollStartKey = Lazy.of(() ->
            new KeyMapping(
                    ROLLER,            // 按键的显示名称（本地化键名）
                    KeyConflictContext.IN_GAME,      // 仅在游戏中生效，而非GUI界面
                    KeyModifier.NONE,                // 默认无组合键
                    InputConstants.Type.KEYSYM,      // 输入类型为键盘
                    GLFW.GLFW_KEY_F,                 // 默认按键为 O
                    KEY_CATEGORY             // 在控制菜单中所属的分类b
            )
    );
}
*///?}