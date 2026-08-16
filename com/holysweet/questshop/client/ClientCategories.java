package com.holysweet.questshop.client;

import com.holysweet.questshop.api.ShopCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientCategories {
    private static Map<ResourceLocation, ShopCategory> CATEGORIES = new ConcurrentHashMap<>();
    private static Set<ResourceLocation> UNLOCKED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ClientCategories() {}

    public static void applySnapshot(Map<ResourceLocation, ShopCategory> categories, Set<ResourceLocation> unlocked) {
        CATEGORIES = new ConcurrentHashMap<>(categories);
        UNLOCKED = Collections.newSetFromMap(new ConcurrentHashMap<>());
        UNLOCKED.addAll(unlocked);
    }

    public static boolean isUnlocked(ResourceLocation category) {
        if (category == null) return true;
        ShopCategory cat = CATEGORIES.get(category);
        if (cat != null && cat.unlockedByDefault()) return true;
        return UNLOCKED.contains(category);
    }

    public static Map<ResourceLocation, ShopCategory> categories() {
        return CATEGORIES;
    }
}
