package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ShopDataPayload(List<ShopEntry> entries) implements CustomPacketPayload {
    public static final Type<ShopDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "shop_data"));

    public static final StreamCodec<FriendlyByteBuf, ShopDataPayload> CODEC = new StreamCodec<>() {
        @Override
        public ShopDataPayload decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<ShopEntry> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ResourceLocation itemId = buf.readResourceLocation();
                int amount = buf.readVarInt();
                int cost = buf.readVarInt();
                ResourceLocation category = buf.readResourceLocation();
                int dailyLimit = buf.readVarInt();
                int totalStock = buf.readVarInt();
                int discountPercent = buf.readVarInt();
                list.add(new ShopEntry(itemId, amount, cost, category, dailyLimit, totalStock, discountPercent));
            }
            return new ShopDataPayload(list);
        }

        @Override
        public void encode(FriendlyByteBuf buf, ShopDataPayload payload) {
            List<ShopEntry> list = payload.entries();
            buf.writeVarInt(list.size());
            for (ShopEntry e : list) {
                buf.writeResourceLocation(e.itemId());
                buf.writeVarInt(e.amount());
                buf.writeVarInt(e.cost());
                buf.writeResourceLocation(e.category());
                buf.writeVarInt(e.dailyLimit());
                buf.writeVarInt(e.totalStock());
                buf.writeVarInt(e.discountPercent());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
