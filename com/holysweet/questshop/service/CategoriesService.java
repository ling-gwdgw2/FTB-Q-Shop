package com.holysweet.questshop.service;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.api.categories.CategorySetting;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class CategoriesService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicReference<CategoriesProvider> ACTIVE = new AtomicReference<>(new VanillaCategoriesProvider());

    private CategoriesService() {}

    public static CategoriesProvider provider() {
        return ACTIVE.get();
    }

    public static void swapProvider(CategoriesProvider newProvider) {
        if (newProvider != null) {
            ACTIVE.set(newProvider);
            LOGGER.info("Categories backend swapped to: {}", newProvider.id());
        }
    }

    public static Map<ResourceLocation, ShopCategory> categories() {
        return ShopCatalog.INSTANCE.categories();
    }

    public static List<ResourceLocation> categoryOrder() {
        List<ShopCategory> list = ShopCatalog.INSTANCE.sortedCategories();
        return list.stream().map(ShopCategory::id).toList();
    }

    public static boolean isUnlocked(ServerPlayer player, ResourceLocation category) {
        if (player == null || category == null) return true;
        ShopCategory cat = categories().get(category);
        CategorySetting setting = provider().getSetting(player, category);
        if (setting == CategorySetting.UNLOCKED) return true;
        if (setting == CategorySetting.LOCKED) return false;
        return cat == null || cat.unlockedByDefault();
    }

    public static Set<ResourceLocation> effectiveUnlocked(ServerPlayer player) {
        Set<ResourceLocation> unlocked = new HashSet<>();
        if (player == null) return unlocked;
        for (Map.Entry<ResourceLocation, ShopCategory> e : categories().entrySet()) {
            if (isUnlocked(player, e.getKey())) {
                unlocked.add(e.getKey());
            }
        }
        return unlocked;
    }

    public static boolean setUnlocked(ServerPlayer player, ResourceLocation category, boolean unlocked) {
        if (player == null || category == null) return false;
        CategorySetting setting = unlocked ? CategorySetting.UNLOCKED : CategorySetting.LOCKED;
        boolean ok = provider().setSetting(player, category, setting);
        if (ok) {
            Net.sendCategoriesSnapshot(player);
        }
        return ok;
    }

    public static boolean clear(ServerPlayer player, ResourceLocation category) {
        if (player == null || category == null) return false;
        boolean ok = provider().setSetting(player, category, CategorySetting.DEFAULT);
        if (ok) {
            Net.sendCategoriesSnapshot(player);
        }
        return ok;
    }
}
