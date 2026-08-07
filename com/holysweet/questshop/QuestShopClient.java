package com.holysweet.questshop;

import net.neoforged.neoforge.common.NeoForge;
import com.holysweet.questshop.client.InventoryButtonListener;

public class QuestShopClient {
    public QuestShopClient(ModContainer container) {
        NeoForge.EVENT_BUS.register(InventoryButtonListener.class);
    }
}
