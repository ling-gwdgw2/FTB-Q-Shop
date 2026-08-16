package com.holysweet.questshop.loot.conditions;

import com.holysweet.questshop.QuestShop;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LootConditions {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, QuestShop.MODID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> ENTITY_IS_ENEMY =
            CONDITIONS.register("entity_is_enemy", () -> new LootItemConditionType(EntityIsEnemyCondition.CODEC));

    public static void register(IEventBus eventBus) {
        CONDITIONS.register(eventBus);
    }
}
