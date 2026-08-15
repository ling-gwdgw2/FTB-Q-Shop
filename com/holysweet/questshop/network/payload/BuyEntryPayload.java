package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyEntryPayload(
        ResourceLocation itemId,
        int amount,
        int cost,
        ResourceLocation category
) implements CustomPacketPayload {
    public static final Type<BuyEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "buy_entry"));

    public static final StreamCodec<FriendlyByteBuf, BuyEntryPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BuyEntryPayload::itemId,
            ByteBufCodecs.VAR_INT, BuyEntryPayload::amount,
            ByteBufCodecs.VAR_INT, BuyEntryPayload::cost,
            ResourceLocation.STREAM_CODEC, BuyEntryPayload::category,
            BuyEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
