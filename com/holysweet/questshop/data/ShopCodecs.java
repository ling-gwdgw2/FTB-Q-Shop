package com.holysweet.questshop.data;

import com.holysweet.questshop.api.ShopEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public final class ShopCodecs {

    public static final ResourceLocation DEFAULT_CATEGORY = ResourceLocation.fromNamespaceAndPath("ling_q_shop", "general");

    public static final Codec<ResourceLocation> CATEGORY_ID_CODEC = Codec.STRING.comapFlatMap(
            str -> {
                if (str.indexOf(':') >= 0) {
                    ResourceLocation parsed = ResourceLocation.tryParse(str);
                    return parsed != null ? DataResult.success(parsed) : DataResult.error(() -> "Invalid ResourceLocation: " + str);
                } else {
                    return DataResult.success(ResourceLocation.fromNamespaceAndPath("ling_q_shop", str));
                }
            },
            ResourceLocation::toString
    );

    public static final Codec<ShopEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(ShopEntry::itemId),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(ShopEntry::amount),
                    Codec.INT.optionalFieldOf("cost", 0).forGetter(ShopEntry::cost),
                    CATEGORY_ID_CODEC.optionalFieldOf("category", DEFAULT_CATEGORY).forGetter(ShopEntry::category),
                    Codec.INT.optionalFieldOf("daily_limit", 0).forGetter(ShopEntry::dailyLimit),
                    Codec.INT.optionalFieldOf("total_stock", -1).forGetter(ShopEntry::totalStock),
                    Codec.INT.optionalFieldOf("discount", 0).forGetter(ShopEntry::discountPercent)
            ).apply(instance, ShopEntry::new)
    );

    private ShopCodecs() {}
}
