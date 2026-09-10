package com.holysweet.questshop.network.payload;

import com.holysweet.questshop.QuestShop;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record TeamMembersPayload(
        boolean hasTeam,
        String teamName,
        int totalPot,
        List<MemberEntry> members
) implements CustomPacketPayload {

    public static final Type<TeamMembersPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "team_members"));

    public record MemberEntry(
            UUID uuid,
            String name,
            int earned,
            int spent,
            boolean isOwner,
            boolean isOnline
    ) {
        public static final StreamCodec<ByteBuf, MemberEntry> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, MemberEntry::uuid,
                ByteBufCodecs.STRING_UTF8, MemberEntry::name,
                ByteBufCodecs.VAR_INT, MemberEntry::earned,
                ByteBufCodecs.VAR_INT, MemberEntry::spent,
                ByteBufCodecs.BOOL, MemberEntry::isOwner,
                ByteBufCodecs.BOOL, MemberEntry::isOnline,
                MemberEntry::new
        );
    }

    public static final StreamCodec<ByteBuf, TeamMembersPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TeamMembersPayload::hasTeam,
            ByteBufCodecs.STRING_UTF8, TeamMembersPayload::teamName,
            ByteBufCodecs.VAR_INT, TeamMembersPayload::totalPot,
            ByteBufCodecs.collection(ArrayList::new, MemberEntry.STREAM_CODEC), TeamMembersPayload::members,
            TeamMembersPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
