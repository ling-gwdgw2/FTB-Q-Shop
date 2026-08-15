package com.holysweet.questshop.server;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.server.commands.HqsCommands;
import com.holysweet.questshop.service.DailyDealsService;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = QuestShop.MODID)
public class ServerEvents {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        HqsCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % 1200 == 0) { // Check deals every 1 minute (1200 ticks)
            DailyDealsService.checkAndRefreshDeals(event.getServer());
        }
    }
}
