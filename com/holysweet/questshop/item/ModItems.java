package com.holysweet.questshop.item;

import com.holysweet.questshop.QuestShop;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QuestShop.MODID);

    public static final DeferredItem<CoinItem> COIN = ITEMS.registerItem("coin",
            CoinItem::new, new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
