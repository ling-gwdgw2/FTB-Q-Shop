package com.holysweet.questshop.api.coins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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
}
