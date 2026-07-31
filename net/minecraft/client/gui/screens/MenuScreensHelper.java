package net.minecraft.client.gui.screens;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.world.inventory.MenuType;
import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.client.screen.ShopMenuScreen;

public class MenuScreensHelper {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerShopScreen(RegisterMenuScreensEvent event, MenuType type) {
        event.register(type, (MenuScreens.ScreenConstructor) ShopMenuScreen::new);
    }
}
