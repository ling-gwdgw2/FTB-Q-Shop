package com.holysweet.questshop.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ShopReloader extends SimplePreparableReloadListener<ShopReloader.Parsed> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final ShopReloader INSTANCE = new ShopReloader();

    public record Parsed(Map<ResourceLocation, ShopCategory> categories, List<ShopEntry> entries) {}

    private ShopReloader() {}

    @Override
    protected Parsed prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ShopCategory> categories = new HashMap<>();
        List<ShopEntry> entries = new ArrayList<>();

        // Load Categories from data/<namespace>/questshop/categories/*.json
        resourceManager.listResources("questshop/categories", loc -> loc.getPath().endsWith(".json"))
                .forEach((loc, resource) -> {
                    try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                        if (obj != null) {
                            String path = loc.getPath();
                            String filename = path.substring(path.lastIndexOf('/') + 1).replace(".json", "");
                            ResourceLocation catId = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), filename);
                            String display = obj.has("display") ? obj.get("display").getAsString() : filename;
                            boolean unlocked = !obj.has("unlocked_by_default") || obj.get("unlocked_by_default").getAsBoolean();
                            int order = obj.has("order") ? obj.get("order").getAsInt() : 0;
                            categories.put(catId, new ShopCategory(catId, display, unlocked, order));
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load category from {}", loc, e);
                    }
                });

        // Load Entries from data/<namespace>/questshop/entries/*.json
        resourceManager.listResources("questshop/entries", loc -> loc.getPath().endsWith(".json"))
                .forEach((loc, resource) -> {
                    try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        JsonElement json = GSON.fromJson(reader, JsonElement.class);
                        if (json != null) {
                            if (json.isJsonArray()) {
                                for (JsonElement elem : json.getAsJsonArray()) {
                                    ShopCodecs.ENTRY_CODEC.parse(JsonOps.INSTANCE, elem)
                                            .resultOrPartial(err -> LOGGER.error("Error parsing shop entry in {}: {}", loc, err))
                                            .ifPresent(entries::add);
                                }
                            } else if (json.isJsonObject()) {
                                ShopCodecs.ENTRY_CODEC.parse(JsonOps.INSTANCE, json)
                                        .resultOrPartial(err -> LOGGER.error("Error parsing shop entry in {}: {}", loc, err))
                                        .ifPresent(entries::add);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load entries from {}", loc, e);
                    }
                });

        return new Parsed(categories, entries);
    }

    @Override
    protected void apply(Parsed parsed, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("Applying Quest Shop datapack reload: {} categories, {} entries",
                parsed.categories().size(), parsed.entries().size());
        ShopCatalog.INSTANCE.replace(parsed.categories(), parsed.entries());
    }
}
