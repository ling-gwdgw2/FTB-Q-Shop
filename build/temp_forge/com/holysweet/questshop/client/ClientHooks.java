package com.holysweet.questshop.client;

import com.holysweet.questshop.client.screen.ShopMenuScreen;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHooks {

    public static void updateShopMenuButtonState() {
        if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
            screen.updateButtonState();
        }
    }

    public static void refreshShopMenuEntries() {
        if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
            screen.refreshEntries();
        }
    }

    public static void onPurchaseResult(BuyResultPayload.Code code) {
        if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
            screen.onPurchaseResult(code);
        }
    }
}
