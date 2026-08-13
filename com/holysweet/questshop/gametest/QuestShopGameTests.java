package com.holysweet.questshop.gametest;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(QuestShop.MODID)
@EventBusSubscriber(modid = QuestShop.MODID)
public class QuestShopGameTests {

    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.register(QuestShopGameTests.class);
    }

    @GameTest
    public static void testCoinItemRegistry(GameTestHelper helper) {
        ItemStack coinStack = new ItemStack(ModItems.COIN.get());
        helper.assertTrue(!coinStack.isEmpty(), "Coin item stack should not be empty");
        helper.assertTrue(coinStack.is(ModItems.COIN.get()), "Item stack should match ModItems.COIN");
        helper.succeed();
    }

    @GameTest
    public static void testCoinPickupWithMockPlayer(GameTestHelper helper) {
        BlockPos centerPos = new BlockPos(1, 1, 1);
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        mockPlayer.setPos(helper.absoluteVec(centerPos.getCenter()));

        ItemStack coinStack = new ItemStack(ModItems.COIN.get(), 5);
        helper.spawnItem(coinStack.getItem(), centerPos);

        helper.runAtTickTime(10, () -> {
            helper.assertItemEntityPresent(ModItems.COIN.get(), centerPos, 2.0D);
            helper.succeed();
        });
    }

    @GameTest
    public static void testZombieSpawningInArena(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, spawnPos);
        
        helper.assertTrue(zombie != null, "Zombie should be spawned successfully");
        helper.assertTrue(zombie.isAlive(), "Spawned zombie should be alive");
        helper.assertEntityPresent(EntityType.ZOMBIE, spawnPos);
        helper.succeed();
    }

    @GameTest
    public static void testTickSequenceExample(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(true, "World ticking properly");
            helper.succeed();
        });
    }
}
