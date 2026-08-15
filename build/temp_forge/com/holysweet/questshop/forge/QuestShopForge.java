package com.holysweet.questshop.forge;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.forge.network.ForgeNetworkBridge;
import com.holysweet.questshop.network.NetworkBridge;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class QuestShopForge {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void initForge() {
        NetworkBridge.set(new ForgeNetworkBridge());
        LOGGER.info("[ling_q_shop] Initialized LING Quest Shop Forge Adapter");
    }
}
