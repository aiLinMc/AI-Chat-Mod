package com.ailinmc.chatai.client;

import com.ailinmc.chatai.AiChatMod;
import com.ailinmc.chatai.gui.ModConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AiChatMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    static {
        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (mc, screen) -> new ModConfigScreen(screen)
        );
    }
    
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
    }
}
