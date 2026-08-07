package com.holysweet.questshop.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdminUpdateCategoryPayload(ResourceLocation categoryId, String display, boolean unlockedByDefault, int order, boolean delete) implements CustomPacketPayload {
    public static final Type<AdminUpdateCategoryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("questshop", "admin_update_category"));
    public static final StreamCodec<FriendlyByteBuf, AdminUpdateCategoryPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AdminUpdateCategoryPayload::categoryId,
            ByteBufCodecs.STRING_UTF8, AdminUpdateCategoryPayload::display,
            ByteBufCodecs.BOOL, AdminUpdateCategoryPayload::unlockedByDefault,
            ByteBufCodecs.VAR_INT, AdminUpdateCategoryPayload::order,
            ByteBufCodecs.BOOL, AdminUpdateCategoryPayload::delete,
            AdminUpdateCategoryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
