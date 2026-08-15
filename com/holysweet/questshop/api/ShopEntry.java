package com.holysweet.questshop.api;

import net.minecraft.resources.ResourceLocation;

public record ShopEntry(
        ResourceLocation itemId,
        int amount,
        int cost,
        ResourceLocation category,
        int dailyLimit,     // 0 = unlimited
        int totalStock,     // -1 = unlimited
        int discountPercent // 0 = 0% discount
) {
    public ShopEntry(ResourceLocation itemId, int amount, int cost, ResourceLocation category) {
        this(itemId, amount, cost, category, 0, -1, 0);
    }

    public ShopEntry(ResourceLocation itemId, int amount, int cost, ResourceLocation category, int dailyLimit, int totalStock) {
        this(itemId, amount, cost, category, dailyLimit, totalStock, 0);
    }

    public int effectiveCost() {
        if (discountPercent <= 0) return cost;
        int discounted = (int) Math.round(cost * (1.0 - (discountPercent / 100.0)));
        return Math.max(1, discounted);
    }

    public boolean hasDiscount() {
        return discountPercent > 0;
    }

    public boolean hasDailyLimit() {
        return dailyLimit > 0;
    }

    public boolean hasTotalStock() {
        return totalStock >= 0;
    }
}
