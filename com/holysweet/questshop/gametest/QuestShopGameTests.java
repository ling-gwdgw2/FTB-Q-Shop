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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * Example NeoForge GameTest class for the QuestShop mod.
 * 
 * GameTests run inside an actual Minecraft world instance during the testing phase.
 * They allow automated testing of entity behaviors, item interactions, block updates, and mod logic.
 */
@GameTestHolder(QuestShop.MODID)
@EventBusSubscriber(modid = QuestShop.MODID, bus = EventBusSubscriber.Bus.MOD)
public class QuestShopGameTests {

    /**
     * Registers this GameTest class to NeoForge's GameTest registry.
     */
    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.register(QuestShopGameTests.class);
    }

    /**
     * Test 1: Verify that ModItems.COIN is correctly registered and has expected properties.
     * Uses the standard "empty3x3" template structure provided by Vanilla Minecraft.
     */
    @GameTest(template = "empty3x3")
    public static void testCoinItemRegistry(GameTestHelper helper) {
        ItemStack coinStack = new ItemStack(ModItems.COIN.get());
        
        // Assert that the item stack is not empty and is registered
        helper.assertTrue(!coinStack.isEmpty(), "Coin item stack should not be empty");
        helper.assertTrue(coinStack.is(ModItems.COIN.get()), "Item stack should match ModItems.COIN");
        
        // Mark test as successful
        helper.succeed();
    }

    /**
     * Test 2: Test item spawning in the world and picking it up with a Mock Player.
     */
    @GameTest(template = "empty3x3")
    public static void testCoinPickupWithMockPlayer(GameTestHelper helper) {
        BlockPos centerPos = new BlockPos(1, 1, 1);

        // Spawn a Mock (Fake) Player at position (1, 1, 1)
        Player mockPlayer = helper.makeMockPlayer();
        mockPlayer.setPos(helper.absoluteVec(centerPos.toCenterPos()));

        // Spawn a Coin Item at position (1, 1, 1)
        ItemStack coinStack = new ItemStack(ModItems.COIN.get(), 5);
        helper.spawnItem(coinStack.getItem(), centerPos);

        // Wait a few ticks and verify the mock player picks up the item or item is present
        helper.runAtTickTime(10, () -> {
            helper.assertItemPresent(ModItems.COIN.get(), centerPos, 2.0D);
            helper.succeed();
        });
    }

    /**
     * Test 3: Test entity spawning and interaction (e.g. Zombie spawning in test arena).
     */
    @GameTest(template = "empty3x3", timeoutTicks = 40)
    public static void testZombieSpawningInArena(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        
        // Spawn a zombie in the test arena
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, spawnPos);
        
        helper.assertTrue(zombie != null, "Zombie should be spawned successfully");
        helper.assertTrue(zombie.isAlive(), "Spawned zombie should be alive");

        // Verify entity is present at spawnPos
        helper.assertEntityPresent(EntityType.ZOMBIE, spawnPos);
        
        helper.succeed();
    }

    /**
     * Test 4: Custom tick-by-tick monitoring test example.
     */
    @GameTest(template = "empty3x3", timeoutTicks = 60)
    public static void testTickSequenceExample(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);

        // Schedule check at tick 20
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(helper.getBlockState(pos) != null, "Block state at test pos must exist");
        });

        // Complete test at tick 30
        helper.runAtTickTime(30, () -> {
            helper.succeed();
        });
    }
}
