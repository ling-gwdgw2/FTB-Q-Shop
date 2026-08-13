package com.holysweet.questshop.client;

import com.holysweet.questshop.client.screen.ShopMenuScreen;
import com.holysweet.questshop.registry.ModMenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientInit {

    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SHOP_MENU.get(), ShopMenuScreen::new);
    }
}
