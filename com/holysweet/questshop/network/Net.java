package com.holysweet.questshop.network;

import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientHooks;
import com.holysweet.questshop.client.ClientShopData;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.payload.*;
import com.holysweet.questshop.service.CategoriesService;
import com.holysweet.questshop.service.CoinsService;
import com.holysweet.questshop.service.ShopAdminService;
import com.holysweet.questshop.service.ShopTransactionService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Net {

    public static void register(PayloadRegistrar registrar) {
        // ==========================================
        // Client-Bound Packets (Server -> Client)
        // ==========================================
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
                if (FMLEnvironment.dist.isClient()) {
                    ClientHooks.onPurchaseOk(payload.itemId(), payload.amount(), payload.cost());
                }
            });
        });

        // ==========================================
        // Server-Bound Packets (Client -> Server)
        // Routed directly to Service Layer
        // ==========================================
        registrar.playToServer(BuyEntryPayload.TYPE, BuyEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    ShopTransactionService.processPurchase(player, payload);
                }
            });
        });

        registrar.playToServer(AdminUpdateCategoryPayload.TYPE, AdminUpdateCategoryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    ShopAdminService.handleUpdateCategory(player, payload);
                }
            });
        });

        registrar.playToServer(AdminUpdateEntryPayload.TYPE, AdminUpdateEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    ShopAdminService.handleUpdateEntry(player, payload);
                }
            });
        });

        registrar.playToServer(AdminRemoveEntryPayload.TYPE, AdminRemoveEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    ShopAdminService.handleRemoveEntry(player, payload);
                }
            });
        });
    }

    public static void sendCoinsBalance(ServerPlayer player, int balance) {
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, new CoinsBalancePayload(balance));
    }

    public static void syncBalance(ServerPlayer player) {
        if (player == null) return;
        int balance = CoinsService.get(player.serverLevel(), player);
        sendCoinsBalance(player, balance);
    }

    public static void sendCategoriesSnapshot(ServerPlayer player) {
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, new CategoriesSnapshotPayload(
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
        PacketDistributor.sendToPlayer(player, new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
    }

    public static void sendShopData(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        PacketDistributor.sendToAllPlayers(new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
    }
}
