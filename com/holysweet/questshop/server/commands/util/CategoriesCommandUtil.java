package com.holysweet.questshop.server.commands.util;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.data.ShopCatalog;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

public final class CategoriesCommandUtil {

    public static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(ShopCatalog.INSTANCE.categories().keySet(), builder);

    private CategoriesCommandUtil() {}

    public static ResourceLocation coerceToQS(ResourceLocation id) {
        if (id == null) return null;
        if ("minecraft".equals(id.getNamespace())) {
            return ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, id.getPath());
        }
        return id;
    }
}
