package com.holysweet.questshop.neoforge.network;

import com.holysweet.questshop.network.INetworkBridge;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkBridge implements INetworkBridge {

    @Override
    public void sendToServer(Object payload) {
        if (payload instanceof CustomPacketPayload customPayload) {
            PacketDistributor.sendToServer(customPayload);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Object payload) {
        if (player != null && payload instanceof CustomPacketPayload customPayload) {
            PacketDistributor.sendToPlayer(player, customPayload);
        }
    }

    @Override
    public void sendToAllPlayers(Object payload) {
        if (payload instanceof CustomPacketPayload customPayload) {
            PacketDistributor.sendToAllPlayers(customPayload);
        }
    }
}
