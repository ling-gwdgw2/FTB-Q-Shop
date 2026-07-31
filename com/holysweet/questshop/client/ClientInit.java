package com.holysweet.questshop.client;

import net.minecraft.client.gui.screens.MenuScreensHelper;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.holysweet.questshop.registry.ModMenuTypes;

public class ClientInit {

    public static void init() {
        NeoForge.EVENT_BUS.register(InventoryButtonListener.class);
    }

    @SuppressWarnings("unchecked")
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        MenuScreensHelper.registerShopScreen(event, (MenuType) ModMenuTypes.SHOP_MENU.get());
    }
}
