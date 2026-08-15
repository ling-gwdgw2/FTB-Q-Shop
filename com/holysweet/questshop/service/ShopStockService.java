package com.holysweet.questshop.service;

import com.holysweet.questshop.Config;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopStockService {

    private static final Map<String, Integer> REMAINING_STOCK = new ConcurrentHashMap<>();
    private static final Map<String, Integer> DAILY_PURCHASES = new ConcurrentHashMap<>();
    private static long lastDayTime = -1;

    private static String getStockKey(ResourceLocation category, ResourceLocation itemId) {
        return category.toString() + "|" + itemId.toString();
    }

    private static String getDailyKey(UUID playerUuid, ResourceLocation category, ResourceLocation itemId) {
        return playerUuid.toString() + "|" + category.toString() + "|" + itemId.toString();
    }

    private static void checkAndResetDay(ServerPlayer player) {
        if (player == null || player.serverLevel() == null) return;
        long currentDay = player.serverLevel().getDayTime() / 24000L;
        if (lastDayTime == -1) {
            lastDayTime = currentDay;
        } else if (currentDay != lastDayTime) {
            lastDayTime = currentDay;
            DAILY_PURCHASES.clear();
        }
    }

    public static BuyResultPayload.Code canPurchase(ServerPlayer player, ShopEntry entry, int requestedQty) {
        if (!Config.ENABLE_PURCHASE_LIMITS.get()) {
            return BuyResultPayload.Code.OK;
        }

        checkAndResetDay(player);

        // 1. Total Stock Check
        if (entry.hasTotalStock()) {
            int currentStock = getRemainingStock(entry);
            if (currentStock < requestedQty) {
                return BuyResultPayload.Code.OUT_OF_STOCK;
            }
        }

        // 2. Daily Purchase Limit Check
        if (entry.hasDailyLimit()) {
            int purchasedToday = getDailyPurchased(player, entry);
            if (purchasedToday + requestedQty > entry.dailyLimit()) {
                return BuyResultPayload.Code.DAILY_LIMIT_REACHED;
            }
        }

        return BuyResultPayload.Code.OK;
    }

    public static void recordPurchase(ServerPlayer player, ShopEntry entry, int purchasedQty) {
        if (!Config.ENABLE_PURCHASE_LIMITS.get()) return;

        checkAndResetDay(player);

        if (entry.hasTotalStock()) {
            String stockKey = getStockKey(entry.category(), entry.itemId());
            int current = REMAINING_STOCK.computeIfAbsent(stockKey, k -> entry.totalStock());
            REMAINING_STOCK.put(stockKey, Math.max(0, current - purchasedQty));
        }

        if (entry.hasDailyLimit()) {
            String dailyKey = getDailyKey(player.getUUID(), entry.category(), entry.itemId());
            int current = DAILY_PURCHASES.getOrDefault(dailyKey, 0);
            DAILY_PURCHASES.put(dailyKey, current + purchasedQty);
        }
    }

    public static int getRemainingStock(ShopEntry entry) {
        if (!entry.hasTotalStock()) return -1;
        String stockKey = getStockKey(entry.category(), entry.itemId());
        return REMAINING_STOCK.computeIfAbsent(stockKey, k -> entry.totalStock());
    }

    public static int getDailyPurchased(ServerPlayer player, ShopEntry entry) {
        if (!entry.hasDailyLimit() || player == null) return 0;
        checkAndResetDay(player);
        String dailyKey = getDailyKey(player.getUUID(), entry.category(), entry.itemId());
        return DAILY_PURCHASES.getOrDefault(dailyKey, 0);
    }
}
