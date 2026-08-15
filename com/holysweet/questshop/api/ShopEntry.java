package com.holysweet.questshop.api;

import net.minecraft.resources.ResourceLocation;

public record ShopEntry(
        ResourceLocation itemId,
        int amount,
        int cost,
        ResourceLocation category
) {}
