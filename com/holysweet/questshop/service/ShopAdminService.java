package com.holysweet.questshop.service;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.Net;
import com.holysweet.questshop.network.payload.AdminRemoveEntryPayload;
import com.holysweet.questshop.network.payload.AdminUpdateCategoryPayload;
import com.holysweet.questshop.network.payload.AdminUpdateEntryPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ShopAdminService {

    public static boolean hasAdminPermission(ServerPlayer player) {
        return player != null && (player.hasPermissions(2) || player.isCreative());
    }

    public static void handleUpdateCategory(ServerPlayer admin, AdminUpdateCategoryPayload payload) {
        if (!hasAdminPermission(admin) || payload == null) return;

        MinecraftServer server = admin.getServer();
        if (payload.delete()) {
            ShopCatalog.INSTANCE.removeCategory(payload.categoryId());
        } else {
            ShopCategory cat = new ShopCategory(payload.categoryId(), payload.display(), payload.unlockedByDefault(), payload.order());
            ShopCatalog.INSTANCE.addOrUpdateCategory(cat);
        }

        ShopCatalog.INSTANCE.saveCategoriesToDisk(server);
        ShopCatalog.INSTANCE.saveToDisk(server);

        Net.sendCategoriesSnapshot(server);
        Net.sendShopData(server);
    }

    public static void handleUpdateEntry(ServerPlayer admin, AdminUpdateEntryPayload payload) {
        if (!hasAdminPermission(admin) || payload == null) return;

        MinecraftServer server = admin.getServer();
        ShopEntry entry = new ShopEntry(payload.itemId(), payload.amount(), payload.cost(), payload.category());
        ShopCatalog.INSTANCE.addOrUpdateEntry(entry);
        ShopCatalog.INSTANCE.saveToDisk(server);

        Net.sendShopData(server);
    }

    public static void handleRemoveEntry(ServerPlayer admin, AdminRemoveEntryPayload payload) {
        if (!hasAdminPermission(admin) || payload == null) return;

        MinecraftServer server = admin.getServer();
        ShopCatalog.INSTANCE.removeEntry(payload.itemId(), payload.category());
        ShopCatalog.INSTANCE.saveToDisk(server);

        Net.sendShopData(server);
    }
}
