package com.holysweet.questshop.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdminRemoveEntryPayload(ResourceLocation itemId, ResourceLocation category) implements CustomPacketPayload {
    public static final Type<AdminRemoveEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("questshop", "admin_remove_entry"));
    public static final StreamCodec<FriendlyByteBuf, AdminRemoveEntryPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AdminRemoveEntryPayload::itemId,
            ResourceLocation.STREAM_CODEC, AdminRemoveEntryPayload::category,
            AdminRemoveEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
