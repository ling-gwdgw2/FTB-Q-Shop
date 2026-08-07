package com.holysweet.questshop.network;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientShopData;
import com.holysweet.questshop.client.screen.ShopMenuScreen;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.payload.*;
import com.holysweet.questshop.service.CategoriesService;
import com.holysweet.questshop.service.CoinsService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

public class Net {

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(CoinsBalancePayload.TYPE, CoinsBalancePayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                ClientCoins.set(payload.balance());
                if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
                    screen.updateButtonState();
                }
            });
        });

        registrar.playToClient(CategoriesSnapshotPayload.TYPE, CategoriesSnapshotPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                ClientCategories.applySnapshot(payload.categories(), payload.unlocked());
                if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
                    screen.refreshEntries();
                }
            });
        });

        registrar.playToClient(ShopDataPayload.TYPE, ShopDataPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                ClientShopData.set(payload.entries());
                if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
                    screen.refreshEntries();
                }
            });
        });

        registrar.playToClient(BuyResultPayload.TYPE, BuyResultPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
                    screen.onPurchaseResult(payload.code());
                }
            });
        });

        registrar.playToClient(BuyOkToastPayload.TYPE, BuyOkToastPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof ShopMenuScreen screen) {
                    screen.onPurchaseOk(payload.itemId(), payload.amount(), payload.cost());
                }
            });
        });

        registrar.playToServer(BuyEntryPayload.TYPE, BuyEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                handleBuy(ctx.player(), payload);
            });
        });

        // Creative / Admin Editing Payloads
        registrar.playToServer(AdminUpdateEntryPayload.TYPE, AdminUpdateEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    if (player.isCreative() || player.hasPermissions(2)) {
                        ShopEntry newEntry = new ShopEntry(payload.itemId(), payload.amount(), payload.cost(), payload.category());
                        ShopCatalog.INSTANCE.addOrUpdateEntry(newEntry);
                        ShopCatalog.INSTANCE.saveToDisk(player.serverLevel().getServer());
                        PacketDistributor.sendToAllPlayers(new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
                    }
                }
            });
        });

        registrar.playToServer(AdminRemoveEntryPayload.TYPE, AdminRemoveEntryPayload.CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    if (player.isCreative() || player.hasPermissions(2)) {
                        ShopCatalog.INSTANCE.removeEntry(payload.itemId(), payload.category());
                        ShopCatalog.INSTANCE.saveToDisk(player.serverLevel().getServer());
                        PacketDistributor.sendToAllPlayers(new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
                    }
                }
            });
        });
    }

    public static void syncBalance(ServerPlayer player) {
        if (player == null) return;
        int balance = CoinsService.get(player.serverLevel(), player);
        sendCoinsBalance(player, balance);
    }

    public static void sendCoinsBalance(ServerPlayer player, int balance) {
        PacketDistributor.sendToPlayer(player, new CoinsBalancePayload(balance));
    }

    public static void sendCategoriesSnapshot(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CategoriesSnapshotPayload(
            ShopCatalog.INSTANCE.categories(),
            CategoriesService.effectiveUnlocked(player)
        ));
    }

    public static void sendShopData(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ShopDataPayload(ShopCatalog.INSTANCE.allEntries()));
    }

    public static void sendBuyResult(ServerPlayer player, BuyResultPayload.Code code) {
        PacketDistributor.sendToPlayer(player, new BuyResultPayload(code));
    }

    public static void sendBuyOkToast(ServerPlayer player, ResourceLocation itemId, int amount, int cost) {
        PacketDistributor.sendToPlayer(player, new BuyOkToastPayload(itemId, amount, cost));
    }

    private static void handleBuy(Player player, BuyEntryPayload payload) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(payload.itemId());
        if (itemOpt.isEmpty()) {
            sendBuyResult(serverPlayer, BuyResultPayload.Code.INVALID_ENTRY);
            return;
        }

        // Find base shop entry
        Optional<ShopEntry> entryOpt = ShopCatalog.INSTANCE.allEntries().stream()
                .filter(e -> e.itemId().equals(payload.itemId()) && e.category().equals(payload.category()))
                .findFirst();

        if (entryOpt.isEmpty()) {
            sendBuyResult(serverPlayer, BuyResultPayload.Code.INVALID_ENTRY);
            return;
        }

        ShopEntry baseEntry = entryOpt.get();
        if (payload.amount() < baseEntry.amount() || payload.amount() % baseEntry.amount() != 0) {
            sendBuyResult(serverPlayer, BuyResultPayload.Code.INVALID_ENTRY);
            return;
        }

        int multiplier = payload.amount() / baseEntry.amount();
        int expectedCost = multiplier * baseEntry.cost();

        if (payload.cost() != expectedCost) {
            sendBuyResult(serverPlayer, BuyResultPayload.Code.INVALID_ENTRY);
            return;
        }

        if (!CategoriesService.isUnlocked(serverPlayer, baseEntry.category())) {
            sendBuyResult(serverPlayer, BuyResultPayload.Code.LOCKED_CATEGORY);
            return;
        }

        int currentCoins = CoinsService.get(serverPlayer.serverLevel(), serverPlayer);
        if (currentCoins < expectedCost) {
            sendBuyResult(serverPlayer, BuyResultPayload.Code.NOT_ENOUGH_COINS);
            return;
        }

        // Deduct coins and give items
        CoinsService.add(serverPlayer.serverLevel(), serverPlayer, -expectedCost);
        sendCoinsBalance(serverPlayer, CoinsService.get(serverPlayer.serverLevel(), serverPlayer));

        giveOrDrop(serverPlayer, new ItemStack(itemOpt.get(), payload.amount()));
        sendBuyOkToast(serverPlayer, payload.itemId(), payload.amount(), expectedCost);
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
