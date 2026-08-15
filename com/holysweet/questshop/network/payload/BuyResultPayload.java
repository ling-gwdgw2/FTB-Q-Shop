package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyResultPayload(Code code) implements CustomPacketPayload {
    public static final Type<BuyResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "buy_result"));

    public static final StreamCodec<ByteBuf, BuyResultPayload> CODEC = ByteBufCodecs.idMapper(
            i -> Code.values()[i],
            Code::ordinal
    ).map(BuyResultPayload::new, BuyResultPayload::code);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Code {
        OK,
        INVALID_ENTRY,
        NOT_ENOUGH_COINS,
        NO_INVENTORY_SPACE,
        LOCKED_CATEGORY,
        OUT_OF_STOCK,
        DAILY_LIMIT_REACHED
    }
}
