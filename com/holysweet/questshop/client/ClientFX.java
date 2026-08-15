package com.holysweet.questshop.client;

import com.holysweet.questshop.Config;
import com.holysweet.questshop.client.toast.PurchaseToast;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class ClientFX {

    public static void purchaseOk(ResourceLocation itemId, int amount, int cost) {
        if (!Config.ENABLE_TOAST_NOTIFICATIONS.get()) return;

        Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(itemId);
        ItemStack stack = new ItemStack(opt.orElse(Items.BARRIER), Math.max(1, amount));
        String name = opt.map(Item::getDescriptionId).orElse(itemId.toString());

        MutableComponent title = Component.translatable("questshop.buy.ok");
        MutableComponent desc = Component.translatable("questshop.buy.toast.detail", amount, Component.translatable(name), cost);

        Minecraft.getInstance().getToasts().addToast(new PurchaseToast(stack, title, desc));
    }

    public static void purchaseError(Component detail) {
        if (!Config.ENABLE_TOAST_NOTIFICATIONS.get()) return;

        ItemStack barrier = new ItemStack(Items.BARRIER, 1);
        MutableComponent title = Component.translatable("questshop.buy.failed");

        Minecraft.getInstance().getToasts().addToast(new PurchaseToast(barrier, title, detail));
    }
}
