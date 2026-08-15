package com.holysweet.questshop.network;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientHooks;
import com.holysweet.questshop.client.ClientShopData;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.payload.*;
import com.holysweet.questshop.service.CategoriesService;
import com.holysweet.questshop.service.CoinsService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

public class Net {

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(CoinsBalancePayload.TYPE, CoinsBalancePayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                ClientCoins.set(payload.balance());
                if (FMLEnvironment.dist.isClient()) {
                    ClientHooks.updateShopMenuButtonState();
                }
            });
        });

        registrar.playToClient(CategoriesSnapshotPayload.TYPE, CategoriesSnapshotPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                ClientCategories.applySnapshot(payload.categories(), payload.unlocked());
                if (FMLEnvironment.dist.isClient()) {
                    ClientHooks.refreshShopMenuEntries();
                }
            });
        });

        registrar.playToClient(ShopDataPayload.TYPE, ShopDataPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                ClientShopData.set(payload.entries());
                if (FMLEnvironment.dist.isClient()) {
                    ClientHooks.refreshShopMenuEntries();
                }
            });
        });

        registrar.playToClient(BuyResultPayload.TYPE, BuyResultPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (FMLEnvironment.dist.isClient()) {
                    ClientHooks.onPurchaseResult(payload.code());
                }
            });
        });

        registrar.playToClient(BuyOkToastPayload.TYPE, BuyOkToastPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                // Handled on client
            });
        });

        registrar.playToServer(BuyEntryPayload.TYPE, BuyEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    processPurchase(player, payload);
                }
            });
        });

        registrar.playToServer(AdminUpdateCategoryPayload.TYPE, AdminUpdateCategoryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && (player.hasPermissions(2) || player.isCreative())) {
                    if (payload.delete()) {
                        ShopCatalog.INSTANCE.removeCategory(payload.categoryId());
                    } else {
                        ShopCategory cat = new ShopCategory(payload.categoryId(), payload.display(), payload.unlockedByDefault(), payload.order());
                        ShopCatalog.INSTANCE.addOrUpdateCategory(cat);
                    }
                    ShopCatalog.INSTANCE.saveCategoriesToDisk(player.getServer());
                    ShopCatalog.INSTANCE.saveToDisk(player.getServer());
                    sendCategoriesSnapshot(player.getServer());
                    sendShopData(player.getServer());
                }
            });
        });

        registrar.playToServer(AdminUpdateEntryPayload.TYPE, AdminUpdateEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && (player.hasPermissions(2) || player.isCreative())) {
                    ShopEntry entry = new ShopEntry(payload.itemId(), payload.amount(), payload.cost(), payload.category());
                    ShopCatalog.INSTANCE.addOrUpdateEntry(entry);
                    ShopCatalog.INSTANCE.saveToDisk(player.getServer());
                    sendShopData(player.getServer());
                }
            });
        });

        registrar.playToServer(AdminRemoveEntryPayload.TYPE, AdminRemoveEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player && (player.hasPermissions(2) || player.isCreative())) {
                    ShopCatalog.INSTANCE.removeEntry(payload.itemId(), payload.category());
                    ShopCatalog.INSTANCE.saveToDisk(player.getServer());
                    sendShopData(player.getServer());
                }
            });
        });
    }

    public static void syncBalance(ServerPlayer player) {
        if (player == null) return;
        int balance = CoinsService.get(player.serverLevel(), player);
        NetworkBridge.sendToPlayer(player, new CoinsBalancePayload(balance));
    }

    public static void sendCategoriesSnapshot(ServerPlayer player) {
        if (player == null) return;
        NetworkBridge.sendToPlayer(player, new CategoriesSnapshotPayload(
                ShopCatalog.INSTANCE.categories(),
                CategoriesService.effectiveUnlocked(player)
        ));
    }

    public static void sendCategoriesSnapshot(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendCategoriesSnapshot(player);
        }
    }

    public static void sendShopData(ServerPlayer player) {
        if (player == null) return;
        NetworkBridge.sendToPlayer(player, new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
    }

    public static void sendShopData(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        NetworkBridge.sendToAllPlayers(new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
    }

    private static void processPurchase(ServerPlayer player, BuyEntryPayload payload) {
        if (!CategoriesService.isUnlocked(player, payload.category())) {
            NetworkBridge.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.LOCKED_CATEGORY));
            return;
        }

        Optional<ShopEntry> entryOpt = ShopCatalog.INSTANCE.entriesInCategory(payload.category()).stream()
                .filter(e -> e.itemId().equals(payload.itemId()))
                .findFirst();

        if (entryOpt.isEmpty()) {
            NetworkBridge.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.INVALID_ENTRY));
            return;
        }

        ShopEntry entry = entryOpt.get();
        int qtyMultiplier = Math.max(1, payload.amount() / Math.max(1, entry.amount()));
        int totalCost = entry.cost() * qtyMultiplier;
        int totalItems = entry.amount() * qtyMultiplier;

        int userCoins = CoinsService.get(player.serverLevel(), player);
        if (userCoins < totalCost) {
            NetworkBridge.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.NOT_ENOUGH_COINS));
            return;
        }

        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(payload.itemId());
        if (itemOpt.isEmpty()) {
            NetworkBridge.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.INVALID_ENTRY));
            return;
        }

        ItemStack purchasedStack = new ItemStack(itemOpt.get(), totalItems);
        if (!hasInventorySpace(player, purchasedStack)) {
            NetworkBridge.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.NO_INVENTORY_SPACE));
            return;
        }

        CoinsService.add(player.serverLevel(), player, -totalCost);
        syncBalance(player);

        if (!player.getInventory().add(purchasedStack)) {
            player.drop(purchasedStack, false);
        }

        NetworkBridge.sendToPlayer(player, new BuyOkToastPayload(payload.itemId(), totalItems, totalCost));
    }

    private static boolean hasInventorySpace(Player player, ItemStack itemToGive) {
        int countToGive = itemToGive.getCount();
        int maxStackSize = itemToGive.getMaxStackSize();

        for (ItemStack slot : player.getInventory().items) {
            if (slot.isEmpty()) {
                countToGive -= maxStackSize;
            } else if (ItemStack.isSameItemSameComponents(slot, itemToGive)) {
                countToGive -= (maxStackSize - slot.getCount());
            }
            if (countToGive <= 0) return true;
        }
        return false;
    }
}
