package com.holysweet.questshop.loot.conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class EntityIsEnemyCondition implements LootItemCondition {
    public static final EntityIsEnemyCondition INSTANCE = new EntityIsEnemyCondition();
    public static final MapCodec<EntityIsEnemyCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public LootItemConditionType getType() {
        return LootConditions.ENTITY_IS_ENEMY.get();
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        return entity instanceof Monster || entity instanceof Enemy;
    }

    public static LootItemCondition.Builder entityIsMonster() {
        return () -> INSTANCE;
    }
}
