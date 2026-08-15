package com.holysweet.questshop.network;

import net.minecraft.server.level.ServerPlayer;

public interface INetworkBridge {
    void sendToServer(Object payload);
    void sendToPlayer(ServerPlayer player, Object payload);
    void sendToAllPlayers(Object payload);
}
