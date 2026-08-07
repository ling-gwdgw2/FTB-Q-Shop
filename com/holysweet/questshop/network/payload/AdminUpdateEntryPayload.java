package com.holysweet.questshop.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdminUpdateEntryPayload(ResourceLocation itemId, int amount, int cost, ResourceLocation category) implements CustomPacketPayload {
    public static final Type<AdminUpdateEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("questshop", "admin_update_entry"));
    public static final StreamCodec<FriendlyByteBuf, AdminUpdateEntryPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AdminUpdateEntryPayload::itemId,
            ByteBufCodecs.VAR_INT, AdminUpdateEntryPayload::amount,
            ByteBufCodecs.VAR_INT, AdminUpdateEntryPayload::cost,
            ResourceLocation.STREAM_CODEC, AdminUpdateEntryPayload::category,
            AdminUpdateEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
