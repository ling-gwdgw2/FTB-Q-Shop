package com.holysweet.questshop.service;

import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.api.categories.CategorySetting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VanillaCategoriesProvider implements CategoriesProvider {

    private final Map<UUID, Map<ResourceLocation, CategorySetting>> settings = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public CategorySetting getSetting(ServerPlayer player, ResourceLocation category) {
        if (player == null || category == null) return CategorySetting.DEFAULT;
        Map<ResourceLocation, CategorySetting> userMap = settings.get(player.getUUID());
        return userMap != null ? userMap.getOrDefault(category, CategorySetting.DEFAULT) : CategorySetting.DEFAULT;
    }

    @Override
    public boolean setSetting(ServerPlayer player, ResourceLocation category, CategorySetting setting) {
        if (player == null || category == null) return false;
        Map<ResourceLocation, CategorySetting> userMap = settings.computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>());
        if (setting == CategorySetting.DEFAULT) {
            userMap.remove(category);
        } else {
            userMap.put(category, setting);
        }
        return true;
    }
}
