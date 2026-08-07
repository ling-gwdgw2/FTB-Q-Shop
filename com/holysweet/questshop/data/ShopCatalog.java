package com.holysweet.questshop.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class ShopCatalog {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ShopCatalog INSTANCE = new ShopCatalog();

    private record Snapshot(Map<ResourceLocation, ShopCategory> categoriesById, List<ShopEntry> entries) {}

    private final AtomicReference<Snapshot> ref = new AtomicReference<>(new Snapshot(Map.of(), List.of()));

    private ShopCatalog() {}

    public void replace(Map<ResourceLocation, ShopCategory> categories, List<ShopEntry> entries) {
        Map<ResourceLocation, ShopCategory> catMap = new HashMap<>(categories);
        List<ShopEntry> entryList = new ArrayList<>(entries);

        entryList.sort(Comparator.comparingInt((ShopEntry e) -> {
            ShopCategory cat = catMap.getOrDefault(e.category(), new ShopCategory(
                    ResourceLocation.fromNamespaceAndPath("questshop", "general"),
                    "General",
                    true,
                    0
            ));
            return cat.order();
        }).thenComparing(e -> e.category().toString()).thenComparing(e -> e.itemId().toString()));

        this.ref.set(new Snapshot(Collections.unmodifiableMap(catMap), Collections.unmodifiableList(entryList)));
    }

    public Map<ResourceLocation, ShopCategory> categories() {
        return this.ref.get().categoriesById();
    }

    public List<ShopCategory> sortedCategories() {
        List<ShopCategory> list = new ArrayList<>(this.ref.get().categoriesById().values());
        list.sort(Comparator.comparingInt(ShopCategory::order).thenComparing(c -> c.id().toString()));
        return list;
    }

    public List<ShopEntry> allEntries() {
        return this.ref.get().entries();
    }

    public List<ShopEntry> entriesInCategory(ResourceLocation categoryId) {
        List<ShopEntry> result = new ArrayList<>();
        for (ShopEntry e : this.ref.get().entries()) {
            if (e.category().equals(categoryId)) {
                result.add(e);
            }
        }
        return result;
    }

    public void addOrUpdateCategory(ShopCategory newCategory) {
        Map<ResourceLocation, ShopCategory> currentCats = new HashMap<>(categories());
        currentCats.put(newCategory.id(), newCategory);
        replace(currentCats, allEntries());
    }

    public void removeCategory(ResourceLocation categoryId) {
        Map<ResourceLocation, ShopCategory> currentCats = new HashMap<>(categories());
        currentCats.remove(categoryId);
        List<ShopEntry> currentEntries = new ArrayList<>(allEntries());
        currentEntries.removeIf(e -> e.category().equals(categoryId));
        replace(currentCats, currentEntries);
    }

    public void addOrUpdateEntry(ShopEntry newEntry) {
        List<ShopEntry> current = new ArrayList<>(allEntries());
        current.removeIf(e -> e.itemId().equals(newEntry.itemId()) && e.category().equals(newEntry.category()));
        current.add(newEntry);
        replace(categories(), current);
    }

    public void removeEntry(ResourceLocation itemId, ResourceLocation categoryId) {
        List<ShopEntry> current = new ArrayList<>(allEntries());
        current.removeIf(e -> e.itemId().equals(itemId) && e.category().equals(categoryId));
        replace(categories(), current);
    }

    public void saveCategoriesToDisk(MinecraftServer server) {
        try {
            File configDir = new File("config/questshop/shop_categories");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            for (ShopCategory cat : categories().values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("display", cat.display());
                obj.addProperty("unlocked_by_default", cat.unlockedByDefault());
                obj.addProperty("order", cat.order());

                File file = new File(configDir, cat.id().getPath() + ".json");
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(obj, writer);
                }
            }
            LOGGER.info("[FtbQshop] Successfully saved shop categories to {}", configDir.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.error("[FtbQshop] Failed to save shop categories to disk", ex);
        }
    }

    public void saveToDisk(MinecraftServer server) {
        try {
            File configDir = new File("config/questshop/shop_entries");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            // Group entries by category path
            Map<String, JsonArray> categoryFiles = new HashMap<>();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            for (ShopEntry entry : allEntries()) {
                String catPath = entry.category().getPath();
                JsonArray array = categoryFiles.computeIfAbsent(catPath, k -> new JsonArray());

                JsonObject obj = new JsonObject();
                obj.addProperty("item", entry.itemId().toString());
                obj.addProperty("amount", entry.amount());
                obj.addProperty("cost", entry.cost());
                obj.addProperty("category", entry.category().toString());
                array.add(obj);
            }

            for (Map.Entry<String, JsonArray> e : categoryFiles.entrySet()) {
                File file = new File(configDir, e.getKey() + ".json");
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(e.getValue(), writer);
                }
            }
            LOGGER.info("[FtbQshop] Successfully saved updated shop entries to {}", configDir.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.error("[FtbQshop] Failed to save shop entries to disk", ex);
        }
    }
}
