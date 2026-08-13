package com.holysweet.questshop.client;

import com.holysweet.questshop.client.screen.ShopMenuScreen;
import com.holysweet.questshop.registry.ModMenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientInit {

    public static void init() {
        NeoForge.EVENT_BUS.register(InventoryButtonListener.class);
    }

    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SHOP_MENU.get(), ShopMenuScreen::new);
    }
}
