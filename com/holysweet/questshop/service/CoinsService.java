package com.holysweet.questshop.service;

import com.holysweet.questshop.api.coins.AccountRef;
import com.holysweet.questshop.api.coins.CoinsProvider;
import com.holysweet.questshop.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

public final class CoinsService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicReference<CoinsProvider> ACTIVE = new AtomicReference<>(new VanillaCoinsProvider());

    private CoinsService() {}

    public static CoinsProvider provider() {
        return ACTIVE.get();
    }

    public static void swapBackend(CoinsProvider newProvider) {
        if (newProvider != null) {
            ACTIVE.set(newProvider);
            LOGGER.info("Coins backend swapped to: {}", newProvider.id());
        }
    }

    public static AccountRef handleFor(ServerPlayer player) {
        return provider().handleFor(player);
    }

    public static int get(ServerLevel level, ServerPlayer player) {
        return get(level, handleFor(player));
    }

    public static int set(ServerLevel level, ServerPlayer player, int amount) {
        int res = set(level, handleFor(player), amount);
        Net.sendCoinsBalance(player, res);
        return res;
    }

    public static int add(ServerLevel level, ServerPlayer player, int delta) {
        int res = add(level, handleFor(player), delta);
        Net.sendCoinsBalance(player, res);
        return res;
    }

    public static int get(ServerLevel level, AccountRef ref) {
        return provider().get(level, ref);
    }

    public static int set(ServerLevel level, AccountRef ref, int amount) {
        int res = provider().set(level, ref, amount);
        broadcastChange(level, ref);
        return res;
    }

    public static int add(ServerLevel level, AccountRef ref, int delta) {
        int res = provider().add(level, ref, delta);
        broadcastChange(level, ref);
        return res;
    }

    private static void broadcastChange(ServerLevel level, AccountRef ref) {
        if (level == null || ref == null) return;
        for (ServerPlayer player : level.players()) {
            AccountRef pRef = handleFor(player);
            if (pRef.equals(ref)) {
                Net.sendCoinsBalance(player, get(level, ref));
            }
        }
    }
}
