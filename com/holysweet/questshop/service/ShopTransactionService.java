package com.holysweet.questshop.service;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.Net;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.holysweet.questshop.network.payload.BuyOkToastPayload;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class ShopTransactionService {

    public static void processPurchase(ServerPlayer player, BuyEntryPayload payload) {
        if (player == null || payload == null) return;

        // 1. Validate category unlocked state
        if (!CategoriesService.isUnlocked(player, payload.category())) {
            PacketDistributor.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.LOCKED_CATEGORY));
            return;
        }

        // 2. Validate entry exists in catalog
        Optional<ShopEntry> entryOpt = ShopCatalog.INSTANCE.entriesInCategory(payload.category()).stream()
                .filter(e -> e.itemId().equals(payload.itemId()))
                .findFirst();

        if (entryOpt.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.INVALID_ENTRY));
            return;
        }

        ShopEntry entry = entryOpt.get();
        int qtyMultiplier = Math.max(1, payload.amount() / Math.max(1, entry.amount()));
        int totalCost = entry.cost() * qtyMultiplier;
        int totalItems = entry.amount() * qtyMultiplier;

        // 3. Validate coin balance
        int userCoins = CoinsService.get(player.serverLevel(), player);
        if (userCoins < totalCost) {
            PacketDistributor.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.NOT_ENOUGH_COINS));
            return;
        }

        // 4. Validate item registry
        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(payload.itemId());
        if (itemOpt.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.INVALID_ENTRY));
            return;
        }

        ItemStack purchasedStack = new ItemStack(itemOpt.get(), totalItems);

        // 5. Validate inventory space
        if (!hasInventorySpace(player, purchasedStack)) {
            PacketDistributor.sendToPlayer(player, new BuyResultPayload(BuyResultPayload.Code.NO_INVENTORY_SPACE));
            return;
        }

        // 6. Execute atomic transaction
        CoinsService.add(player.serverLevel(), player, -totalCost);
        Net.syncBalance(player);

        if (!player.getInventory().add(purchasedStack)) {
            player.drop(purchasedStack, false);
        }

        // 7. Send success feedback
        PacketDistributor.sendToPlayer(player, new BuyOkToastPayload(payload.itemId(), totalItems, totalCost));
    }

    public static boolean hasInventorySpace(Player player, ItemStack itemToGive) {
        if (player == null || itemToGive == null || itemToGive.isEmpty()) return true;
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
