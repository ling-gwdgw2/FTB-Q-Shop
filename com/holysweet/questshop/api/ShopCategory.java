package com.holysweet.questshop.api;

import net.minecraft.resources.ResourceLocation;

public record ShopCategory(
        ResourceLocation id,
        String display,
        boolean unlockedByDefault,
        int order
) {}
