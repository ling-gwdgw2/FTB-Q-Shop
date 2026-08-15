package com.holysweet.questshop.network;

import net.minecraft.server.level.ServerPlayer;

public final class NetworkBridge {

    private static INetworkBridge instance = new FallbackBridge();

    public static void set(INetworkBridge bridge) {
        if (bridge != null) {
            instance = bridge;
        }
    }

    public static INetworkBridge get() {
        return instance;
    }

    public static void sendToServer(Object payload) {
        instance.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        instance.sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(Object payload) {
        instance.sendToAllPlayers(payload);
    }

    private static class FallbackBridge implements INetworkBridge {
        @Override
        public void sendToServer(Object payload) {}

        @Override
        public void sendToPlayer(ServerPlayer player, Object payload) {}

        @Override
        public void sendToAllPlayers(Object payload) {}
    }
}
