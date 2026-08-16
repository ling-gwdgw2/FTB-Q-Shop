package com.holysweet.questshop.service;

import com.holysweet.questshop.api.coins.AccountRef;
import com.holysweet.questshop.api.coins.CoinsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VanillaCoinsProvider implements CoinsProvider {

    private final Map<UUID, Integer> balances = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public AccountRef handleFor(ServerPlayer player) {
        return new AccountRef.Player(player.getUUID());
    }

    @Override
    public int get(ServerLevel level, AccountRef ref) {
        if (ref instanceof AccountRef.Player p) {
            return balances.getOrDefault(p.uuid(), 0);
        }
        return 0;
    }

    @Override
    public int set(ServerLevel level, AccountRef ref, int amount) {
        int clamped = Math.max(0, amount);
        if (ref instanceof AccountRef.Player p) {
            balances.put(p.uuid(), clamped);
        }
        return clamped;
    }
}
