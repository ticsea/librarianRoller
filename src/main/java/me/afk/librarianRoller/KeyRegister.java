//? if NEOFORGE {
/*package me.afk.librarianRoller;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class KeyRegister {

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // 将你的按键注册到游戏中
        event.register(Keymapping.RollStartKey.get());
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        // 使用 consumeClick() 检查按键是否被按下并消费掉本次点击事件
        while (Keymapping.RollStartKey.get().consumeClick()) {
            // 执行你的逻辑，例如向玩家发送一条消息
            Roller.start();
        }
    }
}
*///?}