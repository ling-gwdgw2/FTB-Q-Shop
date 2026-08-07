package com.holysweet.questshop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "questshop", value = Dist.CLIENT)
public class InventoryButtonListener {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
            int x = InventoryScreenHelper.getLeft(screen) + 140;
            int y = InventoryScreenHelper.getTop(screen) + 6;

            Button shopBtn = Button.builder(
                Component.literal("Shop"),
                btn -> {
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.connection.sendCommand("hqs shop");
                    }
                }
            ).bounds(x, y, 32, 16).tooltip(Tooltip.create(Component.literal("FtbQshop (เปิดร้านค้า)"))).build();
            event.addListener(shopBtn);
        }
    }
}
