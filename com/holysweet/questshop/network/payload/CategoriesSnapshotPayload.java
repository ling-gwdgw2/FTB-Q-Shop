package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopCategory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public record CategoriesSnapshotPayload(
        Map<ResourceLocation, ShopCategory> categories,
        Set<ResourceLocation> unlocked
) implements CustomPacketPayload {
    public static final Type<CategoriesSnapshotPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "categories_snapshot"));

    public static final StreamCodec<FriendlyByteBuf, CategoriesSnapshotPayload> CODEC = new StreamCodec<>() {
        @Override
        public CategoriesSnapshotPayload decode(FriendlyByteBuf buf) {
            int catSize = buf.readVarInt();
            Map<ResourceLocation, ShopCategory> catMap = new LinkedHashMap<>(catSize);
            for (int i = 0; i < catSize; i++) {
                ResourceLocation id = buf.readResourceLocation();
                String display = buf.readUtf();
                boolean unlockedByDefault = buf.readBoolean();
                int order = buf.readVarInt();
                catMap.put(id, new ShopCategory(id, display, unlockedByDefault, order));
            }

            int unlSize = buf.readVarInt();
            Set<ResourceLocation> unlSet = new HashSet<>(unlSize);
            for (int i = 0; i < unlSize; i++) {
                unlSet.add(buf.readResourceLocation());
            }

            return new CategoriesSnapshotPayload(catMap, unlSet);
        }

        @Override
        public void encode(FriendlyByteBuf buf, CategoriesSnapshotPayload payload) {
            Map<ResourceLocation, ShopCategory> catMap = payload.categories();
            buf.writeVarInt(catMap.size());
            for (ShopCategory cat : catMap.values()) {
                buf.writeResourceLocation(cat.id());
                buf.writeUtf(cat.display());
                buf.writeBoolean(cat.unlockedByDefault());
                buf.writeVarInt(cat.order());
            }

            Set<ResourceLocation> unlSet = payload.unlocked();
            buf.writeVarInt(unlSet.size());
            for (ResourceLocation id : unlSet) {
                buf.writeResourceLocation(id);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
