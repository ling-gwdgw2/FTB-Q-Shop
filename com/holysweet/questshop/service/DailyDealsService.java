package com.holysweet.questshop.service;

import com.holysweet.questshop.Config;
import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.Net;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DailyDealsService {

    public static final ResourceLocation DEALS_CATEGORY_ID = ResourceLocation.fromNamespaceAndPath("ling_q_shop", "special_deals");
    private static long lastDealsDay = -1;

    public static void checkAndRefreshDeals(MinecraftServer server) {
        if (!Config.ENABLE_DAILY_DEALS.get() || server == null) return;

        ServerLevel overworld = server.overworld();
        if (overworld == null) return;

        long currentDay = overworld.getDayTime() / 24000L;
        if (currentDay != lastDealsDay || ShopCatalog.INSTANCE.entriesInCategory(DEALS_CATEGORY_ID).isEmpty()) {
            lastDealsDay = currentDay;
            generateNewDeals(server);
        }
    }

    public static void generateNewDeals(MinecraftServer server) {
        if (server == null) return;

        // 1. Ensure Special Deals Category exists at top order
        ShopCategory dealsCategory = new ShopCategory(
                DEALS_CATEGORY_ID,
                "🔥 โปรโมชั่น",
                true,
                -100
        );
        ShopCatalog.INSTANCE.addOrUpdateCategory(dealsCategory);

        // 2. Collect eligible items from standard categories
        List<ShopEntry> candidates = new ArrayList<>();
        for (ShopEntry entry : ShopCatalog.INSTANCE.allEntries()) {
            if (!entry.category().equals(DEALS_CATEGORY_ID) && entry.cost() > 0) {
                candidates.add(entry);
            }
        }

        if (candidates.isEmpty()) return;

        Collections.shuffle(candidates, new Random());
        int count = Math.min(Config.DEALS_COUNT.get(), candidates.size());
        int minDiscount = Math.max(5, Config.MIN_DISCOUNT_PERCENT.get());
        int maxDiscount = Math.min(90, Math.max(minDiscount, Config.MAX_DISCOUNT_PERCENT.get()));

        List<ShopEntry> newDeals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ShopEntry original = candidates.get(i);
            int discount = (ThreadLocalRandom.current().nextInt(minDiscount, maxDiscount + 1) / 5) * 5;
            if (discount <= 0) discount = 20;

            ShopEntry dealEntry = new ShopEntry(
                    original.itemId(),
                    original.amount(),
                    original.cost(),
                    DEALS_CATEGORY_ID,
                    Math.max(1, original.dailyLimit()),
                    original.totalStock(),
                    discount
            );
            newDeals.add(dealEntry);
        }

        // 3. Clear old deals and insert new ones
        for (ShopEntry old : ShopCatalog.INSTANCE.entriesInCategory(DEALS_CATEGORY_ID)) {
            ShopCatalog.INSTANCE.removeEntry(old.itemId(), DEALS_CATEGORY_ID);
        }
        for (ShopEntry deal : newDeals) {
            ShopCatalog.INSTANCE.addOrUpdateEntry(deal);
        }

        // 4. Broadcast updated data to clients
        Net.sendCategoriesSnapshot(server);
        Net.sendShopData(server);
    }
}
