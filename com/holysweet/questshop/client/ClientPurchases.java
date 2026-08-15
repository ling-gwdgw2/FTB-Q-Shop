package com.holysweet.questshop.client;

import com.holysweet.questshop.api.ShopEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPurchases {

    private static final Map<String, Integer> DAILY_PURCHASED = new ConcurrentHashMap<>();
    private static final Map<String, Integer> REMAINING_STOCK = new ConcurrentHashMap<>();

    private static String getKey(ResourceLocation category, ResourceLocation itemId) {
        return category.toString() + "|" + itemId.toString();
    }

    public static void recordPurchase(ResourceLocation category, ResourceLocation itemId, int amount) {
        String key = getKey(category, itemId);
        DAILY_PURCHASED.put(key, DAILY_PURCHASED.getOrDefault(key, 0) + 1);
    }

    public static int getPurchased(ShopEntry entry) {
        if (entry == null) return 0;
        String key = getKey(entry.category(), entry.itemId());
        return DAILY_PURCHASED.getOrDefault(key, 0);
    }

    public static boolean isSoldOut(ShopEntry entry) {
        if (entry == null) return false;
        if (entry.hasDailyLimit() && getPurchased(entry) >= entry.dailyLimit()) {
            return true;
        }
        if (entry.hasTotalStock()) {
            String key = getKey(entry.category(), entry.itemId());
            int remaining = REMAINING_STOCK.getOrDefault(key, entry.totalStock());
            if (remaining <= 0) return true;
        }
        return false;
    }

    public static void reset() {
        DAILY_PURCHASED.clear();
        REMAINING_STOCK.clear();
    }
}
