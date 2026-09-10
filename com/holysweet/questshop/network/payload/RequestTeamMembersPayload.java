package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestTeamMembersPayload() implements CustomPacketPayload {
    public static final Type<RequestTeamMembersPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "request_team_members"));
    public static final StreamCodec<ByteBuf, RequestTeamMembersPayload> CODEC = StreamCodec.unit(new RequestTeamMembersPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
