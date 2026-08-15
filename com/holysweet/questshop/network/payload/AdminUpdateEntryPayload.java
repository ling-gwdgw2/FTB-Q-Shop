package com.holysweet.questshop.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.holysweet.questshop.QuestShop;

public record AdminUpdateEntryPayload(
        ResourceLocation itemId,
        int amount,
        int cost,
        ResourceLocation category,
        int dailyLimit,
        int totalStock
) implements CustomPacketPayload {
    public static final Type<AdminUpdateEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "admin_update_entry"));

    public AdminUpdateEntryPayload(ResourceLocation itemId, int amount, int cost, ResourceLocation category) {
        this(itemId, amount, cost, category, 0, -1);
    }

    public static final StreamCodec<FriendlyByteBuf, AdminUpdateEntryPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AdminUpdateEntryPayload::itemId,
            ByteBufCodecs.VAR_INT, AdminUpdateEntryPayload::amount,
            ByteBufCodecs.VAR_INT, AdminUpdateEntryPayload::cost,
            ResourceLocation.STREAM_CODEC, AdminUpdateEntryPayload::category,
            ByteBufCodecs.VAR_INT, AdminUpdateEntryPayload::dailyLimit,
            ByteBufCodecs.VAR_INT, AdminUpdateEntryPayload::totalStock,
            AdminUpdateEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
