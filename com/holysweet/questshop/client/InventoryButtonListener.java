package com.holysweet.questshop.client;

import com.holysweet.questshop.Config;
import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;

@EventBusSubscriber(modid = QuestShop.MODID, value = Dist.CLIENT)
public class InventoryButtonListener {

    public static class CoinShopButton extends Button {
        private final ItemStack coinStack;
        private final ResourceLocation coinTexture = ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "textures/item/coin.png");

        public CoinShopButton(int x, int y, OnPress onPress, Tooltip tooltip) {
            super(x, y, 18, 18, Component.empty(), onPress, DEFAULT_NARRATION);
            setTooltip(tooltip);
            this.coinStack = (ModItems.COIN != null && ModItems.COIN.get() != null) ? new ItemStack(ModItems.COIN.get()) : ItemStack.EMPTY;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            if (!this.coinStack.isEmpty()) {
                guiGraphics.renderItem(this.coinStack, this.getX() + 1, this.getY() + 1);
            } else {
                guiGraphics.blit(this.coinTexture, this.getX() + 1, this.getY() + 1, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenInit(Post event) {
        if (!Config.ENABLE_INVENTORY_BUTTON.get()) return;
        if (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
            int x = InventoryScreenHelper.getLeft(screen) + 148;
            int y = InventoryScreenHelper.getTop(screen) + 6;

            CoinShopButton shopBtn = new CoinShopButton(
                x, y,
                btn -> {
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.connection.sendCommand("hqs shop");
                    }
                },
                Tooltip.create(Component.literal("LING Quest Shop (เปิดร้านค้า)"))
            );
            event.addListener(shopBtn);
        }
    }
}
