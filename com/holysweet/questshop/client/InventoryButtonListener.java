package com.holysweet.questshop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreenHelper;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class InventoryButtonListener {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            int x = InventoryScreenHelper.getLeft(inventoryScreen) + 150;
            int y = InventoryScreenHelper.getTop(inventoryScreen) + 60;
            Button shopBtn = Button.builder(
                Component.literal("Shop"),
                btn -> {
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.connection.sendCommand("shop");
                    }
                }
            ).bounds(x, y, 32, 18).tooltip(Tooltip.create(Component.literal("FtbQshop (เปิดร้านค้า)"))).build();
            event.addListener(shopBtn);
        }
    }
}
