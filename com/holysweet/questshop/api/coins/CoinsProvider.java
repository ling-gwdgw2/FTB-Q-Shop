package com.holysweet.questshop.api.coins;

import com.holysweet.questshop.network.payload.TeamMembersPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface CoinsProvider {
    String id();
    AccountRef handleFor(ServerPlayer player);
    int get(ServerLevel level, AccountRef ref);
    int set(ServerLevel level, AccountRef ref, int amount);
    default int add(ServerLevel level, AccountRef ref, int delta) {
        int current = get(level, ref);
        int next = Math.max(0, current + delta);
        return set(level, ref, next);
    }
    default int add(ServerLevel level, ServerPlayer player, int delta) {
        return add(level, handleFor(player), delta);
    }
    default TeamMembersPayload getTeamData(ServerPlayer player) {
        if (player == null) {
            return new TeamMembersPayload(false, "Solo", 0, List.of());
        }
        int balance = get(player.serverLevel(), handleFor(player));
        TeamMembersPayload.MemberEntry entry = new TeamMembersPayload.MemberEntry(
                player.getUUID(),
                player.getGameProfile().getName(),
                balance,
                0,
                true,
                true
        );
        return new TeamMembersPayload(false, player.getGameProfile().getName() + "'s Wallet", balance, List.of(entry));
    }
}
