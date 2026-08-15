package com.holysweet.questshop.forge.network;

import com.holysweet.questshop.network.INetworkBridge;
import net.minecraft.server.level.ServerPlayer;

public class ForgeNetworkBridge implements INetworkBridge {

    @Override
    public void sendToServer(Object payload) {
        // Forge client packet dispatch implementation
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object payload) {
        // Forge server-to-player packet dispatch implementation
    }

    @Override
    public void sendToAllPlayers(Object payload) {
        // Forge server-to-all packet dispatch implementation
    }
}
