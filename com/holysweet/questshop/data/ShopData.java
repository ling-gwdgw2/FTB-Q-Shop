package com.holysweet.questshop.data;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class ShopData {
    public static final ShopData INSTANCE = new ShopData();

    private ShopData() {}

    public void replace(Map<ResourceLocation, ShopCategory> categories, List<ShopEntry> entries) {
        ShopCatalog.INSTANCE.replace(categories, entries);
    }

    public Map<ResourceLocation, ShopCategory> categories() {
        return ShopCatalog.INSTANCE.categories();
    }

    public List<ShopCategory> sortedCategories() {
        return ShopCatalog.INSTANCE.sortedCategories();
    }

    public List<ShopEntry> allEntries() {
        return ShopCatalog.INSTANCE.allEntries();
    }

    public List<ShopEntry> entriesInCategory(ResourceLocation category) {
        return ShopCatalog.INSTANCE.entriesInCategory(category);
    }
}
