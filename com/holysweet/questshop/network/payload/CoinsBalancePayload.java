package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CoinsBalancePayload(int balance) implements CustomPacketPayload {
    public static final Type<CoinsBalancePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "coins_balance"));

    public static final StreamCodec<FriendlyByteBuf, CoinsBalancePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CoinsBalancePayload::balance,
            CoinsBalancePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
