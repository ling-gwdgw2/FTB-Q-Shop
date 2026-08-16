package com.holysweet.questshop.api.categories;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface CategoriesProvider {
    String id();
    CategorySetting getSetting(ServerPlayer player, ResourceLocation category);
    boolean setSetting(ServerPlayer player, ResourceLocation category, CategorySetting setting);
}
