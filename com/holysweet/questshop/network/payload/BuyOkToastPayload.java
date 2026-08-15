package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyOkToastPayload(
        ResourceLocation itemId,
        int amount,
        int cost
) implements CustomPacketPayload {
    public static final Type<BuyOkToastPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "buy_ok_toast"));

    public static final StreamCodec<FriendlyByteBuf, BuyOkToastPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BuyOkToastPayload::itemId,
            ByteBufCodecs.VAR_INT, BuyOkToastPayload::amount,
            ByteBufCodecs.VAR_INT, BuyOkToastPayload::cost,
            BuyOkToastPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
