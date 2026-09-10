package com.holysweet.questshop.server;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.network.Net;
import com.holysweet.questshop.server.commands.LingShopCommands;
import com.holysweet.questshop.service.CoinsService;
import com.holysweet.questshop.service.DailyDealsService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = QuestShop.MODID)
public class ServerEvents {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LingShopCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                Net.sendCoinsBalance(player, CoinsService.get(player.serverLevel(), player));
                Net.sendCategoriesSnapshot(player);
                Net.sendShopData(player);
                Net.sendTeamMembers(player);
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % 1200 == 0) { // Check deals every 1 minute (1200 ticks)
            DailyDealsService.checkAndRefreshDeals(event.getServer());
        }
    }
}
